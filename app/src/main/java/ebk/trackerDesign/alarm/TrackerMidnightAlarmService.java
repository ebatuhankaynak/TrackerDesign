package ebk.trackerDesign.alarm;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

import ebk.trackerDesign.database.TrackerDatabaseHelper;

/**
 * Created by E.Batuhan Kaynak on 21.7.2016.
 */
public class TrackerMidnightAlarmService extends IntentService {
    private SQLiteDatabase db;
    private Cursor cursor;

    public TrackerMidnightAlarmService() {
        super("TrackerMidnightAlarmService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SQLiteOpenHelper trackerDatabaseHelper = new TrackerDatabaseHelper(this);
        db = trackerDatabaseHelper.getReadableDatabase();
        updateLastDay();
    }

    private void updateLastDay(){
        cursor = db.query("ACTIVITY", new String[]{"_id", "NAME", "START_TIME", "DATE"}, "ONGOING = ?", new String[]{String.valueOf(1)},
                null, null, null);
        if (cursor.moveToFirst()){
            endYesterdayActivity(cursor.getInt(0), cursor.getInt(2));
            startActivityAgain(cursor.getInt(0));
        }
    }

    private void startActivityAgain(int id){
        Cursor newActivityCursor = db.query("ACTIVITY", new String[]{"NAME", "TYPE"}, "_id = ?", new String[]{String.valueOf(id)},
                null, null, null);
        newActivityCursor.moveToFirst();
        ContentValues activityValues = new ContentValues();
        activityValues.put("NAME", newActivityCursor.getString(0));
        activityValues.put("TYPE", newActivityCursor.getString(1));

        activityValues.put("START_TIME", 0);
        activityValues.put("ONGOING", 1);

        Date activityDate = new Date(System.currentTimeMillis());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = simpleDateFormat.format(activityDate);

        activityValues.put("DATE", formattedDate);

        db.insert("ACTIVITY", null, activityValues);
    }

    private void endYesterdayActivity(int id, int startTime){
        ContentValues updatedValues = new ContentValues();
        updatedValues.put("ONGOING", 0);

        int endTime = 86400;
        updatedValues.put("END_TIME", endTime);

        db.update("ACTIVITY", updatedValues, "_id = ?", new String[] {String.valueOf(id)});
    }
}
