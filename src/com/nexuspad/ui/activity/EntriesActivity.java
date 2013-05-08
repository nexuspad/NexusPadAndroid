/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.nexuspad.datamodel.EntryList;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.ui.fragment.EntriesFragment;

/**
 * @author Edmond
 * 
 */
public abstract class EntriesActivity extends PaddedListActivity implements EntriesFragment.Callback {
    public static final String KEY_FOLDER = "key_folder";
    public static final String KEY_PARENT = "key_parent";

    private Folder mFolder;
    private Class<?> mParent;

    public Folder getFolder() {
        return mFolder;
    }

    @Override
    protected void onCreate(Bundle savedState) {
        Intent intent = getIntent();
        mFolder = intent.getParcelableExtra(KEY_FOLDER);
        mParent = (Class<?>)intent.getSerializableExtra(KEY_PARENT);

        super.onCreate(savedState);
    }

    @Override
    public void onListLoaded(EntriesFragment f, EntryList list) {
        String folderName = list.getFolder().getFolderName();
        if (!TextUtils.isEmpty(folderName)) {
            setTitle(folderName);
        }
    }

    @Override
    protected Intent getUpIntent(Class<? extends Activity> activity) {
        return super.getUpIntent(activity).setClass(this, mParent == null ? activity : mParent);
    }
}
