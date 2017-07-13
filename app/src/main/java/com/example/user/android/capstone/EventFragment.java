package com.example.user.android.capstone;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class EventFragment extends Fragment implements EventsFragmentInterface {
    RecyclerView recyclerView;
    static EventFragment eventFragment = null;
   // LifecycleListener lifecycleListener;
    public EventFragment() {
        // Required empty public constructor
    }


    public static EventFragment newInstance() {

        if (eventFragment == null)
            eventFragment = new EventFragment();

        return eventFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        System.out.println("bububu");
        View view = inflater.inflate(R.layout.fragment_event, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycle_view);
       // lifecycleListener.onCreatedView();
        return view;
    }


    public void updateList(List<Event> events) {
        System.out.println("EVENT LIST FORM DATABASE SIZE " + events.size());
        EventAdapter myAdapter = new EventAdapter(getContext(), events);
        recyclerView.setAdapter(myAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setHasFixedSize(true); ////
        recyclerView.setLayoutManager(layoutManager);
    }

//    public void setListener(LifecycleListener lifecycleListener) {
//        this.lifecycleListener = lifecycleListener;
//    }
}
