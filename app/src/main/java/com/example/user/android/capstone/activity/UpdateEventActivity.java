package com.example.user.android.capstone.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class UpdateEventActivity extends AppCompatActivity {

    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mEventsRef = mRootRef.child("events");
    private DatabaseReference mUsersRef = mRootRef.child("users");
    private String placeAddress;
    private EditText mTitleUpdateEvent;
    private EditText mDetailsUpdateEvent;
    private Button mUpdateEventButton;
    private Button mDeleteEventButton;
    private Button mSelectDateButton;
    private Button selectTimeButton;
    private TextView showDateTextView;
    private static TextView showTimeTextView;
    private String dayString;
    private String yearString;
    private String monthString;
    private static String hoursString;
    private static String minutesString;
    private String sportCategory;
    private String peopleNeeded;
    private Spinner spinner;
    private Spinner spinnerPeopleNeeded;
    private ArrayAdapter<CharSequence> adapter;
    private ArrayAdapter<CharSequence> adapterPeople;
    private Calendar calendar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_event);
        initializeButtonsAndTextView();
        final Event event = (Event) getIntent().getSerializableExtra("event");
        displayDateAndTime(event);
        updatEventListener(event);
        setUpSpinnerForCategory();
        setUpSpinnerForPeopleCount();
        getAddress(event);
        setUpDate(event);
        setUpTime(event);
        getEventData(event);
        deleteEventListener(event);
    }


    private void updatEventListener(final Event event) {
        mUpdateEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference eventRef = mEventsRef.child(event.getId());
                String sportTitle = mTitleUpdateEvent.getText().toString();
                String sportDetails = mDetailsUpdateEvent.getText().toString();
                String sportDate = monthString + "/" + dayString + "/" + yearString;
                String sportTime = hoursString + " : " + minutesString;
                String sportCreatorId = event.getCreatorId();
                if (sportTitle.equals("") ||
                        sportDetails.equals("") ||
                        placeAddress.equals("")) {
                    Toast.makeText(getApplicationContext(), "Fill out all fields, please!", Toast.LENGTH_LONG).show();
                } else {
                    Event event = new Event(sportCategory, sportTitle, placeAddress, sportDate, sportTime, sportDetails, peopleNeeded, sportCreatorId);
                    eventRef.setValue(event);
                    Toast.makeText(getApplicationContext(), "You successfully updated sport event", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent();
                    intent.putExtra("event", (Serializable) event);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }

    private void deleteEventListener(final Event event) {
        mDeleteEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteEventFromUsersEventList(event.getId());
                mEventsRef.child(event.getId()).removeValue();
            }

            // Query findParticipatedUsersQuery = mUsersRef.child("userEvents").getKey().equals(event.getId());

        });
    }

    private void deleteEventFromUsersEventList(final String eventId) {
        // GET list of participants IDs:
        final List<String> userIdsList = new ArrayList<>();
        Query getEventUserIdsQuery = mEventsRef.child(eventId).child("attendees");
        getEventUserIdsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        userIdsList.add(eventSnapshot.getKey());
                    }
                }
                removeFromList(userIdsList, eventId);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void removeFromList(List<String> userIdsList, final String eventId) {
        for (String userID : userIdsList) {
            Query eventUsersQuery = mUsersRef.orderByKey().equalTo(userID);
            eventUsersQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            userSnapshot.getRef().child("userEvents").child(eventId).removeValue();
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void displayDateAndTime(Event event) {
        showDateTextView.setText(event.getDate());
        showTimeTextView.setText(event.getTime());
    }

    private void getEventData(Event event) {
        mTitleUpdateEvent.setText(event.getTitle());
        mDetailsUpdateEvent.setText(event.getDetails());
        int position = adapter.getPosition(event.getSportCategory());
        spinner.setSelection(position);
        int positionPeople = adapterPeople.getPosition(event.getPeopleNeeded());
        spinnerPeopleNeeded.setSelection(positionPeople);
    }

    private void getAddress(Event event) {
        placeAddress = event.getAddress();
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_update);
        autocompleteFragment.setText(placeAddress);
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

    private void setUpDate(final Event event) {
        final String date = event.getDate();
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        Date eventDate = null;
        try {
            eventDate = formatter.parse(event.getDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar = Calendar.getInstance();
        calendar.setTime(eventDate);
        if (date.length() == 8) { // 7/9/0000
            yearString = date.substring(4);
            monthString = date.substring(0, 1);
            dayString = date.substring(2, 3);
        } else if (date.length() == 9) {//  9/99/9999 or 99/9/9999
            if (date.indexOf("/") == 1) {
                yearString = date.substring(5);
                monthString = date.substring(0, 1);
                dayString = date.substring(2, 4);
            } else {
                yearString = date.substring(5);
                monthString = date.substring(0, 2);
                dayString = date.substring(3, 4);
            }
        } else {// 99/99/9999
            yearString = date.substring(6);
            monthString = date.substring(0, 2);
            dayString = date.substring(3, 5);
        }
        mSelectDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog picker = new DatePickerDialog(UpdateEventActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        yearString = String.valueOf(i);
                        monthString = String.valueOf(i1 + 1);
                        dayString = String.valueOf(i2);
                        if (monthString == null) {
                            showDateTextView.setText("Can't set date");
                        } else {
                            showDateTextView.setText(monthString + "/" + dayString + "/" + yearString);
                        }
                    }
                },
                        year, month, day);
                picker.setTitle("Choose date");
                picker.show();
            }
        });
    }

    private void setUpTime(final Event event) {
        String time = event.getTime();
        hoursString = time.substring(0, 2);
        minutesString = time.substring(5);
        selectTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new TimePickerFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("event", event);
                newFragment.setArguments(bundle);
                newFragment.show(getFragmentManager(), "timePicker");
            }
        });
    }

    private void initializeButtonsAndTextView() {
        showDateTextView = (TextView) findViewById(R.id.show_date_update);
        mUpdateEventButton = (Button) findViewById(R.id.update_event_button);
        mDeleteEventButton = (Button) findViewById(R.id.delete_event_button);
        showDateTextView = (TextView) findViewById(R.id.show_date_update);
        spinnerPeopleNeeded = (Spinner) findViewById(R.id.people_needed_spinner_update);
        spinner = (Spinner) findViewById(R.id.sport_types_spinner_update);
        mTitleUpdateEvent = (EditText) findViewById(R.id.et_sport_title_update);
        mDetailsUpdateEvent = (EditText) findViewById(R.id.et_details_update);
        mSelectDateButton = (Button) findViewById(R.id.select_date_button_update);
        mTitleUpdateEvent = (EditText) findViewById(R.id.et_sport_title_update);
        mDetailsUpdateEvent = (EditText) findViewById(R.id.et_details_update);
        selectTimeButton = (Button) findViewById(R.id.show_time_picker_button_update);
        showDateTextView = (TextView) findViewById(R.id.show_date_update);
        showTimeTextView = (TextView) findViewById(R.id.show_time_update);
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Event event = (Event) getArguments().getSerializable("event");
            final Calendar c = Calendar.getInstance();
            int hour;
            int minute;
            if (event.getTime() == null) {
                hour = 10;
                minute = 30;
            } else {
                hour = Integer.parseInt(event.getTime().substring(0, 2));
                minute = Integer.parseInt(event.getTime().substring(5));
            }
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
            showTimeTextView.setText(hoursString + " : " + minutesString);
        }
    }


    private void setUpSpinnerForCategory() {
        adapter = ArrayAdapter.createFromResource(this,
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
        adapterPeople = ArrayAdapter.createFromResource(this,
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), UserProfileActivity.class);
        startActivity(intent);
    }

}
