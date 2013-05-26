/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import com.edmondapps.utils.android.activity.DoneDiscardActivity;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.datamodel.NPEntry;
import com.nexuspad.dataservice.ServiceConstants;

/**
 * An {@code Activity} with done/discard {@code ActionBar} enabled.
 * 
 * @author Edmond
 * 
 */
public abstract class NewEntryActivity<T extends NPEntry> extends DoneDiscardActivity {
    public static final String KEY_MODE = "com.nexuspad.ui.activity.NewEntryActivity.mode";
    public static final String KEY_ENTRY = "com.nexuspad.ui.activity.NewEntryActivity.entry";
    public static final String KEY_FOLDER = "com.nexuspad.ui.activity.NewEntryActivity.folder";

    public enum Mode {
        NEW, EDIT
    }

    private Mode mMode = Mode.NEW;
    private T mEntry;
    private Folder mFolder;

    /**
     * @return one of the {@code *_MODULE} constants in {@link ServiceConstants}
     */
    protected abstract int getModule();

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

    protected void handleIntent(Intent i) {
        Mode mode = (Mode)i.getSerializableExtra(KEY_MODE);
        if ( (mode != null) && (mMode != mode)) {
            mMode = mode;
            onNewMode(mode);
        }
        mEntry = i.getParcelableExtra(KEY_ENTRY);
        mFolder = i.getParcelableExtra(KEY_FOLDER);
        if (mFolder == null) {
            mFolder = Folder.initReservedFolder(getModule(), Folder.ROOT_FOLDER);
        }
    }

    protected void onNewMode(Mode mode) {
    }

    protected void setEntry(T entry) {
        mEntry = entry;
    }

    protected T getEntry() {
        return mEntry;
    }

    protected Mode getMode() {
        return mMode;
    }

    protected Folder getFolder() {
        return mFolder;
    }
}
