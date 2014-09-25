package com.nexuspad.common.adapters;

import android.util.Log;
import android.widget.AbsListView;
import com.nexuspad.datamodel.NPDateRange;

/**
 * Created by ren on 8/24/14.
 */
public abstract class OnEventListEndListener implements AbsListView.OnScrollListener {
	public static final int DEFAULT_VISIBLE_THRESHOLD = 5;
	public static final int NUMBER_OF_DAYS_TO_LOAD = 30;

	private boolean mLoading = true;

	private NPDateRange mCurrentDateRange;
	private NPDateRange mNextDateRange;

	public OnEventListEndListener() {
	}

	@Override
	public void onScroll(AbsListView v, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		Log.v("SCROLL LISTENER: ", String.valueOf(mLoading) + " total:" + String.valueOf(totalItemCount)
				+ " visible:" + visibleItemCount + " first visible:" + firstVisibleItem);

		if (mLoading) {
			if (mCurrentDateRange.equals(mNextDateRange)) {
				mLoading = false;
			}
		}

		/*
		 * Based on the visible item position decide whether to trigger list bottom call.
		 */
		if (!mLoading) {
			if (totalItemCount != 0) {
				if ((totalItemCount - visibleItemCount) <= (firstVisibleItem + DEFAULT_VISIBLE_THRESHOLD)) {

				}
			}
		}
	}

	public void reset() {
		mLoading = false;
	}

	/**
	 * Called when the list has reached the end (or the threshold).
	 */
	protected abstract void onListBottom(String bottomYmd);

	protected abstract void onListTop(String topYmd);

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

}
