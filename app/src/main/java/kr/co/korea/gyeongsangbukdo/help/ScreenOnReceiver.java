package kr.co.korea.gyeongsangbukdo.help;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

class ScreenOnReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent in = new Intent(context, UndeadService.class);
            context.startForegroundService(in);
        } else {
            Intent in = new Intent(context, UndeadService.class);
            context.startService(in);
        }

        String action = intent.getAction();
        if (action.equals(Intent.ACTION_SCREEN_OFF)) {
            String isAlarm =  new SharedPreference(context).getPreferences(StringsClass.PREF_IS_SET_ALARM) != null ? new SharedPreference(context).getPreferences(StringsClass.PREF_IS_SET_ALARM) : "off";
            if(!isAlarm.contains("on")){
                new AlarmSetActivity(context).setAlarm();
            }
        } else if (action.equals(Intent.ACTION_USER_PRESENT)) {
            new AlarmSetActivity(context).removeAlarm();
        }
    }
}
