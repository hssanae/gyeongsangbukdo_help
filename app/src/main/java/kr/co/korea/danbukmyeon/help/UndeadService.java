package kr.co.korea.danbukmyeon.help;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.webkit.CookieManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.util.Calendar;

public class UndeadService extends Service {
    public static Intent serviceIntent = null;
    private ScreenOnReceiver screenOnReceiver;
    private boolean isRegister = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        serviceIntent = intent;
        initializeNotification();

        screenOnReceiver = new ScreenOnReceiver();

        if (!isRegister) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.USER_PRESENT");
            filter.addAction("android.intent.action.SCREEN_OFF");
            registerReceiver(screenOnReceiver, filter);

            isRegister = true;
        }


        return START_STICKY;
    }

    private void clearCache() {
        final File cacheDirFile = this.getCacheDir();
        if (null != cacheDirFile && cacheDirFile.isDirectory()) {
            clearSubCacheFiles(cacheDirFile);
        }
    }

    private void clearSubCacheFiles(File cacheDirFile) {
        try {
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeSessionCookie();

            if (null == cacheDirFile || cacheDirFile.isFile()) {
                return;
            }
            for (File cacheFile : cacheDirFile.listFiles()) {
                if (cacheFile.isFile()) {
                    if (cacheFile.exists()) {
                        cacheFile.delete();
                    }
                } else {
                    clearSubCacheFiles(cacheFile);
                }
            }
        } catch (RuntimeException e) {
            System.out.println("예외발생");
        }
    }


    public void initializeNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1");
        builder.setSmallIcon(R.mipmap.logo);
        NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
        style.bigText("설정을 보려면 누르세요.");
        style.setBigContentTitle(null);
        style.setSummaryText("서비스 동작중");
        builder.setContentText(null);
        builder.setContentTitle(null);
        builder.setOngoing(true);
        builder.setStyle(style);
        builder.setWhen(0);
        builder.setShowWhen(false);
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        builder.setContentIntent(pendingIntent);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(new NotificationChannel("1", "구호서비스", NotificationManager.IMPORTANCE_LOW));
        }
        Notification notification = builder.build();
        startForeground(1, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clearCache();

        try {
            if (isRegister) {
                unregisterReceiver(screenOnReceiver);
                isRegister = false;
            }
        } catch (IllegalArgumentException e) {
            System.out.println("System ERROR");
        }

        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 2);
        Intent intent = new Intent(this, UndeadReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 2);
        Intent intent = new Intent(this, UndeadReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
    }
}