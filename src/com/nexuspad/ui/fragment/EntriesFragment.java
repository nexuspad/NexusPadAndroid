/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui.fragment;

import java.lang.ref.WeakReference;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;
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
import com.nexuspad.ui.FoldersAdapter;
import com.nexuspad.ui.OnFolderMenuClickListener;

/**
 * @author Edmond
 * 
 */
public abstract class EntriesFragment extends PaddedListFragment {
    public static final String KEY_FOLDER = "key_folder";

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
    private int mCurrentPage;
    private Folder mFolder;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            mFolder = arguments.getParcelable(KEY_FOLDER);
        }

        if (mFolder == null) {
            mFolder = Folder.initReservedFolder(getModule(), Folder.ROOT_FOLDER);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        queryEntriesAync();
        getListView().setItemsCanFocus(true);
    }

    public void queryEntriesAync() {
        queryEntriesAync(1);
    }

    public void queryEntriesAync(int page) {
        mCurrentPage = page;

        FragmentActivity activity = getActivity();
        try {
            EntryListService service = mEntryListService.get();
            mFolder.setOwner(AccountManager.currentAccount());
            service.getEntriesInFolder(mFolder, getTemplate(), page, PAGE_COUNT);

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

    protected FoldersAdapter newFoldersAdapter(EntryList list, FragmentActivity a, ListView listView) {
        FoldersAdapter foldersAdapter = new FoldersAdapter(a, list.getFolder().getSubFolders());
        foldersAdapter.setOnMenuClickListener(new OnFolderMenuClickListener(listView, getFolderService()));
        return foldersAdapter;
    }

    public EntryList getEntryList() {
        return mEntryList;
    }

    public final Folder getFolder() {
        return mFolder;
    }

    public final int getCurrentPage() {
        return mCurrentPage;
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
