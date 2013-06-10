/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui.activity;

import com.edmondapps.utils.android.activity.SinglePaneActivity;
import com.nexuspad.R;

/**
 * @author Edmond
 * 
 */
public abstract class PaddedListActivity extends SinglePaneActivity {
    @Override
    protected int onCreateLayoutId() {
        return R.layout.no_padding_activity;
    }
}
