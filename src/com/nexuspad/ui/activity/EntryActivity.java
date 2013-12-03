/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import com.edmondapps.utils.android.activity.SinglePaneActivity;
import com.nexuspad.R;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.datamodel.NPEntry;
import com.nexuspad.ui.fragment.EntryFragment;

/**
 * @author Edmond
 */
public abstract class EntryActivity<T extends NPEntry> extends SinglePaneActivity implements EntryFragment.Callback<T> {
    public static final String KEY_ENTRY = "com.nexuspad.ui.activity.EntryActivity.entry";
    public static final String KEY_FOLDER = "com.nexuspad.ui.activity.EntryActivity.folder";

    private T mEntry;
    private Folder mFolder;

    @Override
    protected int onCreateLayoutId() {
        return R.layout.no_padding_activity;
    }

    @Override
    protected void onCreate(Bundle savedState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        handleIntent(getIntent());
        super.onCreate(savedState);

        getActionBar().setIcon(R.drawable.back_to_dashboard);
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
     * @param entry the new entry, same as {@link #getEntry()}
     */
    protected void onNewEntry(T entry) {
        setTitle(entry.getTitle());
    }

    @Override
    public void onDeleting(EntryFragment<T> f, T entry) {
        finish();
    }

    protected T getEntry() {
        return mEntry;
    }

    protected Folder getFolder() {
        return mFolder;
    }
}
