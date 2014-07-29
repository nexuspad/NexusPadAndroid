/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.common.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import com.nexuspad.R;
import com.nexuspad.common.annotation.ModuleId;
import com.nexuspad.common.fragment.EntriesFragment;
import com.nexuspad.datamodel.EntryList;
import com.nexuspad.datamodel.NPFolder;
import com.nexuspad.dataservice.ServiceConstants;

/**
 * You must annotate the class with {@link ModuleId}.
 *
 * @author Edmond
 */
public abstract class EntriesActivity extends SinglePaneActivity implements EntriesFragment.Callback {
    public static final String KEY_FOLDER = "key_folder";

    private NPFolder mFolder;

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
        return R.layout.np_padding_activity;
    }

    @Override
    protected void onCreate(Bundle savedState) {
        mModuleId = ((Object) this).getClass().getAnnotation(ModuleId.class);

        Intent intent = getIntent();
        NPFolder folder = intent.getParcelableExtra(KEY_FOLDER);
        if (folder != null) {
            mFolder = folder;
        } else {
            mFolder = NPFolder.rootFolderOf(getModule(), this);
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

    public NPFolder getFolder() {
        return mFolder;
    }
}
