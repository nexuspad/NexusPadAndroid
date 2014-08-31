package com.nexuspad.common.adapters;

import android.util.Log;
import android.widget.AbsListView;
import com.nexuspad.util.DateUtil;

import java.util.Date;

/**
 * Created by ren on 8/24/14.
 */
public abstract class OnEventListEndListener implements AbsListView.OnScrollListener {
	public static final int DEFAULT_VISIBLE_THRESHOLD = 5;
	public static final int NUMBER_OF_DAYS_TO_LOAD = 30;

	private int mPreviousTotal = 0;
	private boolean mLoading = true;

	private String mStartYmd;
	private String mEndYmd;

	public OnEventListEndListener() {
	}

	@Override
	public void onScroll(AbsListView v, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		Log.v("SCROLL LISTENER: ", String.valueOf(mLoading) + " total:" + String.valueOf(totalItemCount)
				+ " previous total:" + String.valueOf(mPreviousTotal) + " visible:" + visibleItemCount + " first visible:" + firstVisibleItem
				);

		if (mLoading) {
			if (totalItemCount > mPreviousTotal) {
				mLoading = false;
				mPreviousTotal = totalItemCount;
			}
		}

		/*
		 * Based on the visible item position decide whether to trigger list bottom call.
		 */
		if (!mLoading && ((totalItemCount - visibleItemCount) <= (firstVisibleItem + DEFAULT_VISIBLE_THRESHOLD))) {
			Date nextStartDate = DateUtil.addDaysTo(DateUtil.parseFromYYYYMMDD(mEndYmd), NUMBER_OF_DAYS_TO_LOAD);
			onListBottom(DateUtil.convertToYYYYMMDD(nextStartDate));
			mLoading = true;
		}
	}

	public void reset() {
		mLoading = false;
	}

	/**
	 * Called when the list has reached the end (or the threshold).
	 */
	protected abstract void onListBottom(String nextStartYmd);

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

}
