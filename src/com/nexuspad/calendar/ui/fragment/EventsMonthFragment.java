package com.nexuspad.calendar.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import com.nexuspad.common.annotaion.FragmentName;
import com.nexuspad.R;
import com.nexuspad.app.App;

/**
 * Author: edmond
 */
@FragmentName(EventsMonthFragment.TAG)
public class EventsMonthFragment extends Fragment {
    public static final String TAG = "EventsMonthFragment";

    public interface Callback extends CalendarView.OnDateChangeListener {
    }

    private CalendarView mCalendarView;
    private Callback mCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = App.getCallbackOrThrow(activity, Callback.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.events_month_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mCalendarView = (CalendarView)view.findViewById(R.id.calendar_picker);
        mCalendarView.setOnDateChangeListener(mCallback);
    }
}
