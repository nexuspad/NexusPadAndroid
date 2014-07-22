/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.journal.ui.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;
import com.nexuspad.common.annotaion.ParentActivity;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.NPFolder;
import com.nexuspad.datamodel.NPJournal;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.home.activity.DashboardActivity;
import com.nexuspad.journal.ui.fragment.JournalsFragment;
import com.nexuspad.journal.ui.fragment.NewJournalFragment;
import com.nexuspad.common.activity.EntriesActivity;
import com.nexuspad.common.fragment.EntryFragment;

import java.text.DateFormat;

/**
 * @author Edmond
 */
@ParentActivity(DashboardActivity.class)
@ModuleId(moduleId = ServiceConstants.JOURNAL_MODULE, template = EntryTemplate.JOURNAL)
public class JournalsActivity extends EntriesActivity implements JournalsFragment.Callback, NewJournalFragment.Callback {
    public static final String TAG = "JournalsActivity";

    private static final int REQ_CODE_MONTH = 1;
    private static final String KEY_PENDING_DISPLAY_DATE = "key_pending_display_date";

    private DateFormat mDateFormat;
    private long mPendingDisplayDate = -1;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.journals_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_month:
                final Intent intent = new Intent(this, JournalsMonthActivity.class);
                startActivityForResult(intent, REQ_CODE_MONTH);
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
            case REQ_CODE_MONTH:
                mPendingDisplayDate = data.getLongExtra(JournalsMonthActivity.KEY_DATE, -1);
                // cannot set display date right now, onResume() is not called yet
                break;
            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        mDateFormat = android.text.format.DateFormat.getDateFormat(this);

        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.journal);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPendingDisplayDate >= 0) {
            getFragment().setDisplayDate(mPendingDisplayDate);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(KEY_PENDING_DISPLAY_DATE, mPendingDisplayDate);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mPendingDisplayDate = savedInstanceState.getLong(KEY_PENDING_DISPLAY_DATE, -1);
    }

    @Override
    protected Fragment onCreateFragment() {
        return JournalsFragment.of(NPFolder.rootFolderOf(ServiceConstants.JOURNAL_MODULE));
    }

    @Override
    public void onDeleting(EntryFragment<NPJournal> f, NPJournal entry) {
        // do nothing, the JournalFragment will receive update for the EntryList update
    }

    @Override
    protected JournalsFragment getFragment() {
        return (JournalsFragment) super.getFragment();
    }

    @Override
    public void onJournalSelected(JournalsFragment f, NPJournal journal) {
        final String dateString = mDateFormat.format(journal.getCreateTime());

        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(dateString);
        }
    }
}
