/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.view.MenuItem;
import com.edmondapps.utils.android.Logs;
import com.edmondapps.utils.java.Lazy;
import com.nexuspad.R;
import com.nexuspad.account.AccountManager;
import com.nexuspad.app.App;
import com.nexuspad.core.Manifest;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.datamodel.NPEntry;
import com.nexuspad.dataservice.EntryService;
import com.nexuspad.dataservice.EntryService.EntryReceiver;
import com.nexuspad.dataservice.NPException;
import com.nexuspad.dataservice.ServiceError;
import com.nexuspad.ui.activity.FoldersActivity;

/**
 * You must pass in a {@code Folder} with the key {@link EntryFragment#KEY_FOLDER}
 *
 * @author Edmond
 */
public abstract class EntryFragment<T extends NPEntry> extends SherlockDialogFragment {
    public static final String KEY_ENTRY = "com.nexuspad.ui.fragment.EntryFragment.entry";
    public static final String KEY_DETAIL_ENTRY = "com.nexuspad.ui.fragment.EntryFragment.detail_entry";
    public static final String KEY_FOLDER = "com.nexuspad.ui.fragment.EntryFragment.folder";

    private static final String TAG = "EntryFragment";

    public interface Callback<T extends NPEntry> {
        void onDeleting(EntryFragment<T> f, T entry);

        /**
         * Called right after {@link EntryService#getEntry(NPEntry)} is called.
         *
         * @param f     the {@link EntryFragment} instance
         * @param entry the entry
         */
        void onStartLoadingEntry(EntryFragment<T> f, T entry);

        /**
         * Called after {@link EntryService#getEntry(NPEntry)} has returned the
         * detail entry.
         *
         * @param f     the {@link EntryFragment} instance
         * @param entry the entry
         * @see #onStartLoadingEntry(EntryFragment, NPEntry)
         */
        void onGotEntry(EntryFragment<T> f, T entry);
    }

    private final Lazy<EntryService> mEntryService = new Lazy<EntryService>() {
        @Override
        protected EntryService onCreate() {
            return new EntryService(getActivity());
        }
    };

    private final EntryReceiver mEntryReceiver = new EntryReceiver() {
        @Override
        public void onGot(Context context, Intent intent, NPEntry entry) {
            onDetailEntryUpdatedInternal(entry);
        }

        @Override
        protected void onUpdate(Context context, Intent intent, NPEntry entry) {
            onDetailEntryUpdatedInternal(entry);
        }
    };

    private T mEntry;
    private T mDetailEntry;
    private Folder mFolder;
    private Callback<T> mCallback;

    @Override
    @SuppressWarnings("unchecked")
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = App.getCallback(activity, Callback.class);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle bundle = savedInstanceState == null ? getArguments() : savedInstanceState;
        initWithBundle(bundle);

        if (mFolder == null) {
            throw new IllegalArgumentException("you must pass in a Folder with KEY_FOLDER");
        }
    }

    private void initWithBundle(Bundle b) {
        if (b != null) {
            mEntry = b.getParcelable(KEY_ENTRY);
            mFolder = b.getParcelable(KEY_FOLDER);
            mDetailEntry = b.getParcelable(KEY_DETAIL_ENTRY);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);
        b.putParcelable(KEY_ENTRY, mEntry);
        b.putParcelable(KEY_FOLDER, mFolder);
        b.putParcelable(KEY_DETAIL_ENTRY, mDetailEntry);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        T entry = getDetailEntryIfExist();
        switch (item.getItemId()) {
            case R.id.delete:
                getEntryService().safeDeleteEntry(getActivity(), entry);
                mCallback.onDeleting(this, entry);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(
                mEntryReceiver,
                EntryReceiver.getIntentFilter(),
                Manifest.permission.LISTEN_ENTRY_CHANGES,
                null);

        if ((mEntry != null) && (mDetailEntry == null)) {
            try {
                mEntry.setOwner(AccountManager.currentAccount());
                getEntryService().getEntry(mEntry);

                mCallback.onStartLoadingEntry(this, mEntry);
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
            onEntryUpdated(entry);
        }
    }

    public void setDetailEntry(T entry) {
        if (mDetailEntry != entry) {
            mDetailEntry = entry;
            onDetailEntryUpdated(entry);
        }
    }

    /**
     * Calling this method will also invoke {@link NPEntry#setFolder(Folder)}
     * for the simple entry and the detail entry.
     *
     * @see #getDetailEntry()
     * @see #getDetailEntryIfExist()
     */
    public void setFolder(Folder folder) {
        if (mFolder != folder) {
            mFolder = folder;
            onFolderUpdatedInternal(folder);
        }
    }

    private void onFolderUpdatedInternal(Folder folder) {
        if (mEntry != null) {
            mEntry.setFolder(folder);
        }
        if (mDetailEntry != null) {
            mDetailEntry.setFolder(folder);
        }
        onFolderUpdated(folder);
    }

    /**
     * @return the original entry passed in by {@link #KEY_ENTRY} or
     *         {@link #setEntry(T)}
     * @see #getDetailEntry()
     * @see #getDetailEntryIfExist()
     * @deprecated use {@link #getDetailEntryIfExist()} instead
     */
    @Deprecated
    public T getEntry() {
        return mEntry;
    }

    /**
     * @return the entry retrieved by {@link EntryService#getEntry(NPEntry)}
     * @see #getDetailEntryIfExist()
     */
    public T getDetailEntry() {
        return mDetailEntry;
    }

    /**
     * @return returns {@link #getDetailEntry()} if {@link #hasDetailEntry()}
     *         returns true, otherwise, returns {@link #getEntry()}
     */
    public T getDetailEntryIfExist() {
        return hasDetailEntry() ? getDetailEntry() : getEntry();
    }

    /**
     * @return if the detail entry exists
     */
    public boolean hasDetailEntry() {
        return mDetailEntry != null;
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

    /**
     * Called when a folder is updated (usually from the result of selecting a
     * folder).
     *
     * @param folder the new {@link Folder}
     * @see FoldersActivity
     */
    protected void onFolderUpdated(Folder folder) {
    }

    /**
     * Called when the original entry is updated.
     *
     * @param entry the new entry
     */
    protected void onEntryUpdated(T entry) {
    }

    /**
     * Called when the detailed entry is retrieved from the server.
     *
     * @param entry the new detailed entry
     */
    protected void onDetailEntryUpdated(T entry) {
    }

    protected void onEntryUpdateFailed(ServiceError error) {
    }

    @SuppressWarnings("unchecked")
    private void onDetailEntryUpdatedInternal(NPEntry e) {
        T entry = (T) e;
        mDetailEntry = entry;

        mCallback.onGotEntry(this, entry);
        onDetailEntryUpdated(entry);
    }
}
