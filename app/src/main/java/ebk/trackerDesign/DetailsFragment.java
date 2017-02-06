package ebk.trackerDesign;


import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

import ebk.trackerDesign.database.TrackerDatabaseHelper;
import ebk.trackerDesign.model.TrackerActivityType;


/**
 * A simple {@link Fragment} subclass.
 */
public class DetailsFragment extends Fragment {

    private TextView startTimeTextView;
    private TextView endTimeValueTextView;
    private int startTime;
    private int endTime;
    private boolean ongoing;

    private SQLiteDatabase db;
    private Cursor cursor;

    private ImageView activityTypeImageView;

    public DetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        SQLiteOpenHelper trackerDatabaseHelper = new TrackerDatabaseHelper(getContext());
        db = trackerDatabaseHelper.getReadableDatabase();

        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        //ActionBar actionBar = getActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(true);

        Button endActivityButton = (Button) view.findViewById(R.id.endActivityButton);
        Button editActivityButton = (Button) view.findViewById(R.id.trackerActivityEditButton);
        endActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onEndActivityClick();
            }
        });
        editActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onEditActivityClick();
            }
        });
        buildUI(view);
    }

    public void buildUI(View view){
        int position = 0;

        Bundle bundle = this.getArguments();
        if (bundle != null){
            long id = bundle.getLong("Id");
            position = (int) id;
        }

        cursor = db.query("ACTIVITY", new String[] {"NAME", "TYPE", "START_TIME", "END_TIME", "ONGOING", "_id"}, "_id = ?",
                new String[] {Integer.toString(position)}, null, null, null);

        if (cursor.moveToFirst()){
            activityTypeImageView = (ImageView) view.findViewById(R.id.trackerActivityActivityTypeImageView);
            activityTypeImageView.setImageResource(TrackerActivityType.setDrawable(cursor.getString(1)));

            TextView activityNameTextView = (TextView) view.findViewById(R.id.trackerActivityNameTextView);
            activityNameTextView.setText(cursor.getString(0));

            startTimeTextView = (TextView) view.findViewById(R.id.trackerActivityStartValueTextView);
            startTime = cursor.getInt(2);
            startTimeTextView.setText(getTimeString(startTime));

            ongoing = cursor.getInt(4) == 1;
            if (!ongoing){
                TextView endTimeTextView = (TextView) view.findViewById(R.id.trackerActivityEndTextView);
                endTimeTextView.setText(this.getString(R.string.tracker_activity_end));
                endTimeValueTextView = (TextView) view.findViewById(R.id.trackerActivityEndValueTextView);
                endTime = cursor.getInt(3);
                endTimeValueTextView.setText(getTimeString(endTime));
                Button endActivityButton = (Button) view.findViewById(R.id.endActivityButton);
                endActivityButton.setVisibility(View.GONE);
            }
            final TextView durationTextView = (TextView) view.findViewById(R.id.trackerActivityDurationValueTextView);

            final Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    durationTextView.setText(presentableDuration());
                    handler.postDelayed(this, 1000);
                }
            });
        }
    }

    public void onEndActivityClick(){
        ContentValues updatedValues = new ContentValues();
        updatedValues.put("ONGOING", 0);

        Calendar calendar = Calendar.getInstance();
        int endTime = (calendar.get(Calendar.HOUR_OF_DAY) * 3600) + (calendar.get(Calendar.MINUTE) * 60) + (calendar.get(Calendar.SECOND));
        updatedValues.put("END_TIME", endTime);

        db.update("ACTIVITY", updatedValues, "_id = ?", new String[] {String.valueOf(cursor.getInt(5))});
        AddFragment.notificationReceiver.cancelAlarm();
        buildUI(getView());
    }

    public void onEditActivityClick(){
        CharSequence options[] = new CharSequence[] {"Name", "Activity Type", "Start Time", "End Time", "Delete Activity"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0){
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Change Name");
                    //builder.setMessage("What is your name:");

                    final EditText input = new EditText(getContext());
                    builder.setView(input);

                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                            ContentValues updatedValues = new ContentValues();
                            updatedValues.put("NAME", input.getText().toString());
                            db.update("ACTIVITY", updatedValues, "_id = ?", new String[] {String.valueOf(cursor.getInt(5))});
                            TextView activityNameTextView = (TextView) getView().findViewById(R.id.trackerActivityNameTextView);
                            activityNameTextView.setText(input.getText().toString());
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    });
                    builder.show();
                }else if(which == 1){
                    final String[] items = TrackerActivityType.allActivities;
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Edit Activity Type");
                    builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            String newType = TrackerActivityType.getActivityType(item);
                            ContentValues updatedValues = new ContentValues();
                            updatedValues.put("TYPE", newType);
                            db.update("ACTIVITY", updatedValues, "_id = ?", new String[] {String.valueOf(cursor.getInt(5))});
                            activityTypeImageView.setImageResource(TrackerActivityType.setDrawable(newType));
                        }
                    });
                    builder.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            return;
                        }
                    });
                    builder.show();
                }else if(which == 2){
                    Calendar mcurrentTime = Calendar.getInstance();
                    int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                    int minute = mcurrentTime.get(Calendar.MINUTE);
                    TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            ContentValues updatedValues = new ContentValues();
                            updatedValues.put("START_TIME", hourOfDay * 3600 + minute * 60);
                            db.update("ACTIVITY", updatedValues, "_id = ?", new String[] {String.valueOf(cursor.getInt(5))});
                            startTimeTextView.setText(getTimeString(hourOfDay * 3600 + minute * 60));
                        }
                    }, hour, minute, true);

                    timePickerDialog.setTitle("Start Time");
                    timePickerDialog.show();
                }else if(which == 3){
                    Calendar mcurrentTime = Calendar.getInstance();
                    int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                    int minute = mcurrentTime.get(Calendar.MINUTE);

                    TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            ContentValues updatedValues = new ContentValues();
                            updatedValues.put("END_TIME", hourOfDay * 3600 + minute * 60);
                            db.update("ACTIVITY", updatedValues, "_id = ?", new String[] {String.valueOf(cursor.getInt(5))});
                            endTimeValueTextView.setText(getTimeString(hourOfDay * 3600 + minute * 60));
                        }
                    }, hour, minute, true);

                    timePickerDialog.setTitle("End Time");
                    timePickerDialog.show();
                }else if(which == 4){
                    db.delete("ACTIVITY", "_id = ?", new String[] {String.valueOf(cursor.getInt(5))});
                    AddFragment.notificationReceiver.cancelAlarm();
                    Intent intent = new Intent( getContext(), MainActivity.class);
                    startActivity(intent);
                }
            }
        });
        builder.show();
    }

    public int calculateDuration(){
        int difference;
        if (ongoing){
            Calendar calendar = Calendar.getInstance();
            int seconds = (calendar.get(Calendar.HOUR_OF_DAY) * 3600) + (calendar.get(Calendar.MINUTE) * 60) + (calendar.get(Calendar.SECOND));
            difference = seconds - startTime;
        }else {
            difference = endTime - startTime;
        }
        return difference;
    }

    public String presentableDuration(){
        String durationString = "";
        int seconds = calculateDuration();
        int minutes;
        int hours;

        hours = seconds / 3600;
        minutes = (seconds  %3600) / 60;
        seconds = seconds % 60;

        if (hours > 1){
            durationString = durationString + hours + "hours ";
        }else if (hours == 1){
            durationString = durationString + hours + "hour ";
        }

        if (minutes > 1){
            durationString = durationString + minutes + "minutes ";
        }else if (minutes == 1){
            durationString = durationString + minutes + "minute ";
        }

        durationString = durationString + seconds + " seconds";

        return durationString;
    }

    public String getTimeString(int seconds){
        String timeString = "";
        int hours = seconds / 3600;
        int minutes = (seconds  %3600) / 60;
        seconds = seconds % 60;

        if (minutes < 10){
            timeString = hours + ":0" + minutes;
        }else {
            timeString = hours + ":" + minutes;
        }
        return timeString;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        cursor.close();
        db.close();
    }
}
