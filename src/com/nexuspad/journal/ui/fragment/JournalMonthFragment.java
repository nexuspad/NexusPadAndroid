package com.nexuspad.journal.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.nexuspad.R;
import com.nexuspad.ui.fragment.EntriesFragment;

/**
 * User: edmond
 */
public class JournalMonthFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.journals_month_activity, container, false);
    }
}
