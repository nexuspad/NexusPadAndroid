package com.nexuspad.calendar.fragment;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.android.datetimepicker.date.DatePickerDialog;
import com.nexuspad.R;
import com.nexuspad.calendar.view.DateButton;
import com.nexuspad.service.datamodel.Recurrence;
import com.nexuspad.service.util.DateUtil;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by ren on 8/3/14.
 */
public class RecurrenceEditFragment extends DialogFragment {
	public static final String TAG = "RecurrenceEditFragment";
	public static final String KEY_RECURRENCE = "key_recurrence";

	public static RecurrenceEditFragment of (Recurrence recurrence) {
		final Bundle bundle = new Bundle();
		bundle.putParcelable(KEY_RECURRENCE, recurrence);

		final RecurrenceEditFragment fragment = new RecurrenceEditFragment();
		fragment.setArguments(bundle);

		return fragment;
	}

	private Recurrence mRecurrence;

	private RadioGroup mRecurrencePatternRadioGroup;
	private RadioButton mNoRecurrenceRadioButton;
	private RadioButton mRepeatDailyRadioButton;
	private RadioButton mRepeatWeeklyRadioButton;
	private RadioButton mRepeatMonthlyRadioButton;
	private RadioButton mRepeatYearlyRadioButton;

	private View mIntervalView;
	private TextView mRecurrenceIntervalText;
	private Button mReduceIntervalButton;
	private Button mIncreaseIntervalButton;

	private View mRecurTimesView;
	private TextView mRecurrenceTimesText;
	private Button mReduceRepeatTimesButton;
	private Button mIncreaseRepeatTimesButton;
	private DateButton mRepeatEndDateButton;
	private Switch mRepeatForEverSwitch;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.event_recurrence_edit_frag, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// Recurrence pattern
		mRecurrencePatternRadioGroup = (RadioGroup)view.findViewById(R.id.rbg_recurrence_pattern);

		/*
		 * Recurrence pattern change
		 */
		mRecurrencePatternRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (mRecurrence == null) {
					mRecurrence = new Recurrence();
				}

				if (mNoRecurrenceRadioButton.isChecked()) {
					mRecurrence.setPattern(Recurrence.Pattern.NOREPEAT);
					mIntervalView.setVisibility(View.GONE);
					mRecurTimesView.setVisibility(View.GONE);
				}

				if (mRepeatDailyRadioButton.isChecked()) {
					mRecurrence.setPattern(Recurrence.Pattern.DAILY);
					mIntervalView.setVisibility(View.VISIBLE);
					mRecurTimesView.setVisibility(View.VISIBLE);
				}

				if (mRepeatWeeklyRadioButton.isChecked()) {
					mRecurrence.setPattern(Recurrence.Pattern.WEEKLY);
					mIntervalView.setVisibility(View.VISIBLE);
					mRecurTimesView.setVisibility(View.VISIBLE);
				}

				if (mRepeatMonthlyRadioButton.isChecked()) {
					mRecurrence.setPattern(Recurrence.Pattern.MONTHLY);
					mIntervalView.setVisibility(View.VISIBLE);
					mRecurTimesView.setVisibility(View.VISIBLE);
				}

				if (mRepeatYearlyRadioButton.isChecked()) {
					mRecurrence.setPattern(Recurrence.Pattern.YEARLY);
					mIntervalView.setVisibility(View.VISIBLE);
					mRecurTimesView.setVisibility(View.VISIBLE);
				}

				updateUI();
			}
		});

		mNoRecurrenceRadioButton = (RadioButton)view.findViewById(R.id.rb_no_recurrence);
		mRepeatDailyRadioButton = (RadioButton)view.findViewById(R.id.rb_repeat_daily);
		mRepeatWeeklyRadioButton = (RadioButton)view.findViewById(R.id.rb_repeat_weekly);
		mRepeatMonthlyRadioButton = (RadioButton)view.findViewById(R.id.rb_repeat_monthly);
		mRepeatYearlyRadioButton = (RadioButton)view.findViewById(R.id.rb_repeat_yearly);

		// Recurrence interval
		mIntervalView = view.findViewById(R.id.layout_recurrence_interval);
		mRecurrenceIntervalText = (TextView)view.findViewById(R.id.txt_recurrence_interval);

		/*
		 * Update the interval
		 */

		// Interval "-"
		mReduceIntervalButton = (Button)view.findViewById(R.id.btn_reduce_interval);
		mReduceIntervalButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mRecurrence.reduceInterval();
				setIntervalText();
			}
		});

		// Interval "+"
		mIncreaseIntervalButton = (Button)view.findViewById(R.id.btn_increase_interval);
		mIncreaseIntervalButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mRecurrence.increaseInterval();
				setIntervalText();
			}
		});

		/*
		 * Repeat times and end date
		 */

		// Recurrence times and end date
		mRecurTimesView = view.findViewById(R.id.layout_recurrence_times);
		mRecurrenceTimesText = (TextView)view.findViewById(R.id.txt_repeat_times);

		// Repeat times "-"
		mReduceRepeatTimesButton = (Button)view.findViewById(R.id.btn_reduce_interval);
		mReduceRepeatTimesButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mRecurrence.reduceRecurrenceTimes();
				mRecurrence.setEndDate(null);
				mRecurrence.setRepeatForever(false);
				setRecurrenceEndUI(mRecurrence.getRecurrenceTimes());
			}
		});

		// Repeat times "+"
		mIncreaseRepeatTimesButton = (Button)view.findViewById(R.id.btn_increase_repeat_times);
		mIncreaseRepeatTimesButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mRecurrence.increaseRecurrenceTimes();
				mRecurrence.setEndDate(null);
				mRecurrence.setRepeatForever(false);
				setRecurrenceEndUI(mRecurrence.getRecurrenceTimes());
			}
		});

		mRepeatEndDateButton = (DateButton)view.findViewById(R.id.btn_repeat_end_date);
		mRepeatEndDateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final Calendar c = Calendar.getInstance();

				if (mRecurrence.getEndDate() != null) {
					c.setTime(mRecurrence.getEndDate());
				}

				DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(mRepeatEndDateButton,
						c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

				datePickerDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
					@Override
					public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {
						Date endDate = DateUtil.getDate(year, monthOfYear, dayOfMonth);
						mRecurrence.setEndDate(endDate);
						mRecurrence.setRecurrenceTimes(0);
						mRecurrence.setRepeatForever(false);
						setRecurrenceEndUI(endDate);
					}
				});

				datePickerDialog.show(getActivity().getFragmentManager(), String.valueOf(R.id.btn_repeat_end_date));
			}
		});

		// Forever switch
		mRepeatForEverSwitch = (Switch)view.findViewById(R.id.switch_repeat_forever);
		mRepeatForEverSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mRecurrence.setRecurrenceTimes(0);
				mRecurrence.setEndDate(null);
				mRecurrence.setRepeatForever(true);
				setRecurrenceEndUI(isChecked);
			}
		});

		updateUI();
	}

	private void updateUI() {
		if (mRecurrence != null) {
			switch (mRecurrence.getPattern()) {
				case NOREPEAT:
					mNoRecurrenceRadioButton.setChecked(true);
					break;

				case DAILY:
					mRepeatDailyRadioButton.setChecked(true);
					break;

				case WEEKLY:
					mRepeatWeeklyRadioButton.setChecked(true);
					break;

				case MONTHLY:
					mRepeatMonthlyRadioButton.setChecked(true);
					break;

				case YEARLY:
					mRepeatYearlyRadioButton.setChecked(true);
					break;
			}

			setIntervalText();
		}

		if (mRecurrence != null && mRecurrence.getPattern() != Recurrence.Pattern.NOREPEAT) {
			// Repeat times
			mRecurrenceTimesText.setText("Repeat n times");

			// End date
			if (mRecurrence.getEndDate() != null) {
				mRepeatEndDateButton.setTime(mRecurrence.getEndDate());
			}

			// Repeat forever
			if (mRecurrence.isRepeatForever()) {
				mRepeatForEverSwitch.setChecked(true);
			} else {
				mRepeatForEverSwitch.setChecked(false);
			}

			mIntervalView.setVisibility(View.VISIBLE);
			mRecurTimesView.setVisibility(View.VISIBLE);

		} else {
			mNoRecurrenceRadioButton.setChecked(true);
			mIntervalView.setVisibility(View.GONE);
			mRecurTimesView.setVisibility(View.GONE);
		}
	}

	public Recurrence getEditedRecurrence() {
		return mRecurrence;
	}

	/**
	 * Refresh the recurrence interval text
	 */
	private void setIntervalText() {
		switch (mRecurrence.getPattern()) {
			case DAILY:
				mRecurrenceIntervalText.setText(getString(R.string.recurrence_interval_daily, mRecurrence.getInterval()));
				break;
			case WEEKLY:
				mRecurrenceIntervalText.setText(getString(R.string.recurrence_interval_weekly, mRecurrence.getInterval()));
				break;
			case MONTHLY:
				mRecurrenceIntervalText.setText(getString(R.string.recurrence_interval_monthly, mRecurrence.getInterval()));
				break;
			case YEARLY:
				mRecurrenceIntervalText.setText(getString(R.string.recurrence_interval_yearly, mRecurrence.getInterval()));
				break;
		}
	}

	/**
	 * Refresh the recurrence end information with repeat times.
	 *
	 * @param repeatTimes
	 */
	private void setRecurrenceEndUI(int repeatTimes) {
		mRecurrenceTimesText.setText(getString(R.string.repeat_n_times, String.valueOf(repeatTimes)));
		mRepeatEndDateButton.setText(getString(R.string.select_date));
		mRepeatForEverSwitch.setChecked(false);
	}

	/**
	 * Refresh the recurrence end information with end date.
	 *
	 * @param recurrenceEndDate
	 */
	private void setRecurrenceEndUI(Date recurrenceEndDate) {
		mRecurrenceTimesText.setText(getString(R.string.repeat_n_times, ""));
		mRepeatEndDateButton.setText(DateUtil.displayDate(getActivity(), recurrenceEndDate));
		mRepeatForEverSwitch.setChecked(false);
	}

	/**
	 * Refresh recurrence end information when "repeat forever" is changed.
	 *
	 * @param repeatForever
	 */
	private void setRecurrenceEndUI(boolean repeatForever) {
		if (repeatForever) {
			mRecurrenceTimesText.setText(getString(R.string.repeat_n_times, ""));
			mRepeatEndDateButton.setText(getString(R.string.select_date));
			mRepeatForEverSwitch.setChecked(true);

		} else {
			mRecurrenceTimesText.setText(getString(R.string.repeat_n_times, ""));
			mRepeatEndDateButton.setText(getString(R.string.select_date));
			mRepeatForEverSwitch.setChecked(false);
		}
	}
}