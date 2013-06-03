/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.nexuspad.datamodel.EntryList;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.ui.fragment.EntriesFragment;

/**
 * @author Edmond
 */
public abstract class EntriesActivity extends PaddedListActivity implements EntriesFragment.Callback {
    public static final String KEY_FOLDER = "key_folder";

    private Folder mFolder = Folder.rootFolderOf(getModule());

    /**
     * @return one of the {@code *_MODULE} constants in {@link ServiceConstants}
     */
    protected abstract int getModule();

    @Override
    protected void onCreate(Bundle savedState) {
        Intent intent = getIntent();
        Folder folder = intent.getParcelableExtra(KEY_FOLDER);
        if (folder != null) {
            mFolder = folder;
        }

        super.onCreate(savedState);
    }

    @Override
    public void onListLoaded(EntriesFragment f, EntryList list) {
        Folder folder = list.getFolder();
        if (folder.getFolderId() != Folder.ROOT_FOLDER) {
            String folderName = folder.getFolderName();
            if (!TextUtils.isEmpty(folderName)) {
                setTitle(folderName);
            }
        }
    }

    public Folder getFolder() {
        return mFolder;
    }
}
