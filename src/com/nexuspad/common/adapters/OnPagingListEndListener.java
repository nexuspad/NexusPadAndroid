/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.common.adapters;

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

	private int mCurrentPage = 0;
	private int mPreviousTotal = 0;
	private boolean mLoading = true;

	public OnPagingListEndListener() {
	}

	@Override
	public void onScroll(AbsListView v, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		Log.v("SCROLL LISTENER: ", String.valueOf(mLoading) + " total:" + String.valueOf(totalItemCount)
				+ " previous total:" + String.valueOf(mPreviousTotal) + " visible:" + visibleItemCount + " first visible:" + firstVisibleItem
				+ " current page:" + mCurrentPage);

		if (mLoading) {
			if (totalItemCount > mPreviousTotal) {
				mLoading = false;
				mPreviousTotal = totalItemCount;
				mCurrentPage++;
			}
		}

		if (!mLoading && ((totalItemCount - visibleItemCount) <= (firstVisibleItem + DEFAULT_VISIBLE_THRESHOLD))) {
			onListBottom(mCurrentPage + 1);
			mLoading = true;
		}
	}

	public void reset() {
		mLoading = false;
	}

	/**
	 * Called when the list has reached the end (or the threshold).
	 */
	protected abstract void onListBottom(int page);

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}
}
