/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.home.activity;

import android.content.Intent;
import android.support.v4.app.Fragment;
import com.nexuspad.common.activity.SinglePaneActivity;
import com.nexuspad.service.datamodel.NPUser;
import com.nexuspad.home.fragment.LoginFragment;

/**
 * @author Edmond
 */
public class LoginActivity extends SinglePaneActivity implements LoginFragment.Callback {

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
