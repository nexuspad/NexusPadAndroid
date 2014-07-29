package com.nexuspad.calendar.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import com.android.datetimepicker.date.DatePickerDialog;
import com.android.datetimepicker.time.TimePickerDialog;
import com.nexuspad.R;
import com.nexuspad.common.annotation.ModuleId;
import com.nexuspad.calendar.view.DateButton;
import com.nexuspad.calendar.view.TimeButton;
import com.nexuspad.common.annotation.FragmentName;
import com.nexuspad.common.fragment.UpdateEntryFragment;
import com.nexuspad.common.view.LocationTextView;
import com.nexuspad.contacts.activity.UpdateLocationActivity;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.NPEvent;
import com.nexuspad.datamodel.NPFolder;
import com.nexuspad.datamodel.Location;

import java.util.Calendar;
import java.util.Date;

import static com.nexuspad.dataservice.ServiceConstants.CALENDAR_MODULE;

/**
 * Author: edmond
 */
@FragmentName(NewEventFragment.TAG)
@ModuleId(moduleId = CALENDAR_MODULE, template = EntryTemplate.EVENT)
public class NewEventFragment extends UpdateEntryFragment<NPEvent> {
    public static final String TAG = "NewEventFragment";

    private static final int REQ_LOCATION = REQ_SUBCLASSES + 1;

    public static NewEventFragment of(NPEvent event, NPFolder folder) {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_ENTRY, event);
        bundle.putParcelable(KEY_FOLDER, folder);

        final NewEventFragment fragment = new NewEventFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private EditText mTitleV;
    private LocationTextView mLocationV;

    private DateButton mStartDayV;
    private TimeButton mStartTimeV;
    private DateButton mToDayV;
    private TimeButton mToTimeV;

    private CheckBox mAllDayV;
    private TextView mRepeatV;
    private EditText mTagsV;
    private EditText mNoteV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.event_edit_frag, container, false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQ_LOCATION:
                if (resultCode == Activity.RESULT_OK) {
                    final Location location = data.getParcelableExtra(UpdateLocationActivity.KEY_LOCATION);
                    mLocationV.setLocation(location);
                }
                break;
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
                        final Intent intent = UpdateLocationActivity.of(getActivity(), event == null ? null : event.getLocation());
                        startActivityForResult(intent, REQ_LOCATION);
                        break;
                    case R.id.spinner_start_day:
                        showDatePicker((DateButton) v, nowOrEventTime(event), String.valueOf(id));
                        break;
                    case R.id.spinner_start_time:
                        showTimePicker((TimeButton) v, nowOrEventTime(event), String.valueOf(id));
                        break;
                    case R.id.spinner_to_day:
                        showDatePicker((DateButton) v, nowOrEventTime(event), String.valueOf(id));
                        break;
                    case R.id.spinner_to_time:
                        showTimePicker((TimeButton) v, nowOrEventTime(event), String.valueOf(id));
                        break;
                    default:
                        throw new AssertionError("unexpected id: " + id);
                }
            }
        };

        mLocationV.setOnClickListener(listener);
        mStartDayV.setOnClickListener(listener);
        mStartTimeV.setOnClickListener(listener);
        mToDayV.setOnClickListener(listener);
        mToTimeV.setOnClickListener(listener);

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
        mTitleV = (EditText)view.findViewById(R.id.txt_title);
        mLocationV = (LocationTextView)view.findViewById(R.id.txt_location);

        mStartDayV = (DateButton)view.findViewById(R.id.spinner_start_day);
        mStartTimeV = (TimeButton)view.findViewById(R.id.spinner_start_time);
        mToDayV = (DateButton)view.findViewById(R.id.spinner_to_day);
        mToTimeV = (TimeButton)view.findViewById(R.id.spinner_to_time);

        mAllDayV = (CheckBox)view.findViewById(R.id.chk_all_day);
        mRepeatV = (TextView)view.findViewById(R.id.spinner_repeat);
        mTagsV = (EditText)view.findViewById(R.id.txt_tags);
        mNoteV = (EditText)view.findViewById(R.id.txt_note);
    }

    // only works after findViews(View)
    private DateButton findDatePicker(int id) {
        switch (id) {
            case R.id.spinner_start_day:
                return mStartDayV;
            case R.id.spinner_to_day:
                return mToDayV;
            default:
                throw new AssertionError("unexpected id: " + id);
        }
    }

    // only works after findViews(View)
    private TimeButton findTimePicker(int id) {
        switch (id) {
            case R.id.spinner_start_time:
                return mStartTimeV;
            case R.id.spinner_to_time:
                return mToTimeV;
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

    private void showDatePicker(DateButton dateButton, Calendar c, String fragmentTag) {
        DatePickerDialog.newInstance(dateButton,
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
                .show(getActivity().getFragmentManager(), fragmentTag);
    }

    private void showTimePicker(TimeButton timeButton, Calendar c, String fragmentTag) {
        TimePickerDialog.newInstance(timeButton,
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false)
                .show(getActivity().getFragmentManager(), fragmentTag);
    }

    @Override
    protected void updateUI() {
        super.updateUI();
        final NPEvent event = getEntry();
        if (event != null) {
            mTitleV.setText(event.getTitle());
            mLocationV.setLocation(event.getLocation());

            final Date startTime = event.getStartTime();
            mStartTimeV.setTime(startTime);
            mStartDayV.setTime(startTime);

            final Date endTime = event.getEndTime();
            mToTimeV.setTime(endTime);
            mToDayV.setTime(endTime);

            mAllDayV.setChecked(event.isAllDayEvent());

            //TODO repeat
            mTagsV.setText(event.getTags());
            mNoteV.setTag(event.getNote());
        }
    }

	@Override
	public boolean isEditedEntryValid() {
		return true;
	}

	@Override
    public NPEvent getEditedEntry() {
        final NPEvent entry = getEntry();
        NPEvent event = entry == null ? new NPEvent(getFolder()) : new NPEvent(entry);

        event.setTitle(mTitleV.getText().toString());
        event.setLocation(mLocationV.getLocation());
        event.setStartTime(combineDateTime(mStartDayV.getTime(), mStartTimeV.getTime()));
        event.setEndTime(combineDateTime(mToDayV.getTime(), mToTimeV.getTime()));
        event.setAllDayEvent(mAllDayV.isChecked());
        event.setTags(mTagsV.getText().toString());
        event.setNote(mNoteV.getText().toString());

        setEntry(event);
        return event;
    }

    @SuppressWarnings("deprecation")
    private Date combineDateTime(Date date, Date time) {
        final Date out = new Date();
        out.setMonth(date.getMonth());
        out.setDate(date.getDate());
        out.setHours(time.getHours());
        out.setMinutes(time.getMinutes());
        out.setSeconds(time.getSeconds());
        return out;
    }
}