package com.nexuspad.view;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;
import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;

import com.edmondapps.utils.android.view.RunnableAnimatorListener;
import com.nineoldandroids.view.ViewHelper;

/**
 * 
 * @author Edmond
 * 
 */
public class OnSwipeUpListener implements OnTouchListener {
    private static final int MIN_SWIPE_VELOCITY = 100;
    private static final int MIN_SWIPE_DISTANCE = 100;
    private static final long SWIPE_DURATION = 150L;

    private final VelocityTracker mVelocityTracker = VelocityTracker.obtain();
    private final int mMinSwipeDistance;
    private final int mTouchSlop;

    private float mDownX;
    private float mDownY;
    private boolean mIsDown;
    private boolean mIsSwiping;

    /**
     * Uses the the default minimum swipe distance.
     */
    public OnSwipeUpListener(Context c) {
        this(c, -1);
    }

    /**
     * Uses the provided minimum swipe distance. A negative number indicates the
     * use of the default minimum swipe distance.
     */
    public OnSwipeUpListener(Context c, int minSwipeDistance) {
        mMinSwipeDistance = minSwipeDistance;
        mTouchSlop = ViewConfiguration.get(c).getScaledTouchSlop();
    }

    /**
     * Determines if the touch distance is far enough to consider "swiping".
     */
    private boolean isSwipeFarEnough(View view, float distance) {
        if (mMinSwipeDistance >= 0) {
            return distance > mMinSwipeDistance;
        } else {
            return distance > MIN_SWIPE_DISTANCE;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent ev) {
        switch (MotionEventCompat.getActionMasked(ev)) {
            case MotionEvent.ACTION_DOWN: {
                if (mIsDown) {
                    return false;
                }
                mIsDown = true;
                mVelocityTracker.addMovement(ev);

                mDownX = ev.getX();
                mDownY = ev.getY();

                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                mVelocityTracker.addMovement(ev);

                float deltaX = ev.getX() - mDownX;
                float deltaXAbs = Math.abs(deltaX);

                float deltaY = ev.getY() - mDownY;
                float deltaYAbs = Math.abs(deltaY);

                float alpha = 1 - (deltaYAbs / view.getHeight());

                if (!mIsSwiping) {
                    if ( (deltaYAbs > deltaXAbs) && (deltaYAbs > mTouchSlop)) {
                        mIsSwiping = true;
                        onSwipingVertically(view, ev);
                    }
                }
                if (mIsSwiping) {
                    // setAlpha on ImageView is super buggy, use parent instead
                    View parent = (View)view.getParent();
                    ViewHelper.setTranslationY(parent, deltaY);
                    ViewHelper.setAlpha(parent, alpha);
                }

                return true;
            }
            case MotionEvent.ACTION_UP: {
                mIsDown = false;
                mVelocityTracker.addMovement(ev);
                if (mIsSwiping) {
                    mIsSwiping = false;

                    mVelocityTracker.computeCurrentVelocity(1000);
                    float yVelocity = mVelocityTracker.getYVelocity();

                    float deltaY = ev.getY() - mDownY;
                    float deltaYAbs = Math.abs(yVelocity);

                    if ( (deltaYAbs > MIN_SWIPE_VELOCITY) && isSwipeFarEnough(view, deltaYAbs)) {
                        swipeAway(view, yVelocity, deltaY);
                    } else {
                        restore(view);
                    }
                }
                mVelocityTracker.clear();
                return true;
            }
            case MotionEvent.ACTION_CANCEL: {
                mIsDown = false;
                mIsSwiping = false;
                restore(view);
                return true;
            }
        }
        return false;
    }

    /**
     * Called when a swipe motion is detected.
     * 
     * @see #onTouch(View, MotionEvent)
     */
    protected void onSwipingVertically(View view, MotionEvent ev) {
    }

    protected void restore(View view) {
        animate((View)view.getParent()).setDuration(SWIPE_DURATION).translationY(0).alpha(1);
    }

    protected void swipeAway(final View view, float velocity, float deltaY) {
        final View parent = (View)view.getParent();
        final int height = parent.getHeight();
        final float endY = velocity > 0 ? height : -height;
        final long duration = (long) ( (1 - (Math.abs(deltaY) / height)) * SWIPE_DURATION);

        animate(parent)
                .setDuration(duration)
                .alpha(0)
                .translationY(endY)
                .setListener(new RunnableAnimatorListener(true).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        parent.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                ViewHelper.setAlpha(parent, 1);
                                ViewHelper.setTranslationY(parent, 0);
                            }
                        }, 2000);
                    }
                }));
    }
}
