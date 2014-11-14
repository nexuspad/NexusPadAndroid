package com.nexuspad.journal.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import com.nexuspad.R;
import com.nexuspad.common.annotation.ModuleId;
import com.nexuspad.common.fragment.EntryEditFragment;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.NPFolder;
import com.nexuspad.datamodel.NPJournal;
import com.nexuspad.datamodel.NPModule;
import com.nexuspad.util.DateUtil;

import java.util.Date;

import static com.nexuspad.dataservice.ServiceConstants.JOURNAL_MODULE;

/**
 * User: edmond
 */
@ModuleId(moduleId = JOURNAL_MODULE, template = EntryTemplate.JOURNAL)
public class JournalEditFragment extends EntryEditFragment<NPJournal> {
	public static final String TAG = "JournalEditFragment";

	private String journalDateYmd;
	private TextView noteView;
	private Date lastModifiedTime;

	public static JournalEditFragment of(NPJournal journal) {
		final Bundle argument = new Bundle();
		argument.putParcelable(KEY_ENTRY, journal);
		argument.putParcelable(KEY_FOLDER, NPFolder.rootFolderOf(NPModule.JOURNAL));

		final JournalEditFragment fragment = new JournalEditFragment();
		fragment.setArguments(argument);

		fragment.journalDateYmd = journal.getJournalYmd();

		return fragment;
	}

	public interface JournalDetailCallback extends EntryDetailCallback<NPJournal> {
	}

	@Override
	public void setEntry(NPJournal journal) {
		if (journal.getJournalYmd().equals(journalDateYmd)) {
			Log.i(TAG, "Update UI for " + journalDateYmd);
			noteView.setText(journal.getNote());
			lastModifiedTime = journal.getLastModifiedTime();
		}
	}

	public boolean journalEdited() {
		NPJournal j = NPJournal.fromEntry(mEntry);
		if (lastModifiedTime != null && lastModifiedTime.after(j.getLastModifiedTime())) {
			return true;
		}
		return false;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.journal_edit_frag, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		noteView = (EditText)view.findViewById(R.id.journal_text);
		noteView.setText(mEntry.getNote());

		noteView.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				if (s.toString().length() > 0) {
					lastModifiedTime = DateUtil.now();
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
		});
	}

	@Override
	public boolean isEditedEntryValid() {
		return true;
	}

	/**
	 * Used in updateEntry.
	 *
	 * @return
	 */
	@Override
	public NPJournal getEntryFromEditor() {
		NPJournal journal = NPJournal.fromEntry(mEntry);
		journal.setNote(noteView.getText().toString());
		return journal;
	}

	public String getJournalDateYmd() {
		return journalDateYmd;
	}

	NPJournal getUpdatedJournal() {
		NPJournal j = NPJournal.fromEntry(mEntry);
		j.setNote(noteView.getText().toString());

		return j;
	}
}
