package kr.co.korea.danbukmyeon.help;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import static kr.co.korea.danbukmyeon.help.AlarmSetActivity.context;


public class RemoveActivity extends BasicActivity {
    private TextView textView;
    private Button removeAlarmBtn;
    private ImageView rootImg;
    private CountDownTimer countDownTimer;
    private Intent intent;
    private PendingIntent ServicePending;
    private AlarmManager alarmManager;
    private AudioManager audioManager;
    private MediaPlayer alarmSound;
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove);

        SharedPreferences pref = this.getSharedPreferences(StringsClass.PREF_NAME, Activity.MODE_PRIVATE);
        boolean isTime = pref.getBoolean(StringsClass.PREF_TIME_IS_CHECKED, false);
        int alarmTime = 0;


        if (isTime) {
            alarmTime = checkDisturb();

            if (alarmTime > 0) {
                new AlarmSetActivity(context).setDisturbAlarm(alarmTime);
                RemoveActivity.this.finish();
            } else {
                setAlertAlarm();
            }
        } else {
            setAlertAlarm();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (alarmSound != null) {
            alarmSound.release();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setAlertAlarm() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        rootImg = findView(R.id.imgView_removeActivity_bg);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        textView = findView(R.id.tv_removeActivity_count);
        removeAlarmBtn = findView(R.id.removeAlarm);
        removeAlarmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeAlarm();
            }
        });

        DbHandler db = new DbHandler(this);
        ArrayList<HashMap<String, String>> userList = db.GetUsers();

        if (userList.size() > 0) {
            audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

            sound_mode_check();

            countDownTimer = new CountDownTimer(1000 * 60 * 30, 1000 * 60) {
                public void onTick(long millisUntilFinished) {
                    textView.setText(String.format(Locale.getDefault(), "%d 분 후 문자가 발송됩니다.", millisUntilFinished / 60000L));

                    removeAlarmBtn.setText("문자발송 중지");
                }

                public void onFinish() {
                    sendSMS();
                    removeAlarmBtn.setText("확인");
                }
            }.start();
        } else {
            textView.setText("등록된 긴급구호자가 없습니다. \n긴급구호자를 등록 해주세요.");
            removeAlarmBtn.setText("확인");
        }

        rootImg.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                removeAlarm();
                return true;
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        removeAlarm();
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        removeAlarm();
        return false;
    }

    private int checkDisturb() {
        boolean isAlarm = true;
        boolean isNextDay = false;

        int hourStartIndex = 22;
        int minStartIndex = 0;
        int hourEndIndex = 7;
        int minEndIndex = 0;

        int hourNowIndex = 22;
        int minNowIndex = 0;

        int nextSecond = 60 * 60 * 24;

        try {
            Date date = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            hourNowIndex = cal.get(Calendar.HOUR_OF_DAY);
            minNowIndex = cal.get(Calendar.MINUTE);
        } catch (NullPointerException | IllegalArgumentException e) {
            System.out.print("예외발생");
        }

        SharedPreferences pref = RemoveActivity.this.getSharedPreferences(StringsClass.PREF_NAME, Activity.MODE_PRIVATE);
        hourStartIndex = pref.getInt(StringsClass.PREF_TIME_FROM_HOUR, 22);
        minStartIndex = pref.getInt(StringsClass.PREF_TIME_FROM_MINUTE, 0);
        hourEndIndex = pref.getInt(StringsClass.PREF_TIME_TO_HOUR, 7);
        minEndIndex = pref.getInt(StringsClass.PREF_TIME_TO_MINUTE, 0);

        if (hourStartIndex < hourEndIndex) {
            if (hourStartIndex < hourNowIndex && hourEndIndex > hourNowIndex) {
                isAlarm = false;
            } else if (hourStartIndex == hourNowIndex) {
                if (minStartIndex <= minNowIndex) {
                    isAlarm = false;
                }
            } else if (hourEndIndex == hourNowIndex) {
                if (minEndIndex >= minNowIndex) {
                    isAlarm = false;
                }
            }
        } else if (hourStartIndex == hourEndIndex) {
            if (minStartIndex < minEndIndex) {
                if (hourStartIndex == hourNowIndex) {
                    if (minStartIndex <= minNowIndex && minEndIndex >= minNowIndex) {
                        isAlarm = false;
                    }
                }
            } else if (minStartIndex >= minEndIndex) {
                if (hourStartIndex < hourNowIndex) {
                    isNextDay = true;
                    isAlarm = false;
                } else if (hourStartIndex == hourNowIndex) {
                    if (minStartIndex <= minNowIndex) {
                        isNextDay = true;
                        isAlarm = false;
                    } else if (minEndIndex > minNowIndex) {
                        isAlarm = false;
                    }
                } else if (hourEndIndex > hourNowIndex) {
                    isAlarm = false;
                }
            }
        } else if (hourStartIndex > hourEndIndex) {
            if (hourStartIndex < hourNowIndex) {
                isNextDay = true;
                isAlarm = false;
            } else if (hourStartIndex == hourNowIndex) {
                if (minStartIndex <= minNowIndex) {
                    isNextDay = true;
                    isAlarm = false;
                } else if (minEndIndex > minNowIndex) {
                    isAlarm = false;
                }
            } else if (hourEndIndex > hourNowIndex) {
                isAlarm = false;
            }
        }

        if (!isAlarm) {
            long now = System.currentTimeMillis();
            Date dateNow = new Date(now);
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            SimpleDateFormat alarmFormat = new SimpleDateFormat("yyyy-MM-dd");

            try {
                String alarmStr = alarmFormat.format(now) + " " + hourEndIndex + ":" + minEndIndex;
                Date dateAlarm = dateFormat.parse(alarmStr);


                long duration = dateAlarm.getTime() - dateNow.getTime();
                int second = (int) duration / 1000;
                if (isNextDay) {
                    second += nextSecond;
                }


                if (second < 0) {
                    second = 0;
                }

                return second;

            } catch (ParseException e) {
                System.out.print("예외발생");
            }
        }

        return 0;
    }

    private void sound_mode_check() {
        switch (audioManager.getRingerMode()) {
            case AudioManager.RINGER_MODE_NORMAL:
                break;
            case AudioManager.RINGER_MODE_SILENT:
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                break;
        }

        audioManager.setStreamVolume(AudioManager.STREAM_RING,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_RING), AudioManager.FLAG_PLAY_SOUND);

        alarmSound = MediaPlayer.create(getBaseContext(), R.raw.alarm);
        alarmSound.setLooping(true);
        alarmSound.setVolume(1, 1);
        if (alarmSound != null && !alarmSound.isPlaying()) {
            alarmSound.start();
        }
        if (vibrator != null) {
            vibrator.vibrate(new long[]{1000, 3000}, 0);
        }
    }

    private void removeAlarm() {
        if (alarmSound != null) {
            alarmSound.release();
        }

        if (vibrator != null) {
            vibrator.cancel();
        }

        intent = new Intent(RemoveActivity.this, AlarmReceiver.class);
        ServicePending = PendingIntent.getBroadcast(
                RemoveActivity.this, 3087, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        if (alarmManager != null) {
            alarmManager.cancel(ServicePending);
        }

        new AlarmSetActivity(context).removeAlarm();

        Intent mainActivityIntent = new Intent(RemoveActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        startActivity(mainActivityIntent);
        finish();
    }

    private void sendSMS() {
        DbHandler db = new DbHandler(this);
        ArrayList<HashMap<String, String>> userList = db.GetUsers();
        String userPhoneStr = new SharedPreference(RemoveActivity.this).getPreferences(StringsClass.PREF_USER_PHONE) != null ? new SharedPreference(RemoveActivity.this).getPreferences(StringsClass.PREF_USER_PHONE) : "";
        String nameStr = new SharedPreference(RemoveActivity.this).getPreferences(StringsClass.PREF_USER_NAME) != null ? new SharedPreference(RemoveActivity.this).getPreferences(StringsClass.PREF_USER_NAME) : "";
        String addressStr = new SharedPreference(RemoveActivity.this).getPreferences(StringsClass.PREF_USER_ADDRESS) != null ? new SharedPreference(RemoveActivity.this).getPreferences(StringsClass.PREF_USER_ADDRESS) : "";
        String addressStr2 = new SharedPreference(RemoveActivity.this).getPreferences(StringsClass.PREF_USER_ADDRESS2) != null ? new SharedPreference(RemoveActivity.this).getPreferences(StringsClass.PREF_USER_ADDRESS2) : "";
        String noteStr = new SharedPreference(RemoveActivity.this).getPreferences(StringsClass.PREF_USER_COMMENT) != null ? (new SharedPreference(RemoveActivity.this).getPreferences(StringsClass.PREF_USER_COMMENT)) : "";
        String setTimeStr = new SharedPreference(RemoveActivity.this).getPreferences(StringsClass.PREF_SET_TIME) != null ? new SharedPreference(RemoveActivity.this).getPreferences(StringsClass.PREF_SET_TIME) : StringsClass.SET_DEFAULT_TIME;

        String addressStr3 = addressStr + " " + addressStr2;

        GpsTracker gpsTracker = new GpsTracker(RemoveActivity.this);
        double latitude = gpsTracker.getLatitude();         // 위도
        double longitude = gpsTracker.getLongitude();         //경도
        String address = getCurrentAddress(RemoveActivity.this, latitude, longitude);

        if(address.contains("미발견")){
            address = addressStr3;
        }

        String commentStr =
                "[긴급구호요청문자]\n" +
                        nameStr + (noteStr.trim().length() > 0 ? "(" + noteStr + ")" : "") + "\n" +
                        "- " + setTimeStr + "시간 동안 휴대폰 미사용\n" +
//                        (userPhoneStr.trim().length() > 0 ? "- " + phoneFormat(userPhoneStr) : "") + "\n" +
                        "- 위치정보 : " + address;

        for (int i = 0; i < userList.size(); i++) {
            try {
                if (userList.get(i).get("phone") != null) {
                    SmsManager sms = SmsManager.getDefault();
                    ArrayList<String> parts = sms.divideMessage(commentStr);
                    sms.sendMultipartTextMessage(userList.get(i).get("phone"), null, parts, null, null);
                }
            } catch (RuntimeException e) {
                System.out.print("예외발생");
            }
        }

        textView.setText("저장된 긴급구호자에게 문자가 발송되었습니다.");

        db.close();

        if (alarmSound != null) {
            alarmSound.release();
        }
        if (vibrator != null) {
            vibrator.cancel();
        }
    }
}
