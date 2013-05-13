/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui.activity;

import com.nexuspad.datamodel.NPEntry;

/**
 * An {@code Activity} with done/discard {@code ActionBar} enabled.
 * 
 * @author Edmond
 * 
 */
public abstract class NewEntryActivity<T extends NPEntry> extends EntryActivity<T> {

    @Override
    protected boolean isDoneDiscardEnabled() {
        return true;
    }

    @Override
    public void onBackPressed() {
        onDonePressed();
    }
}
