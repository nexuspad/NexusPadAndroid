/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.edmondapps.utils.android.activity.DoneDiscardActivity;
import com.nexuspad.R;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.dataservice.FolderService;
import com.nexuspad.ui.fragment.NewFolderFragment;

/**
 * @author Edmond
 * 
 */
public class NewFolderActivity extends DoneDiscardActivity {
    public static final String KEY_FOLDER = "key_folder";
    public static final String KEY_ORIGINAL_FOLDER = "key_original_folder";

    /**
     * Start a new {@link NewFolderActivity} with the default parent folder
     * {@code f}.
     */
    public static void startWithParentFolder(Folder f, Context c) {
        startWithParentFolder(f, null, c);
    }

    /**
     * Start a new {@link NewFolderActivity} with the default parent folder
     * {@code p} and default folder {@code f}.
     */
    public static void startWithParentFolder(Folder p, Folder f, Context c) {
        Intent intent = new Intent(c, NewFolderActivity.class);
        intent.putExtra(KEY_FOLDER, p);
        intent.putExtra(KEY_ORIGINAL_FOLDER, f);
        c.startActivity(intent);
    }

    private final FolderService mFolderService = new FolderService(this, null);

    private Folder mParentFolder;
    private Folder mOringinalFolder;

    @Override
    protected int onCreateLayoutId() {
        return R.layout.no_padding_activity;
    }

    @Override
    protected void onCreate(Bundle savedState) {
        Intent intent = getIntent();
        mParentFolder = intent.getParcelableExtra(KEY_FOLDER);
        mOringinalFolder = intent.getParcelableExtra(KEY_ORIGINAL_FOLDER);

        super.onCreate(savedState);
    }

    @Override
    protected Fragment onCreateFragment() {
        return NewFolderFragment.of(mParentFolder, mOringinalFolder);
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
