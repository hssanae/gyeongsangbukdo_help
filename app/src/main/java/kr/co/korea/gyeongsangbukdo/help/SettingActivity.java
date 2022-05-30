package kr.co.korea.gyeongsangbukdo.help;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.IdRes;

public class SettingActivity extends BasicActivity {
    InputMethodManager imm;

    LinearLayout parentLayout;
    private LinearLayout timeLayout;
    private LinearLayout timeFromBtn;
    private LinearLayout timeToBtn;
    private TextView timeFromTv;
    private TextView timeToTv;
    private EditText nameEditText;
    private EditText addressEditText;
    private EditText addressEditText2;
    private EditText phoneEditText;
    private EditText noteEditText;
    private EditText setTimeEditText;
    private Button submitBtn;
    private ImageButton timeInfoBtn;
    private CheckBox timeCheckBox;

    private RadioGroup radioGroup;

    private String nameStr = null;
    private String addressStr = null;
    private String addressStr2 = null;
    private String userPhoneStr = null;
    private String noteStr = null;
    private String setTimeStr = null;
    private String typeIndex = "1";

    private boolean isTime = true;

    private int timeFromHour = 22;
    private int timeFromMin = 0;
    private int timeToHour = 7;
    private int timeToMin = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        initGetPreference();
        initSet();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (view != null && view instanceof EditText) {
                Rect r = new Rect();
                view.getGlobalVisibleRect(r);
                int rawX = (int) ev.getRawX();
                int rawY = (int) ev.getRawY();
                if (!r.contains(rawX, rawY)) {
                    view.clearFocus();
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void initGetPreference() {
        SharedPreferences pref = SettingActivity.this.getSharedPreferences(StringsClass.PREF_NAME, Activity.MODE_PRIVATE);
        timeFromHour = pref.getInt(StringsClass.PREF_TIME_FROM_HOUR, 22);
        timeFromMin = pref.getInt(StringsClass.PREF_TIME_FROM_MINUTE, 0);
        timeToHour = pref.getInt(StringsClass.PREF_TIME_TO_HOUR, 7);
        timeToMin = pref.getInt(StringsClass.PREF_TIME_TO_MINUTE, 0);
        isTime = pref.getBoolean(StringsClass.PREF_TIME_IS_CHECKED, true);

        nameStr = new SharedPreference(SettingActivity.this).getPreferences(StringsClass.PREF_USER_NAME) != null ? new SharedPreference(SettingActivity.this).getPreferences(StringsClass.PREF_USER_NAME) : "";
        addressStr = new SharedPreference(SettingActivity.this).getPreferences(StringsClass.PREF_USER_ADDRESS) != null ? new SharedPreference(SettingActivity.this).getPreferences(StringsClass.PREF_USER_ADDRESS) : "";
        addressStr2 = new SharedPreference(SettingActivity.this).getPreferences(StringsClass.PREF_USER_ADDRESS2) != null ? new SharedPreference(SettingActivity.this).getPreferences(StringsClass.PREF_USER_ADDRESS2) : "";
        userPhoneStr = new SharedPreference(SettingActivity.this).getPreferences(StringsClass.PREF_USER_PHONE) != null ? new SharedPreference(SettingActivity.this).getPreferences(StringsClass.PREF_USER_PHONE) : "";
        noteStr = new SharedPreference(SettingActivity.this).getPreferences(StringsClass.PREF_USER_COMMENT) != null ? new SharedPreference(SettingActivity.this).getPreferences(StringsClass.PREF_USER_COMMENT) : "";
        setTimeStr = new SharedPreference(SettingActivity.this).getPreferences(StringsClass.PREF_SET_TIME) != null ? new SharedPreference(SettingActivity.this).getPreferences(StringsClass.PREF_SET_TIME) : StringsClass.SET_DEFAULT_TIME;
        typeIndex = new SharedPreference(SettingActivity.this).getPreferences(StringsClass.PREF_COMMENT_TYPE) != null ? new SharedPreference(SettingActivity.this).getPreferences(StringsClass.PREF_COMMENT_TYPE) : "1";

    }

    private void initSet() {
        parentLayout = findView(R.id.layout_settingActivity_parent);
        timeFromBtn = findView(R.id.layout_settingActivity_timeFrom);
        timeToBtn = findView(R.id.layout_settingActivity_timeTo);
        timeFromTv = findView(R.id.tv_settingActivity_from);
        timeToTv = findView(R.id.tv_settingActivity_to);
        nameEditText = findView(R.id.eTxt_settingActivity_name);
        addressEditText = findView(R.id.eTxt_settingActivity_address);
        addressEditText2 = findView(R.id.eTxt_settingActivity_address2);
        phoneEditText = findView(R.id.eTxt_settingActivity_userPhone);
        noteEditText = findView(R.id.eTxt_settingActivity_note);
        setTimeEditText = findView(R.id.eTxt_settingActivity_setTime);
        submitBtn = findView(R.id.btn_settingActivity_submit);
        timeInfoBtn = findView(R.id.btn_settingActivity_timeInfo);
        timeCheckBox = findView(R.id.cb_settingActivity_isTime);
        timeLayout = findView(R.id.layout_settingActivity_time);
        radioGroup = findView(R.id.radioGroup);

        nameEditText.setText(nameStr);
        addressEditText.setText(addressStr);
        addressEditText2.setText(addressStr2);
        phoneEditText.setText(userPhoneStr);
        setTimeEditText.setText(setTimeStr);

        if (typeIndex != null) {
            if (typeIndex.equals("0")) {
                radioGroup.check(R.id.rgBtn_settingActivity_type0);
                noteEditText.setText(noteStr);
                noteEditText.setVisibility(View.VISIBLE);
            } else if (typeIndex.equals("1")) {
                radioGroup.check(R.id.rgBtn_settingActivity_type1);
            } else if (typeIndex.equals("2")) {
                radioGroup.check(R.id.rgBtn_settingActivity_type2);
            } else if (typeIndex.equals("3")) {
                radioGroup.check(R.id.rgBtn_settingActivity_type3);
            } else if (typeIndex.equals("4")) {
                radioGroup.check(R.id.rgBtn_settingActivity_type4);
            } else if (typeIndex.equals("5")) {
                radioGroup.check(R.id.rgBtn_settingActivity_type5);
            } else if (typeIndex.equals("6")) {
                radioGroup.check(R.id.rgBtn_settingActivity_type6);
            } else if (typeIndex.equals("7")) {
                radioGroup.check(R.id.rgBtn_settingActivity_type7);
            } else if (typeIndex.equals("8")) {
                radioGroup.check(R.id.rgBtn_settingActivity_type8);
            }
        } else {
            typeIndex = "1";
            radioGroup.check(R.id.rgBtn_settingActivity_type1);
        }

        // 방해금지 시간대 설정
        timeCheckBox.setChecked(isTime); // 방해금지 설정여부
        if (isTime) {
            timeLayout.setVisibility(View.VISIBLE);
        } else {
            timeLayout.setVisibility(View.GONE);
        }

        timeFromTv.setText(maskTime(timeFromHour) + "시 " + maskTime(timeFromMin) + "분");
        timeToTv.setText(maskTime(timeToHour) + "시 " + maskTime(timeToMin) + "분");

        radioGroup.setOnCheckedChangeListener(radioGroupButtonChangeListener);

        timeFromBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog dialog = new TimePickerDialog(SettingActivity.this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, fromListener, 15, 24, true);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.show();
            }
        });

        timeToBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog dialog = new TimePickerDialog(SettingActivity.this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar, toListener, 15, 24, true);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.show();
            }
        });

        timeCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timeCheckBox.isChecked()) {
                    timeLayout.setVisibility(View.VISIBLE);
                    isTime = true;
                } else {
                    timeLayout.setVisibility(View.GONE);
                    isTime = false;
                }
            }
        });

        timeInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog(SettingActivity.this, getResources().getString(R.string.time_info));
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mAddressStr = addressEditText.getText().toString().trim();
                String mAddressStr2 = addressEditText2.getText().toString().trim();
                String mSetTimeStr = setTimeEditText.getText().toString().trim();

                if (mAddressStr != null) {
                    if (mAddressStr.length() > 12) {
                        Toast.makeText(SettingActivity.this, "읍면동은 공백포함 최대 12자리까지 입력가능합니다.", Toast.LENGTH_SHORT).show();

                        return;
                    }
                }
                if (mAddressStr2 != null) {
                    if (mAddressStr2.length() > 7) {
                        Toast.makeText(SettingActivity.this, "리 및 마을명은 공백포함 최대 7자리까지 입력가능합니다.", Toast.LENGTH_SHORT).show();

                        return;
                    }
                }

                if (mSetTimeStr != null && mSetTimeStr.length() > 0) {
                    int setTimeIndex = Integer.valueOf(mSetTimeStr);
                    if (setTimeIndex < StringsClass.SET_DEFAULT_TIME_INT) {
                        Toast.makeText(SettingActivity.this, "최소지정시간은 "+ StringsClass.SET_DEFAULT_TIME +"시간 입니다.", Toast.LENGTH_SHORT).show();
                    } else if (setTimeIndex > 72) {
                        Toast.makeText(SettingActivity.this, "최대지정시간은 72시간 입니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        setTimeStr = mSetTimeStr;
                        saveInfoData();
                    }
                } else {
                    setTimeStr = StringsClass.SET_DEFAULT_TIME;
                    saveInfoData();
                }
            }
        });

        nameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        phoneEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        noteEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        setTimeEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

    }

    private String maskTime(int value) {
        if (value > 9) {
            return String.valueOf(value);
        } else {
            return "0" + value;
        }
    }

    private TimePickerDialog.OnTimeSetListener fromListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            timeFromHour = hourOfDay;
            timeFromMin = minute;
            timeFromTv.setText(maskTime(hourOfDay) + "시 " + maskTime(minute) + "분");

        }
    };

    private TimePickerDialog.OnTimeSetListener toListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            timeToHour = hourOfDay;
            timeToMin = minute;
            timeToTv.setText(maskTime(hourOfDay) + "시 " + maskTime(minute) + "분");
        }
    };

    private void saveInfoData() {
        nameStr = nameEditText.getText().toString();
        userPhoneStr = phoneEditText.getText().toString();
        addressStr = addressEditText.getText().toString();
        addressStr2 = addressEditText2.getText().toString();
        setTimeStr = setTimeEditText.getText().toString();
        if (typeIndex.equals("0")) {
            noteStr = noteEditText.getText().toString();
        }

        new SharedPreference(SettingActivity.this).setPreferences(StringsClass.PREF_USER_NAME, nameStr);
        new SharedPreference(SettingActivity.this).setPreferences(StringsClass.PREF_USER_PHONE, userPhoneStr);
        new SharedPreference(SettingActivity.this).setPreferences(StringsClass.PREF_USER_ADDRESS, addressStr);
        new SharedPreference(SettingActivity.this).setPreferences(StringsClass.PREF_USER_ADDRESS2, addressStr2);
        new SharedPreference(SettingActivity.this).setPreferences(StringsClass.PREF_USER_COMMENT, noteStr);
        new SharedPreference(SettingActivity.this).setPreferences(StringsClass.PREF_SET_TIME, setTimeStr);
        new SharedPreference(SettingActivity.this).setPreferences(StringsClass.PREF_COMMENT_TYPE, typeIndex);

        new SharedPreference(SettingActivity.this).setPreferences(StringsClass.PREF_TIME_IS_CHECKED, isTime);
        new SharedPreference(SettingActivity.this).setPreferences(StringsClass.PREF_TIME_FROM_HOUR, timeFromHour);
        new SharedPreference(SettingActivity.this).setPreferences(StringsClass.PREF_TIME_FROM_MINUTE, timeFromMin);
        new SharedPreference(SettingActivity.this).setPreferences(StringsClass.PREF_TIME_TO_HOUR, timeToHour);
        new SharedPreference(SettingActivity.this).setPreferences(StringsClass.PREF_TIME_TO_MINUTE, timeToMin);


        AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
        builder.setCancelable(false);
        builder.setTitle("안내");
        builder.setMessage("저장되었습니다.");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                Intent intent = new Intent(SettingActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
        builder.create().show();

    }

    RadioGroup.OnCheckedChangeListener radioGroupButtonChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
            noteEditText.setVisibility(View.GONE);
            if (i == R.id.rgBtn_settingActivity_type0) {
                typeIndex = "0";
                noteStr = "";
                noteEditText.setVisibility(View.VISIBLE);
            } else if (i == R.id.rgBtn_settingActivity_type1) {
                typeIndex = "1";
                noteStr = "뇌병변";
            } else if (i == R.id.rgBtn_settingActivity_type2) {
                typeIndex = "2";
                noteStr = "치매";
            } else if (i == R.id.rgBtn_settingActivity_type3) {
                typeIndex = "3";
                noteStr = "중풍";
            } else if (i == R.id.rgBtn_settingActivity_type4) {
                typeIndex = "4";
                noteStr = "장애인";
            } else if (i == R.id.rgBtn_settingActivity_type5) {
                typeIndex = "5";
                noteStr = "거동불편";
            } else if (i == R.id.rgBtn_settingActivity_type6) {
                typeIndex = "6";
                noteStr = "환자";
            } else if (i == R.id.rgBtn_settingActivity_type7) {
                typeIndex = "7";
                noteStr = "어린이";
            } else if (i == R.id.rgBtn_settingActivity_type8) {
                typeIndex = "8";
                noteStr = "학생";
            }
        }
    };
}