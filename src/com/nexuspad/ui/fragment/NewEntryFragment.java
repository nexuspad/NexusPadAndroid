/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui.fragment;

import android.app.Activity;

import com.nexuspad.account.AccountManager;
import com.nexuspad.datamodel.NPEntry;
import com.nexuspad.dataservice.NPException;
import com.nexuspad.dataservice.ServiceError;

/**
 * @author Edmond
 * 
 */
public abstract class NewEntryFragment<T extends NPEntry> extends EntryFragment<T> {
    public abstract boolean isEditedEntryValid();

    public abstract T getEditedEntry();

    public interface Callback<T extends NPEntry> {
        void onEntryUpdated(NewEntryFragment<T> f, T entry);

        void onEntryUpdateFailed(NewEntryFragment<T> f, ServiceError error);
    }

    private Callback<T> mCallback;

    @Override
    @SuppressWarnings("unchecked")
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof Callback) {
            mCallback = (Callback<T>)activity;
        } else {
            throw new IllegalStateException(activity + " must implement Callback.");
        }
    }

    public void updateEntry() {
        if (isEditedEntryValid()) {
            T entry = getEditedEntry();
            try {
                entry.setOwner(AccountManager.currentAccount());
            } catch (NPException e) {
                throw new AssertionError("WTF, I thought I am logged in!");
            }
            getEntryService().updateEntry(entry);
        }
    }

    @Override
    protected void onEntryUpdated(T entry) {
        super.onEntryUpdated(entry);
        mCallback.onEntryUpdated(this, entry);
    }

    @Override
    protected void onEntryUpdateFailed(ServiceError error) {
        super.onEntryUpdateFailed(error);
        mCallback.onEntryUpdateFailed(this, error);
    }
}
