/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.common.activity;

import android.content.Intent;
import android.os.Bundle;
import com.nexuspad.R;
import com.nexuspad.common.annotation.ModuleId;
import com.nexuspad.common.fragment.EntryEditFragment;
import com.nexuspad.common.fragment.EntryFragment;
import com.nexuspad.datamodel.NPEntry;
import com.nexuspad.datamodel.NPFolder;
import com.nexuspad.dataservice.ServiceConstants;

/**
 * An {@code Activity} with done/discard {@code ActionBar} enabled.
 * <p/>
 * Annotate the class with ModuleId.
 *
 * @author Edmond
 */
public abstract class EntryEditActivity<T extends NPEntry> extends DoneDiscardActivity implements EntryFragment.Callback<T> {
    public static final String KEY_ENTRY = "com.nexuspad.ui.activity.NewEntryActivity.entry";
    public static final String KEY_FOLDER = "com.nexuspad.ui.activity.NewEntryActivity.folder";

    public enum Mode {
        NEW, EDIT
    }

    private Mode mMode = Mode.NEW;
    private T mEntry;
    private NPFolder mFolder;
    private ModuleId mModuleId;

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
        mModuleId = ((Object)this).getClass().getAnnotation(ModuleId.class);

        handleIntent(getIntent());
        super.onCreate(savedState);
    }

    @Override
    public void onNewIntent(Intent i) {
        super.onNewIntent(i);
        handleIntent(i);
    }

    protected void handleIntent(Intent i) {
        mEntry = i.getParcelableExtra(KEY_ENTRY);
        mFolder = i.getParcelableExtra(KEY_FOLDER);
        if (mFolder == null) {
            mFolder = NPFolder.rootFolderOf(getModule(), this);
        }
        mMode = mEntry == null ? Mode.NEW : Mode.EDIT;
    }

    /**
     * Calls {@link #onDoneAdding()} ()}, or {@link #onDoneEditing()} depending on the
     * value of {@link #getMode()}.
     */
    @Override
    protected void onDonePressed() {
        super.onDonePressed();

        switch (getMode()) {
            case EDIT:
                onDoneEditing();
                break;
            case NEW:
            default:
                onDoneAdding();
                break;
        }
    }

    /**
     * Called when the "DONE" button is pressed in {@link Mode#EDIT}. The
     * default implementation updates the entry and calls {@link #goUp()}.
     * <p/>
     * This method assumes {@link #getFragment()} returns a type of
     * {@link com.nexuspad.common.fragment.EntryEditFragment}.
     *
     * @throws ClassCastException if {@link #getFragment()} is not a type of
     *                            {@link com.nexuspad.common.fragment.EntryEditFragment}
     */
    protected void onDoneEditing() {
        EntryEditFragment<T> fragment = getFragment();
        if (fragment.isEditedEntryValid()) {
            fragment.updateEntry();
            goUp();
        }
    }

    /**
     * Called when the "DONE" button is pressed in {@link Mode#NEW}. The
     * default implementation updates the entry and calls {@link #goUp()}.
     * <p/>
     * This method assumes {@link #getFragment()} returns a type of
     * {@link com.nexuspad.common.fragment.EntryEditFragment}.
     *
     * @throws ClassCastException if {@link #getFragment()} is not a type of
     *                            {@link com.nexuspad.common.fragment.EntryEditFragment}
     */
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

    protected void setEntry(T entry) {
        mEntry = entry;
    }

    protected T getEntry() {
        return mEntry;
    }

    protected Mode getMode() {
        return mMode;
    }

    protected NPFolder getFolder() {
        return mFolder;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected EntryEditFragment<T> getFragment() {
        return (EntryEditFragment<T>) super.getFragment();
    }
}
