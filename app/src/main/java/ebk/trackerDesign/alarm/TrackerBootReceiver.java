package ebk.trackerDesign.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by E.Batuhan Kaynak on 20.7.2016.
 */

public class TrackerBootReceiver extends BroadcastReceiver {
    TrackerMidnightAlarmReceiver trackerMidnightAlarmReceiver = new TrackerMidnightAlarmReceiver();
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // Set the alarm here.
            trackerMidnightAlarmReceiver.setAlarm(context);
        }
    }
}
