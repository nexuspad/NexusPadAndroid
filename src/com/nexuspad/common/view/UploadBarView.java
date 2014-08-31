package com.nexuspad.common.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.nexuspad.R;
import com.nexuspad.app.service.UploadOperationUIHelper;
import com.nexuspad.common.activity.UploadCenterActivity;

import static com.nexuspad.app.service.UploadOperationUIHelper.OnUploadCountChangeListener;

/**
 * Author: edmond
 */
public class UploadBarView extends FrameLayout {

    /**
     * Maintain a reference since {@link com.nexuspad.app.service.UploadOperationUIHelper#addOnUploadCountChangeListener(OnUploadCountChangeListener) addOnUploadCountChangeListener}
     * holds a {@code WeakReference} to the listener.<p>
     * Anonymous inner class will cause the listener to be
     * garbage collected, thus a field reference is needed. (The listener holds a reference to the {@code UploadBarView},
     * leaking memory if it is strongly referenced in {@code UploadService}.
     */
    private OnUploadCountChangeListener mListener = new OnUploadCountChangeListener() {
        @Override
        public void onUploadCountChanged(int uploadCount) {
            setUploadCount(uploadCount);
        }
    };

    public UploadBarView(Context context) {
        this(context, null, 0);
    }

    public UploadBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UploadBarView(final Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setClickable(true);
        setFocusable(true);

        final TypedArray array = context.obtainStyledAttributes(new int[]{android.R.attr.selectableItemBackground});
        final Drawable background = array.getDrawable(0);
        array.recycle();
	    setBackground(background);

        UploadOperationUIHelper.addOnUploadCountChangeListener(mListener);

        super.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(context, UploadCenterActivity.class);
                context.startActivity(intent);
            }
        });
    }

    @Override
    @Deprecated
    public void setOnClickListener(OnClickListener l) {
        throw new UnsupportedOperationException("my monopoly");
    }

    private boolean mInflated;
    private TextView mUploadTextV;
    private TextView mDownloadTextV;

    private void inflateIfNeeded() {
        if (!mInflated) {
            final View view = inflate(getContext(), R.layout.include_upload_bar, this);
            mUploadTextV = (TextView)view.findViewById(R.id.lbl_upload);
            mDownloadTextV = (TextView)view.findViewById(R.id.lbl_download);

            mUploadTextV.setVisibility(View.GONE);
            mDownloadTextV.setVisibility(View.GONE);

            mInflated = true;
        }
    }

    public void setUploadCount(int count) {
        inflateIfNeeded();
        setCount(count, mUploadTextV);
    }

    public void setDownloadCount(int count) {
        inflateIfNeeded();
        setCount(count, mDownloadTextV);
    }

    private void setCount(int count, TextView view) {
        if (count > 0) {
            view.setVisibility(VISIBLE);
            view.setText(String.valueOf(count));
        } else {
            view.setVisibility(GONE);
        }
    }
}
