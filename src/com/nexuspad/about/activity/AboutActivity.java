/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.about.activity;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Window;
import com.nexuspad.common.activity.SinglePaneActivity;
import com.nexuspad.common.annotaion.ParentActivity;
import com.nexuspad.R;
import com.nexuspad.about.fragment.AboutFragment;
import com.nexuspad.home.activity.DashboardActivity;

/**
 * @author Edmond
 */
@ParentActivity(DashboardActivity.class)
public class AboutActivity extends SinglePaneActivity {
    @Override
    protected int onCreateLayoutId() {
        return R.layout.no_padding_activity;
    }

    @Override
    protected void onCreate(Bundle savedState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedState);

        final ActionBar actionBar = getActionBar();
        actionBar.hide();
    }

    @Override
    protected Fragment onCreateFragment() {
        return new AboutFragment();
    }
}
