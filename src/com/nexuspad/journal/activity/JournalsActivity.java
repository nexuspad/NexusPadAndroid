/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.journal.activity;

import android.app.ActionBar;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import com.nexuspad.R;
import com.nexuspad.common.Constants;
import com.nexuspad.common.activity.EntriesActivity;
import com.nexuspad.common.annotation.ModuleInfo;
import com.nexuspad.common.annotation.ParentActivity;
import com.nexuspad.common.fragment.EntryFragment;
import com.nexuspad.home.activity.DashboardActivity;
import com.nexuspad.journal.fragment.JournalEditFragment;
import com.nexuspad.journal.fragment.JournalsFragment;
import com.nexuspad.service.datamodel.EntryTemplate;
import com.nexuspad.service.datamodel.NPFolder;
import com.nexuspad.service.datamodel.NPJournal;
import com.nexuspad.service.dataservice.ServiceConstants;
import com.nexuspad.service.util.DateUtil;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Edmond
 */
@ParentActivity(DashboardActivity.class)
@ModuleInfo(moduleId = ServiceConstants.JOURNAL_MODULE, template = EntryTemplate.JOURNAL)
public class JournalsActivity extends EntriesActivity implements JournalsFragment.JournalsCallback, JournalEditFragment.JournalDetailCallback {
	public static final String TAG = "JournalsActivity";

	private static final String KEY_PENDING_DISPLAY_DATE = "key_pending_display_date";

	private Date mPendingDisplayDate = null;

	public class DatePickerFragment extends DialogFragment
			implements DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);

			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		/**
		 * Handles date selected.
		 *
		 * @param view
		 * @param year
		 * @param month
		 * @param day
		 */
		public void onDateSet(DatePicker view, int year, int month, int day) {
			getFragment().setDisplayDate(DateUtil.getDate(year, month, day));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.journals_topmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_month:
//				final Intent intent = new Intent(this, JournalsMonthActivity.class);
//				startActivityForResult(intent, DATE_SELECTOR_REQUEST);

				DialogFragment datePickerFragment = new DatePickerFragment();
				datePickerFragment.show(getSupportFragmentManager(), "datePicker");

				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK) {
			return;
		}

		switch (requestCode) {
			default:
				break;
		}
	}

	@Override
	protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);

		final ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setTitle(R.string.journal);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mPendingDisplayDate != null) {
			getFragment().setDisplayDate(mPendingDisplayDate);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mPendingDisplayDate != null) {
			outState.putLong(KEY_PENDING_DISPLAY_DATE, mPendingDisplayDate.getTime());
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		long ts = savedInstanceState.getLong(KEY_PENDING_DISPLAY_DATE, -1);
		if (ts != -1) {
			mPendingDisplayDate = DateUtil.dateFromTimestampInSeconds(ts);
		} else {
			mPendingDisplayDate = DateUtil.now();
		}
	}

	@Override
	protected Fragment onCreateFragment() {
		final Bundle bundle = new Bundle();
		bundle.putParcelable(Constants.KEY_FOLDER, NPFolder.rootFolderOf(ServiceConstants.JOURNAL_MODULE));

		final JournalsFragment fragment = new JournalsFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onDeleting(EntryFragment<NPJournal> f, NPJournal entry) {
		// do nothing, the JournalFragment will receive update for the EntryList update
	}

	@Override
	protected JournalsFragment getFragment() {
		return (JournalsFragment) super.getFragment();
	}
}
