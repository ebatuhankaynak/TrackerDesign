package ebk.trackerDesign.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.text.LoginFilter;
import android.util.Log;

import ebk.trackerDesign.model.TrackerActivityType;

/**
 * Created by E.Batuhan Kaynak on 10.8.2016.
 */
public class NotificationReceiver extends WakefulBroadcastReceiver{

    AlarmManager alarmManager;
    PendingIntent alarmIntent;
    static String name;

    public void setAlarm(Context context, String name, String type){
        alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        int alarmTime = getAlarmTime(type);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + alarmTime, alarmIntent);

        this.name = name;

        Log.i("LOG SET ALARM", "ALARM SET");
        /*
        ComponentName receiver = new ComponentName(context, TrackerBootReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);*/
    }
    // TODO: 23.7.2016 OPTIMAL SLEEP: 8, EDU: 6 hours tot 60 normal ENTER: 2 normal 7 tot SPORTS: 1,5 norm 3 tot
    private int getAlarmTime(String type) {
        int result = 60 * 1000;
        if (type.equals(TrackerActivityType.EDUCATION)){
            result = result * 60;
        } else if (type.equals(TrackerActivityType.ENTERTAINMENT)){
            result = result * 120;
        } else if (type.equals(TrackerActivityType.SPORTS)) {
            result = result * 90;
        }
        return result;
    }

    public void cancelAlarm() {
        // If the alarm has been set, cancel it.
        if (alarmManager!= null) {
            alarmManager.cancel(alarmIntent);
        }

        // Disable {@code SampleBootReceiver} so that it doesn't automatically restart the
        // alarm when the device is rebooted.
        /*
        ComponentName receiver = new ComponentName(context, TrackerBootReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);*/
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, NotificationService.class);
        service.putExtra("message", getNotificationMessage(name));
        startWakefulService(context, service);
    }

    private String getNotificationMessage(String name) {
        return "Didn't you have enough of " + name + "? Do something else!";
        // TODO: 11.8.2016 RANDOMIZE AND MAKE SMART 
    }
}