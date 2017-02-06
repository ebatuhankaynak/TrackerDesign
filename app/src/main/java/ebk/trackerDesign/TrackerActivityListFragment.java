package ebk.trackerDesign;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import ebk.trackerDesign.database.TrackerDatabaseHelper;
import ebk.trackerDesign.model.TrackerActivityType;

public class TrackerActivityListFragment extends ListFragment {
    private SQLiteDatabase db;
    private Cursor cursor;

    private SimpleCursorAdapter listAdapter;
    private ArrayList<String> allDates;
    private int datePosition;

    public TrackerActivityListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        SQLiteOpenHelper trackerDatabaseHelper = new TrackerDatabaseHelper(getContext());
        db = trackerDatabaseHelper.getReadableDatabase();

        allDates = MainActivity.allDates;
        datePosition = MainActivity.datePosition;
        String date = allDates.get(datePosition);

        cursor = db.query("ACTIVITY", new String[]{"_id", "NAME", "TYPE", "ONGOING"}, "DATE = ?", new String[]{date},
                null, null, "START_TIME ASC");
        listAdapter = new SimpleCursorAdapter(inflater.getContext(),
                R.layout.tracker_list,
                cursor,
                new String[]{"NAME", "TYPE", "ONGOING"},
                new int[]{R.id.listTextView, R.id.listImageView, R.id.listOngoingImageView},
                0);
        listAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (columnIndex == 2){
                    String imageId = cursor.getString(columnIndex);
                    ImageView imageView = (ImageView) view;
                    imageView.setImageResource(TrackerActivityType.setDrawable(imageId));
                    return true;
                }
                if (columnIndex == 3){
                    int ongoing = cursor.getInt(columnIndex);
                    ImageView imageView = (ImageView) view;
                    if (ongoing == 1){
                        imageView.setImageResource(R.drawable.green);
                    }else if (ongoing == 0){
                        imageView.setImageResource(R.drawable.red);
                    }
                    return true;
                }
                return false;
            }
        });
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        setListAdapter(listAdapter);
        prepareButtons();
        TextView dateTextView = (TextView) getView().findViewById(R.id.dateTextView);
        dateTextView.setText(allDates.get(datePosition));
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id){
        super.onListItemClick(listView, view, position, id);

        Fragment fragment = new DetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putLong("Id", id);
        fragment.setArguments(bundle);

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, fragment);
        ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }

    public void switchFragment(){
        Fragment fragment = new TrackerActivityListFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, fragment);
        ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }

    // TODO: 13.7.2016 REVISE
    public void onYesterdayButtonClick(){
        datePosition++;
        MainActivity.datePosition++;
        if (datePosition == allDates.size()){
            prepareButtons();
        }else {
            switchFragment();
            prepareButtons();
        }
    }

    public void onTomorrowButtonClick(){
        datePosition--;
        MainActivity.datePosition--;
        if (datePosition == allDates.size()){
            prepareButtons();
        }else {
            switchFragment();
            prepareButtons();
        }
    }

    private void prepareButtons(){
        Button yesterdayButton = (Button) getView().findViewById(R.id.yesterdayButton);
        yesterdayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onYesterdayButtonClick();
            }
        });
        if (datePosition == allDates.size() - 1){
            yesterdayButton.setVisibility(View.INVISIBLE);
            yesterdayButton.setClickable(false);
        }else {
            yesterdayButton.setVisibility(View.VISIBLE);
        }
        Button tomorrowButton = (Button) getView().findViewById(R.id.tomorrowButton);
        tomorrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onTomorrowButtonClick();
            }
        });
        if (datePosition == 0){
            tomorrowButton.setVisibility(View.INVISIBLE);
            tomorrowButton.setClickable(false);
        }else {
            tomorrowButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        cursor.close();
        db.close();
    }
}