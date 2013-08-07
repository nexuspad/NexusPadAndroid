package com.nexuspad.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * http://stackoverflow.com/questions/2607698/click-in-a-listview-item-changes-
 * status-of-elements-inside-the-item
 * 
 * @author Edmond
 * 
 */
public class DuplicateParentStateAwareFrameLayout extends FrameLayout {

    public DuplicateParentStateAwareFrameLayout(Context context) {
        super(context);
    }

    public DuplicateParentStateAwareFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DuplicateParentStateAwareFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void dispatchSetPressed(boolean pressed) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.isDuplicateParentStateEnabled()) {
                getChildAt(i).setPressed(pressed);
            }
        }
    }
}
