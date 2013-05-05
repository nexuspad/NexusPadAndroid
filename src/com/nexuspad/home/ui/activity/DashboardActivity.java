/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.home.ui.activity;

import static com.nexuspad.dataservice.ServiceConstants.BOOKMARK_MODULE;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.edmondapps.utils.android.Logs;
import com.edmondapps.utils.android.activity.SinglePaneActivity;
import com.nexuspad.bookmark.ui.activity.BookmarksActivity;
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
        Class<? extends Activity> activity = null;
        switch (moduleType) {
            case BOOKMARK_MODULE:
                activity = BookmarksActivity.class;
                break;
            default:
                Logs.v(TAG, "moduleType: " + moduleType);
                break;
        }
        if (activity != null) {
            startActivity(new Intent(this, activity));
        }
    }
}
