/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.journal.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.edmondapps.utils.android.annotaion.FragmentName;
import com.edmondapps.utils.android.view.ViewUtils;
import com.nexuspad.R;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.datamodel.Journal;
import com.nexuspad.ui.fragment.EntryFragment;

/**
 * @author Edmond
 * 
 */
@FragmentName(JournalFragment.TAG)
public class JournalFragment extends EntryFragment<Journal> {
    public static final String TAG = "JournalFragment";

    public static JournalFragment of(Journal j, Folder f) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_ENTRY, j);
        bundle.putParcelable(KEY_FOLDER, f);

        JournalFragment fragment = new JournalFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public interface Callback extends EntryFragment.Callback<Journal> {
    }

    private EditText mNoteV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.journal_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mNoteV = ViewUtils.findView(view, R.id.txt_note);

        updateUI();
    }

    private void updateUI() {
        Journal entry = getDetailEntryIfExist();
        if (entry != null) {
            mNoteV.setText(entry.getNote());
        }
    }

    @Override
    protected void onEntryUpdated(Journal entry) {
        super.onEntryUpdated(entry);
        updateUI();
    }

    @Override
    protected void onDetailEntryUpdated(Journal entry) {
        super.onDetailEntryUpdated(entry);
        updateUI();
    }
}
