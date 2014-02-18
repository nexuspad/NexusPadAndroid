/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.journal.ui.fragment;

import android.app.Activity;
import android.os.*;
import android.view.*;
import android.widget.*;
import com.edmondapps.utils.android.annotaion.*;
import com.edmondapps.utils.android.view.*;
import com.nexuspad.*;
import com.nexuspad.app.App;
import com.nexuspad.datamodel.*;
import com.nexuspad.journal.ui.activity.NewJournalActivity;
import com.nexuspad.ui.fragment.*;

/**
 * @author Edmond
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

    private TextView mNoteV;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.journal_frag, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit:
                NewJournalActivity.startWith(getActivity(), getEntry(), getFolder());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.journal_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mNoteV = ViewUtils.findView(view, R.id.lbl_note);

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void updateUI() {
        final Journal entry = getEntry();
        if (entry != null) {
            mNoteV.setText(entry.getNote());
        }
    }
}
