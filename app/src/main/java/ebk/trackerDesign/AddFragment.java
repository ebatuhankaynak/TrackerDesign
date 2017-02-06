package ebk.trackerDesign;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import ebk.trackerDesign.database.TrackerDatabaseHelper;
import ebk.trackerDesign.model.TrackerActivityType;
import ebk.trackerDesign.notification.NotificationReceiver;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddFragment extends Fragment {

    private String activityType;
    private SQLiteDatabase db;
    private Cursor cursor;

    private View view;
    private ArrayList<String> allDates;
    private int datePosition;
    private String formattedDate;

    public static NotificationReceiver notificationReceiver = new NotificationReceiver();

    public AddFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState){
        this.view = view;
        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.activityTypeRadioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                onRadioButtonClicked(view);
            }
        });
        Button addActivityButton = (Button) view.findViewById(R.id.addActivityButton);
        addActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAddActivityClick();
            }
        });
    }

    public void onAddActivityClick(){
        EditText activityNameEditText = (EditText) view.findViewById(R.id.nameEditText);
        RadioGroup activityTypeRadioGroup = (RadioGroup) view.findViewById(R.id.activityTypeRadioGroup);

        final String activityName = activityNameEditText.getText().toString();

        int id = activityTypeRadioGroup.getCheckedRadioButtonId();
        if (id == -1){
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("No Type!");
            builder.setPositiveButton("Oops (^_^;)", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int whichButton) {
                    return;
                }
            });
            builder.show();
        }
        else{
            SQLiteOpenHelper trackerDatabaseHelper = new TrackerDatabaseHelper(getContext());
            db = trackerDatabaseHelper.getWritableDatabase();

            final Cursor cursor = db.query("ACTIVITY", new String[]{"_id"}, "ONGOING = ?", new String[]{String.valueOf(1)}, null, null, null);
            if (cursor.moveToFirst()){
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Ongoing Activity");
                builder.setMessage("There is an ongoing activity, would you like to terminate it?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        ContentValues updatedValues = new ContentValues();
                        updatedValues.put("ONGOING", 0);

                        Calendar calendar = Calendar.getInstance();
                        int endTime = (calendar.get(Calendar.HOUR_OF_DAY) * 3600) + (calendar.get(Calendar.MINUTE) * 60) + (calendar.get(Calendar.SECOND));
                        updatedValues.put("END_TIME", endTime);

                        db.update("ACTIVITY", updatedValues, "_id = ?", new String[] {String.valueOf(cursor.getInt(0))});
                        //AddFragment.notificationReceiver.cancelAlarm();
                        insertActivity(activityName, activityType);

                        getAllDates();
                        formattedDate = allDates.get(datePosition);
                        switchFragment(formattedDate);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
                builder.show();
            } else {
                insertActivity(activityName, activityType);

                getAllDates();
                formattedDate = allDates.get(datePosition);
                switchFragment(formattedDate);
                // TODO: 10.8.2016 DAFUQ DIS WORK< 
            }
        }
    }

    public void onRadioButtonClicked(View view){
        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.activityTypeRadioGroup);
        TextView activityTypeTextView = (TextView) view.findViewById(R.id.activityTypeTextView);

        int id = radioGroup.getCheckedRadioButtonId();
        RadioButton activityTypeRadioButton = (RadioButton) view.findViewById(id);
        String radioString = activityTypeRadioButton.getResources().getResourceEntryName(id);
        activityType = TrackerActivityType.getActivityType(radioString);

        activityTypeTextView.setText(activityType);
    }

    private void insertActivity(String activityName, String activityType) {
        ContentValues activityValues = new ContentValues();
        activityValues.put("NAME", activityName);
        activityValues.put("TYPE", activityType);

        Calendar calendar = Calendar.getInstance();
        int startTime = (calendar.get(Calendar.HOUR_OF_DAY) * 3600) + (calendar.get(Calendar.MINUTE) * 60) + (calendar.get(Calendar.SECOND));
        activityValues.put("START_TIME", startTime);

        activityValues.put("ONGOING", 1);

        Date activityDate = new Date(System.currentTimeMillis());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = simpleDateFormat.format(activityDate);

        activityValues.put("DATE", formattedDate);

        db.insert("ACTIVITY", null, activityValues);
        notificationReceiver.setAlarm(getContext(), activityName, activityType);
    }

    public void switchFragment(String date){
        Fragment fragment = new TrackerActivityListFragment();

        Bundle bundle = new Bundle();
        bundle.putString("date", date);
        fragment.setArguments(bundle);

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, fragment);
        ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }

    public void getAllDates(){
        allDates = new ArrayList<String>();
        datePosition = 0;
        cursor = db.query("ACTIVITY", new String[]{"DATE", "COUNT(DATE) AS count"}, null, null, "DATE", null, null);
        if (cursor.moveToFirst()){
            do {
                if (cursor.getString(0) != null){
                    allDates.add(cursor.getString(0));
                }
            }while (cursor.moveToNext());
        }
        if (!(allDates.contains(new SimpleDateFormat("dd-MMM-yyyy").format(new Date())))){
            allDates.add(new SimpleDateFormat("dd-MMM-yyyy").format(new Date()));
        }
        Collections.reverse(allDates);
    }
}
