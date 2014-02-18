/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.about.activity;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Window;
import com.edmondapps.utils.android.activity.SinglePaneActivity;
import com.edmondapps.utils.android.annotaion.ParentActivity;
import com.nexuspad.R;
import com.nexuspad.about.fragment.AboutFragment;
import com.nexuspad.home.ui.activity.DashboardActivity;

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
