package com.example.user.android.capstone.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.example.user.android.capstone.adapter.EventAdapter;
import com.example.user.android.capstone.R;
import com.example.user.android.capstone.model.Event;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class EventFragment extends Fragment  {
    RecyclerView recyclerView;
    static EventFragment eventFragment = null;
    public EventFragment() {
    }

    public static EventFragment newInstance() {
        if (eventFragment == null)
            eventFragment = new EventFragment();
        return eventFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycle_view);
        return view;
    }

    public void updateList(List<Event> events, boolean filter) {
        EventAdapter myAdapter = new EventAdapter(getContext(), events, null);
        if (filter){
            myAdapter.filter(events);
        }
        recyclerView.setAdapter(myAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
    }



}
