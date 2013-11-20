package com.nexuspad.calendar.ui.activity;

import android.support.v4.app.Fragment;
import com.edmondapps.utils.android.annotaion.ParentActivity;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.calendar.ui.fragment.EventsAgendaFragment;
import com.nexuspad.calendar.ui.fragment.EventsMonthFragment;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.home.ui.activity.DashboardActivity;
import com.nexuspad.ui.activity.EntriesActivity;

import static com.nexuspad.dataservice.ServiceConstants.CALENDAR_MODULE;

/**
 * Author: edmond
 */
@ParentActivity(DashboardActivity.class)
@ModuleId(moduleId = CALENDAR_MODULE, template = EntryTemplate.EVENT)
public class EventsActivity extends EntriesActivity {

    @Override
    protected Fragment onCreateFragment() {
        return EventsAgendaFragment.of(Folder.rootFolderOf(getModule(), this));
    }
}
