package com.nexuspad.ui.view;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

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

    private int getOffsetAmount() {
        // somewhere in the middle so both left and right can be scrolled to; must be a multiple of count
        return mAdapter.getTrueCount() * 100;
    }

    private int getPositionForAndroid(int position) {
        return getOffsetAmount() + mAdapter.getTruePosition(position);
    }

    private static class EndlessAdapter extends PagerAdapter {
        private final PagerAdapter mAdapter;

        private EndlessAdapter(PagerAdapter adapter) {
            mAdapter = adapter;
        }

        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        private int getTrueCount() {
            return mAdapter.getCount();
        }

        private int getTruePosition(int position) {
            return position % getTrueCount();
        }

        @Override
        public void startUpdate(ViewGroup container) {
            mAdapter.startUpdate(container);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            return mAdapter.instantiateItem(container, getTruePosition(position));
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            mAdapter.destroyItem(container, getTruePosition(position), object);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            mAdapter.setPrimaryItem(container, getTruePosition(position), object);
        }

        @Override
        public void finishUpdate(ViewGroup container) {
            mAdapter.finishUpdate(container);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return mAdapter.isViewFromObject(view, object);
        }

        @Override
        public Parcelable saveState() {
            return mAdapter.saveState();
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
            mAdapter.restoreState(state, loader);
        }

        @Override
        public int getItemPosition(Object object) {
            return mAdapter.getItemPosition(object);
        }

        @Override
        public void notifyDataSetChanged() {
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {
            mAdapter.registerDataSetObserver(observer);
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            mAdapter.unregisterDataSetObserver(observer);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mAdapter.getPageTitle(getTruePosition(position));
        }

        @Override
        public float getPageWidth(int position) {
            return mAdapter.getPageWidth(getTruePosition(position));
        }
    }
}
