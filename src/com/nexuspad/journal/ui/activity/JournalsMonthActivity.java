package com.nexuspad.journal.ui.activity;

import android.support.v4.app.Fragment;
import com.edmondapps.utils.android.activity.SinglePaneActivity;
import com.nexuspad.datamodel.EntryList;
import com.nexuspad.journal.ui.fragment.JournalMonthFragment;
import com.nexuspad.ui.fragment.EntriesFragment;

/**
 * User: edmond
 */
public class JournalsMonthActivity extends SinglePaneActivity implements EntriesFragment.Callback {

    @Override
    protected Fragment onCreateFragment() {
        return new JournalMonthFragment();
    }

    @Override
    public void onListLoaded(EntriesFragment f, EntryList list) {
        // do nothing
    }
}
