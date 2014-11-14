package com.nexuspad.common.adapters;

import android.util.Log;
import android.widget.AbsListView;
import com.nexuspad.datamodel.NPDateRange;
import com.nexuspad.util.DateUtil;

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
		Log.v("EVENT SCROLL LISTENER: ", String.valueOf(mLoading) + " total:" + String.valueOf(totalItemCount)
				+ " visible:" + visibleItemCount + " first visible:" + firstVisibleItem);

		if (mLoading) {
			if (mCurrentDateRange != null && mCurrentDateRange.equals(mNextDateRange)) {
				mLoading = false;
			}
		}

		/*
		 * Based on the visible item position decide whether to trigger list bottom call.
		 */
		if (!mLoading) {
			if (totalItemCount != 0) {
				if ((totalItemCount - visibleItemCount) <= (firstVisibleItem + DEFAULT_VISIBLE_THRESHOLD)) {
					extendEndDate();
					Log.i("LOAD MORE EVENTS: ", "New date range: " + mNextDateRange.toString());
					mLoading = true;
					onListBottom(mNextDateRange);
				}
			}
		}
	}

	public void reset() {
		mLoading = false;
		if (mNextDateRange != null) {
			// Reset after "loading more"
			mCurrentDateRange = mNextDateRange;
		} else {
			// Reset after first load
			mNextDateRange = mCurrentDateRange;
		}
	}

	public void setCurrentDateRange(NPDateRange dateRange) {
		mCurrentDateRange = dateRange;
	}

	/**
	 * Called when the list has reached the end (or the threshold).
	 */
	protected abstract void onListBottom(NPDateRange dateRange);

	protected abstract void onListTop(NPDateRange dateRange);

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	private NPDateRange extendEndDate() {
		String endYmd = DateUtil.convertToYYYYMMDD(DateUtil.addDaysTo(DateUtil.parseFromYYYYMMDD(mCurrentDateRange.getEndYmd()), 30));
		mNextDateRange = new NPDateRange(mCurrentDateRange.getStartYmd(), endYmd);
		return mNextDateRange;
	}
}
