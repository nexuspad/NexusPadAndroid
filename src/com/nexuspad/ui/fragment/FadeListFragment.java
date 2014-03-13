/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.nexuspad.R;
import com.nexuspad.ui.UndoBarController;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import static android.view.animation.AnimationUtils.loadAnimation;

/**
 * Unlike {@link android.support.v4.app.ListFragment}, this {@code Fragment}
 * does not throw if your layout does not contain {@link android.R.id#list}
 *
 * @author Edmond
 */
public abstract class FadeListFragment extends Fragment implements UndoBarController.UndoBarListener {
    public static final String TAG = "FadeListFragment";

    private ListViewManager mListViewManager;
    private UndoBarController mUndoBarController;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mUndoBarController != null) {
            mUndoBarController.onSaveInstanceState(outState);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_content, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final View undoBarV = view.findViewById(R.id.undobar);
        if (undoBarV != null) {
            mUndoBarController = new UndoBarController(undoBarV, this);
            mUndoBarController.onRestoreInstanceState(savedInstanceState);
        }

        // ListViewManager.of(…) overloads will determine which manager to return
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

    protected void hideUndoBar(boolean immediate) {
        getUndoBarController().hideUndoBar(immediate);
    }

    protected void showUndoBar(boolean immediate, CharSequence message, Intent undoToken) {
        getUndoBarController().showUndoBar(immediate, message, undoToken);
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
            fadeInListFrame();
        }
        if (mListViewManager != null) {
            mListViewManager.setListAdapter(adapter);
        }
    }

    /**
     * fade in the list frame if the layout (returned by {@link #getView()}) contains {@link R.id#frame_progress}
     * and {@link R.id#frame_list}.<br/>
     * The progress view will be fade out and the list will be fade in.
     */
    protected void fadeInListFrame() {
        final FragmentActivity activity = getActivity();
        final View view = getView();

        final View progressFrame = view.findViewById(R.id.frame_progress);
        final View listFrame = view.findViewById(R.id.frame_list);

        if (progressFrame == null || listFrame == null) {
            return;
        }

        progressFrame.startAnimation(loadAnimation(activity, android.R.anim.fade_out));
        listFrame.startAnimation(loadAnimation(activity, android.R.anim.fade_in));

        progressFrame.setVisibility(View.GONE);
        listFrame.setVisibility(View.VISIBLE);
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
     * @see FadeListFragment#onListItemClick(ListView, View, int, long)
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

    protected ListViewManager getListViewManager() {
        return mListViewManager;
    }

    protected UndoBarController getUndoBarController() {
        if (mUndoBarController == null) {
            throw new IllegalStateException("the layout does not contain R.id.undobar");
        }
        return mUndoBarController;
    }

    /**
     * It can return null if your layout does not contain
     * {@link android.R.id#list}
     */
    protected ListView getListView() {
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

    /**
     * Delegate calls to a {@link ListView} or a {@link StickyListHeadersListView}.
     */
    protected static abstract class ListViewManager {
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

        public abstract void setFastScrollEnabled(boolean enabled);

        public abstract void smoothScrollToPosition(int position);

        protected abstract void setListAdapter(ListAdapter adapter);

        protected abstract ListAdapter getListAdapter();

        protected abstract ListView getListView();

        protected abstract void setOnScrollListener(AbsListView.OnScrollListener listener);
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
        public void setFastScrollEnabled(boolean enabled) {
            mListView.setFastScrollEnabled(enabled);
        }

        @Override
        public void smoothScrollToPosition(int position) {
            mListView.smoothScrollToPosition(position);
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
        protected void setOnScrollListener(AbsListView.OnScrollListener listener) {
            mListView.setOnScrollListener(listener);
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
        public void setFastScrollEnabled(boolean enabled) {
            mListView.setFastScrollEnabled(enabled);
        }

        @Override
        public void smoothScrollToPosition(int position) {
            mListView.smoothScrollToPosition(position + mListView.getHeaderViewsCount());
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
        protected void setOnScrollListener(AbsListView.OnScrollListener listener) {
            mListView.setOnScrollListener(listener);
        }
    }
}