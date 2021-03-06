package com.example.user.android.capstone.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TableLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.example.user.android.capstone.R;
import com.example.user.android.capstone.adapter.EventAdapter;
import com.example.user.android.capstone.model.Event;
import com.example.user.android.capstone.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {


    private RecyclerView recyclerView;
    private TextView mUserAge;
    private TextView mUserGender;
    private ImageView mUserPhotoImage;
    private TextView mUserEmail;
    private FloatingActionButton mEditProfileButton;
    private final int REQUEST_CODE = 23;
    private TextView eventsUserCreatedTextView;
    private TextView eventsUserParticipatedTextView;
    private FirebaseAuth mAuth;
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mUserRef = mRootRef.child("users");
    private DatabaseReference mEventRef = mRootRef.child("events");
    private RatingBar userRatingBar;
    View checkBoxView;
    CheckBox checkBox;
    final String DONT_SHOW_AGAIN = "DontShowAgain";
    FirebaseUser currentUser;
    CoordinatorLayout userInfoLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String userEmail = getIntent().getStringExtra("userEmail"); 
        mEditProfileButton = (FloatingActionButton) findViewById(R.id.edit_profile);
        userInfoLayout = (CoordinatorLayout) findViewById(R.id.user_info_layout);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) userInfoLayout.getLayoutParams();
        if (userEmail == null) {
            mAuth = FirebaseAuth.getInstance();
            currentUser = mAuth.getCurrentUser();
            userEmail = currentUser.getEmail();
            layoutParams.setMargins(0, 84, 0, 0);
            userInfoLayout.setLayoutParams(layoutParams);
        } else {
            layoutParams.setMargins(0, 0, 0, 0);
            userInfoLayout.setLayoutParams(layoutParams);
            currentUser = null;
            mEditProfileButton.setVisibility(View.GONE);
        }
        setUpProfileInfo(userEmail);
    }


    private void setUpProfileInfo(String userEmail) {
        Query userProfileQuery = mUserRef.orderByChild("email").equalTo(userEmail);
        userProfileQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    final User user;
                    String currentUserId = "";
                    String name = "";
                    String email = "";
                    String age = "";
                    String gender = "";
                    String photo = "";
                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        findTextViews();
                        currentUserId = (String) eventSnapshot.getKey();
                        name = (String) eventSnapshot.child("name").getValue();
                        email = (String) eventSnapshot.child("email").getValue();
                        age = (String) eventSnapshot.child("age").getValue();
                        gender = (String) eventSnapshot.child("gender").getValue();
                        photo = (String) eventSnapshot.child("photo").getValue();
                    }
                    user = new User(currentUserId, email, name, gender, photo, age);
                    setTextToViews(email, name, age, gender, photo);
                    findCreatedByUserEvents(currentUserId);
                    findEventsUserParticipatedIn(currentUserId);


                    mEditProfileButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intentToUpdateProfile = new Intent(getApplicationContext(), UpdateProfileActivity.class);
                            intentToUpdateProfile.putExtra("user", (Serializable) user);
                            startActivityForResult(intentToUpdateProfile, REQUEST_CODE);
                        }
                    });


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                User user = (User) data.getSerializableExtra("user");
                setTextToViews(user.getEmail(), user.getName(), user.getAge(), user.getGender(), user.getPhoto());
            }
        }
    }

    private void findEventsUserParticipatedIn(final String currentUserId) {
        // GET list of user event IDs:
        final List<String> eventIdsList = new ArrayList<>();
        Query getUserEventIdsQuery = mUserRef.child(currentUserId).child("userEvents");
        getUserEventIdsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        eventIdsList.add(eventSnapshot.getKey());
                    }
                    getEventsUserParticipatedIn(eventIdsList, currentUserId);
                } else {
                    eventsUserParticipatedTextView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void getEventsUserParticipatedIn(final List<String> eventIdsList, final String currentUserId) {
        //TO GET EVENTS USER PARTICIPATED IN:
        final List<Event> notRatedEvents = new ArrayList<Event>();
        final List<Event> userEvents = new ArrayList<>();
        for (final String eventID : eventIdsList) {
            Query eventsUserAttendsQuery = mEventRef.orderByKey().equalTo(eventID);
            eventsUserAttendsQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()) {
                        for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                            String id = eventSnapshot.getKey();
                            String address = (String) eventSnapshot.child("address").getValue();
                            String date = (String) eventSnapshot.child("dataTime").getValue();
                            String time = (String) eventSnapshot.child("time").getValue();
                            String creatorId = eventSnapshot.child("creatorId").getValue().toString();
                            String details = (String) eventSnapshot.child("details").getValue();
                            String peopleNeeded = eventSnapshot.child("peopleNeeded").getValue().toString();
                            String title = (String) eventSnapshot.child("title").getValue();
                            String sportCategory = (String) eventSnapshot.child("sportCategory").getValue();
                            Event e1 = new Event(sportCategory, id, title, address, date, time, details, peopleNeeded, creatorId);
                            userEvents.add(e1);
                            if (!e1.checkIfDateInFuture(e1.getDate()) &&
                                    !eventSnapshot.child("votedUsers").hasChild(currentUserId)) {
                                notRatedEvents.add(e1);
                            }
                        }

                        if (eventID.equals(eventIdsList.get(eventIdsList.size() - 1))) {
                            if (notRatedEvents.size() > 0 && getIntent().getStringExtra("userEmail") == null) {
                                AlertDialog.Builder builder;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    builder = new AlertDialog.Builder(UserProfileActivity.this);
                                } else {
                                    builder = new AlertDialog.Builder(getApplicationContext());
                                }
                                final List<String> eventsTitles = new ArrayList<String>();
                                for (Event event : notRatedEvents) {
                                    eventsTitles.add(event.getTitle());
                                }
                                final CharSequence[] charSequencesItems = eventsTitles.toArray(new CharSequence[eventsTitles.size()]);
                                LayoutInflater adbInflater = LayoutInflater.from(getApplicationContext());
                                View layoutCheckbox = adbInflater.inflate(R.layout.checkbox, null);
                                SharedPreferences settings = getSharedPreferences(DONT_SHOW_AGAIN, 0);
                                String skipMessage = settings.getString("skipMessage", "NOT checked");

                                checkBox = (CheckBox) layoutCheckbox.findViewById(R.id.skip);
                                builder.setView(layoutCheckbox);
                                builder.setTitle("Rate your past events")
                                        .setItems(charSequencesItems, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int item) {
                                                Intent intentToEventDetails = new Intent(getApplicationContext(), EventInfoActivity.class);
                                                intentToEventDetails.putExtra("event", (Serializable) notRatedEvents.get(item));
                                                startActivity(intentToEventDetails);
                                            }
                                        })
                                        .setIcon(android.R.drawable.ic_dialog_info)
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                String checkBoxResult = "NOT checked";
                                                if (checkBox.isChecked()) {
                                                    checkBoxResult = "checked";
                                                }
                                                SharedPreferences settings = getSharedPreferences(DONT_SHOW_AGAIN, 0);
                                                SharedPreferences.Editor editor = settings.edit();
                                                editor.putString("skipMessage", checkBoxResult);
                                                editor.commit();
                                                return;
                                            }
                                        })
                                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                String checkBoxResult = "NOT checked";
                                                if (checkBox.isChecked()) {
                                                    checkBoxResult = "checked";
                                                }
                                                SharedPreferences settings = getSharedPreferences(DONT_SHOW_AGAIN, 0);
                                                SharedPreferences.Editor editor = settings.edit();
                                                editor.putString("skipMessage", checkBoxResult);
                                                editor.commit();
                                                return;
                                            }
                                        });
                                if (!skipMessage.equals("checked")) {
                                    builder.show();
                                }
                            }
                        }
                        setUpRecycleView(userEvents, false);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void findCreatedByUserEvents(String currentUserId) {
        final List<Event> userEvents = new ArrayList<>();
        Query createdByUserEventsQuery = mEventRef.orderByChild("creatorId").equalTo(currentUserId);
        createdByUserEventsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                double userRating = 0;
                int eventWithRatingCount = 0;
                if (dataSnapshot.exists()) {
                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        String id = eventSnapshot.getKey();
                        String address = (String) eventSnapshot.child("address").getValue();
                        String date = (String) eventSnapshot.child("dataTime").getValue();
                        String time = (String) eventSnapshot.child("time").getValue();
                        String creatorId = eventSnapshot.child("creatorId").getValue().toString();
                        String details = (String) eventSnapshot.child("details").getValue();
                        String peopleNeeded = eventSnapshot.child("peopleNeeded").getValue().toString();
                        String title = (String) eventSnapshot.child("title").getValue();
                        String sportCategory = (String) eventSnapshot.child("sportCategory").getValue();
                        if (eventSnapshot.hasChild("rating")) {
                            eventWithRatingCount++;
                            userRating += Double.parseDouble(eventSnapshot.child("rating").getValue().toString());
                        }
                        Event e1 = new Event(sportCategory, id, title, address, date, time, details, peopleNeeded, creatorId);
                        userEvents.add(e1);
                    }
                }
                if (userEvents.size() == 0) {
                    eventsUserCreatedTextView.setVisibility(View.GONE);
                } else {
                    if (eventWithRatingCount > 0) {
                        float userRatingValue = (float) userRating / eventWithRatingCount;
                        userRatingBar.setRating(userRatingValue);
                    }
                }
                setUpRecycleView(userEvents, true);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void findTextViews() {
        mUserEmail = (TextView) findViewById(R.id.email_profile_info);
        mUserAge = (TextView) findViewById(R.id.age_profile_info);
        mUserGender = (TextView) findViewById(R.id.gender_profile_info);
        mUserPhotoImage = (ImageView) findViewById(R.id.user_photo);
        eventsUserCreatedTextView = (TextView) findViewById(R.id.events_user_created_textview);
        eventsUserParticipatedTextView = (TextView) findViewById(R.id.events_user_participated_textview);
        userRatingBar = (RatingBar) findViewById(R.id.rating_user_profile);
        checkBoxView = View.inflate(this, R.layout.checkbox, null);
    }

    private void setTextToViews(String email, String name, String age, String gender, String photo) {
        mUserEmail.setText(email);
        CollapsingToolbarLayout toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolbarLayout.setExpandedTitleTextAppearance(R.style.MyExpandedAppBarProfile);
        toolbarLayout.setTitle(name);
        mUserAge.setText(age);
        mUserGender.setText(gender);
        Glide.with(getApplicationContext()).load(photo).asBitmap().centerCrop().into(new BitmapImageViewTarget(mUserPhotoImage) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(getApplication().getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                mUserPhotoImage.setImageDrawable(circularBitmapDrawable);
            }
        });

    }

    private void setUpRecycleView(List<Event> userEvents, boolean eventsCreatedByUser) {
        if (eventsCreatedByUser) {
            recyclerView = (RecyclerView) findViewById(R.id.recycle_view_events_created_by_user);
        } else {
            recyclerView = (RecyclerView) findViewById(R.id.recycle_view_events_user_participated_in);
        }
//        EventAdapter myAdapter = new EventAdapter(getApplicationContext(), userEvents, UserProfileActivity.class);
        EventAdapter myAdapter = new EventAdapter(getApplicationContext(), userEvents, null);
        recyclerView.setAdapter(myAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
}
