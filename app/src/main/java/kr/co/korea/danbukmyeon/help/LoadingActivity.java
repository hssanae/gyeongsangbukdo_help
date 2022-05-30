package kr.co.korea.danbukmyeon.help;
import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.lahm.library.EasyProtectorLib;

import java.io.File;
import java.io.IOException;

public class LoadingActivity extends Activity {
    public static final String ROOT_PATH = Environment.getExternalStorageDirectory() + "";
    public static final String ROOTING_PATH_1 = "/system/bin/su";
    public static final String ROOTING_PATH_2 = "/system/xbin/su";
    public static final String ROOTING_PATH_3 = "/system/app/SuperUser.apk";
    public static final String ROOTING_PATH_4 = "/data/data/com.noshufou.android.su";

    public String[] RootFilesPath = new String[]{
            ROOT_PATH + ROOTING_PATH_1,
            ROOT_PATH + ROOTING_PATH_2,
            ROOT_PATH + ROOTING_PATH_3,
            ROOT_PATH + ROOTING_PATH_4
    };

    AlertDialog.Builder alertDialog;

    private CustomDialog permissionPopupView;
    private NotificationManager notificationManager;

    AlertDialog.Builder builder;

    boolean isCheckSilent = false;
    boolean isCheckGrid = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (EasyProtectorLib.checkIsRoot() || checkRooting()) {
            Toast.makeText(LoadingActivity.this, "루팅된 기기에서는 해당 서비스를 이용하실 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            alertDialog = new AlertDialog.Builder(this);
            notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            builder = new AlertDialog.Builder(LoadingActivity.this);

            checkVerify();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (builder != null) {
            builder.create().dismiss();
        }
    }

    private boolean checkRooting() {
        boolean isRootingFlag = false;

        try {
            Runtime.getRuntime().exec("su");
            isRootingFlag = true;
        } catch (RuntimeException | IOException e) {
            // Exception 나면 루팅 false;
            isRootingFlag = false;
        }

        if (!isRootingFlag) {
            isRootingFlag = checkRootingFiles(createFiles(RootFilesPath));
        }

        return isRootingFlag;
    }


    public void checkVerify() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (
                    checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED
                            || checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                            || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
                int width = dm.widthPixels;

                permissionPopupView = new CustomDialog(LoadingActivity.this);
                WindowManager.LayoutParams wm = permissionPopupView.getWindow().getAttributes();
                wm.copyFrom(permissionPopupView.getWindow().getAttributes());
                wm.width = (int) (width * 0.8);

                permissionPopupView.setCancelable(false);
                permissionPopupView.show();
            } else {
                checkSilentMode();
            }
        } else {
            if (isCheckSilent && isCheckGrid) {
                Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
                startActivity(intent);
                LoadingActivity.this.finish();
            } else {
                Handler h = new Handler();
                h.postDelayed(new splashhandler(), 2000);
            }
        }

    }

    private void checkSilentMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!notificationManager.isNotificationPolicyAccessGranted()) {

                builder.setCancelable(false);
                builder.setTitle("안내");
                builder.setMessage("방해 금지 권한을 허용해주세요");
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        isCheckSilent = true;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            LoadingActivity.this.startActivity(new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS));
                        }
                        else {
                            LoadingActivity.this.startActivity(new Intent("android.settings.NOTIFICATION_POLICY_ACCESS_SETTINGS"));
                        }
                    }
                });
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        LoadingActivity.this.finish();
                    }
                });
                builder.create().show();
            } else {
                checkOtherGridMode();
            }
        }
    }

    // 10 버전 이슈
    private void checkOtherGridMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!Settings.canDrawOverlays(this)) {

                builder.setCancelable(false);
                builder.setTitle("안내");
                builder.setMessage("다른 화면 위에 그리기 권한이 필요합니다. \n 권한을 허용해주세요");
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        isCheckGrid = true;
//                        LoadingActivity.this.startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
                        startActivity(
                                new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()))
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        );
                    }
                });
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        LoadingActivity.this.finish();
                    }
                });
                builder.create().show();
            } else {
                if (isCheckSilent && isCheckGrid) {
                    Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
                    startActivity(intent);
                    LoadingActivity.this.finish();
                } else {
                    Handler h = new Handler();
                    h.postDelayed(new splashhandler(), 2000);
                }
            }
        } else {
            Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
            startActivity(intent);
            LoadingActivity.this.finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0) {
                for (int i = 0; i < grantResults.length; ++i) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        new AlertDialog.Builder(this).setTitle("알림").setMessage("권한을 허용해주셔야 앱을 이용할 수 있습니다.")
                                .setPositiveButton("종료", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        LoadingActivity.this.finish();
                                    }
                                }).setNegativeButton("권한 설정", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        .setData(Uri
                                                .parse("package:" + getApplicationContext().getPackageName()));
                                getApplicationContext().startActivity(intent);
                            }
                        }).setCancelable(false).show();

                        return;
                    }
                }
                checkSilentMode();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            System.exit(0);
        }

        return (super.onKeyDown(keyCode, event));
    }

    class splashhandler implements Runnable {
        public void run() {
            Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
            startActivity(intent);
            LoadingActivity.this.finish();
        }
    }


    public class CustomDialog extends Dialog {

        Button popupSubmitBtn;

        public CustomDialog(Context context) {
            super(context);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            setContentView(R.layout.popup_loading_permission);

            popupSubmitBtn = (Button) findViewById(R.id.btn_loadingPopup_submit);

            popupSubmitBtn.setOnClickListener(new View.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    permissionPopupView.dismiss();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{
                                Manifest.permission.SEND_SMS,
                                Manifest.permission.READ_PHONE_STATE,
                                Manifest.permission.ACCESS_FINE_LOCATION,

                        }, 1);
                    }
                }
            });
        }

    }

    /**
     * 루팅파일 의심 Path를 가진 파일들을 생성 한다.
     */
    private File[] createFiles(String[] sfiles) {
        File[] rootingFiles = new File[sfiles.length];
        for (int i = 0; i < sfiles.length; i++) {
            rootingFiles[i] = new File(sfiles[i]);
        }
        return rootingFiles;
    }

    /**
     * 루팅파일 여부를 확인 한다.
     */
    private boolean checkRootingFiles(File... file) {
        boolean result = false;
        for (File f : file) {
            if (f != null && f.exists() && f.isFile()) {
                result = true;
                break;
            } else {
                result = false;
            }
        }
        return result;
    }
}