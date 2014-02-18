/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.journal.ui.activity;

import android.support.v4.app.Fragment;
import android.widget.Toast;
import com.edmondapps.utils.android.annotaion.ParentActivity;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.datamodel.Journal;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.home.ui.activity.DashboardActivity;
import com.nexuspad.journal.ui.fragment.JournalFragment;
import com.nexuspad.journal.ui.fragment.JournalsFragment;
import com.nexuspad.ui.activity.EntriesActivity;
import com.nexuspad.ui.fragment.EntryFragment;

/**
 * @author Edmond
 */
@ParentActivity(DashboardActivity.class)
@ModuleId(moduleId = ServiceConstants.JOURNAL_MODULE, template = EntryTemplate.JOURNAL)
public class JournalsActivity extends EntriesActivity implements JournalFragment.Callback {

    @Override
    protected Fragment onCreateFragment() {
        return JournalsFragment.of(Folder.rootFolderOf(ServiceConstants.JOURNAL_MODULE));
    }

    @Override
    public void onDeleting(EntryFragment<Journal> f, Journal entry) {
        // do nothing, the JournalFragment will receive update for the EntryList update
    }

    @Override
    protected JournalsFragment getFragment() {
        return (JournalsFragment)super.getFragment();
    }
}
