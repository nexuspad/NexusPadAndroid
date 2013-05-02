package com.nexuspad.ui.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;
import com.nexuspad.R;

public abstract class ForwardActivity extends SherlockActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Resources res = getResources();

        Intent intent;
        // order is important,
        if (res.getBoolean(R.bool.ed__has_10_inch)) {
            intent = onCreate10InchTabletIntent();
        } else if (res.getBoolean(R.bool.ed__has_7_inch)) {
            intent = onCreate7InchTabletIntent();
        } else {
            intent = onCreatePhoneIntent();
        }
        startActivity(intent);
        finish();
    }

    protected abstract Intent onCreatePhoneIntent();

    protected abstract Intent onCreate7InchTabletIntent();

    protected abstract Intent onCreate10InchTabletIntent();
}
