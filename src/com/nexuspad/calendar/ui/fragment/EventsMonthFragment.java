package com.nexuspad.calendar.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.app.App;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.dataservice.EntryListService;
import com.nexuspad.dataservice.NPException;
import com.nexuspad.ui.fragment.EntriesFragment;
import com.nexuspad.util.DateUtil;

import static com.edmondapps.utils.android.view.ViewUtils.findView;
import static com.nexuspad.dataservice.ServiceConstants.CALENDAR_MODULE;

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

        mCalendarView = findView(view, R.id.calendar_picker);
        mCalendarView.setOnDateChangeListener(mCallback);
    }
}
