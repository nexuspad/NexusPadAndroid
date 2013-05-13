/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import com.edmondapps.utils.android.activity.DoneDiscardActivity;
import com.nexuspad.datamodel.NPEntry;

/**
 * @author Edmond
 * 
 */
public abstract class EntryActivity<T extends NPEntry> extends DoneDiscardActivity {
    public static final String KEY_ENTRY = "com.nexuspad.ui.activity.EntryActivity.entry";

    private T mEntry;

    @Override
    protected boolean isDoneDiscardEnabled() {
        return false;
    }

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
        T entry = i.getParcelableExtra(KEY_ENTRY);
        if (mEntry != entry) {
            mEntry = entry;
            onNewEntry(entry);
        }
    }

    /**
     * Called when the entry has changed, usually a result of
     * {@link #onCreate(Bundle)} or {@link #onNewIntent(Intent)}.
     * 
     * @param entry
     *            the new entry, same as {@link #getEntry()}
     */
    protected void onNewEntry(T entry) {
    }

    protected T getEntry() {
        return mEntry;
    }
}
