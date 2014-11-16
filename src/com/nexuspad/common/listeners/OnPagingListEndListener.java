/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.common.listeners;

import android.util.Log;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

/**
 * Credit: http://benjii.me/2010/08/endless-scrolling-listview-in-android/
 *
 * @author Edmond
 */
public abstract class OnPagingListEndListener implements OnScrollListener {
	public static final int DEFAULT_VISIBLE_THRESHOLD = 5;

	private int mCurrentPage = 1;       // Always the first page to start with
	private int mNextPage = 0;

	private boolean mLoading = false;

	public OnPagingListEndListener() {
		mLoading = false;
	}

	@Override
	public void onScroll(AbsListView v, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		Log.v("SCROLL LISTENER: ", String.valueOf(mLoading) + " total:" + String.valueOf(totalItemCount)
				+ " visible:" + visibleItemCount + " first visible:" + firstVisibleItem
				+ " current page:" + mCurrentPage);

		if (mLoading) {
			if (mCurrentPage == mNextPage) {
				mLoading = false;
			}
		}

		if (!mLoading) {
			if (totalItemCount != 0) {
				if ((totalItemCount - visibleItemCount) <= (firstVisibleItem + DEFAULT_VISIBLE_THRESHOLD)) {
					mNextPage = mCurrentPage + 1;
					onListBottom(mNextPage);
					mLoading = true;
				}
			}
		}
	}

	public void reset() {
		mLoading = false;
	}

	public void setCurrentPage(int page) {
		mCurrentPage = page;
	}

	/**
	 * Called when the list has reached the end (or the threshold).
	 */
	protected abstract void onListBottom(int page);

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}
}
