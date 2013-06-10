/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.EntryList;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.ui.fragment.EntriesFragment;

/**
 * @author Edmond
 */
public abstract class EntriesActivity extends PaddedListActivity implements EntriesFragment.Callback {
    public static final String KEY_FOLDER = "key_folder";

    private Folder mFolder;

    private ModuleId mModuleId;

    /**
     * @return one of the {@code *_MODULE} constants in {@link ServiceConstants}
     */
    protected int getModule() {
        if (mModuleId == null) {
            throw new IllegalStateException("You must annotate the class with ModuleId, or override this method.");
        }
        return mModuleId.moduleId();
    }

    @Override
    protected void onCreate(Bundle savedState) {
        mModuleId = getClass().getAnnotation(ModuleId.class);

        Intent intent = getIntent();
        Folder folder = intent.getParcelableExtra(KEY_FOLDER);
        if (folder != null) {
            mFolder = folder;
        } else {
            mFolder = Folder.rootFolderOf(getModule());
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
