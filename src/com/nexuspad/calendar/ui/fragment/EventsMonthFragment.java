package com.nexuspad.calendar.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.calendar.ui.activity.EventActivity;
import com.nexuspad.datamodel.*;
import com.nexuspad.dataservice.EntryListService;
import com.nexuspad.dataservice.NPException;
import com.nexuspad.ui.fragment.EntriesFragment;
import com.nexuspad.util.DateUtil;
import com.nexuspad.util.Logs;

import java.util.List;

import static com.nexuspad.dataservice.ServiceConstants.CALENDAR_MODULE;

/**
 * Author: edmond
 */
@FragmentName(EventsMonthFragment.TAG)
@ModuleId(moduleId = CALENDAR_MODULE, template = EntryTemplate.EVENT)
public class EventsMonthFragment extends EntriesFragment {
    public static final String TAG = "EventsMonthFragment";

    public static EventsMonthFragment of(Folder folder) {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_FOLDER, folder);

        final EventsMonthFragment fragment = new EventsMonthFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.events_month_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void getEntriesInFolder(EntryListService service, Folder folder, int page) throws NPException {
        service.getEntriesBetweenDates(folder, getTemplate(), DateUtil.now(), DateUtil.addDaysTo(DateUtil.now(), 30), page, getEntriesCountPerPage());
    }

    @Override
    protected void onListLoaded(EntryList list) {
        super.onListLoaded(list);
        final List<NPEntry> entries = list.getEntries();
        Logs.d(TAG, entries.toString());
        if (!entries.isEmpty()) {
            EventActivity.startWith(getActivity(), (Event) entries.get(0), getFolder());
        }
    }
}
