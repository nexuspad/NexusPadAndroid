/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.common.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import com.nexuspad.account.AccountManager;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.common.activity.FoldersNavigatorActivity;
import com.nexuspad.common.activity.UpdateEntryActivity;
import com.nexuspad.datamodel.NPEntry;
import com.nexuspad.datamodel.NPFolder;
import com.nexuspad.dataservice.*;

/**
 * Annotate it with {@link ModuleId}.
 *
 * @author Edmond
 */
public abstract class UpdateEntryFragment<T extends NPEntry> extends EntryFragment<T> {
    public static final String TAG = UpdateEntryFragment.class.getSimpleName();

    protected static final int REQ_FOLDER = 1;
    /**
     * the starting int request code for subclasses, you must use an int higher than this constant to prevent collision
     * with the super class
     */
    protected static final int REQ_SUBCLASSES = 2;

    /**
     * @return if calling {@link #getEditedEntry()} would return a valid entry
     */
    public abstract boolean isEditedEntryValid();

    /**
     * Callers of this method should first check with
     * {@link #isEditedEntryValid()} to guarantee the validity of the edited
     * entry.<p>
     * Do not modify the detail entry here, create a copy and use {@link #setEntry(NPEntry)} instead.
     * Or else equality checks will fail, and no entries will be updated.<p>
     *
     * @return an edited entry that reflects the user's changes
     */
    public abstract T getEditedEntry();

    private ModuleId mModuleId;
    private UpdateEntryActivity.Mode mMode;

    protected UpdateEntryActivity.Mode getMode() {
        return mMode;
    }

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mModuleId = ((Object) this).getClass().getAnnotation(ModuleId.class);
        mMode = getEntry() == null ? UpdateEntryActivity.Mode.NEW : UpdateEntryActivity.Mode.EDIT;
    }

    public final void addEntry() {
        if (!UpdateEntryActivity.Mode.NEW.equals(mMode)) {
            throw new IllegalStateException("not in Mode.NEW");
        }
        if (isEditedEntryValid()) {
            T entry = getEditedEntry();
            try {
                entry.setOwner(AccountManager.currentAccount());
            } catch (NPException e) {
                throw new AssertionError("WTF, I thought I am logged in!");
            }
            onAddEntry(entry);
        } else {
            onEntryUpdateFailed(new ServiceError(ErrorCode.MISSING_PARAM, "entry is not valid"));
        }
    }

    /**
     * Called when the entry is proven valid, and owner info is set correctly.
     * <p/>
     * The default implementation calls {@link EntryService#addEntry(NPEntry)}.
     *
     * @param entry the edited entry
     * @see #isEditedEntryValid()
     * @see #getEditedEntry()
     */
    protected void onAddEntry(T entry) {
        try {
            getEntryService().addEntry(entry);
        } catch (NPException e) {
            throw new RuntimeException(e);
        }
    }

    public final void updateEntry() {
        if (!UpdateEntryActivity.Mode.EDIT.equals(mMode)) {
            throw new IllegalStateException("not in Mode.EDIT");
        }
        if (isEditedEntryValid()) {
            final T originalEntry = getEntry();
            final T entry = getEditedEntry();
            if (!entry.equals(originalEntry)) {
                try {
                    entry.setOwner(AccountManager.currentAccount());
                } catch (NPException e) {
                    throw new AssertionError("WTF, I thought I am logged in!");
                }
                onUpdateEntry(entry);
            } else {
                Log.w(TAG, "entry not updated because no changes when made: " + entry);
            }
        }
    }

    /**
     * Called when the entry is proven valid, and owner info is set correctly.
     * <p/>
     * The default implementation calls {@link EntryService#updateEntry(NPEntry)}.
     *
     * @param entry the edited entry
     * @see #isEditedEntryValid()
     * @see #getEditedEntry()
     */
    protected void onUpdateEntry(T entry) {
        try {
            getEntryService().updateEntry(entry);
        } catch (NPException e) {
            throw new RuntimeException(e);
        }
    }

    protected void installFolderSelectorListener(View v) {
        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final FragmentActivity activity = getActivity();
                final NPFolder folder = NPFolder.rootFolderOf(getModule(), activity);
                final Intent intent = FoldersNavigatorActivity.ofParentFolder(activity, folder);
                startActivityForResult(intent, REQ_FOLDER);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_FOLDER:
                if (resultCode == Activity.RESULT_OK) {
                    final NPFolder folder = data.getParcelableExtra(FoldersNavigatorActivity.KEY_FOLDER);
                    setFolder(folder);
                }
                break;
        }
    }
}
