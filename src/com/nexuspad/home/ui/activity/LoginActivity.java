/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.home.ui.activity;

import android.support.v4.app.Fragment;

import com.edmondapps.utils.android.activity.SinglePaneActivity;
import com.nexuspad.home.ui.fragment.LoginFragment;

/**
 * @author Edmond
 * 
 */
public class LoginActivity extends SinglePaneActivity {
    @Override
    protected Fragment onCreateFragment() {
        return new LoginFragment();
    }
}
