/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.home.ui.fragment;

import static com.nexuspad.dataservice.ServiceConstants.BOOKMARK_MODULE;
import static com.nexuspad.dataservice.ServiceConstants.CALENDAR_MODULE;
import static com.nexuspad.dataservice.ServiceConstants.CONTACT_MODULE;
import static com.nexuspad.dataservice.ServiceConstants.DOC_MODULE;
import static com.nexuspad.dataservice.ServiceConstants.JOURNAL_MODULE;
import static com.nexuspad.dataservice.ServiceConstants.PHOTO_MODULE;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.nexuspad.R;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.ui.IconListAdapter;

/**
 * @author Edmond
 * 
 */
@FragmentName(DashboardFragment.TAG)
public class DashboardFragment extends SherlockListFragment {
    public static final String TAG = "DashboardFragment";

    /*
     * All 3 arrays must be consistent when re-ordering them
     */
    private static final int[] sModules = {
            CONTACT_MODULE, CALENDAR_MODULE, JOURNAL_MODULE,
            DOC_MODULE, PHOTO_MODULE, BOOKMARK_MODULE
    };
    private static final int[] sDrawables = {
            R.drawable.contact, R.drawable.event, R.drawable.journal,
            R.drawable.doc, R.drawable.photo, R.drawable.bookmark
    };
    private static final int[] sStrings = {
            R.string.contacts, R.string.events, R.string.journal,
            R.string.docs, R.string.photos, R.string.bookmarks
    };

    public interface Callback {
        /**
         * Called when an module is clicked.
         * 
         * @param f
         *            caller of this method
         * @param moduleType
         *            one of the {@code *_MODULE} constants defined in
         *            {@link ServiceConstants}
         */
        void onModuleClicked(DashboardFragment f, int moduleType);
    }

    private Callback mCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof Callback) {
            mCallback = (Callback)activity;
        } else {
            throw new IllegalStateException(activity + " must implement Callback.");
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setListAdapter(new IconListAdapter(getActivity(), sDrawables, sStrings));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        mCallback.onModuleClicked(this, sModules[position]);
    }
}