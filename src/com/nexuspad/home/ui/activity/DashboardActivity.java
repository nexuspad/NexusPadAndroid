/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.home.ui.activity;

import android.support.v4.app.Fragment;

import com.edmondapps.utils.android.Logs;
import com.edmondapps.utils.android.activity.SinglePaneActivity;
import com.nexuspad.home.ui.fragment.DashboardFragment;

/**
 * @author Edmond
 * 
 */
public class DashboardActivity extends SinglePaneActivity implements DashboardFragment.Callback {
    public static final String TAG = "MainPhoneActivity";

    @Override
    protected Fragment onCreateFragment() {
        return new DashboardFragment();
    }

    @Override
    public void onModuleClicked(DashboardFragment f, int moduleType) {
        Logs.v(TAG, "moduleType: " + moduleType);
    }
}
