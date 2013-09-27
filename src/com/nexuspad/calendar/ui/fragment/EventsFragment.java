package com.nexuspad.calendar.ui.fragment;

import com.edmondapps.utils.android.annotaion.FragmentName;
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
@FragmentName(EventsFragment.TAG)
@ModuleId(moduleId = CALENDAR_MODULE, template = EntryTemplate.EVENT)
public class EventsFragment extends EntriesFragment {
    public static final String TAG = "EventsFragment";

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
