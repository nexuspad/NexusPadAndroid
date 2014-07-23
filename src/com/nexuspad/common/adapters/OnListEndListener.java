/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.common.adapters;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

/**
 * Credit: http://benjii.me/2010/08/endless-scrolling-listview-in-android/
 *
 * @author Edmond
 */
public abstract class OnListEndListener implements OnScrollListener {
    public static final int DEFAULT_VISIBLE_THRESHOLD = 5;

    private final int mVisibleThreshold;

    private int mCurrentPage = 0;
    private int mPreviousTotal = 0;
    private boolean mLoading = true;

    /**
     * Constructs a new {@link OnListEndListener} with the
     * {@link #DEFAULT_VISIBLE_THRESHOLD}.
     */
    public OnListEndListener() {
        mVisibleThreshold = DEFAULT_VISIBLE_THRESHOLD;
    }

    /**
     * Constructs a new {@link OnListEndListener} with the
     * given visible threshold.
     */
    public OnListEndListener(int visibleThreshold) {
        mVisibleThreshold = visibleThreshold;
    }

    @Override
    public void onScroll(AbsListView v, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//	    Log.i("SCROLL LISTENER: ", String.valueOf(mLoading) + " total:" + String.valueOf(totalItemCount)
//			    + " previous total:" + String.valueOf(mPreviousTotal) + " visible:" + visibleItemCount + " first visible:" + firstVisibleItem
//			    + " current page:" + mCurrentPage);

        if (mLoading) {
            if (totalItemCount > mPreviousTotal) {
                mLoading = false;
                mPreviousTotal = totalItemCount;
                mCurrentPage++;
            }
        }

	    if (!mLoading && ((totalItemCount - visibleItemCount) <= (firstVisibleItem + mVisibleThreshold))) {
            onListEnd(mCurrentPage + 1);
            mLoading = true;
        }
    }

	public void reset() {
		mLoading = false;
	}

    /**
     * Called when the list has reached the end (or the threshold).
     */
    protected abstract void onListEnd(int page);

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }
}
