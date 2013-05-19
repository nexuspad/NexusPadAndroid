/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import com.nexuspad.datamodel.NPEntry;

/**
 * An {@code Activity} with done/discard {@code ActionBar} enabled.
 * 
 * @author Edmond
 * 
 */
public abstract class NewEntryActivity<T extends NPEntry> extends EntryActivity<T> {
    public static final String KEY_MODE = "com.nexuspad.ui.activity.NewEntryActivity.mode";

    public enum Mode {
        NEW, EDIT
    }

    private Mode mMode = Mode.NEW;

    @Override
    protected void onCreate(Bundle savedState) {
        handleIntent(getIntent());
        super.onCreate(savedState);
    }

    @Override
    public void onNewIntent(Intent i) {
        super.onNewIntent(i);
        handleIntent(i);
    }

    private void handleIntent(Intent i) {
        Mode mode = (Mode)i.getSerializableExtra(KEY_MODE);
        if ( (mode != null) && (mMode != mode)) {
            mMode = mode;
            onNewMode(mode);
        }
    }

    protected void onNewMode(Mode mode) {
    }

    protected Mode getMode() {
        return mMode;
    }

    @Override
    protected boolean isDoneDiscardEnabled() {
        return true;
    }

    @Override
    public void onBackPressed() {
        onDonePressed();
    }
}
