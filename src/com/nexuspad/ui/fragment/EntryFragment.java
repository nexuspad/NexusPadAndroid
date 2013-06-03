/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui.fragment;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.edmondapps.utils.android.Logs;
import com.edmondapps.utils.java.Lazy;
import com.nexuspad.R;
import com.nexuspad.account.AccountManager;
import com.nexuspad.core.Manifest;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.datamodel.NPEntry;
import com.nexuspad.dataservice.EntryService;
import com.nexuspad.dataservice.EntryService.EntryReceiver;
import com.nexuspad.dataservice.EntryServiceCallback;
import com.nexuspad.dataservice.NPException;
import com.nexuspad.dataservice.ServiceError;

/**
 * You must pass in a {@code Folder} with the key {@link KEY_FOLDER}
 * 
 * @author Edmond
 * 
 */
public abstract class EntryFragment<T extends NPEntry> extends SherlockDialogFragment {
    public static final String KEY_ENTRY = "com.nexuspad.ui.fragment.EntryFragment.entry";
    public static final String KEY_FOLDER = "com.nexuspad.ui.fragment.EntryFragment.folder";

    private static final String TAG = "EntryFragment";

    public interface Callback<T extends NPEntry> {
        void onDeleting(EntryFragment<T> f, T entry);
    }

    private final Lazy<EntryService> mEntryService = new Lazy<EntryService>() {
        @Override
        protected EntryService onCreate() {
            return new EntryService(getActivity(), new EntryCallback(EntryFragment.this));
        }
    };

    private final EntryReceiver mEntryReceiver = new EntryReceiver() {
        @Override
        public void onGot(Context context, Intent intent, NPEntry entry) {
            onEntryUpdatedInternal(entry);
        }
    };

    private T mEntry;
    private Folder mFolder;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            mEntry = arguments.getParcelable(KEY_ENTRY);
            mFolder = arguments.getParcelable(KEY_FOLDER);
        }

        if (mFolder == null) {
            throw new IllegalArgumentException("you must pass in a Folder with KEY_FOLDER");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(
                mEntryReceiver,
                EntryService.getEntryReceiverIntentFilter(),
                Manifest.permission.LISTEN_ENTRY_CHANGES,
                null);

        if (mEntry != null) {
            try {
                mEntry.setOwner(AccountManager.currentAccount());
                getEntryService().getEntry(mEntry);
            } catch (NPException e) {
                Logs.e(TAG, e);
                Toast.makeText(getActivity(), R.string.err_internal, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mEntryReceiver);
    }

    public void setEntry(T entry) {
        if (mEntry != entry) {
            mEntry = entry;
            onEntryUpdatedInternal(entry);
        }
    }

    public void setFolder(Folder folder) {
        if (mFolder != folder) {
            mFolder = folder;
            onFolderUpdated(folder);
        }
    }

    protected void onFolderUpdated(Folder folder) {
    }

    public T getEntry() {
        return mEntry;
    }

    public Folder getFolder() {
        return mFolder;
    }

    public EntryService getEntryService() {
        return mEntryService.get();
    }

    protected void deleteEntry() {
        getEntryService().safeDeleteEntry(getActivity(), mEntry);
        mCallback.onDeleting(this, mEntry);
    }

    protected void onEntryUpdated(T entry) {
        mEntry = entry;
    }

    protected void onEntryUpdateFailed(ServiceError error) {
    }

    @SuppressWarnings("unchecked")
    private void onEntryUpdatedInternal(NPEntry e) {
        onEntryUpdated((T)e);
    }

    private static class EntryCallback implements EntryServiceCallback {
        private final WeakReference<EntryFragment<?>> mFragment;

        private EntryCallback(EntryFragment<?> f) {
            mFragment = new WeakReference<EntryFragment<?>>(f);
        }

        @Override
        public void successfulRetrieval(NPEntry entry) {
        }

        @Override
        public void successfulUpdate(NPEntry updatedEntry) {
            EntryFragment<?> fragment = mFragment.get();
            if ( (fragment != null) && fragment.isAdded()) {
                fragment.onEntryUpdatedInternal(updatedEntry);
            }
        }

        @Override
        public void failureCallback(ServiceError error) {
            EntryFragment<?> fragment = mFragment.get();
            if ( (fragment != null) && fragment.isAdded()) {
                fragment.onEntryUpdateFailed(error);
            }
        }
    }
}
