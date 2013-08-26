/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import com.edmondapps.utils.android.Logs;
import com.nexuspad.account.AccountManager;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.datamodel.NPEntry;
import com.nexuspad.dataservice.EntryService;
import com.nexuspad.dataservice.ErrorCode;
import com.nexuspad.dataservice.NPException;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.dataservice.ServiceError;
import com.nexuspad.ui.activity.FoldersActivity;
import com.nexuspad.ui.activity.NewEntryActivity;

/**
 * Annotate it with {@link ModuleId}.
 *
 * @author Edmond
 */
public abstract class NewEntryFragment<T extends NPEntry> extends EntryFragment<T> {
    public static final String TAG = "NewEntryFragment";

    protected static final int REQ_FOLDER = 1;

    /**
     * @return if calling {@link #getEditedEntry()} would return a valid entry
     */
    public abstract boolean isEditedEntryValid();

    /**
     * Callers of this method should first check with
     * {@link #isEditedEntryValid()} to guarantee the validity of the edited
     * entry.<p>
     * Do not modify the detail entry here, create a copy and use {@link #setDetailEntry(NPEntry)} instead.
     * Or else equality checks will fail, and no entries will be updated.
     *
     * @return an edited entry that reflects the user's changes
     */
    public abstract T getEditedEntry();

    private ModuleId mModuleId;
    private NewEntryActivity.Mode mMode;

    protected NewEntryActivity.Mode getMode() {
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
        mModuleId = getClass().getAnnotation(ModuleId.class);
        mMode = getDetailEntryIfExist() == null ? NewEntryActivity.Mode.NEW : NewEntryActivity.Mode.EDIT;
    }

    public final void addEntry() {
        if(!NewEntryActivity.Mode.NEW.equals(mMode)) {
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
        getEntryService().addEntry(entry);
    }

    public final void updateEntry() {
        if(!NewEntryActivity.Mode.EDIT.equals(mMode)) {
            throw new IllegalStateException("not in Mode.EDIT");
        }
        if (isEditedEntryValid()) {
            final T originalEntry = getDetailEntryIfExist();
            final T entry = getEditedEntry();
            if (!entry.equals(originalEntry)) {
                try {
                    entry.setOwner(AccountManager.currentAccount());
                } catch (NPException e) {
                    throw new AssertionError("WTF, I thought I am logged in!");
                }
                onUpdateEntry(entry);
            } else {
                Logs.w(TAG, "entry not updated because no changes when made: " + entry);
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
        getEntryService().updateEntry(entry);
    }

    protected void installFolderSelectorListener(View v) {
        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final FragmentActivity activity = getActivity();
                final Folder folder = Folder.rootFolderOf(getModule(), activity);
                final Intent intent = FoldersActivity.ofParentFolder(activity, folder);
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
                    final Folder folder = data.getParcelableExtra(FoldersActivity.KEY_FOLDER);
                    setFolder(folder);
                }
                break;
        }
    }
}
