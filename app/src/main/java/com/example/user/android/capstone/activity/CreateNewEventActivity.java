package com.example.user.android.capstone.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.user.android.capstone.R;
import com.example.user.android.capstone.model.Event;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class CreateNewEventActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText mSportTitleEdit;
    private EditText mSportDetailsEdit;
    private Button mCreateNewEventButton;
    private TextView showDateTextView;
    private Button selectDateButton;
    private static Button selectTimeButton;
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mEventsRef = mRootRef.child("events");
    private DatabaseReference mUserRef = mRootRef.child("users");
    public String dayString;
    public String yearString;
    public String monthString;
    public static String hoursString;
    public static String minutesString;
    public String placeAddress;
    private String sportCategory;
    private String peopleNeeded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_event);
        setUpSpinnerForCategory();
        setUpSpinnerForPeopleCount();
        setUpTime();
        setUpDate();
        initializeTextViewAndButtons();
        getAddress();
        createNewEvent();
    }

    private void createNewEvent() {
        mCreateNewEventButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                mAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = mAuth.getCurrentUser();
                String userEmail = currentUser.getEmail();

                Query findUserQuery = mUserRef.orderByChild("email").equalTo(userEmail);
                System.out.println(userEmail);
                findUserQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                                String sportCreatorId = eventSnapshot.getKey();// find user's id to create event
                                String sportTitle = mSportTitleEdit.getText().toString();
                                String sportAddress = placeAddress;
                                String sportDate = monthString + "/" + dayString + "/" + yearString;
                                String sportTime = hoursString + " : " + minutesString;
                                String sportDetails = mSportDetailsEdit.getText().toString();
                                if (sportTitle.equals("") ||
                                        sportDetails.equals("") || sportCategory.equals("")) {
                                    Toast.makeText(getApplicationContext(), "Fill out all fields, please!", Toast.LENGTH_LONG).show();
                                } else {
                                    DatabaseReference newEventRef = mEventsRef.push();
                                    Event event = new Event(sportCategory, sportTitle, sportAddress, sportDate, sportTime, sportDetails, peopleNeeded, sportCreatorId);
                                    newEventRef.setValue(event);
                                    // USER THAT CREATED EVENT AUTOMATICALLY ATTENDS IT:
                                    newEventRef.child("attendees").child(sportCreatorId).setValue("true");
                                    mUserRef.child(event.getCreatorId()).child("userEvents").child(newEventRef.getKey()).setValue("true");
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

    private void initializeTextViewAndButtons() {
        mSportTitleEdit = (EditText) findViewById(R.id.et_sport_title);
        mSportDetailsEdit = (EditText) findViewById(R.id.et_details);
        mCreateNewEventButton = (Button) findViewById(R.id.add_new_event_button);
    }

    private void clearForm() {
        mSportTitleEdit.setText("");
        mSportDetailsEdit.setText("");
    }

    private void getAddress() {
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                placeAddress = place.getAddress().toString();
            }

            @Override
            public void onError(Status status) {
                System.out.println("An error occurred in get address from autocomplete form: " + status);
            }
        });
    }


    private void setUpDate() {
        selectDateButton = (Button) findViewById(R.id.select_date_button);
        selectDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int year = 2017;
                int month = 7;
                int day = 1;

                DatePickerDialog picker = new DatePickerDialog(CreateNewEventActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        yearString = String.valueOf(i);
                        monthString = String.valueOf(i1 + 1);
                        dayString = String.valueOf(i2);
                        selectDateButton.setText(monthString + "/" + dayString + "/" + yearString);
                    }
                },
                        year, month, day);
                picker.setTitle("Choose date");
                picker.show();
            }
        });

    }


    private void setUpTime() {
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
            if (minute < 10) {
                minutesString = "0" + minutesString;
            }
            if (hourOfDay < 10) {
                hoursString = "0" + hoursString;
            }
            displayTime(hoursString, minutesString);

        }
    }

    private static void displayTime(String hour, String minute) {
        selectTimeButton.setText(hour + ":" + minute);

    }

    private void setUpSpinnerForCategory() {
        Spinner spinner = (Spinner) findViewById(R.id.sport_types_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sport_types_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                sportCategory = (String) adapterView.getItemAtPosition(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


    private void setUpSpinnerForPeopleCount() {
        Spinner spinnerPeopleNeeded = (Spinner) findViewById(R.id.people_needed_spinner);
        ArrayAdapter<CharSequence> adapterPeople = ArrayAdapter.createFromResource(this,
                R.array.people_needed_array, android.R.layout.simple_spinner_item);
        adapterPeople.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPeopleNeeded.setAdapter(adapterPeople);
        spinnerPeopleNeeded.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                peopleNeeded = (String) adapterView.getItemAtPosition(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

}

