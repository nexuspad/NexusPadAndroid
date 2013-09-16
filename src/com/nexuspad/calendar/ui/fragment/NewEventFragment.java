package com.nexuspad.calendar.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Event;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.ui.fragment.NewEntryFragment;

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

    private EditText mTitleV;
    private EditText mLocationV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.event_new_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mTitleV = findView(view, R.id.txt_title);
        mLocationV = findView(view, R.id.txt_location);
        //TODO work-in-progress

        super.onViewCreated(view, savedInstanceState);
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
