package ebk.trackerDesign;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.labo.kaji.fragmentanimations.MoveAnimation;
import com.labo.kaji.fragmentanimations.PushPullAnimation;

import java.util.ArrayList;
import java.util.Calendar;

import ebk.trackerDesign.database.TrackerDatabaseHelper;

/**
 * A simple {@link Fragment} subclass.
 */
public class PieFragment extends Fragment {

    private SQLiteDatabase db;

    public PieFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        SQLiteOpenHelper trackerDatabaseHelper = new TrackerDatabaseHelper(getContext());
        db = trackerDatabaseHelper.getReadableDatabase();

        return inflater.inflate(R.layout.fragment_pie, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        setUpPieChart(MainActivity.allDates.get(MainActivity.datePosition));
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (enter) {
            return PushPullAnimation.create(PushPullAnimation.LEFT, enter, 500);
        } else {
            return MoveAnimation.create(MoveAnimation.UP, enter, 500);
        }
    }

    public void setUpPieChart(String date){
        PieChart pieChart = (PieChart) getView().findViewById(R.id.pieChart);
        //pieChart.setDescriptionColor(Color.WHITE);
        //pieChart.setBackgroundColor(Color.WHITE);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setEntryLabelTextSize(14f);

        ArrayList<String> labels = new ArrayList<String>();
        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();

        Cursor cursor = db.query("ACTIVITY", new String[] {"TYPE", "SUM(START_TIME) AS startTime", "SUM(END_TIME) AS endTime"}, "DATE = ?",
                new String[]{date},
                "TYPE", null, null);

        if (cursor.moveToFirst()){
            int count = 0;
            do {
                String type = cursor.getString(0);
                int durationSum = cursor.getInt(2) - cursor.getInt(1);
                labels.add(type);
                if (durationSum < 0){
                    Calendar calendar = Calendar.getInstance();
                    int time = (calendar.get(Calendar.HOUR_OF_DAY) * 3600) + (calendar.get(Calendar.MINUTE) * 60) + (calendar.get(Calendar.SECOND));
                    durationSum = time + durationSum;
                }
                entries.add(new PieEntry(Float.parseFloat(String.valueOf(durationSum)), type));
                count++;
            } while (cursor.moveToNext());
        }

        /*
        Legend l = pieChart.getLegend();
        l.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);*/

        PieDataSet dataset = new PieDataSet(entries, "% of the day");
        dataset.setColors(ColorTemplate.COLORFUL_COLORS);

        pieChart.setDrawEntryLabels(true);
        PieData data = new PieData(dataset);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(14f);
        data.setValueTextColor(Color.WHITE);
        pieChart.setData(data);

        pieChart.setDescription("");
    }

}
