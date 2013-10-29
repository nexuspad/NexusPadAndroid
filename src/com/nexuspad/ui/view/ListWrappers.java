package com.nexuspad.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.widget.ListView;
import com.nexuspad.R;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Author: Edmond
 */
public class ListWrappers {
    private ListWrappers() {
        throw new AssertionError("nice try");
    }

    public static final class StickyWrapper extends StickyListHeadersListView {
        public StickyWrapper(Context context) {
            this(context, null);
        }

        public StickyWrapper(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        // fixes the black on black fast scroll indicator color on pre-3.0 devices
        public StickyWrapper(Context context, AttributeSet attrs, int defStyle) {
            super(new ContextThemeWrapper(context, R.style.fastscrollThemedListView), attrs, defStyle);
        }
    }

    public static final class StockWrapper extends ListView {
        public StockWrapper(Context context) {
            this(context, null);
        }

        public StockWrapper(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        // fixes the black on black fast scroll indicator color on pre-3.0 devices
        public StockWrapper(Context context, AttributeSet attrs, int defStyle) {
            super(new ContextThemeWrapper(context, R.style.fastscrollThemedListView), attrs, defStyle);
        }
    }
}
