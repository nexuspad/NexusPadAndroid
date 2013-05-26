/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.edmondapps.utils.android.activity.DoneDiscardActivity;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.dataservice.FolderService;
import com.nexuspad.ui.fragment.NewFolderFragment;

/**
 * @author Edmond
 * 
 */
public class NewFolderActivity extends DoneDiscardActivity {
    public static final String KEY_FOLDER = "key_folder";

    /**
     * Start a new {@link NewFolderActivity} with the default parent folder
     * {@code f}
     */
    public static void startWithParentFolder(Folder f, Context c) {
        Intent intent = new Intent(c, NewFolderActivity.class);
        intent.putExtra(KEY_FOLDER, f);
        c.startActivity(intent);
    }

    private final FolderService mFolderService = new FolderService(this, null);

    private Folder mParentFolder;

    @Override
    protected void onCreate(Bundle savedState) {
        mParentFolder = getIntent().getParcelableExtra(KEY_FOLDER);
        super.onCreate(savedState);
    }

    @Override
    protected Fragment onCreateFragment() {
        return NewFolderFragment.of(mParentFolder);
    }

    @Override
    protected NewFolderFragment getFragment() {
        return (NewFolderFragment)super.getFragment();
    }

    @Override
    protected void onDonePressed() {
        NewFolderFragment fragment = getFragment();
        if (fragment.isEditedFolderValid()) {
            mFolderService.updateFolder(fragment.getEditedFolder());
            finish();
        }
    }
}
