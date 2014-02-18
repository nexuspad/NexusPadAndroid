/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.home.ui.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.edmondapps.utils.android.activity.SinglePaneActivity;
import com.nexuspad.R;
import com.nexuspad.datamodel.NPUser;
import com.nexuspad.home.ui.fragment.LoginFragment;

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
