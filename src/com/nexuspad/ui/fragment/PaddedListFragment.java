/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui.fragment;

import static android.view.animation.AnimationUtils.loadAnimation;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import com.actionbarsherlock.app.SherlockListFragment;
import com.nexuspad.R;

/**
 * @author Edmond
 * 
 */
public abstract class PaddedListFragment extends SherlockListFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_content, container, false);
    }

    @Override
    public void setListAdapter(ListAdapter adapter) {
        if (getListAdapter() == null) {
            FragmentActivity activity = getActivity();
            View view = getView();

            View progressFrame = view.findViewById(R.id.frame_progress);
            View listFrame = view.findViewById(R.id.frame_list);

            progressFrame.startAnimation(loadAnimation(activity, android.R.anim.fade_out));
            listFrame.startAnimation(loadAnimation(activity, android.R.anim.fade_in));

            progressFrame.setVisibility(View.GONE);
            listFrame.setVisibility(View.VISIBLE);
        }
        super.setListAdapter(adapter);
    }
}
