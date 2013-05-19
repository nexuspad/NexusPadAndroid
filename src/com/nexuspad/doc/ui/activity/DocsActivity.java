/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.doc.ui.activity;

import android.support.v4.app.Fragment;

import com.edmondapps.utils.android.annotaion.ParentActivity;
import com.nexuspad.doc.ui.fragment.DocsFragment;
import com.nexuspad.home.ui.activity.DashboardActivity;
import com.nexuspad.ui.activity.EntriesActivity;

/**
 * @author Edmond
 * 
 */
@ParentActivity(DashboardActivity.class)
public class DocsActivity extends EntriesActivity {
    @Override
    protected Fragment onCreateFragment() {
        return new DocsFragment();
    }
}
