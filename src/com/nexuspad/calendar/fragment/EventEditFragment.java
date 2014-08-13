package com.nexuspad.calendar.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import com.android.datetimepicker.date.DatePickerDialog;
import com.android.datetimepicker.time.RadialPickerLayout;
import com.android.datetimepicker.time.TimePickerDialog;
import com.nexuspad.R;
import com.nexuspad.calendar.activity.RecurrenceEditActivity;
import com.nexuspad.calendar.view.DateButton;
import com.nexuspad.calendar.view.RecurrenceTextView;
import com.nexuspad.calendar.view.TimeButton;
import com.nexuspad.common.annotation.FragmentName;
import com.nexuspad.common.annotation.ModuleId;
import com.nexuspad.common.fragment.EntryEditFragment;
import com.nexuspad.common.view.LocationTextView;
import com.nexuspad.contacts.activity.LocationEditActivity;
import com.nexuspad.datamodel.*;
import com.nexuspad.util.DateUtil;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static com.nexuspad.dataservice.ServiceConstants.CALENDAR_MODULE;

/**
 * Author: edmond
 */
@FragmentName(EventEditFragment.TAG)
@ModuleId(moduleId = CALENDAR_MODULE, template = EntryTemplate.EVENT)
public class EventEditFragment extends EntryEditFragment<NPEvent> {
	public static final String TAG = "EventEditFragment";

	private static final int LOCATION_EDIT_REQUEST = 2;
	private static final int RECURRENCE_EDIT_REQUEST = 3;

	public static EventEditFragment of(NPEvent event, NPFolder folder) {
		final Bundle bundle = new Bundle();
		bundle.putParcelable(KEY_ENTRY, event);
		bundle.putParcelable(KEY_FOLDER, folder);

		final EventEditFragment fragment = new EventEditFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	private EditText mTitleView;
	private LocationTextView mLocationView;

	private DateButton mFromDateView;
	private TimeButton mFromTimeView;
	private DateButton mToDateView;
	private TimeButton mToTimeView;

	private CheckBox mAllDayView;
	private RecurrenceTextView mRecurrenceView;
	private EditText mTagsView;
	private EditText mNoteView;

	private String mStartYmd = null;
	private String mEndYmd = null;
	private String mStartHourMin = null;         // HH24
	private String mEndHourMin = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.event_edit_frag, container, false);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case LOCATION_EDIT_REQUEST:
				if (resultCode == Activity.RESULT_OK) {
					final Location location = data.getParcelableExtra(LocationEditActivity.KEY_LOCATION);
					updateLocationView(location);
				}
				break;

			case RECURRENCE_EDIT_REQUEST:
				if (resultCode == Activity.RESULT_OK) {
					Recurrence recurrence = data.getParcelableExtra(RecurrenceEditActivity.KEY_RECURRENCE);
					updateRecurrenceView(recurrence);
				}
			default:
				super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		findViews(view);

		final View.OnClickListener listener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final NPEvent event = getEntry();
				final int id = v.getId();
				switch (id) {
					case R.id.txt_location:
						final Intent locationEditIntent = LocationEditActivity.of(getActivity(), event == null ? null : event.getLocation());
						startActivityForResult(locationEditIntent, LOCATION_EDIT_REQUEST);
						break;

					case R.id.spinner_start_day:
						Calendar startCal = nowOrEventTime(event);
						final DatePickerDialog startDatePicker = DatePickerDialog.newInstance((DateButton) v,
								startCal.get(Calendar.YEAR), startCal.get(Calendar.MONTH), startCal.get(Calendar.DAY_OF_MONTH));
						startDatePicker.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
							@Override
							public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {
								mFromDateView.onDateSet(dialog, year, monthOfYear, dayOfMonth);
								mStartYmd = String.format("%4d%02d%02d", year, monthOfYear, dayOfMonth);
								updateEventFromEditor();
							}
						});
						startDatePicker.show(getActivity().getFragmentManager(), String.valueOf(id));
						break;

					case R.id.spinner_start_time:
						Calendar startTimeCal = nowOrEventTime(event);
						TimePickerDialog timePickerDialog = TimePickerDialog.newInstance((TimeButton) v,
								startTimeCal.get(Calendar.HOUR_OF_DAY), startTimeCal.get(Calendar.MINUTE), false);

						timePickerDialog.setOnTimeSetListener(new TimePickerDialog.OnTimeSetListener() {
							@Override
							public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
								mFromTimeView.onTimeSet(view, hourOfDay, minute);
								mStartHourMin = String.format("%02d%02d", hourOfDay, minute);
								updateEventFromEditor();
							}
						});
						timePickerDialog.show(getActivity().getFragmentManager(), String.valueOf(id));
						break;

					case R.id.spinner_to_day:
						Calendar endCal = nowOrEventTime(event);
						DatePickerDialog endDatePicker = DatePickerDialog.newInstance((DateButton) v,
								endCal.get(Calendar.YEAR), endCal.get(Calendar.MONTH), endCal.get(Calendar.DAY_OF_MONTH));
						endDatePicker.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
							@Override
							public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {
								mToDateView.onDateSet(dialog, year, monthOfYear, dayOfMonth);
								mEndYmd = String.format("%4d%02d%02d", year, monthOfYear, dayOfMonth);
								updateEventFromEditor();
							}
						});
						endDatePicker.show(getActivity().getFragmentManager(), String.valueOf(id));
						break;

					case R.id.spinner_to_time:
						Calendar endTimeCal = nowOrEventTime(event);
						TimePickerDialog endTimePickerDialog = TimePickerDialog.newInstance((TimeButton) v,
								endTimeCal.get(Calendar.HOUR_OF_DAY), endTimeCal.get(Calendar.MINUTE), false);

						endTimePickerDialog.setOnTimeSetListener(new TimePickerDialog.OnTimeSetListener() {
							@Override
							public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
								mToTimeView.onTimeSet(view, hourOfDay, minute);
								mEndHourMin = String.format("%02d%02d", hourOfDay, minute);
								updateEventFromEditor();
							}
						});
						endTimePickerDialog.show(getActivity().getFragmentManager(), String.valueOf(id));
						break;

					case R.id.txt_recurrence:
						final Intent recurrenceEditIntent = RecurrenceEditActivity.of(getActivity(), (Recurrence) mRecurrenceView.getTag());
						startActivityForResult(recurrenceEditIntent, RECURRENCE_EDIT_REQUEST);
						break;

					default:
						throw new AssertionError("unexpected id: " + id);
				}
			}
		};

		mLocationView.setOnClickListener(listener);
		mFromDateView.setOnClickListener(listener);
		mFromTimeView.setOnClickListener(listener);
		mToDateView.setOnClickListener(listener);
		mToTimeView.setOnClickListener(listener);
		mRecurrenceView.setOnClickListener(listener);

		if (savedInstanceState != null) {
			final FragmentManager manager = getActivity().getFragmentManager();
			final TimePickerDialog timePicker = findTimePickerDialog(manager);
			final DatePickerDialog datePicker = findDatePickerDialog(manager);

			if (timePicker != null) {
				final int id = Integer.parseInt(timePicker.getTag());
				timePicker.setOnTimeSetListener(findTimePicker(id));
			}
			if (datePicker != null) {
				final int id = Integer.parseInt(datePicker.getTag());
				datePicker.setOnDateSetListener(findDatePicker(id));
			}
		}

		super.onViewCreated(view, savedInstanceState);
	}

	private TimePickerDialog findTimePickerDialog(FragmentManager manager) {
		Fragment fragment = manager.findFragmentByTag(String.valueOf(R.id.spinner_start_time));
		if (fragment == null) {
			fragment = manager.findFragmentByTag(String.valueOf(R.id.spinner_to_time));
		}
		return (TimePickerDialog) fragment;
	}

	private DatePickerDialog findDatePickerDialog(FragmentManager manager) {
		Fragment fragment = manager.findFragmentByTag(String.valueOf(R.id.spinner_start_day));
		if (fragment == null) {
			fragment = manager.findFragmentByTag(String.valueOf(R.id.spinner_to_day));
		}
		return (DatePickerDialog) fragment;
	}

	private void findViews(View view) {
		mTitleView = (EditText)view.findViewById(R.id.txt_title);
		mLocationView = (LocationTextView)view.findViewById(R.id.txt_location);

		mFromDateView = (DateButton)view.findViewById(R.id.spinner_start_day);
		mFromTimeView = (TimeButton)view.findViewById(R.id.spinner_start_time);
		mToDateView = (DateButton)view.findViewById(R.id.spinner_to_day);
		mToTimeView = (TimeButton)view.findViewById(R.id.spinner_to_time);

		mAllDayView = (CheckBox)view.findViewById(R.id.chk_all_day);
		mRecurrenceView = (RecurrenceTextView)view.findViewById(R.id.txt_recurrence);
		mTagsView = (EditText)view.findViewById(R.id.txt_tags);
		mNoteView = (EditText)view.findViewById(R.id.journal_text);
	}

	// only works after findViews(View)
	private DateButton findDatePicker(int id) {
		switch (id) {
			case R.id.spinner_start_day:
				return mFromDateView;
			case R.id.spinner_to_day:
				return mToDateView;
			default:
				throw new AssertionError("unexpected id: " + id);
		}
	}

	// only works after findViews(View)
	private TimeButton findTimePicker(int id) {
		switch (id) {
			case R.id.spinner_start_time:
				return mFromTimeView;
			case R.id.spinner_to_time:
				return mToTimeView;
			default:
				throw new AssertionError("unexpected id: " + id);
		}
	}

	private Calendar nowOrEventTime(NPEvent event) {
		final long time = event == null ? System.currentTimeMillis() : event.getStartTime().getTime();
		final Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);
		return c;
	}


	@Override
	protected void updateUI() {
		super.updateUI();

		final NPEvent event = getEntry();
		if (event != null) {
			mTitleView.setText(event.getTitle());

			updateLocationView(event.getLocation());

			final Date startTime = event.getStartTime();
			mFromDateView.setTime(startTime);
			mFromTimeView.setTime(startTime);

			final Date endTime = event.getEndTime();
			if (endTime != null) {
				mToDateView.setTime(endTime);
				mToTimeView.setTime(endTime);
			}

			mAllDayView.setChecked(event.isAllDayEvent());

			updateRecurrenceView(event.getRecurrence());

			mTagsView.setText(event.getTags());
			mNoteView.setTag(event.getNote());
		}
	}

	private void updateLocationView(Location location) {
		if (location != null) {
			mLocationView.setLocation(location);
			mLocationView.setTag(location);
		}
	}

	private void updateRecurrenceView(Recurrence recurrence) {
		if (recurrence != null) {
			mRecurrenceView.setText(recurrence.toString());
			mRecurrenceView.setTag(recurrence);
		}
	}

	@Override
	public boolean isEditedEntryValid() {
		NPEvent event = NPEvent.fromEntry(getEntry());

		if (event.getStartTime() == null || (event.getEndTime() != null && event.getStartTime().after(event.getEndTime()))) {
			return false;
		}

		return true;
	}

	/**
	 * Update the entry object.
	 * Called when the editor is updated.
	 */
	private void updateEventFromEditor() {
		final NPEvent entry = getEntry();
		NPEvent event = entry == null ? new NPEvent(getFolder()) : new NPEvent(entry);

		event.setTitle(mTitleView.getText().toString());

		// Location
		if (mLocationView.getLocation() != null) {
			event.setLocation(mLocationView.getLocation());
		}

		// Recurrence
		if (mRecurrenceView.getRecurrence() != null) {
			event.setRecurrence(mRecurrenceView.getRecurrence());
		}

		// Start time
		if (mStartYmd != null) {
			try {
				event.setStartTime(DateUtil.parseEventDateStringAndTimeString(mStartYmd, mStartHourMin, TimeZone.getDefault()));
			} catch (ParseException e) {
				Log.e(TAG, "Error parsing start time: " + mStartYmd + " " + String.valueOf(mStartHourMin));
			}

			if (mEndYmd == null) {
				if (mStartHourMin == null) {
					event.setNoStartingTime(true);
				} else {
					event.setSingleTimeEvent(true);
				}
			}
		}

		// End time
		if (mEndYmd != null) {
			try {
				event.setEndTime(DateUtil.parseEventDateStringAndTimeString(mEndYmd, mEndHourMin, TimeZone.getDefault()));
			} catch (ParseException e) {
				Log.e(TAG, "Error parsing end time: " + mEndYmd + " " + String.valueOf(mEndHourMin));
			}
		}

		// Set the flags

		if (mAllDayView.isChecked()) {
			event.setAllDayEvent(mAllDayView.isChecked());
			event.setSingleTimeEvent(false);
			event.setNoStartingTime(true);
		}

		event.setTags(mTagsView.getText().toString());
		event.setNote(mNoteView.getText().toString());

		mEntry = event;
	}

	@Override
	public NPEvent getEntryFromEditor() {
		updateEventFromEditor();
		return NPEvent.fromEntry(getEntry());
	}

}