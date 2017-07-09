package com.example.user.android.capstone;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
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

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class UpdateEventActivity extends AppCompatActivity {

    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference mEventsRef = mRootRef.child("events");
    public String placeAddress;

    EditText mTitleUpdateEvent;
    EditText mDetailsUpdateEvent;
    Button mUpdateEventButton;
    Button mDeleteEventButton;
    Button mSelectDateButton;
    Button selectTimeButton;
    TextView showDateTextView;


    public String dayString;
    public String yearString;
    public String monthString;
    public static String hoursString;
    public static String minutesString;

    private String sportCategory;
    private String peopleNeeded;
    Spinner spinner;
    Spinner spinnerPeopleNeeded;
    ArrayAdapter<CharSequence> adapter;
    ArrayAdapter<CharSequence> adapterPeople;
    Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_event);

        final Event event = (Event) getIntent().getSerializableExtra("event");
        updatEventListener(event);
        showEventDataInEditViews();
        setUpSpinnerForCategory();
        setUpSpinnerForPeopleCount();
        getAddress(event);
        setUpDate(event);
        setUpTime();
        getEventData(event);
        displayDate(event);
    }

    private void updatEventListener(final Event event) {
        mUpdateEventButton = (Button) findViewById(R.id.update_event_button);
        mUpdateEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference eventRef = mEventsRef.child(event.getCreatorId());

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
                    eventRef.setValue(new Event(sportCategory, sportTitle, placeAddress, sportDate, sportTime, sportDetails, peopleNeeded, sportCreatorId));
                    Toast.makeText(getApplicationContext(), "You successfully updated sport event", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });
    }


    private void showEventDataInEditViews() {
        showDateTextView = (TextView) findViewById(R.id.show_date_update);
        spinnerPeopleNeeded = (Spinner) findViewById(R.id.people_needed_spinner_update);
        spinner = (Spinner) findViewById(R.id.sport_types_spinner_update);
        mTitleUpdateEvent = (EditText) findViewById(R.id.et_sport_title_update);
        mDetailsUpdateEvent = (EditText) findViewById(R.id.et_details_update);
    }

    private void displayDate(Event event) {
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        Date eventDate = null;
        try {
            eventDate = formatter.parse(event.getDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar = Calendar.getInstance();
        calendar.setTime(eventDate);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        showDateTextView = (TextView) findViewById(R.id.show_date_update);
        showDateTextView.setText(month + "/" + day + "/" + year);
    }


    private void getEventData(Event event) {
        mTitleUpdateEvent = (EditText) findViewById(R.id.et_sport_title_update);
        mDetailsUpdateEvent = (EditText) findViewById(R.id.et_details_update);

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
        mSelectDateButton = (Button) findViewById(R.id.select_date_button_update);
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

    private void setUpTime() {
        selectTimeButton = (Button) findViewById(R.id.show_time_picker_button_update);
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
            if (minute == 0){
                minutesString = "00";
            }
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



}
