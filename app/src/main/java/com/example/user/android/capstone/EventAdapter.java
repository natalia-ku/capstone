package com.example.user.android.capstone;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;


/**
 * Created by nataliakuleniuk on 6/26/17.
 */

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {
    ImageView eventPhoto;

    List<Event> events;
    Context context;


    public EventAdapter(Context context, List<Event> events) {
        this.events = events;
        this.context = context;
    }


    @Override
    public EventAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
        return new EventAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EventAdapter.ViewHolder holder, final int position) {
        holder.eventTitleTextView.setText(events.get(position).getTitle());
        holder.eventDateTextView.setText(events.get(position).getDate());
        holder.eventLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Class destinationClass = EventInfoActivity.class;
                Intent intentToStartEventInfoActivity = new Intent(context, destinationClass);
                intentToStartEventInfoActivity.putExtra("event", (Parcelable) events.get(position));
                context.startActivity(intentToStartEventInfoActivity);
            }
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        LinearLayout eventLinearLayout;
        TextView eventTitleTextView;
        TextView eventDateTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv);

            eventDateTextView = (TextView) itemView.findViewById(R.id.event_date);
            eventTitleTextView = (TextView) itemView.findViewById(R.id.event_sport_title);
            eventLinearLayout = (LinearLayout) itemView.findViewById(R.id.layout_item);
            eventPhoto = (ImageView) itemView.findViewById(R.id.event_photo);
            Uri imageUri = Uri.parse("https://cdn0.iconfinder.com/data/icons/sport-and-fitness/500/Ball_football_game_play_soccer_sport_sports_man-512.png");
            eventPhoto.setImageURI(imageUri);

            // Picasso.with(context).load("http://i.imgur.com/DvpvklR.png").into(eventPhoto);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }


}
