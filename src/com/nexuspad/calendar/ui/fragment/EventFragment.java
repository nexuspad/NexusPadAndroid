package com.nexuspad.calendar.ui.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.*;
import android.widget.TextView;
import com.nexuspad.common.annotaion.FragmentName;
import com.nexuspad.R;
import com.nexuspad.app.App;
import com.nexuspad.calendar.ui.activity.NewEventActivity;
import com.nexuspad.datamodel.NPEvent;
import com.nexuspad.datamodel.NPFolder;
import com.nexuspad.common.fragment.EntryFragment;

import java.util.Date;

/**
 * Author: edmond
 */
@FragmentName(EventFragment.TAG)
public class EventFragment extends EntryFragment<NPEvent> {
    public static final String TAG = "EventFragment";

    public static EventFragment of(NPEvent event, NPFolder folder) {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_ENTRY, event);
        bundle.putParcelable(KEY_FOLDER, folder);

        final EventFragment fragment = new EventFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private TextView mTitleV;
    private TextView mDateTimeV;
    private TextView mLocationV;
    private TextView mTagsV;
    private TextView mNoteV;

    private TextView mLocationFrameV;
    private TextView mTagsFrameV;
    private TextView mNoteFrameV;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.event_frag, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit:
                NewEventActivity.startWithEvent(getActivity(), getFolder(), getEntry());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.event_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mTitleV = (TextView)view.findViewById(R.id.lbl_title);
        mDateTimeV = (TextView)view.findViewById(R.id.lbl_date_time);
        mLocationV = (TextView)view.findViewById(R.id.lbl_location);
        mTagsV = (TextView)view.findViewById(R.id.lbl_tags);
        mNoteV = (TextView)view.findViewById(R.id.lbl_note);

        mLocationFrameV = (TextView)view.findViewById(R.id.lbl_location_frame);
        mTagsFrameV = (TextView)view.findViewById(R.id.lbl_tags_title);
        mNoteFrameV = (TextView)view.findViewById(R.id.lbl_note_frame);

        mTitleV.setTypeface(App.getRobotoLight());

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void updateUI() {
        final NPEvent event = getEntry();
        if (event != null) {
            mTitleV.setText(event.getTitle());
            final Date eventTime = event.getStartTime();
            mDateTimeV.setText(DateUtils.getRelativeTimeSpanString(eventTime.getTime(), System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS));
            mLocationV.setText(event.getLocation().getFullAddress());
            mTagsV.setText(event.getTags());
            mNoteV.setText(event.getNote());
            event.getColorLabel();
            updateVisibility(event);
        }
    }

    private void updateVisibility(NPEvent event) {
        final int locationFlag = event.getLocation().isEmpty() ? View.GONE : View.VISIBLE;
        final int tagsFlag = TextUtils.isEmpty(event.getTags()) ? View.GONE : View.VISIBLE;
        final int noteFlag = TextUtils.isEmpty(event.getNote()) ? View.GONE : View.VISIBLE;

        mLocationV.setVisibility(locationFlag);
        mLocationFrameV.setVisibility(locationFlag);
        mTagsV.setVisibility(tagsFlag);
        mTagsFrameV.setVisibility(tagsFlag);
        mNoteV.setVisibility(noteFlag);
        mNoteFrameV.setVisibility(noteFlag);
    }
}
