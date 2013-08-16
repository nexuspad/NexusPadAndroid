/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import com.edmondapps.utils.android.activity.DoneDiscardActivity;
import com.edmondapps.utils.android.annotaion.ParentActivity;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.datamodel.NPEntry;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.photos.ui.activity.PhotosActivity;
import com.nexuspad.ui.fragment.EntryFragment;
import com.nexuspad.ui.fragment.NewEntryFragment;

/**
 * An {@code Activity} with done/discard {@code ActionBar} enabled.
 * 
 * @author Edmond
 * 
 */
public abstract class NewEntryActivity<T extends NPEntry> extends DoneDiscardActivity implements EntryFragment.Callback<T> {
    public static final String KEY_MODE = "com.nexuspad.ui.activity.NewEntryActivity.mode";
    public static final String KEY_ENTRY = "com.nexuspad.ui.activity.NewEntryActivity.entry";
    public static final String KEY_FOLDER = "com.nexuspad.ui.activity.NewEntryActivity.folder";

    public enum Mode {
        NEW, EDIT
    }

    private Mode mMode = Mode.NEW;
    private T mEntry;
    private Folder mFolder;
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
        return R.layout.no_padding_activity;
    }

    @Override
    protected void onCreate(Bundle savedState) {
        mModuleId = getClass().getAnnotation(ModuleId.class);

        handleIntent(getIntent());
        super.onCreate(savedState);

        getSupportActionBar().setIcon(R.drawable.back_to_dashboard);
    }

    @Override
    public void onNewIntent(Intent i) {
        super.onNewIntent(i);
        handleIntent(i);
    }

    protected void handleIntent(Intent i) {
        Mode mode = (Mode)i.getSerializableExtra(KEY_MODE);
        if ( (mode != null) && (mMode != mode)) {
            mMode = mode;
            onNewMode(mode);
        }
        mEntry = i.getParcelableExtra(KEY_ENTRY);
        mFolder = i.getParcelableExtra(KEY_FOLDER);
        if (mFolder == null) {
            mFolder = Folder.rootFolderOf(getModule(), this);
        }
    }

    protected void onNewMode(Mode mode) {
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
     * <p>
     * This method assumes {@link #getFragment()} returns a type of
     * {@link NewEntryFragment}.
     * 
     * @throws ClassCastException
     *             if {@link #getFragment()} is not a type of
     *             {@link NewEntryFragment}
     */
    protected void onDoneEditing() {
        NewEntryFragment<T> fragment = getFragment();
        if (fragment.isEditedEntryValid()) {
            fragment.updateEntry();
            goUp();
        }
    }

    /**
     * Called when the "DONE" button is pressed in {@link Mode#NEW}. The
     * default implementation updates the entry and calls {@link #goUp()}.
     * <p>
     * This method assumes {@link #getFragment()} returns a type of
     * {@link NewEntryFragment}.
     * 
     * @throws ClassCastException
     *             if {@link #getFragment()} is not a type of
     *             {@link NewEntryFragment}
     */
    protected void onDoneAdding() {
        NewEntryFragment<T> fragment = getFragment();
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

    @Override
    public void onGotEntry(EntryFragment<T> f, T entry) {
        // do nothing
    }

    @Override
    public void onStartLoadingEntry(EntryFragment<T> f, T entry) {
        // do nothing
    }

    protected void goUp() {
        startActivity(getUpIntent(getUpActivity()));
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

    protected Folder getFolder() {
        return mFolder;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected NewEntryFragment<T> getFragment() {
        return (NewEntryFragment<T>)super.getFragment();
    }
}
