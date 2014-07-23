package com.nexuspad.journal.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.CalendarView;
import com.nexuspad.common.activity.SinglePaneActivity;
import com.nexuspad.datamodel.EntryList;
import com.nexuspad.journal.fragment.JournalMonthFragment;
import com.nexuspad.common.fragment.EntriesFragment;

/**
 * User: edmond
 */
public class JournalsMonthActivity extends SinglePaneActivity implements
        EntriesFragment.Callback,
        JournalMonthFragment.Callback {

    /**
     * contains the long of the date
     */
    public static final String KEY_DATE = "JournalsMonthActivity.date";

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setResult(RESULT_CANCELED);
    }

    @Override
    protected Fragment onCreateFragment() {
        return new JournalMonthFragment();
    }

    @Override
    public void onListLoaded(EntriesFragment f, EntryList list) {
        // do nothing
    }

    @Override
    public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
        getFragment().dismiss();

        final Intent data = new Intent();
        data.putExtra(KEY_DATE, view.getDate());

        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    protected JournalMonthFragment getFragment() {
        return (JournalMonthFragment) super.getFragment();
    }
}
