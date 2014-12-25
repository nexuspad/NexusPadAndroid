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
import com.nexuspad.common.Constants;
import com.nexuspad.common.annotation.ModuleInfo;
import com.nexuspad.common.fragment.EntryEditFragment;
import com.nexuspad.service.datamodel.EntryTemplate;
import com.nexuspad.service.datamodel.NPFolder;
import com.nexuspad.service.datamodel.NPJournal;
import com.nexuspad.service.datamodel.NPModule;
import com.nexuspad.service.util.DateUtil;

import java.util.Date;

import static com.nexuspad.service.dataservice.ServiceConstants.JOURNAL_MODULE;

/**
 * User: edmond
 */
@ModuleInfo(moduleId = JOURNAL_MODULE, template = EntryTemplate.JOURNAL)
public class JournalEditFragment extends EntryEditFragment<NPJournal> {
	public static final String TAG = "JournalEditFragment";

	private String journalDateYmd;
	private TextView noteView;
	private Date lastModifiedTime;

	public static JournalEditFragment of(NPJournal journal) {
		final Bundle argument = new Bundle();
		argument.putParcelable(Constants.KEY_ENTRY, journal);
		argument.putParcelable(Constants.KEY_FOLDER, NPFolder.rootFolderOf(NPModule.JOURNAL));

		final JournalEditFragment fragment = new JournalEditFragment();
		fragment.setArguments(argument);

		fragment.journalDateYmd = journal.getJournalYmd();

		return fragment;
	}

	public interface JournalDetailCallback extends EntryDetailCallback<NPJournal> {
	}

	/*
	 * This is called in EntryFragment EntryReceiver.onGet
	 */
	@Override
	public void setEntry(NPJournal journal) {
		if (journal.getJournalYmd().equals(journalDateYmd)) {
			Log.i(TAG, "Load Journal into UI editor " + journalDateYmd);
			noteView.setText(journal.getNote());
			lastModifiedTime = journal.getLastModifiedTime();
		}
	}

	public boolean journalEdited() {
		if (mEntry == null) {
			return false;
		}

		NPJournal j = NPJournal.fromEntry(mEntry);

		if (j.getLastModifiedTime() == null) {
			return true;
		}

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

		noteView = (EditText)view.findViewById(R.id.txt_note);
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

	@Override
	public NPJournal getEntryFromEditor() {
		Log.e(TAG, "No implementation!!!!");
		return null;
	}

	public String getJournalDateYmd() {
		return journalDateYmd;
	}

	public String getJournalText() {
		return noteView.getText().toString();
	}
}
