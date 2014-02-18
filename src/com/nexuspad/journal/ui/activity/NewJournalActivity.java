package com.nexuspad.journal.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import com.edmondapps.utils.android.annotaion.ParentActivity;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.datamodel.Journal;
import com.nexuspad.journal.ui.fragment.NewJournalFragment;
import com.nexuspad.ui.activity.NewEntryActivity;

import static com.nexuspad.dataservice.ServiceConstants.JOURNAL_MODULE;

/**
 * User: edmond
 */
@ModuleId(moduleId = JOURNAL_MODULE, template = EntryTemplate.JOURNAL)
@ParentActivity(JournalsActivity.class)
public class NewJournalActivity extends NewEntryActivity<Journal> implements NewJournalFragment.Callback {

    public static void startWith(Context context, Journal journal, Folder folder) {
        context.startActivity(NewJournalActivity.of(context, journal, folder));
    }

    public static Intent of(Context context, Journal journal, Folder folder) {
        final Intent intent = new Intent(context, NewJournalActivity.class);
        intent.putExtra(KEY_ENTRY, journal);
        intent.putExtra(KEY_FOLDER, folder);
        return intent;
    }

    @Override
    protected Fragment onCreateFragment() {
        return NewJournalFragment.of(getEntry(), getFolder());
    }
}