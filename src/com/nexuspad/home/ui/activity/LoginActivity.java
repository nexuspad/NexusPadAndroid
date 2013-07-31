/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.home.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.actionbarsherlock.app.ActionBar;
import com.edmondapps.utils.android.activity.SinglePaneActivity;
import com.nexuspad.R;
import com.nexuspad.datamodel.NPUser;
import com.nexuspad.home.ui.fragment.LoginFragment;

/**
 * @author Edmond
 */
public class LoginActivity extends SinglePaneActivity implements LoginFragment.Callback {
    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setIcon(R.drawable.back_to_dashboard);
        }
    }

    @Override
    protected Fragment onCreateFragment() {
        return new LoginFragment();
    }

    @Override
    public void onLogin(NPUser user) {
        startActivity(new Intent(this, DashboardActivity.class));
        finish();
    }
}
