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
import com.nexuspad.common.fragment.EntryEditFragment;
import com.nexuspad.common.view.LocationTextView;
import com.nexuspad.contacts.activity.LocationEditActivity;
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
    private TextView mRepeatV;
    private EditText mTagsView;
    private EditText mNoteV;

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
                    mLocationView.setLocation(location);
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
                        final Intent intent = LocationEditActivity.of(getActivity(), event == null ? null : event.getLocation());
                        startActivityForResult(intent, LOCATION_EDIT_REQUEST);
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

        mLocationView.setOnClickListener(listener);
        mFromDateView.setOnClickListener(listener);
        mFromTimeView.setOnClickListener(listener);
        mToDateView.setOnClickListener(listener);
        mToTimeView.setOnClickListener(listener);

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
        mRepeatV = (TextView)view.findViewById(R.id.spinner_repeat);
        mTagsView = (EditText)view.findViewById(R.id.txt_tags);
        mNoteV = (EditText)view.findViewById(R.id.journal_text);
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
            mTitleView.setText(event.getTitle());
            mLocationView.setLocation(event.getLocation());

            final Date startTime = event.getStartTime();
            mFromTimeView.setTime(startTime);
            mFromDateView.setTime(startTime);

            final Date endTime = event.getEndTime();
            mToTimeView.setTime(endTime);
            mToDateView.setTime(endTime);

            mAllDayView.setChecked(event.isAllDayEvent());

            //TODO repeat
            mTagsView.setText(event.getTags());
            mNoteV.setTag(event.getNote());
        }
    }

	@Override
	public boolean isEditedEntryValid() {
		return true;
	}

	@Override
    public NPEvent getEntryFromEditor() {
        final NPEvent entry = getEntry();
        NPEvent event = entry == null ? new NPEvent(getFolder()) : new NPEvent(entry);

        event.setTitle(mTitleView.getText().toString());
        event.setLocation(mLocationView.getLocation());
        event.setStartTime(combineDateTime(mFromDateView.getTime(), mFromTimeView.getTime()));
        event.setEndTime(combineDateTime(mToDateView.getTime(), mToTimeView.getTime()));
        event.setAllDayEvent(mAllDayView.isChecked());
        event.setTags(mTagsView.getText().toString());
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