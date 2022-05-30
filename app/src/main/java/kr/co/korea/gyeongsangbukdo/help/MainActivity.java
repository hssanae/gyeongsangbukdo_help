package kr.co.korea.gyeongsangbukdo.help;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.HashMap;

import kr.chanven.commonpulltorefresh.PtrClassicFrameLayout;
import kr.chanven.commonpulltorefresh.PtrDefaultHandler;
import kr.chanven.commonpulltorefresh.PtrFrameLayout;


public class MainActivity extends BasicActivity implements MainListAdapter.MyRecyclerViewClickListener {
    private TextView noDataTv;
    private EditText popupNameEt;
    private EditText popupPhoneEt;
    private Button popupSubmitBtn;
    private Button popupCancelBtn;
    private ImageButton addBtn;
    private ImageButton infoBtn;
    private ImageButton settingBtn;
    private ImageButton pushBtn;
    private ImageButton versionBtn;
    private Button sendSmsBtn;
    private TextView versionTv;

    private ArrayList<HashMap<String, String>> userList;
    private String nameStr = null;
    private String phoneStr = null;
    private String mainPhoneStr = null;

    private DbHandler db;

    private ListView mainList;
    private MainListItems mainItems;
    private MainListAdapter mainAdapter;
    private ArrayList<MainListItems> mainArrayList = new ArrayList<MainListItems>();
    private PtrClassicFrameLayout mainPtrClassicFrameLayout;
    private boolean pullPosition = true;

    private CustomDialog contactPopupView;

    private long time = 0;

    private Intent foregroundServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (null == UndeadService.serviceIntent) {
            foregroundServiceIntent = new Intent(this, UndeadService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(foregroundServiceIntent);
            } else {
                startService(foregroundServiceIntent);
            }
        } else {
            foregroundServiceIntent = UndeadService.serviceIntent;
        }

        initSet();
        listSet();
        initSetContactData();
    }

    @Override
    protected void onResume() {
        super.onResume();

        initSetContactData();
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - time >= 2000) {
            time = System.currentTimeMillis();
            Toast.makeText(getApplicationContext(), "뒤로 버튼을 한번 더 누르면 종료합니다.", Toast.LENGTH_SHORT).show();
        } else if (System.currentTimeMillis() - time < 2000) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != foregroundServiceIntent) {
            stopService(foregroundServiceIntent);
            foregroundServiceIntent = null;
        }
        db.close();
    }

    private void initSet() {

        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        final int width = dm.widthPixels;

        contactPopupView = new CustomDialog(MainActivity.this, null, null, null);
        WindowManager.LayoutParams wm = contactPopupView.getWindow().getAttributes();
        wm.copyFrom(contactPopupView.getWindow().getAttributes());
        wm.width = (int) (width * 0.8);

        addBtn = findView(R.id.btn_mainActivity_add);
        sendSmsBtn = findView(R.id.btn_mainActivity_sendSms);
        versionBtn = findView(R.id.btn_mainActivity_version);
        versionTv = findView(R.id.tv_mainActivity_version);
        noDataTv = findView(R.id.tv_mainActivity_noData);
        infoBtn = findView(R.id.btn_mainActivity_info);
        settingBtn = findView(R.id.btn_mainActivity_setting);
        pushBtn = findView(R.id.btn_mainActivity_push);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
                builder.setCancelable(true);
                builder.setTitle("안내");
                builder.setMessage("긴급구호자 연락처 입력방법을 선택 해주세요.");
                builder.setPositiveButton("직접입력 하기", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        popupNameEt.setText(null);
                        popupPhoneEt.setText(null);

                        contactPopupView = new CustomDialog(MainActivity.this, null, null, null);
                        WindowManager.LayoutParams wm = contactPopupView.getWindow().getAttributes();
                        wm.copyFrom(contactPopupView.getWindow().getAttributes());
                        wm.width = (int) (width * 0.8);
                        contactPopupView.show();
                    }
                });
                builder.setNegativeButton("연락처에서 가져오기", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent mIntent = new Intent(Intent.ACTION_PICK);
                        mIntent.setData(ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                        startActivityForResult(mIntent, 0);

                    }
                });
                builder.setNeutralButton("닫기", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });

        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });

        infoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, InfoActivity.class);
                startActivity(intent);
            }
        });

        sendSmsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mainPhoneStr != null && mainPhoneStr.length() > 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setCancelable(false);
                    builder.setTitle("안내");
                    builder.setMessage("테스트 문자는 제일 상단에 있는 대표전화번호로만 발송됩니다. 발송 하시겠습니까?");
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            sendSMS();
                        }
                    });
                    builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();

                } else {
                    showAlertDialog(MainActivity.this, "저장된 긴급구호자가 없습니다. 1명 이상 등록 후 이용가능합니다.");
                }

            }
        });

        versionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 구글 스토어
//                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
//                try {
//                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
//                } catch (android.content.ActivityNotFoundException anfe) {
//                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
//                }

                // 원스토어
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("onestore://common/product//OA00762191")));
                } catch (RuntimeException e) {
                    System.out.println("No OneStore");
                    Toast.makeText(MainActivity.this, "원스토어를 열수없습니다. 확인 후 이용바랍니다.", Toast.LENGTH_SHORT).show();
                }

            }
        });

        pushBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                //for Android 5-7
                intent.putExtra("app_package", getPackageName());
                intent.putExtra("app_uid", getApplicationInfo().uid);
                // for Android 8 and above
                intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());

                startActivity(intent);
            }
        });

        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionTv.setText("V " + pi.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Cursor cursor = getContentResolver().query(data.getData(), new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null);
            cursor.moveToFirst();
            String sName = cursor.getString(0);
            String sNumber = cursor.getString(1);
            cursor.close();

            saveDate(sName, sNumber, null);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onItemUpdateClicked(final int mPosition) {
        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        int width = dm.widthPixels;

        contactPopupView = new CustomDialog(MainActivity.this, userList.get(mPosition).get("id"), userList.get(mPosition).get("name"), userList.get(mPosition).get("phone"));
        WindowManager.LayoutParams wm = contactPopupView.getWindow().getAttributes();
        wm.copyFrom(contactPopupView.getWindow().getAttributes());
        wm.width = (int) (width * 0.8);

        contactPopupView.show();
    }

    @Override
    public void onItemDeleteClicked(final int mPosition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(false);
        builder.setTitle("안내");
        builder.setMessage("해당 긴급구호자를 삭제하시겠습니까?");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                db.DeleteUser(userList.get(mPosition).get("id"));
                showAlertDialog(MainActivity.this, "정상적으로 삭제되었습니다.");
                initSetContactData();
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void listSet() {
        db = new DbHandler(this);

        mainAdapter = new MainListAdapter(MainActivity.this, R.layout.adapter_main, mainArrayList);
        mainPtrClassicFrameLayout = findView(R.id.pcf_mainActivity_frame);

        mainAdapter.setOnClickListener(MainActivity.this);

        mainList = findView(R.id.lv_mainActivity_list);
        mainList.setAdapter(mainAdapter);
        mainList.getScrollY();

        setPullToRL();
    }

    /* 데이터 셋 */
    private void initSetContactData() {
        userList = db.GetUsers();

        mainList.setVisibility(View.GONE);
        noDataTv.setVisibility(View.GONE);

        if (userList != null && userList.size() > 0) {
            mainList.setVisibility(View.VISIBLE);
            mainArrayList.clear();

            mainItems = null;
            mainPhoneStr = null;

            for (int i = 0; i < userList.size(); i++) {
                String mPhoneStr = userList.get(i).get("phone");

                if (i == 0) {
                    mainPhoneStr = mPhoneStr;
                }

                if (mPhoneStr.length() > 10) {
                    mPhoneStr = maskNumber(userList.get(i).get("phone"), "###-####-####");
                } else {
                    mPhoneStr = maskNumber(userList.get(i).get("phone"), "###-###-####");
                }
                mainItems = new MainListItems(
                        userList.get(i).get("id"),
                        userList.get(i).get("name"),
                        mPhoneStr,
                        userList.get(i).get("note")
                );
                mainArrayList.add(mainItems);
            }

            mainAdapter.notifyDataSetChanged();

            if (pullPosition) {
                mainPtrClassicFrameLayout.refreshComplete();
                mainPtrClassicFrameLayout.setLoadMoreEnable(true);
            } else {
                mainPtrClassicFrameLayout.loadMoreComplete(true);
            }
        } else {
            noDataTv.setVisibility(View.VISIBLE);
        }

    }

    private void setPullToRL() {
        mainPtrClassicFrameLayout.setPtrHandler(new PtrDefaultHandler() {

            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                pullPosition = true;
                initSetContactData();
            }
        });

        mainPtrClassicFrameLayout.setLoadMoreHandler(new PtrFrameLayout.LoadMoreHandler() {

            @Override
            public void loadMore() {
                pullPosition = false;
                initSetContactData();
            }
        });
    }

    private void saveDate(String mNameStr, String mPhoneStr, String mNoteStr) {
        if (checkPhoneNumber(mPhoneStr)) {
            DbHandler dbHandler = new DbHandler(MainActivity.this);
            dbHandler.insertUserDetails(mNameStr, mPhoneStr.replace("-", "").trim(), mNoteStr);
            showAlertDialog(MainActivity.this, "긴급구호자가 추가되었습니다.");
            resetData();
            initSetContactData();
        } else {
            showAlertDialog(MainActivity.this, "연락처가 올바르지 않습니다.");
        }
    }

    private void updateDate(String mIdStr, String mNameStr, String mPhoneStr, String mNoteStr) {
        if (checkPhoneNumber(mPhoneStr)) {
            DbHandler dbHandler = new DbHandler(MainActivity.this);
            dbHandler.UpdateUserDetails(mIdStr, mNameStr, mPhoneStr.replace("-", "").trim(), mNoteStr);
            showAlertDialog(MainActivity.this, "긴급구호자가 수정되었습니다.");
            resetData();
            initSetContactData();
        } else {
            showAlertDialog(MainActivity.this, "연락처가 올바르지 않습니다.");
        }
    }

    private void resetData() {
        popupNameEt.setText(null);
        popupPhoneEt.setText(null);
        nameStr = null;
        phoneStr = null;
        mainPhoneStr = null;
    }

    public class CustomDialog extends Dialog {

        public CustomDialog(Context context, final String mId, final String mName, final String mPhone) {
            super(context);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            setContentView(R.layout.popup_main_contact);

            boolean isUpdate = false;

            popupNameEt = (EditText) findViewById(R.id.eTxt_mainPopup_name);
            popupPhoneEt = (EditText) findViewById(R.id.eTxt_mainPopup_phone);
            popupSubmitBtn = (Button) findViewById(R.id.btn_mainPopup_submit);
            popupCancelBtn = (Button) findViewById(R.id.btn_mainPopup_cancel);

            if (mName != null) {
                popupNameEt.setText(mName);
                isUpdate = true;
            }
            if (mPhone != null) {
                popupPhoneEt.setText(mPhone);
                isUpdate = true;
            }

            final boolean finalIsUpdate = isUpdate;
            popupSubmitBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (checkValidation()) {
                        contactPopupView.dismiss();
                        if (mId == null) {
                            saveDate(nameStr, phoneStr, null);
                        } else {
                            if (finalIsUpdate) {
                                updateDate(mId, nameStr, phoneStr, null);
                            } else {
                                saveDate(nameStr, phoneStr, null);
                            }
                        }
                    }
                }
            });

            popupCancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    contactPopupView.dismiss();
                }
            });
        }

        private boolean checkValidation() {
            nameStr = popupNameEt.getText().toString();
            phoneStr = popupPhoneEt.getText().toString();

            if (nameStr == null || nameStr.length() < 1 || nameStr.equals("")) {
                Toast.makeText(MainActivity.this, "이름을 입력 해주세요.", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (phoneStr == null || phoneStr.length() < 1 || phoneStr.equals("")) {
                Toast.makeText(MainActivity.this, "휴대전화번호를 입력 해주세요.", Toast.LENGTH_SHORT).show();
                return false;
            }

            return true;
        }
    }

    private boolean checkPhoneNumber(String mPhoneStr) {
        String telStr = mPhoneStr.replace("-", "");
        String[] phoneFormat = {"010", "011", "012", "013", "015", "016", "017", "018", "019", "060", "070"};

        for (int i = 0; i < phoneFormat.length; i++) {
            if (telStr.substring(0, 3).equals(phoneFormat[i])) {
                if (phoneFormat[i].equals("010") || phoneFormat[i].equals("060") || phoneFormat[i].equals("070")) {
                    if (telStr.length() == 11) {
                        return true;
                    }
                } else {
                    if (telStr.length() >= 10) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static String maskNumber(String number, String mask) {

        if (number.length() > 0) {

            int index = 0;
            StringBuilder masked = new StringBuilder();
            for (int i = 0; i < mask.length(); i++) {
                char c = mask.charAt(i);
                if (c == '#') {
                    masked.append(number.charAt(index));
                    index++;
                } else {
                    masked.append(c);
                }
            }
            return masked.toString();
        }

        return "";
    }


    private void sendSMS() {
        String userPhoneStr = new SharedPreference(MainActivity.this).getPreferences(StringsClass.PREF_USER_PHONE) != null ? new SharedPreference(MainActivity.this).getPreferences(StringsClass.PREF_USER_PHONE) : "";
        String nameStr = new SharedPreference(MainActivity.this).getPreferences(StringsClass.PREF_USER_NAME) != null ? new SharedPreference(MainActivity.this).getPreferences(StringsClass.PREF_USER_NAME) : "";
        String addressStr = new SharedPreference(MainActivity.this).getPreferences(StringsClass.PREF_USER_ADDRESS) != null ? new SharedPreference(MainActivity.this).getPreferences(StringsClass.PREF_USER_ADDRESS) : "";
        String addressStr2 = new SharedPreference(MainActivity.this).getPreferences(StringsClass.PREF_USER_ADDRESS2) != null ? new SharedPreference(MainActivity.this).getPreferences(StringsClass.PREF_USER_ADDRESS2) : "";
        String noteStr = new SharedPreference(MainActivity.this).getPreferences(StringsClass.PREF_USER_COMMENT) != null ? (new SharedPreference(MainActivity.this).getPreferences(StringsClass.PREF_USER_COMMENT)) : "";
        String setTimeStr = new SharedPreference(MainActivity.this).getPreferences(StringsClass.PREF_SET_TIME) != null ? new SharedPreference(MainActivity.this).getPreferences(StringsClass.PREF_SET_TIME) : StringsClass.SET_DEFAULT_TIME;

        String addressStr3 = addressStr + " " + addressStr2;

        GpsTracker gpsTracker = new GpsTracker(MainActivity.this);
        double latitude = gpsTracker.getLatitude();         // 위도
        double longitude = gpsTracker.getLongitude();         //경도
        String address = getCurrentAddress(MainActivity.this, latitude, longitude);

        if(address.contains("미발견")){
            address = addressStr3;
        }

        String commentStr =
                "[테스트문자]\n" +
                        nameStr + (noteStr.trim().length() > 0 ? "(" + noteStr + ")" : "") + "\n" +
                        "- " + setTimeStr + "시간 동안 휴대폰 미사용\n" +
//                        (userPhoneStr.trim().length() > 0 ? "- " + phoneFormat(userPhoneStr) : "") + "\n" +
                        "- 위치정보 : " + address;

        SmsManager sms = SmsManager.getDefault();
        ArrayList<String> parts = sms.divideMessage(commentStr);
        sms.sendMultipartTextMessage(mainPhoneStr, null, parts, null, null);

        showAlertDialog(MainActivity.this, "대표 긴급구호자에게 테스트 문자가 발송되었습니다.");
    }


}