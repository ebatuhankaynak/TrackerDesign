package ebk.trackerDesign;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import ebk.trackerDesign.database.TrackerDatabaseHelper;
import ebk.trackerDesign.model.TrackerActivityType;

public class AddTodoFragment extends Fragment {

    private RadioGroup todoTypeRadioGroup;

    private SQLiteDatabase db;

    public AddTodoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_todo, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        todoTypeRadioGroup = (RadioGroup) view.findViewById(R.id.todoTypeRadioGroup);
        Button addTodoButton = (Button) view.findViewById(R.id.addTodoButton);
        Button todoDecButton = (Button) view.findViewById(R.id.todoDecButton);
        Button todoIncButton = (Button) view.findViewById(R.id.todoIncButton);
        addTodoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAddTodoClick();
                Fragment fragment = new TodoListFragment();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, fragment);
                ft.addToBackStack(null);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.commit();
            }
        });
        final TextView estTimeTextView = (TextView) getView().findViewById(R.id.todoIncDecTextView);
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

    private void onAddTodoClick(){
        int id = todoTypeRadioGroup.getCheckedRadioButtonId();
        if (id == -1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("No Type!");
            builder.setPositiveButton("Oops (^_^;)", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int whichButton) {
                    return;
                }
            });
            builder.show();
        } else {
            EditText todoEditText = (EditText) getView().findViewById(R.id.todoEditText);
            String todo = todoEditText.getText().toString();

            RadioButton activityTypeRadioButton = (RadioButton) getView().findViewById(id);
            String radioString = activityTypeRadioButton.getResources().getResourceEntryName(id);
            String todoType = TrackerActivityType.getActivityType(radioString);

            insertTodo(todo, todoType);
        }
    }

    private void insertTodo(String todo, String todoType){
        SQLiteOpenHelper trackerDatabaseHelper = new TrackerDatabaseHelper(getContext());
        db = trackerDatabaseHelper.getWritableDatabase();

        ContentValues activityValues = new ContentValues();
        activityValues.put("TODO", todo);
        activityValues.put("TYPE", todoType);
        // TODO: 10.8.2016 WEEKLY, DAILY TODOS?
        TextView estTimeTextView = (TextView) getView().findViewById(R.id.todoIncDecTextView);
        int estTime = Integer.parseInt(estTimeTextView.getText().toString());
        activityValues.put("ESTIMATED_TIME", estTime);

        db.insert("TODO", null, activityValues);
        db.close();
    }
}
