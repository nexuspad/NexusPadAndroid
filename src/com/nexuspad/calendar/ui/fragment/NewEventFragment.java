package com.nexuspad.calendar.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.contacts.ui.activity.NewLocationActivity;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Event;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.ui.fragment.NewEntryFragment;

import java.text.DateFormat;
import java.util.Date;

import static com.edmondapps.utils.android.view.ViewUtils.findView;
import static com.nexuspad.dataservice.ServiceConstants.CALENDAR_MODULE;

/**
 * Author: edmond
 */
@FragmentName(NewEventFragment.TAG)
@ModuleId(moduleId = CALENDAR_MODULE, template = EntryTemplate.EVENT)
public class NewEventFragment extends NewEntryFragment<Event> {
    public static final String TAG = "NewEventFragment";

    public static NewEventFragment of(Event event, Folder folder) {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_ENTRY, event);
        bundle.putParcelable(KEY_FOLDER, folder);

        final NewEventFragment fragment = new NewEventFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private DateFormat mDateFormat;
    private DateFormat mTimeFormat;

    private EditText mTitleV;
    private TextView mLocationV;
    private TextView mFromDayV;
    private TextView mFromTimeV;
    private TextView mToDayV;
    private TextView mToTimeV;
    private CheckBox mAllDayV;
    private Spinner mRepeatV;
    private EditText mTagsV;
    private EditText mNoteV;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDateFormat = android.text.format.DateFormat.getDateFormat(getActivity());
        mTimeFormat = android.text.format.DateFormat.getTimeFormat(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.event_new_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        findViews(view);

        mLocationV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Event event = getDetailEntryIfExist();
                final Intent intent = NewLocationActivity.of(getActivity(), event == null ? null : event.getLocation());
                startActivity(intent); //TODO receive result
            }
        });

        super.onViewCreated(view, savedInstanceState);
    }

    private void findViews(View view) {
        mTitleV = findView(view, R.id.txt_title);
        mLocationV = findView(view, R.id.txt_location);

        mFromDayV = findView(view, R.id.spinner_from_day);
        mFromTimeV = findView(view, R.id.spinner_from_time);
        mToDayV = findView(view, R.id.spinner_to_day);
        mToTimeV = findView(view, R.id.spinner_to_time);

        mAllDayV = findView(view, R.id.chk_all_day);
        mRepeatV = findView(view, R.id.spinner_repeat);
        mTagsV = findView(view, R.id.txt_tags);
        mNoteV = findView(view, R.id.txt_note);
    }

    @Override
    protected void updateUI() {
        super.updateUI();
        final Event event = getDetailEntryIfExist();
        if (event != null) {
            mTitleV.setText(event.getTitle());
            mLocationV.setText(event.getLocation().getFullAddress());

            mFromDayV.setText(mDateFormat.format(event.getStartTime()));
            mFromTimeV.setText(mTimeFormat.format(event.getStartTime()));

            mToDayV.setText(mDateFormat.format(event.getEndTime()));
            mToTimeV.setText(mTimeFormat.format(event.getEndTime()));

            mAllDayV.setChecked(event.isAllDayEvent());

            //TODO repeat
            mTagsV.setText(event.getTags());
            mNoteV.setTag(event.getNote());
        }
    }

    @Override
    public boolean isEditedEntryValid() {
        return false;
    }

    @Override
    public Event getEditedEntry() {
        return null;
    }
}
