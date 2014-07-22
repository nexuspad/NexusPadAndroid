/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.common.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.nexuspad.R;
import com.nexuspad.common.UndoBarController;
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

//    private ListViewManager mListViewManager;
    private UndoBarController mUndoBarController;
    private LoadingUiManager mLoadingUiManager;

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

	    /*
	     * The undo bar for folder deletion.
	     */
        final View undoBarV = view.findViewById(R.id.undobar);
        if (undoBarV != null) {
            mUndoBarController = new UndoBarController(undoBarV, this);
            mUndoBarController.onRestoreInstanceState(savedInstanceState);
        }

        // ListViewManager.of(…) overloads will determine which manager to return
//        final View listView = view.findViewById(android.R.id.list);
//        if (listView != null) {
//            mListViewManager = ListViewManager.of(listView);
//            mListViewManager.setOnItemClickListener(new OnItemClickListener() {
//                @Override
//                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    onListItemClick(mListViewManager.getListView(), view, position, id);
//                }
//            });
//        }

        final View listFrame = view.findViewById(R.id.frame_list);
        final View progressFrame = view.findViewById(R.id.frame_progress);
        final View retryFrame = view.findViewById(R.id.frame_retry);

        if (listFrame != null && progressFrame != null && retryFrame != null) {
            mLoadingUiManager = new LoadingUiManager(listFrame, retryFrame, progressFrame, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mLoadingUiManager.fadeInProgressFrame();
                    onRetryClicked(v);
                }
            });
        }
    }

    protected void onRetryClicked(View button) {
    }

    protected void hideUndoBar(boolean immediate) {
        getUndoBarController().hideUndoBar(immediate);
    }

    protected void showUndoBar(boolean immediate, CharSequence message, Intent undoToken) {
        getUndoBarController().showUndoBar(immediate, message, undoToken);
    }


	/**
	 * Fade out the progress or retrying screen element.
	 */
    protected void fadeInListFrame() {
        mLoadingUiManager.fadeInListFrame();
    }

    protected void fadeInProgressFrame() {
        mLoadingUiManager.fadeInProgressFrame();
    }

    protected void fadeInRetryFrame() {
        mLoadingUiManager.fadeInRetryFrame();
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

    protected UndoBarController getUndoBarController() {
        if (mUndoBarController == null) {
            throw new IllegalStateException("the layout does not contain R.id.undobar");
        }
        return mUndoBarController;
    }


    protected static final class LoadingUiManager {
        private final View mListV;
        private final View mRetryV;
        private final View mProgressV;
        private final Animation mFadeOutA;
        private final Animation mFadeInA;

        protected LoadingUiManager(View listV, View retryV, View progressV, View.OnClickListener onRetryListener) {
            mListV = listV;
            mRetryV = retryV;
            mProgressV = progressV;

            mProgressV.setVisibility(View.VISIBLE);
            mRetryV.setVisibility(View.GONE);
            mListV.setVisibility(View.GONE);

            final Context context = listV.getContext();
            mFadeOutA = loadAnimation(context, android.R.anim.fade_out);
            mFadeInA = loadAnimation(context, android.R.anim.fade_in);

            mRetryV.findViewById(R.id.btn_retry).setOnClickListener(onRetryListener);
        }

        protected void fadeInListFrame() {
            boolean isRetryVisible = mRetryV.getVisibility() == View.VISIBLE;
            boolean isProgressVisible = mProgressV.getVisibility() == View.VISIBLE;

            if (isRetryVisible) {
                fadeOut(mRetryV);
            }
            if (isProgressVisible) {
                fadeOut(mProgressV);
            }

            fadeIn(mListV);
        }

        protected void fadeInRetryFrame() {
            boolean isListVisible = mListV.getVisibility() == View.VISIBLE;
            boolean isProgressVisible = mProgressV.getVisibility() == View.VISIBLE;

            if (isListVisible) {
                fadeOut(mListV);
            }
            if (isProgressVisible) {
                fadeOut(mProgressV);
            }

            fadeIn(mRetryV);
        }

        protected void fadeInProgressFrame() {
            boolean isListVisible = mListV.getVisibility() == View.VISIBLE;
            boolean isRetryVisible = mRetryV.getVisibility() == View.VISIBLE;

            if (isListVisible) {
                fadeOut(mListV);
            }
            if (isRetryVisible) {
                fadeOut(mRetryV);
            }

            fadeIn(mProgressV);
        }

        private void fadeOut(View view) {
            view.startAnimation(mFadeOutA);
            view.setVisibility(View.GONE);
        }

        private void fadeIn(View view) {
            view.setVisibility(View.VISIBLE);
            view.startAnimation(mFadeInA);
        }
    }

    /**
     * Delegate calls to a {@link ListView} or a {@link StickyListHeadersListView}.
     */
//    protected static abstract class ListViewManager {
//        /**
//         * Automatically returns the correct {@code ListViewManager}.
//         *
//         * @param view supports {@link ListView} and {@link StickyListHeadersListView}
//         * @return the manager
//         * @throws UnsupportedOperationException if the view is neither a {@link ListView} or {@link StickyListHeadersListView}
//         */
//        public static ListViewManager of(View view) {
//            if (view instanceof ListView) {
//                final ListView listView = (ListView) view;
//                return ListViewManager.of(listView);
//            } else if (view instanceof StickyListHeadersListView) {
//                StickyListHeadersListView headersListView = (StickyListHeadersListView) view;
//                return ListViewManager.of(headersListView);
//            }
//            throw new UnsupportedOperationException();
//        }
//
//        public static ListViewManager of(ListView listView) {
//            return new NativeListViewManager(listView);
//        }
//
//        public static ListViewManager of(StickyListHeadersListView listView) {
//            return new StickyListHeadersListViewManager(listView);
//        }
//
//        /**
//         * Register a callback to be invoked when an item in this AdapterView has
//         * been clicked.
//         *
//         * @param listener The callback that will be invoked.
//         */
//        public abstract void setOnItemClickListener(OnItemClickListener listener);
//
//        public abstract void setFastScrollEnabled(boolean enabled);
//
//        public abstract void smoothScrollToPosition(int position);
//
//        protected abstract void setListAdapter(ListAdapter adapter);
//
//        protected abstract ListAdapter getListAdapter();
//
//        protected abstract ListView getListView();
//
//        protected abstract void setOnScrollListener(AbsListView.OnScrollListener listener);
//    }
//
//    private static class NativeListViewManager extends ListViewManager {
//
//        private final ListView mListView;
//
//        private NativeListViewManager(ListView listView) {
//            mListView = listView;
//        }
//
//        @Override
//        public void setOnItemClickListener(OnItemClickListener listener) {
//            mListView.setOnItemClickListener(listener);
//        }
//
//        @Override
//        public void setFastScrollEnabled(boolean enabled) {
//            mListView.setFastScrollEnabled(enabled);
//        }
//
//        @Override
//        public void smoothScrollToPosition(int position) {
//            mListView.smoothScrollToPosition(position);
//        }
//
//        @Override
//        protected void setListAdapter(ListAdapter adapter) {
//            mListView.setAdapter(adapter);
//        }
//
//        @Override
//        protected ListAdapter getListAdapter() {
//            return mListView.getAdapter();
//        }
//
//        @Override
//        protected ListView getListView() {
//            return mListView;
//        }
//
//        @Override
//        protected void setOnScrollListener(AbsListView.OnScrollListener listener) {
//            mListView.setOnScrollListener(listener);
//        }
//    }
//
//    private static class StickyListHeadersListViewManager extends ListViewManager {
//
//        private final StickyListHeadersListView mListView;
//
//        private StickyListHeadersListViewManager(StickyListHeadersListView listView) {
//            mListView = listView;
//        }
//
//        @Override
//        public void setOnItemClickListener(OnItemClickListener listener) {
//            mListView.setOnItemClickListener(listener);
//        }
//
//        @Override
//        public void setFastScrollEnabled(boolean enabled) {
//            mListView.setFastScrollEnabled(enabled);
//        }
//
//        @Override
//        public void smoothScrollToPosition(int position) {
//            mListView.smoothScrollToPosition(position + mListView.getHeaderViewsCount());
//        }
//
//        @Override
//        protected void setListAdapter(ListAdapter adapter) {
//            try {
//                mListView.setAdapter((StickyListHeadersAdapter) adapter);
//            } catch (ClassCastException e) {
//                throw new IllegalArgumentException(e);
//            }
//        }
//
//        @Override
//        protected ListAdapter getListAdapter() {
//            return mListView.getAdapter();
//        }
//
//        @Override
//        protected ListView getListView() {
//            return mListView.getWrappedList();
//        }
//
//        @Override
//        protected void setOnScrollListener(AbsListView.OnScrollListener listener) {
//            mListView.setOnScrollListener(listener);
//        }
//    }
}