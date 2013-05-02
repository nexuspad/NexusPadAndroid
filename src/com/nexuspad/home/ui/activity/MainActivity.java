package com.nexuspad.home.ui.activity;

import com.nexuspad.ui.activity.ForwardActivity;

import android.content.Intent;

public class MainActivity extends ForwardActivity {
    @Override
    protected Intent onCreatePhoneIntent() {
        return new Intent(this, MainPhoneActivity.class);
    }

    @Override
    protected Intent onCreate7InchTabletIntent() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Intent onCreate10InchTabletIntent() {
        throw new UnsupportedOperationException();
    }
}
