package com.nexuspad.common.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.nexuspad.common.utils.Logs;

/**
 * Hacky fix for Issue #4 and
 * http://code.google.com/p/android/issues/detail?id=18990
 * <p/>
 * ScaleGestureDetector seems to mess up the touch events, which means that
 * ViewGroups which make use of onInterceptTouchEvent throw a lot of
 * IllegalArgumentException: pointerIndex out of range.
 * <p/>
 * There's not much I can do in my code for now, but we can mask the result by
 * just catching the problem and ignoring it.
 * <p/>
 * Ref: https://github.com/chrisbanes/PhotoView/blob/master/sample/src/uk/co/senab/photoview/sample/HackyViewPager.java
 *
 * @author Chris Banes
 */
public class HackyViewPager extends ViewPager {
    public static final String TAG = "HackyViewPager";

    public HackyViewPager(Context context) {
        super(context);
    }

    public HackyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            Logs.e(TAG, e);
            return false;
        } catch (ArrayIndexOutOfBoundsException e) {
            Logs.e(TAG, e);
            return false;
        }
    }
}