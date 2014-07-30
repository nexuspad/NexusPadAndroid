package com.nexuspad.journal.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.nexuspad.common.annotation.FragmentName;
import com.nexuspad.R;
import com.nexuspad.common.annotation.ModuleId;
import com.nexuspad.common.fragment.EntryEditFragment;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.NPFolder;
import com.nexuspad.datamodel.NPJournal;

import static com.nexuspad.dataservice.ServiceConstants.JOURNAL_MODULE;

/**
 * User: edmond
 */
@FragmentName(JournalEditFragment.TAG)
@ModuleId(moduleId = JOURNAL_MODULE, template = EntryTemplate.JOURNAL)
public class JournalEditFragment extends EntryEditFragment<NPJournal> {
    public static final String TAG = "JournalEditFragment";

    public static JournalEditFragment of(NPJournal journal, NPFolder folder) {
        final Bundle argument = new Bundle();
        argument.putParcelable(KEY_ENTRY, journal);
        argument.putParcelable(KEY_FOLDER, folder);

        final JournalEditFragment fragment = new JournalEditFragment();
        fragment.setArguments(argument);
        return fragment;
    }

    public interface Callback extends EntryEditFragment.Callback<NPJournal> {
    }

    private TextView mNoteView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.journal_edit_frag, container, false);
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
