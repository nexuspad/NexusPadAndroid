package com.nexuspad.ui;

import android.widget.AbsListView;

import static android.widget.AbsListView.OnScrollListener;

/**
 * Author: Edmond
 */
public abstract class DirectionalScrollListener implements OnScrollListener {
    private static final int DEFAULT_OFFSET = 2;

    private final int mOffset;
    private final OnScrollListener mOtherOnScrollListener;

    private int mLastFirstVisibleItem;
    private boolean mIsScrollingUp = true;

    public DirectionalScrollListener() {
        this(DEFAULT_OFFSET);
    }

    public DirectionalScrollListener(int offset) {
        this(offset, null);
    }

    /**
     * Creates a {@link DirectionalScrollListener} with the given offset.
     * @param offset the minimum number of items that has to scroll before a direction change is considered
     * @param other the other listener (since {@link AbsListView#setOnScrollListener(OnScrollListener)} only accepts one listener)
     */
    public DirectionalScrollListener(int offset, OnScrollListener other) {
        mOffset = offset;
        mOtherOnScrollListener = other;
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (mOtherOnScrollListener != null) {
            mOtherOnScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (mOtherOnScrollListener != null) {
            mOtherOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
        final int offset = Math.abs(mLastFirstVisibleItem - firstVisibleItem);
        if (offset > mOffset) {
            boolean isScrollingUP = mIsScrollingUp;
            if (firstVisibleItem > mLastFirstVisibleItem) {
                isScrollingUP = false;
            } else if (firstVisibleItem < mLastFirstVisibleItem) {
                isScrollingUP = true;
            }
            mLastFirstVisibleItem = firstVisibleItem;
            if (mIsScrollingUp != isScrollingUP) {
                mIsScrollingUp = isScrollingUP;
                onScrollDirectionChanged(mIsScrollingUp);
            }
        }
    }

    public abstract void onScrollDirectionChanged(boolean isScrollingUp);
}
