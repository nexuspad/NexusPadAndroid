/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.common.activity;

import android.content.Intent;
import android.os.Bundle;
import com.nexuspad.R;
import com.nexuspad.common.Constants;
import com.nexuspad.common.annotation.ModuleInfo;
import com.nexuspad.common.fragment.EntryEditFragment;
import com.nexuspad.common.fragment.EntryFragment;
import com.nexuspad.service.datamodel.NPEntry;
import com.nexuspad.service.datamodel.NPFolder;
import com.nexuspad.service.dataservice.ServiceConstants;

/**
 * An {@code Activity} with done/discard {@code ActionBar} enabled.
 * <p/>
 * Annotate the class with ModuleId.
 *
 * @author Edmond
 */
public abstract class EntryEditActivity<T extends NPEntry> extends DoneDiscardActivity implements EntryFragment.EntryDetailCallback<T> {

    protected T mEntry;
    protected NPFolder mFolder;

    private ModuleInfo mModuleId;

    /**
     * @return one of the {@code *_MODULE} constants in {@link ServiceConstants}
     */
    protected int getModule() {
        if (mModuleId == null) {
            throw new IllegalStateException("You must override getModule() or annotate the class with ModuleId");
        }
        return mModuleId.moduleId();
    }

    @Override
    protected int onCreateLayoutId() {
        return R.layout.np_padding_activity;
    }

    @Override
    protected void onCreate(Bundle savedState) {
        mModuleId = ((Object)this).getClass().getAnnotation(ModuleInfo.class);

        handleIntent(getIntent());
        super.onCreate(savedState);
    }

    @Override
    public void onNewIntent(Intent i) {
        super.onNewIntent(i);
        handleIntent(i);
    }

    protected void handleIntent(Intent i) {
        mEntry = i.getParcelableExtra(Constants.KEY_ENTRY);
        mFolder = i.getParcelableExtra(Constants.KEY_FOLDER);
        if (mFolder == null) {
            mFolder = NPFolder.rootFolderOf(getModule(), this);
        }
    }

    @Override
    protected void onDonePressed() {
        super.onDonePressed();

        if (getEntryFromFragment().isNewEntry()) {
            onDoneAdding();
        } else {
            onDoneEditing();
        }
    }

    protected void onDoneEditing() {
        EntryEditFragment<T> fragment = getFragment();
        if (fragment.isEditedEntryValid()) {
            fragment.updateEntry();
            goUp();
        }
    }

    protected void onDoneAdding() {
        EntryEditFragment<T> fragment = getFragment();
        if (fragment.isEditedEntryValid()) {
            fragment.addEntry();
            goUp();
        }
    }

    @Override
    protected void onDiscardPressed() {
        goUp();
    }

    @Override
    public void onDeleting(EntryFragment<T> f, T entry) {
        goUp();
    }

    protected void goUp() {
        startActivity(getGoBackIntent(mParentActivity));
        finish();
    }

    protected T getEntryFromFragment() {
        EntryEditFragment<T> fragment = getFragment();

        T entry = fragment.getEntryFromEditor();
        if (entry == null) {
            throw new IllegalStateException("Entry object in Fragment cannot be null.");
        }

        return entry;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected EntryEditFragment<T> getFragment() {
        return (EntryEditFragment<T>) super.getFragment();
    }
}
