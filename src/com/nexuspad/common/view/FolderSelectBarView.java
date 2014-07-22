package com.nexuspad.common.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.nexuspad.R;

/**
 * Author: edmond
 */
public class FolderSelectBarView extends FrameLayout {

    public FolderSelectBarView(Context context) {
        this(context, null, 0);
    }

    public FolderSelectBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FolderSelectBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        final View view = inflate(getContext(), R.layout.include_folder_selector, this);
        final TextView textView = (TextView)view.findViewById(R.id.lbl_folder);
        textView.setTextColor(Color.WHITE);
    }
}
