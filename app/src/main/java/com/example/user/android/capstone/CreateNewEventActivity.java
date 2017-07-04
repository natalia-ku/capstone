package com.example.user.android.capstone;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CreateNewEventActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText mSportTypeEdit;
    private EditText mSportAddressEdit;
    private EditText mSportDetailsEdit;
    private EditText mSportPeopleNeededEdit;
    private Button mCreateNewEventButton;
    private TextView showDateTextView;
    private Button selectDateButton;
    private Button selectTimeButton;
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mEventsRef = mRootRef.child("events");
    DatabaseReference mUserRef = mRootRef.child("users");

    public String dayString;
    public String yearString;
    public String monthString;
    public static  String hoursString;
    public static String minutesString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_event);

        setUpTime();
        setUpDate();

        mSportTypeEdit = (EditText) findViewById(R.id.et_sport_type);
        mSportAddressEdit = (EditText) findViewById(R.id.et_address);
        mSportDetailsEdit = (EditText) findViewById(R.id.et_details);
        mSportPeopleNeededEdit = (EditText) findViewById(R.id.et_people_needed);
        mCreateNewEventButton = (Button) findViewById(R.id.add_new_event_button);

        mCreateNewEventButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = mAuth.getCurrentUser();
                String userEmail = currentUser.getEmail();

                Query findUserQuery = mUserRef.orderByChild("email").equalTo(userEmail);

                findUserQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                                String sportCreatorId = eventSnapshot.getKey();// find user's id to create event

                                String sportType = mSportTypeEdit.getText().toString();
                                String sportAddress = mSportAddressEdit.getText().toString();
//
                                String sportDate = monthString + "/" + dayString + "/" + yearString;
                                String sportDetails = mSportDetailsEdit.getText().toString();
                                String sportPeopleNeeded = mSportPeopleNeededEdit.getText().toString();

                                if (sportAddress.equals("") || sportAddress.equals("")  ||
                                        sportDetails.equals("") || sportPeopleNeeded.equals("")) {
                                    Toast.makeText(getApplicationContext(), "Fill out all fields, please!", Toast.LENGTH_LONG).show();
                                } else {
                                    mEventsRef.push().setValue(new Event(sportType, sportAddress, sportDate, sportDetails, sportPeopleNeeded, sportCreatorId));
                                    Toast.makeText(getApplicationContext(), "You successfully created new sport event", Toast.LENGTH_LONG).show();
                                    clearForm();
                                    finish();
                                }
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });

    }

    private void clearForm() {
        mSportTypeEdit.setText("");
        mSportAddressEdit.setText("");
        mSportDetailsEdit.setText("");
        mSportPeopleNeededEdit.setText("");
    }


    private void setUpDate(){
        selectDateButton = (Button) findViewById(R.id.select_date_button);
        selectDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int year = 2017;
                int month = 1;
                int day = 1;

                DatePickerDialog picker = new DatePickerDialog(CreateNewEventActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        Toast.makeText(getApplicationContext(), i + " " + i1 + " " + i2, Toast.LENGTH_LONG).show();
                        yearString = String.valueOf(i);
                        monthString = String.valueOf(i1);
                        dayString = String.valueOf(i2);
                        showDateTextView = (TextView) findViewById(R.id.show_date);
                        showDateTextView.setText(monthString + "/" + dayString + "/" + yearString);
                    }
                },
                        year, month, day);
                picker.setTitle("Choose date");
                picker.show();
            }
        });

    }



    private void setUpTime(){
        selectTimeButton = (Button) findViewById(R.id.show_time_picker_button);
        selectTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getFragmentManager(), "timePicker");
            }
        });

    }


    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            hoursString = String.valueOf(hourOfDay);
            minutesString = String.valueOf(minute);
        }
    }


//    public void showTimePickerDialog(View v) {
//        DialogFragment newFragment = new TimePickerFragment();
//        newFragment.show(getFragmentManager(), "timePicker");
//
//    }

}
