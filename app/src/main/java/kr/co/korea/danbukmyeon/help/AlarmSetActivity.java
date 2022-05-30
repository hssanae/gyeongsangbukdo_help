package kr.co.korea.danbukmyeon.help;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.Calendar;

public class AlarmSetActivity extends BasicActivity {
    public static Context context;

    private Calendar Time;
    private Intent intent;
    private PendingIntent ServicePending;
    private AlarmManager alarmManager;

    private String setTimeStr = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
    }

    AlarmSetActivity(Context mContext) {
        context = mContext;
        alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
    }

    public void setAlarm() {
        new SharedPreference(context).setPreferences(StringsClass.PREF_IS_SET_ALARM, "on");
        setTimeStr = new SharedPreference(context).getPreferences(StringsClass.PREF_SET_TIME) != null ? new SharedPreference(context).getPreferences(StringsClass.PREF_SET_TIME) : StringsClass.SET_DEFAULT_TIME;
        int setTimeIndex = Integer.valueOf(setTimeStr);

        Time = Calendar.getInstance();
        Time.add(Calendar.HOUR, setTimeIndex);
//        Time.add(Calendar.MINUTE, 1);
//        Time.add(Calendar.SECOND, 5);

        intent = new Intent(context, AlarmReceiver.class);
        ServicePending = PendingIntent.getBroadcast(
                context, 3087, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.set(AlarmManager.RTC_WAKEUP, Time.getTimeInMillis(), ServicePending);
    }

    public void setDisturbAlarm(int mSecond) {
        removeAlarm();

        Time = Calendar.getInstance();
        Time.add(Calendar.SECOND, mSecond);

        intent = new Intent(context, AlarmReceiver.class);
        ServicePending = PendingIntent.getBroadcast(
                context, 3087, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.set(AlarmManager.RTC_WAKEUP, Time.getTimeInMillis(), ServicePending);

    }

    public void removeAlarm() {
        new SharedPreference(context).setPreferences(StringsClass.PREF_IS_SET_ALARM, "off");
        intent = new Intent(context, AlarmReceiver.class);
        ServicePending = PendingIntent.getBroadcast(
                context, 3087, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(ServicePending);
    }
}
