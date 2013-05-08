/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui.fragment;

import java.lang.ref.WeakReference;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.edmondapps.utils.android.Logs;
import com.edmondapps.utils.java.Lazy;
import com.nexuspad.R;
import com.nexuspad.account.AccountManager;
import com.nexuspad.datamodel.EntryList;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.datamodel.NPEntry;
import com.nexuspad.dataservice.ActionResult;
import com.nexuspad.dataservice.EntryListService;
import com.nexuspad.dataservice.EntryListServiceCallback;
import com.nexuspad.dataservice.EntryService;
import com.nexuspad.dataservice.EntryServiceCallback;
import com.nexuspad.dataservice.ErrorCode;
import com.nexuspad.dataservice.FolderService;
import com.nexuspad.dataservice.FolderServiceCallback;
import com.nexuspad.dataservice.NPException;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.dataservice.ServiceError;
import com.nexuspad.home.ui.activity.LoginActivity;

/**
 * @author Edmond
 * 
 */
public abstract class EntriesFragment extends PaddedListFragment {
    private static final int PAGE_COUNT = 20;
    private static final String TAG = "EntriesFragment";

    public interface Callback {
        void onListLoaded(EntriesFragment f, EntryList list);
    }

    private final Lazy<FolderService> mFolderService = new Lazy<FolderService>() {
        @Override
        protected FolderService onCreate() {
            return new FolderService(getActivity(), new FolderCallback(EntriesFragment.this));
        }
    };

    private final Lazy<EntryService> mEntryService = new Lazy<EntryService>() {
        @Override
        protected EntryService onCreate() {
            return new EntryService(getActivity(), new EntryCallback(EntriesFragment.this));
        }
    };

    private final Lazy<EntryListService> mEntryListService = new Lazy<EntryListService>() {
        @Override
        protected EntryListService onCreate() {
            return new EntryListService(getActivity(), mEntryListCallback);
        }
    };

    private final EntryListCallback mEntryListCallback = new EntryListCallback(this);
    private EntryList mEntryList;
    private Callback mCallback;

    /**
     * @see #getTemplate()
     * @return one of the {@code *_MODULE} constants in {@link ServiceConstants}
     */
    protected abstract int getModule();

    /**
     * @return should correspond with {@link #getModule()}
     */
    protected abstract EntryTemplate getTemplate();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof Callback) {
            mCallback = (Callback)activity;
        } else {
            throw new IllegalStateException(activity + " must implement Callback.");
        }
    }

    public void queryEntriesAync(Folder folder) {
        queryEntriesAync(folder, 1);
    }

    public void queryEntriesAync(Folder folder, int page) {
        FragmentActivity activity = getActivity();

        try {
            EntryListService service = new EntryListService(activity, mEntryListCallback);
            folder.setOwner(AccountManager.currentAccount());
            service.getEntriesInFolder(folder, getTemplate(), page, PAGE_COUNT);

        } catch (NPException e) {
            Logs.e(TAG, e);
            if (e.getServiceError().getErrorCode() == ErrorCode.INVALID_USER_TOKEN) {
                startActivity(new Intent(activity, LoginActivity.class));
                activity.finish();
            } else {
                Toast.makeText(activity, R.string.err_internal, Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void onListLoaded(EntryList list) {
        mEntryList = list;
        mCallback.onListLoaded(this, list);
    }

    public EntryList getEntryList() {
        return mEntryList;
    }

    public final FolderService getFolderService() {
        return mFolderService.get();
    }

    public final EntryService getEntryService() {
        return mEntryService.get();
    }

    public final EntryListService getEntryListService() {
        return mEntryListService.get();
    }

    private static class EntryListCallback implements EntryListServiceCallback {
        private final WeakReference<EntriesFragment> mEntriesFragment;

        public EntryListCallback(EntriesFragment f) {
            mEntriesFragment = new WeakReference<EntriesFragment>(f);
        }

        @Override
        public void successfulRetrieval(EntryList list) {
            EntriesFragment fragment = mEntriesFragment.get();
            if (fragment != null) {
                fragment.onListLoaded(list);
            }
        }

        @Override
        public void failureCallback(ServiceError error) {
            EntriesFragment fragment = mEntriesFragment.get();
            if (fragment != null) {
                Toast.makeText(fragment.getActivity(), R.string.err_internal, Toast.LENGTH_LONG).show();
            }
        }
    }

    private static class FolderCallback implements FolderServiceCallback {
        private final WeakReference<EntriesFragment> mEntriesFragment;

        private FolderCallback(EntriesFragment f) {
            mEntriesFragment = new WeakReference<EntriesFragment>(f);
        }

        @Override
        public void successfulRetrieval(Map<String, Folder> folders) {
        }

        @Override
        public void successfulUpdate(ActionResult actionResult) {
            EntriesFragment fragment = mEntriesFragment.get();
            if (fragment != null) {
                if (fragment.mEntryList != null) {
                    fragment.mEntryList.getFolder()
                            .removeSubFolder(fragment.getFolderService().getJustDeletedFolder());
                }
                ((BaseAdapter)fragment.getListAdapter()).notifyDataSetChanged();
            }
        }

        @Override
        public void failureCallback(ServiceError error) {
            EntriesFragment fragment = mEntriesFragment.get();
            if (fragment != null) {
                Toast.makeText(fragment.getActivity(), R.string.err_network, Toast.LENGTH_LONG).show();
            }
        }
    }

    private static class EntryCallback implements EntryServiceCallback {
        private final WeakReference<EntriesFragment> mEntriesFragment;

        private EntryCallback(EntriesFragment f) {
            mEntriesFragment = new WeakReference<EntriesFragment>(f);
        }

        @Override
        public void successfulRetrieval(NPEntry entry) {
        }

        @Override
        public void successfulUpdate(NPEntry updatedEntry) {
            EntriesFragment fragment = mEntriesFragment.get();
            if (fragment != null) {
                if (fragment.mEntryList != null) {
                    fragment.mEntryList.getEntries()
                            .remove(fragment.getEntryService().getJustDeletedEntry());
                }
                ((BaseAdapter)fragment.getListAdapter()).notifyDataSetChanged();
            }
        }

        @Override
        public void failureCallback(ServiceError error) {
            EntriesFragment fragment = mEntriesFragment.get();
            if (fragment != null) {
                Toast.makeText(fragment.getActivity(), R.string.err_network, Toast.LENGTH_LONG).show();
            }
        }
    }
}
