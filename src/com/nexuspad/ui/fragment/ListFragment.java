/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.nexuspad.R;

import static android.view.animation.AnimationUtils.loadAnimation;
import static com.edmondapps.utils.android.view.ViewUtils.findView;

/**
 * Unlike {@link android.support.v4.app.ListFragment}, this {@code Fragment}
 * does not throw if your layout does not contain {@link android.R.id#list}
 *
 * @author Edmond
 */
public abstract class ListFragment extends Fragment {
    public static final String TAG = "ListFragment";

    private ListView mListV;
    private ListAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_content, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mListV = findView(view, android.R.id.list);
        if (mListV != null) {
            mListV.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    onListItemClick(mListV, view, position, id);
                }
            });
        }
    }

    /**
     * If animation is enabled, the view must contain
     * {@link R.id#frame_progress} and {@link R.id#frame_list} for the animation
     * to function.
     * <p/>
     * It has no effect if your layout does not contain
     * {@link android.R.id#list}.
     *
     * @param adapter the adapter
     * @see #isFadeInEnabled()
     */
    public void setListAdapter(ListAdapter adapter) {
        if ((getListAdapter() == null) && isFadeInEnabled()) {
            FragmentActivity activity = getActivity();
            View view = getView();

            View progressFrame = view.findViewById(R.id.frame_progress);
            View listFrame = view.findViewById(R.id.frame_list);

            progressFrame.startAnimation(loadAnimation(activity, android.R.anim.fade_out));
            listFrame.startAnimation(loadAnimation(activity, android.R.anim.fade_in));

            progressFrame.setVisibility(View.GONE);
            listFrame.setVisibility(View.VISIBLE);
        }
        if (mListV != null) {
            mListV.setAdapter(adapter);
        }
        mAdapter = adapter;
    }

    /**
     * This method will be called when an item in the list is selected.
     * Subclasses should override. Subclasses can call
     * getListView().getItemAtPosition(position) if they need to access the
     * data associated with the selected item.
     *
     * @param l        The ListView where the click happened
     * @param v        The view that was clicked within the ListView
     * @param position The position of the view in the list
     * @param id       The row id of the item that was clicked
     * @see ListFragment#onListItemClick(ListView, View, int, long)
     */
    public void onListItemClick(ListView l, View v, int position, long id) {
    }

    /**
     * Controls whether fade-in animation should used when the
     * {@link ListAdapter} is created.
     * <p/>
     * The view must contain {@link R.id#frame_progress} and
     * {@link R.id#frame_list} for the animation to function.
     *
     * @return false if no animation should be used
     * @see #setListAdapter(ListAdapter)
     */
    public boolean isFadeInEnabled() {
        return true;
    }

    /**
     * It can return null if your layout does not contain
     * {@link android.R.id#list}
     */
    public ListView getListView() {
        return mListV;
    }

    public ListAdapter getListAdapter() {
        return mAdapter;
    }
}
