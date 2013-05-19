/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import com.edmondapps.utils.android.activity.DoneDiscardActivity;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.datamodel.NPEntry;

/**
 * @author Edmond
 * 
 */
public abstract class EntryActivity<T extends NPEntry> extends DoneDiscardActivity {
    public static final String KEY_ENTRY = "com.nexuspad.ui.activity.EntryActivity.entry";
    public static final String KEY_FOLDER = "com.nexuspad.ui.activity.EntryActivity.folder";

    private T mEntry;
    private Folder mFolder;

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
        mFolder = i.getParcelableExtra(KEY_FOLDER);
        if (mFolder == null) {
            throw new IllegalArgumentException("you must pass in a Folder with KEY_FOLDER");
        }

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
        setTitle(entry.getTitle());
    }

    protected T getEntry() {
        return mEntry;
    }

    protected Folder getFolder() {
        return mFolder;
    }
}
