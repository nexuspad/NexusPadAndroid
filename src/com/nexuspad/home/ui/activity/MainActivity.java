/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.home.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.Toast;
import com.edmondapps.utils.android.activity.SinglePaneActivity;
import com.nexuspad.R;
import com.nexuspad.datamodel.NPUser;
import com.nexuspad.home.ui.fragment.MainFragment;

/**
 * @author Edmond
 */
public class MainActivity extends SinglePaneActivity implements MainFragment.Callback {
    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        getSupportActionBar().setIcon(R.drawable.back_to_dashboard);
    }

    @Override
    protected Fragment onCreateFragment() {
        return new MainFragment();
    }

    @Override
    public void onLogin(MainFragment f, NPUser user) {
        startActivity(new Intent(this, DashboardActivity.class));
        finish();
    }

    @Override
    public void onLoginFailed(MainFragment f, String userName, String password) {
        Toast.makeText(this, R.string.err_login_failed, Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    public void onNoUserStored(MainFragment f) {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
