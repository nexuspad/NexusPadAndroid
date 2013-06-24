/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.about.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.view.Window;
import com.edmondapps.utils.android.activity.SinglePaneActivity;
import com.edmondapps.utils.android.annotaion.ParentActivity;
import com.nexuspad.R;
import com.nexuspad.about.fragment.AboutFragment;
import com.nexuspad.home.ui.activity.DashboardActivity;

/**
 * @author Edmond
 * 
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
        getSupportActionBar().hide();
        super.onCreate(savedState);
    }

    @Override
    protected Fragment onCreateFragment() {
        return new AboutFragment();
    }
}
