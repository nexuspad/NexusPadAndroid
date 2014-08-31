package com.nexuspad.calendar.activity;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import com.google.common.collect.ImmutableList;
import com.nexuspad.R;
import com.nexuspad.calendar.fragment.EventsListFragment;
import com.nexuspad.calendar.fragment.EventsMonthFragment;
import com.nexuspad.common.activity.EntriesActivity;
import com.nexuspad.common.annotation.ModuleId;
import com.nexuspad.common.fragment.EntriesFragment;
import com.nexuspad.datamodel.EntryList;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.NPFolder;
import com.nexuspad.util.DateUtil;

import java.util.Date;
import java.util.List;

import static com.nexuspad.dataservice.ServiceConstants.CALENDAR_MODULE;

/**
 * Author: edmond
 */
@ModuleId(moduleId = CALENDAR_MODULE, template = EntryTemplate.EVENT)
public class EventsActivity extends EntriesActivity implements EventsMonthFragment.Callback, ActionBar.OnNavigationListener {
	private static final String KEY_CALENDAR_DEFAULT_VIEW = "calendar_default_view";

	// must not be messed with, or breaks preferences compatibility
	private static final int CALENDAR_DEFAULT_VIEW_MONTH = 0;
	private static final int CALENDAR_DEFAULT_VIEW_AGENDA = 1;

	private EventsListFragment mEventsListFragment;
	private EventsMonthFragment mEventsMonthFragment;

	public static void startWithFolder(NPFolder f, Context c) {
		Intent intent = new Intent(c, EventsActivity.class);
		intent.putExtra(KEY_FOLDER, f);
		c.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);

		mEventsMonthFragment = new EventsMonthFragment();

		final ActionBar actionBar = getActionBar();

		final List<String> list = ImmutableList.of(getString(R.string.month), getString(R.string.agenda));

		final ArrayAdapter<?> adapter = new ArrayAdapter<String>(actionBar.getThemedContext(), R.layout.list_item_spinner, android.R.id.text1, list);

		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setListNavigationCallbacks(adapter, this);
		actionBar.setSelectedNavigationItem(readDefaultView());

		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	protected Fragment onCreateFragment() {
		final Bundle bundle = new Bundle();
		bundle.putParcelable(KEY_FOLDER, mFolder);

		Date today = DateUtil.now();
		String startYmd = DateUtil.convertToYYYYMMDD(DateUtil.addDaysTo(today, -30));
		String endYmd = DateUtil.convertToYYYYMMDD(DateUtil.addDaysTo(today, 30));

		bundle.putString(EventsListFragment.KEY_START_YMD, startYmd);
		bundle.putString(EventsListFragment.KEY_END_YMD, endYmd);

		mEventsListFragment = new EventsListFragment();
		mEventsListFragment.setArguments(bundle);

		return mEventsListFragment;
	}

	@Override
	public void onListLoaded(EntriesFragment f, EntryList list) {
		super.onListLoaded(f, list);
	}

	@Override
	public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
//		final Date oldDate = mStartTime;
//		final Date newDate = new Date(view.getDate());
//		if (!DateUtil.isSameDay(oldDate, newDate)) {
//			mStartTime = newDate;
//			getActionBar().setSelectedNavigationItem(CALENDAR_DEFAULT_VIEW_AGENDA);
//		}
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
				return true;
		}
	}

	private void switchView(int view) {
		final Fragment oldFragment;
		final Fragment newFragment;

		final String oldFragmentTag;
		final String newFragmentTag;

		switch (view) {
			case CALENDAR_DEFAULT_VIEW_AGENDA:
				oldFragment = mEventsMonthFragment;
				oldFragmentTag = EventsMonthFragment.TAG;

				Date today = DateUtil.now();
				String startYmd = DateUtil.convertToYYYYMMDD(DateUtil.addDaysTo(today, -30));
				String endYmd = DateUtil.convertToYYYYMMDD(DateUtil.addDaysTo(today, 30));

				mEventsListFragment.setStartEndTime(startYmd, endYmd);

				newFragment = mEventsListFragment;
				newFragmentTag = EventsListFragment.TAG;
				break;

			case CALENDAR_DEFAULT_VIEW_MONTH:
				oldFragment = mEventsListFragment;
				oldFragmentTag = EventsListFragment.TAG;

				newFragment = mEventsMonthFragment;
				newFragmentTag = EventsMonthFragment.TAG;
				break;

			default:
				throw new AssertionError("unexpected view: " + view);
		}

		final FragmentManager manager = getSupportFragmentManager();
		final FragmentTransaction transaction = manager.beginTransaction();

		if (manager.findFragmentByTag(newFragmentTag) != null) {
			transaction.show(newFragment);
		} else {
			transaction.add(getFragmentId(), newFragment, newFragmentTag);
		}

		if (manager.findFragmentByTag(oldFragmentTag) != null) {
			transaction.hide(oldFragment);
		}

		transaction.commit();

		writeDefaultView(view);
	}
}
