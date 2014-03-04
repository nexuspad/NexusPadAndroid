package com.nexuspad.journal.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.edmondapps.utils.android.view.ViewUtils;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.app.App;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.datamodel.Journal;
import com.nexuspad.ui.fragment.EntryFragment;
import com.nexuspad.ui.fragment.NewEntryFragment;

import java.text.DateFormat;

import static com.edmondapps.utils.android.view.ViewUtils.findView;
import static com.nexuspad.dataservice.ServiceConstants.JOURNAL_MODULE;

/**
 * User: edmond
 */
@ModuleId(moduleId = JOURNAL_MODULE, template = EntryTemplate.JOURNAL)
public class NewJournalFragment extends NewEntryFragment<Journal> {

    public static NewJournalFragment of(Journal journal, Folder folder) {
        final Bundle argument = new Bundle();
        argument.putParcelable(KEY_ENTRY, journal);
        argument.putParcelable(KEY_FOLDER, folder);

        final NewJournalFragment fragment = new NewJournalFragment();
        fragment.setArguments(argument);
        return fragment;
    }

    public interface Callback extends NewEntryFragment.Callback<Journal> {
    }

    private TextView mNoteView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.journal_new_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mNoteView = findView(view, R.id.txt_note);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void updateUI() {
        super.updateUI();

        final Journal entry = getEntry();
        if (entry != null) {
            mNoteView.setText(entry.getNote());
        }
    }

    @Override
    public boolean isEditedEntryValid() {
        return true;  // there's nothing to validate for a journal
    }

    @Override
    public Journal getEditedEntry() {
        final Journal entry = getEntry();
        final Journal journal = entry == null ? new Journal(getFolder()) : new Journal(entry);

        journal.setNote(mNoteView.getText().toString());

        setEntry(journal);
        return journal;
    }
}
