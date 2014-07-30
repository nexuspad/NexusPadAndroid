package com.nexuspad.common.view;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.AttributeSet;

public class EndlessViewPager extends HackyViewPager {
	private EndlessAdapter mAdapter;

	public EndlessViewPager(Context context) {
		super(context);
	}

	public EndlessViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void setAdapter(PagerAdapter adapter) {
		if (adapter instanceof EndlessAdapter) {
			mAdapter = (EndlessAdapter) adapter;
		} else {
			mAdapter = new EndlessAdapter(adapter);
		}
		super.setAdapter(mAdapter);
	}

	@Override
	public void setCurrentItem(int item) {
		super.setCurrentItem(getPositionForAndroid(item));
	}

	@Override
	public void setCurrentItem(int item, boolean smoothScroll) {
		super.setCurrentItem(getPositionForAndroid(item), smoothScroll);
	}

	private int getPositionForAndroid(int position) {
		return mAdapter.getRealCount() * 100 + mAdapter.getRealPosition(position);
	}

	@Override
	public EndlessAdapter getAdapter() {
		return (EndlessAdapter) super.getAdapter();
	}

}
