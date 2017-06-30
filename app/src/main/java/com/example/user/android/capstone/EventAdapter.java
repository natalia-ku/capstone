package com.example.user.android.capstone;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.user.android.capstone.Event;
import com.example.user.android.capstone.R;

import java.util.List;




/**
 * Created by nataliakuleniuk on 6/26/17.
 */

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    List<Event> events;
    Context context;


    public EventAdapter( Context context, List<Event> events) {
        this.events = events;
        this.context = context;
    }


    @Override
    public EventAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
        return new EventAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EventAdapter.ViewHolder holder, int position) {
        holder.eventSportTypeTextView.setText(events.get(position).getSportType());
//        holder.itemLanguageTextView.setText(items.get(position).getLanguage());
//        holder.itemOwnerTextView.setText(items.get(position).getOwnerLogin());
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView eventSportTypeTextView;
        public ViewHolder(View itemView) {
            super(itemView);
            eventSportTypeTextView = (TextView) itemView.findViewById(R.id.event_sport_type);
        }
    }



}
