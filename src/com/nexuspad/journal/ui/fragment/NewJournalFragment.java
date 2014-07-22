package com.nexuspad.journal.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.nexuspad.common.annotaion.FragmentName;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.NPFolder;
import com.nexuspad.datamodel.NPJournal;
import com.nexuspad.common.fragment.NewEntryFragment;

import static com.nexuspad.dataservice.ServiceConstants.JOURNAL_MODULE;

/**
 * User: edmond
 */
@FragmentName(NewJournalFragment.TAG)
@ModuleId(moduleId = JOURNAL_MODULE, template = EntryTemplate.JOURNAL)
public class NewJournalFragment extends NewEntryFragment<NPJournal> {
    public static final String TAG = "NewJournalFragment";

    public static NewJournalFragment of(NPJournal journal, NPFolder folder) {
        final Bundle argument = new Bundle();
        argument.putParcelable(KEY_ENTRY, journal);
        argument.putParcelable(KEY_FOLDER, folder);

        final NewJournalFragment fragment = new NewJournalFragment();
        fragment.setArguments(argument);
        return fragment;
    }

    public interface Callback extends NewEntryFragment.Callback<NPJournal> {
    }

    private TextView mNoteView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.journal_new_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mNoteView = (TextView)view.findViewById(R.id.txt_note);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void updateUI() {
        super.updateUI();

        final NPJournal entry = getEntry();
        if (entry != null) {
            mNoteView.setText(entry.getNote());
        }
    }

    @Override
    public boolean isEditedEntryValid() {
        return true;  // there's nothing to validate for a journal
    }

    @Override
    public NPJournal getEditedEntry() {
        final NPJournal entry = getEntry();
        final NPJournal journal = entry == null ? new NPJournal(getFolder()) : new NPJournal(entry);

        journal.setNote(mNoteView.getText().toString());

        setEntry(journal);
        return journal;
    }
}
