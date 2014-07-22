package com.nexuspad.journal.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import com.nexuspad.R;
import com.nexuspad.app.App;

/**
 * User: edmond
 */
public class JournalMonthFragment extends DialogFragment {

    public interface Callback extends CalendarView.OnDateChangeListener {
    }

    private Callback mCallback;
    private CalendarView mCalendarV;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = App.getCallbackOrThrow(activity, Callback.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.journals_month_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCalendarV = (CalendarView)view.findViewById(R.id.calendar_view);
        mCalendarV.setOnDateChangeListener(mCallback);
    }
}
