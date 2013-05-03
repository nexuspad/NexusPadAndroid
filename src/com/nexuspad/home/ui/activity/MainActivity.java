/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.home.ui.activity;

import android.content.Intent;
import android.support.v4.app.Fragment;

import com.edmondapps.utils.android.Logs;
import com.edmondapps.utils.android.activity.SinglePaneActivity;
import com.nexuspad.datamodel.NPUser;
import com.nexuspad.home.ui.fragment.MainFragment;

/**
 * @author Edmond
 * 
 */
public class MainActivity extends SinglePaneActivity implements MainFragment.Callback {
    @Override
    protected Fragment onCreateFragment() {
        return new MainFragment();
    }

    @Override
    public void onUserLoggedIn(MainFragment f, NPUser user) {
        Logs.d("WelcomeActivity", "User has a session id: " + user.getSessionId());
        startActivity(new Intent(this, DashboardActivity.class));
        finish();
    }

    @Override
    public void onNoUserStored(MainFragment f) {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
