/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import com.edmondapps.utils.android.activity.SinglePaneActivity;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.EntryList;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.ui.fragment.EntriesFragment;

/**
 * You must annotate the class with {@link ModuleId}.
 *
 * @author Edmond
 */
public abstract class EntriesActivity extends SinglePaneActivity implements EntriesFragment.Callback {
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
    protected int onCreateLayoutId() {
        return R.layout.no_padding_activity;
    }

    @Override
    protected void onCreate(Bundle savedState) {
        mModuleId = getClass().getAnnotation(ModuleId.class);

        Intent intent = getIntent();
        Folder folder = intent.getParcelableExtra(KEY_FOLDER);
        if (folder != null) {
            mFolder = folder;
        } else {
            mFolder = Folder.rootFolderOf(getModule(), this);
        }

        super.onCreate(savedState);

        final ActionBar actionBar = getActionBar();
        actionBar.setIcon(R.drawable.ic_ab);
        actionBar.setTitle(mFolder.getFolderName());
    }

    @Override
    public void onListLoaded(EntriesFragment f, EntryList list) {
        // do nothing
    }

    public Folder getFolder() {
        return mFolder;
    }
}
