package com.nexuspad.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.nexuspad.BuildConfig;
import com.nexuspad.R;

import static com.edmondapps.utils.android.Utils.hasJellyBean;
import static com.edmondapps.utils.android.view.ViewUtils.findView;

/**
 * Author: edmond
 */
public class UploadBarView extends FrameLayout {

    public UploadBarView(Context context) {
        this(context, null, 0);
    }

    public UploadBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UploadBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //noinspection PointlessBooleanExpression,ConstantConditions
        if (isInEditMode() || BuildConfig.DEBUG) {
            inflateIfNeeded();
            setDownloadCount(0);
        }
        setClickable(true);
        setFocusable(true);

        final TypedArray array = context.obtainStyledAttributes(new int[]{R.attr.selectableItemBackground});
        final Drawable background = array.getDrawable(0);
        array.recycle();
        setBackgroundCompat(background);
    }

    @SuppressWarnings("deprecation")
    private void setBackgroundCompat(Drawable drawable) {
        if (hasJellyBean()) {
            setBackground(drawable);
        } else {
            //noinspection deprecation
            setBackgroundDrawable(drawable);
        }
    }

    private boolean mInflated;
    private TextView mUploadTextV;
    private TextView mDownloadTextV;

    private void inflateIfNeeded() {
        if (!mInflated) {
            final View view = inflate(getContext(), R.layout.include_upload_bar, this);
            mUploadTextV = findView(view, R.id.lbl_upload);
            mDownloadTextV = findView(view, R.id.lbl_download);
            mInflated = true;
        }
    }

    public void setUploadCount(int count) {
        setCount(count, mUploadTextV);
    }

    public void setDownloadCount(int count) {
        setCount(count, mDownloadTextV);
    }

    private void setCount(int count, TextView view) {
        inflateIfNeeded();
        if (count > 0) {
            view.setVisibility(VISIBLE);
            view.setText(String.valueOf(count));
        } else {
            view.setVisibility(GONE);
        }
    }
}
