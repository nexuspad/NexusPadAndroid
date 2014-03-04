package com.nexuspad.calendar.ui.activity;

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import com.edmondapps.utils.android.annotaion.ParentActivity;
import com.google.common.collect.ImmutableList;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.calendar.ui.fragment.EventsAgendaFragment;
import com.nexuspad.calendar.ui.fragment.EventsMonthFragment;
import com.nexuspad.datamodel.EntryList;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.home.ui.activity.DashboardActivity;
import com.nexuspad.ui.activity.EntriesActivity;
import com.nexuspad.ui.fragment.EntriesFragment;

import java.util.List;

import static com.nexuspad.dataservice.ServiceConstants.CALENDAR_MODULE;

/**
 * Author: edmond
 */
@ParentActivity(DashboardActivity.class)
@ModuleId(moduleId = CALENDAR_MODULE, template = EntryTemplate.EVENT)
public class EventsActivity extends EntriesActivity implements EventsMonthFragment.Callback, ActionBar.OnNavigationListener {
    private static final String KEY_CALENDAR_DEFAULT_VIEW = "calendar_default_view";

    // must not be messed with, or breaks preferences compatibility
    private static final int CALENDAR_DEFAULT_VIEW_MONTH = 0;
    private static final int CALENDAR_DEFAULT_VIEW_AGENDA = 1;

    private EventsAgendaFragment mEventsAgendaFragment;
    private EventsMonthFragment mEventsMonthFragment;
    private long mStartTime = -1;
    private MenuItem mNewEventItem;
    private MenuItem mSearchItem;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        final Folder folder = Folder.rootFolderOf(getModule(), this);


        mEventsMonthFragment = EventsMonthFragment.of(folder);
        mEventsAgendaFragment = EventsAgendaFragment.of(folder);

        final ActionBar actionBar = getActionBar();

        final List<String> list = ImmutableList.of(getString(R.string.month), getString(R.string.agenda));

        final ArrayAdapter<?> adapter = new ArrayAdapter<String>(
                actionBar.getThemedContext(), R.layout.list_item_spinner, android.R.id.text1, list);

        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setListNavigationCallbacks(adapter, this);
        actionBar.setSelectedNavigationItem(readDefaultView());
    }

    @Override
    protected Fragment onCreateFragment() {
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.events_frag, menu);
        mNewEventItem = menu.findItem(R.id.new_event);
        mSearchItem = menu.findItem(R.id.search);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_event:
                NewEventActivity.startWithFolder(this, getFolder());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onListLoaded(EntriesFragment f, EntryList list) {
        super.onListLoaded(f, list);
        mNewEventItem.setVisible(true);
        mSearchItem.setVisible(true);
    }

    @Override
    public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
        mStartTime = view.getDate();
        getActionBar().setSelectedNavigationItem(CALENDAR_DEFAULT_VIEW_AGENDA);
    }

    private int readDefaultView() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getInt(KEY_CALENDAR_DEFAULT_VIEW, CALENDAR_DEFAULT_VIEW_MONTH);
    }

    private void writeDefaultView(int view) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.edit().putInt(KEY_CALENDAR_DEFAULT_VIEW, view).apply();
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        switch (itemPosition) {
            case CALENDAR_DEFAULT_VIEW_AGENDA:
            case CALENDAR_DEFAULT_VIEW_MONTH:
                switchView(itemPosition);
                return true;
            default:
                return false;
        }
    }

    private void switchView(int view) {
        final Fragment newFragment;
        final String newFragmentTag;

        switch (view) {
            case CALENDAR_DEFAULT_VIEW_AGENDA:
                mEventsAgendaFragment.setStartTime(mStartTime);
                newFragment = mEventsAgendaFragment;
                newFragmentTag = EventsAgendaFragment.TAG;
                break;
            case CALENDAR_DEFAULT_VIEW_MONTH:
                newFragment = mEventsMonthFragment;
                newFragmentTag = EventsMonthFragment.TAG;
                break;
            default:
                throw new AssertionError("unexpected view: " + view);
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(getFragmentId(), newFragment, newFragmentTag)
                .commit();

        writeDefaultView(view);
    }
}
