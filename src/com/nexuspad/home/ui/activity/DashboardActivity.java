/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.home.ui.activity;

import static com.nexuspad.dataservice.ServiceConstants.BOOKMARK_MODULE;
import static com.nexuspad.dataservice.ServiceConstants.DOC_MODULE;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.edmondapps.utils.android.Logs;
import com.nexuspad.bookmark.ui.activity.BookmarksActivity;
import com.nexuspad.doc.ui.activity.DocsActivity;
import com.nexuspad.home.ui.fragment.DashboardFragment;
import com.nexuspad.ui.activity.PaddedListActivity;

/**
 * @author Edmond
 * 
 */
public class DashboardActivity extends PaddedListActivity implements DashboardFragment.Callback {
    public static final String TAG = "MainPhoneActivity";

    @Override
    protected Fragment onCreateFragment() {
        return new DashboardFragment();
    }

    @Override
    public void onModuleClicked(DashboardFragment f, int moduleType) {
        Class<?> activity = getActivityForModule(moduleType);
        if (activity != null) {
            startActivity(new Intent(this, activity));
        }
    }

    private static Class<?> getActivityForModule(int moduleType) {
        switch (moduleType) {
            case BOOKMARK_MODULE:
                return BookmarksActivity.class;
            case DOC_MODULE:
                return DocsActivity.class;
            default:
                Logs.v(TAG, "moduleType: " + moduleType);
                return null;
        }
    }
}
