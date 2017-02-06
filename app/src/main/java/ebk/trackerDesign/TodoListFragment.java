package ebk.trackerDesign;


import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import ebk.trackerDesign.database.TrackerDatabaseHelper;
import ebk.trackerDesign.model.TodoListAdapter;
import ebk.trackerDesign.model.TrackerActivityType;

public class TodoListFragment extends Fragment {

    SQLiteDatabase db;
    Cursor cursor;
    private int[] idArray;
    private TodoListAdapter adapter;
    private RecyclerView todoRecycler;

    public TodoListFragment() {
        // Required empty public constructor
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RecyclerView todoRecycler = (RecyclerView)inflater.inflate(R.layout.recycler_todo, container, false);

        SQLiteOpenHelper trackerDatabaseHelper = new TrackerDatabaseHelper(getContext());
        db = trackerDatabaseHelper.getReadableDatabase();

        cursor = db.query("TODO", new String[]{"TODO", "ESTIMATED_TIME", "TYPE", "_id"}, null, null, null, null, null);

        int size = cursor.getCount();
        String[] todoArray = new String[size];
        String[] estTimeArray = new String[size];
        int[] typeArray = new int[size];
        idArray = new int[size];

        if (cursor.moveToFirst()) {
            int i = 0;
            do {
                todoArray[i] = cursor.getString(0);
                estTimeArray[i] = cursor.getString(1);
                typeArray[i] = TrackerActivityType.getTodoType(cursor.getString(2));
                idArray[i] = cursor.getInt(3);
                i++;
            } while(cursor.moveToNext());
        }

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, 1);
        todoRecycler.setLayoutManager(layoutManager);
        adapter = new TodoListAdapter(todoArray, estTimeArray, typeArray);
        todoRecycler.setAdapter(adapter);

        //db.close();
        return todoRecycler;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState){
        adapter.setListener(new TodoListAdapter.Listener() {
            public void onClick(int position) {
                cursor = db.query("TODO", new String[]{"TODO", "ESTIMATED_TIME", "TYPE", "_id"}, "_id = ?",
                        new String[]{Integer.toString(idArray[position])}, null, null, null);

                if (cursor.moveToFirst()) {
                    CharSequence options[] = new CharSequence[]{"Edit Name", "Edit Type", "Edit Time", "Delete Todo"};

                    final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Edit");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Change Name");

                                final EditText input = new EditText(getContext());
                                builder.setView(input);

                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        ContentValues updatedValues = new ContentValues();
                                        updatedValues.put("TODO", input.getText().toString());
                                        db.update("TODO", updatedValues, "_id = ?", new String[]{String.valueOf(cursor.getInt(3))});
                                        refreshLayout();
                                    }
                                });
                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        refreshLayout();
                                        return;
                                    }
                                });
                                builder.show();
                            } else if (which == 1) {
                                final String[] items = TrackerActivityType.allTodos;
                                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Edit Type");
                                builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int item) {
                                        String newType = TrackerActivityType.getActivityType(item);
                                        ContentValues updatedValues = new ContentValues();
                                        updatedValues.put("TYPE", newType);
                                        db.update("TODO", updatedValues, "_id = ?", new String[]{String.valueOf(cursor.getInt(3))});
                                        refreshLayout();
                                    }
                                });
                                builder.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        refreshLayout();
                                        return;
                                    }
                                });
                                builder.show();
                            } else if (which == 2) {
                                LayoutInflater inflater = getActivity().getLayoutInflater();
                                View dialogLayout = inflater.inflate(R.layout.inc_dec_buttons, null);
                                builder.setView(dialogLayout);
                                Button todoDecButton = (Button) dialogLayout.findViewById(R.id.todoDecButton);
                                Button todoIncButton = (Button) dialogLayout.findViewById(R.id.todoIncButton);
                                final TextView estTimeTextView = (TextView) view.findViewById(R.id.todoIncDecTextView);
                                // TODO: 11.8.2016 FIX EDIT TIME
                                todoDecButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        onDecButtonClick(estTimeTextView);
                                    }
                                });
                                todoIncButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        onIncButtonClick(estTimeTextView);
                                    }
                                });
                                builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        ContentValues updatedValues = new ContentValues();
                                        updatedValues.put("ESTIMATED_TIME", Integer.parseInt(estTimeTextView.getText().toString()));
                                        db.update("TODO", updatedValues, "_id = ?", new String[]{String.valueOf(cursor.getInt(3))});
                                        refreshLayout();
                                    }
                                });
                            } else if (which == 3) {
                                db.delete("TODO", "_id = ?", new String[]{String.valueOf(cursor.getInt(3))});
                                refreshLayout();
                            }
                        }
                    });
                    builder.show();
                }
            }
        });
    }

    // TODO: 11.8.2016 FIND BETTER SOLUTINO
    private void refreshLayout() {
        Fragment fragment = new TodoListFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, fragment);
        ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }

    private void onDecButtonClick(TextView estTimeTextView) {
        if (estTimeTextView.getText().toString().equals("0")){
            return;
        } else{
            estTimeTextView.setText(Integer.parseInt(estTimeTextView.getText().toString()) - 15 + "");
        }
    }

    private void onIncButtonClick(TextView estTimeTextView) {
        estTimeTextView.setText(Integer.parseInt(estTimeTextView.getText().toString()) + 15 + "");
        // TODO: 10.8.2016 MAY NEED ERROR HANDLING
    }

}