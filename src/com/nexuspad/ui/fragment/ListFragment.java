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
import com.edmondapps.utils.android.Logs;
import com.nexuspad.R;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import static android.view.animation.AnimationUtils.loadAnimation;

/**
 * Unlike {@link android.support.v4.app.ListFragment}, this {@code Fragment}
 * does not throw if your layout does not contain {@link android.R.id#list}
 *
 * @author Edmond
 */
public abstract class ListFragment extends Fragment {
    public static final String TAG = "ListFragment";

    private ListViewManager mListViewManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_content, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final View listView = view.findViewById(android.R.id.list);
        if (listView != null) {
            mListViewManager = ListViewManager.of(listView);
            mListViewManager.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    onListItemClick(mListViewManager.getListView(), view, position, id);
                }
            });
        }
    }

    /**
     * If animation is enabled, the view must contain
     * {@link com.nexuspad.R.id#frame_progress} and {@link com.nexuspad.R.id#frame_list} for the animation
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
        if (mListViewManager != null) {
            mListViewManager.setListAdapter(adapter);
        }
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
     * The view must contain {@link com.nexuspad.R.id#frame_progress} and
     * {@link com.nexuspad.R.id#frame_list} for the animation to function.
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
        if (mListViewManager != null) {
            return mListViewManager.getListView();
        }
        return null;
    }

    public ListAdapter getListAdapter() {
        if (mListViewManager != null) {
            return mListViewManager.getListAdapter();
        }
        return null;
    }

    public void smoothScrollToPosition(int position) {
        if (mListViewManager != null) {
            mListViewManager.smoothScrollToPosition(position);
        }
    }

    /**
     * Delegate calls to a {@link ListView} or a {@link StickyListHeadersListView}.
     */
    private static abstract class ListViewManager {
        /**
         * Automatically returns the correct {@code ListViewManager}.
         *
         * @param view supports {@link ListView} and {@link StickyListHeadersListView}
         * @return the manager
         * @throws UnsupportedOperationException if the view is neither a {@link ListView} or {@link StickyListHeadersListView}
         */
        public static ListViewManager of(View view) {
            if (view instanceof ListView) {
                final ListView listView = (ListView) view;
                return ListViewManager.of(listView);
            } else if (view instanceof StickyListHeadersListView) {
                StickyListHeadersListView headersListView = (StickyListHeadersListView) view;
                return ListViewManager.of(headersListView);
            }
            throw new UnsupportedOperationException();
        }

        public static ListViewManager of(ListView listView) {
            return new NativeListViewManager(listView);
        }

        public static ListViewManager of(StickyListHeadersListView listView) {
            return new StickyListHeadersListViewManager(listView);
        }

        /**
         * Register a callback to be invoked when an item in this AdapterView has
         * been clicked.
         *
         * @param listener The callback that will be invoked.
         */
        public abstract void setOnItemClickListener(OnItemClickListener listener);

        protected abstract void setListAdapter(ListAdapter adapter);

        protected abstract ListAdapter getListAdapter();

        protected abstract ListView getListView();

        protected abstract void smoothScrollToPosition(int position);
    }

    private static class NativeListViewManager extends ListViewManager {

        private final ListView mListView;

        private NativeListViewManager(ListView listView) {
            mListView = listView;
        }

        @Override
        public void setOnItemClickListener(OnItemClickListener listener) {
            mListView.setOnItemClickListener(listener);
        }

        @Override
        protected void setListAdapter(ListAdapter adapter) {
            mListView.setAdapter(adapter);
        }

        @Override
        protected ListAdapter getListAdapter() {
            return mListView.getAdapter();
        }

        @Override
        protected ListView getListView() {
            return mListView;
        }

        @Override
        public void smoothScrollToPosition(int position) {
            mListView.smoothScrollToPosition(position);
        }
    }

    private static class StickyListHeadersListViewManager extends ListViewManager {

        private final StickyListHeadersListView mListView;

        private StickyListHeadersListViewManager(StickyListHeadersListView listView) {
            mListView = listView;
        }

        @Override
        public void setOnItemClickListener(OnItemClickListener listener) {
            mListView.setOnItemClickListener(listener);
        }

        @Override
        protected void setListAdapter(ListAdapter adapter) {
            try {
                mListView.setAdapter((StickyListHeadersAdapter) adapter);
            } catch (ClassCastException e) {
                throw new IllegalArgumentException(e);
            }
        }

        @Override
        protected ListAdapter getListAdapter() {
            return mListView.getAdapter();
        }

        @Override
        protected ListView getListView() {
            return mListView.getWrappedList();
        }

        @Override
        protected void smoothScrollToPosition(int position) {
            mListView.smoothScrollToPosition(position + mListView.getHeaderViewsCount());
        }
    }
}