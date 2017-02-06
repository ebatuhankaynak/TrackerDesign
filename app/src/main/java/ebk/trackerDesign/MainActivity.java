package ebk.trackerDesign;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import ebk.trackerDesign.alarm.TrackerMidnightAlarmReceiver;
import ebk.trackerDesign.database.AndroidDatabaseManager;
import ebk.trackerDesign.database.TrackerDatabaseHelper;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private float x1,x2;
    static final int MIN_DISTANCE = 50;

    private SQLiteDatabase db;
    private Cursor cursor;
    private TrackerMidnightAlarmReceiver trackerMidnightAlarmReceiver = new TrackerMidnightAlarmReceiver();
    private FloatingActionButton addFab;
    private FloatingActionButton pieFab;
    private FloatingActionButton addTodoFab;

    public static ArrayList<String> allDates;
    public static int datePosition;
    private String formattedDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        addFab = (FloatingActionButton) findViewById(R.id.addFab);
        pieFab = (FloatingActionButton) findViewById(R.id.pieFab);

        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new AddFragment();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, fragment);
                ft.addToBackStack(null);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.commit();
                addFab.setVisibility(View.GONE);
                pieFab.setVisibility(View.GONE);
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
            }
        });
        pieFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new PieFragment();
                Bundle bundle = new Bundle();
                formattedDate = allDates.get(datePosition);
                bundle.putString("date", formattedDate);
                fragment.setArguments(bundle);
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, fragment);
                ft.addToBackStack(null);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.commit();
                pieFab.setVisibility(View.GONE);
                addFab.setVisibility(View.GONE);
            }
        });
        addTodoFab = (FloatingActionButton) findViewById(R.id.addTodoFab);
        addTodoFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new AddTodoFragment();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, fragment);
                ft.addToBackStack(null);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.commit();
            }
        });
        addTodoFab.setVisibility(View.GONE);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SQLiteOpenHelper trackerDatabaseHelper = new TrackerDatabaseHelper(this);
        db = trackerDatabaseHelper.getWritableDatabase();

        trackerMidnightAlarmReceiver.setAlarm(this);

        //db.delete("ACTIVITY", null, null);
        //db.execSQL("DROP TABLE IF EXISTS ACTIVITY");
        //db.setVersion(0);

        getAllDates();

        formattedDate = allDates.get(datePosition);
        setTrackerActivityListFragment(formattedDate);
        // TODO: 13.7.2016 ADD DURATION(MAYBE AS PERCENTAGE)
        // TODO: 13.7.2016 NOTIFICATIONS 1)WHEN SOMETHING TAKES TOO MUCH TIME 2)WHEN SOMETHING EXCEEDS DESIRED STANDARDS
        // TODO: 13.7.2016 DAILY REPORT
        // TODO: 13.7.2016 SHOW TO-DO LIST IF NEEDED AT DAILY REPORTS
        // TODO: 23.7.2016 ANY UNATTENDED TIME WILL BE "UNKNOWN"
        // TODO: 12.8.2016 REGISTER DAILY DATA
    }

    public void setTrackerActivityListFragment(String date){
        TrackerActivityListFragment trackerActivityListFragment = new TrackerActivityListFragment();

        Bundle bundle = new Bundle();
        bundle.putString("date", date);
        trackerActivityListFragment.setArguments(bundle);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, trackerActivityListFragment);
        ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commitAllowingStateLoss(); // TODO: 13.7.2016 SAFE?
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                float deltaX = x2 - x1;
                if (Math.abs(deltaX) > MIN_DISTANCE) {
                    // Left to Right swipe action
                    if (x2 > x1) {
                        Toast.makeText(this, "Left to Right swipe [Next]", Toast.LENGTH_SHORT).show ();
                    }else {
                        Toast.makeText(this, "Right to Left swipe [Previous]", Toast.LENGTH_SHORT).show ();
                        Intent dbmanager = new Intent(this ,AndroidDatabaseManager.class);
                        startActivity(dbmanager);
                    }
                }else {
                    // consider as something else - a screen tap for example
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onRestart(){
        super.onRestart();
        setTrackerActivityListFragment(formattedDate);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        cursor.close();
        db.close();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        addFab.setVisibility(View.VISIBLE);
        pieFab.setVisibility(View.VISIBLE);
        addTodoFab.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        Fragment fragment = new TrackerActivityListFragment();
        addFab.setVisibility(View.VISIBLE);
        pieFab.setVisibility(View.VISIBLE);
        addTodoFab.setVisibility(View.GONE);
        if (id == R.id.nav_home){
            fragment = new TrackerActivityListFragment();
        } else if (id == R.id.nav_todo){
            fragment = new TodoListFragment();
            addFab.setVisibility(View.GONE);
            pieFab.setVisibility(View.GONE);
            addTodoFab.setVisibility(View.VISIBLE);
        } else if (id == R.id.nav_daily_reports){
            //fragment = ;
        } else if (id == R.id.nav_averages){
            //fragment = ;
        } else if (id == R.id.nav_share) {
            //fragment = ;
        } else if (id == R.id.nav_send) {
            //fragment = ;
            Intent dbmanager = new Intent(getBaseContext() ,AndroidDatabaseManager.class);
            startActivity(dbmanager);
            return true;
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, fragment);
        ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
