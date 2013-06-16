/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui.fragment;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;
import com.edmondapps.utils.android.Logs;
import com.edmondapps.utils.android.ui.CompoundAdapter;
import com.edmondapps.utils.android.ui.SingleAdapter;
import com.edmondapps.utils.java.Lazy;
import com.nexuspad.Manifest;
import com.nexuspad.R;
import com.nexuspad.account.AccountManager;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.EntryList;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.datamodel.NPEntry;
import com.nexuspad.dataservice.ActionResult;
import com.nexuspad.dataservice.EntryListService;
import com.nexuspad.dataservice.EntryListServiceCallback;
import com.nexuspad.dataservice.EntryService;
import com.nexuspad.dataservice.EntryService.EntryReceiver;
import com.nexuspad.dataservice.EntryServiceCallback;
import com.nexuspad.dataservice.ErrorCode;
import com.nexuspad.dataservice.FolderService;
import com.nexuspad.dataservice.FolderService.FolderReceiver;
import com.nexuspad.dataservice.FolderServiceCallback;
import com.nexuspad.dataservice.NPException;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.dataservice.ServiceError;
import com.nexuspad.home.ui.activity.LoginActivity;
import com.nexuspad.ui.FoldersAdapter;
import com.nexuspad.ui.OnFolderMenuClickListener;
import com.nexuspad.ui.OnListEndListener;
import com.nexuspad.ui.activity.NewFolderActivity;

/**
 * Manages an EntryList.
 * <p>
 * You may use {@link ModuleId} annotation on the {@code Fragment} class, if you
 * don't, you must override {@link #getTemplate()} and {@link #getModule()}.
 * 
 * @author Edmond
 * 
 */
public abstract class EntriesFragment extends ListFragment {
    public static final String KEY_FOLDER = "key_folder";
    public static final int PAGE_COUNT = 20;

    private static final String TAG = "EntriesFragment";
    private static final String KEY_ENTRY_LIST = "key_entry_list";

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

    private final FolderReceiver mFolderReceiver = new FolderReceiver() {
        @Override
        protected void onNew(Context c, Intent i, Folder f) {
            if (f.getParentId() == mFolder.getParentId()) {
                onNewFolder(c, i, f);
            }
        }
    };
    private final EntryReceiver mEntryReceiver = new EntryReceiver() {
        @Override
        public void onDelete(Context context, Intent intent, NPEntry entry) {
            EntryList entryList = getEntryList();
            if (entryList != null) {
                entryList.getEntries().remove(entry);
                getListAdapter().notifyDataSetChanged();
            }
        }

        @Override
        public void onNew(Context context, Intent intent, NPEntry entry) {
            EntryList entryList = getEntryList();
            if (entryList != null) {
                entryList.getEntries().add(entry);
                getListAdapter().notifyDataSetChanged();
            }
        }
    };

    private final Lazy<SingleAdapter<View>> mLoadMoreAdapter = new Lazy<SingleAdapter<View>>() {
        @Override
        protected SingleAdapter<View> onCreate() {
            return new SingleAdapter<View>(getActivity().getLayoutInflater()
                    .inflate(R.layout.list_item_load_more, null, false));
        }
    };

    private final EntryListCallback mEntryListCallback = new EntryListCallback(this);

    private EntryList mEntryList;
    private Callback mCallback;
    private int mCurrentPage;
    private Folder mFolder;

    private ModuleId mModuleId;

    /**
     * @see #getTemplate()
     * @return one of the {@code *_MODULE} constants in {@link ServiceConstants}
     */
    protected int getModule() {
        if (mModuleId == null) {
            throw new IllegalStateException("You must annotate the class with a ModuleId, or override this method.");
        }
        return mModuleId.moduleId();
    }

    /**
     * @return should correspond with {@link #getModule()}
     */
    protected EntryTemplate getTemplate() {
        if (mModuleId == null) {
            throw new IllegalStateException("You must annotate the class with a ModuleId, or override this method.");
        }
        return mModuleId.template();
    }

    protected abstract void onNewFolder(Context c, Intent i, Folder f);

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof Callback) {
            mCallback = (Callback)activity;
        } else {
            throw new IllegalStateException(activity + " must implement Callback.");
        }

        mModuleId = getClass().getAnnotation(ModuleId.class);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentActivity activity = getActivity();
        Bundle arguments = getArguments();

        if (arguments != null) {
            mFolder = arguments.getParcelable(KEY_FOLDER);
        }

        if (mFolder == null) {
            mFolder = Folder.rootFolderOf(getModule());
        }

        activity.registerReceiver(
                mFolderReceiver,
                FolderReceiver.getIntentFilter(),
                Manifest.permission.LISTEN_FOLDER_CHANGES,
                null);

        activity.registerReceiver(
                mEntryReceiver,
                EntryReceiver.getIntentFilter(),
                Manifest.permission.LISTEN_ENTRY_CHANGES,
                null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_ENTRY_LIST, mEntryList);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ListView listView = getListView();
        if (listView != null) {
            listView.setItemsCanFocus(true);
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            listView.setOnScrollListener(new OnListEndListener() {
                @Override
                protected void onListEnd(int page) {
                    queryEntriesAync(getCurrentPage() + 1);
                }
            });
        }

        if (savedInstanceState == null) {
            queryEntriesAync();
        } else {
            onListLoadedInternal(savedInstanceState.<EntryList> getParcelable(KEY_ENTRY_LIST));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_folder:
                NewFolderActivity.startWithParentFolder(getFolder(), getActivity());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        FragmentActivity activity = getActivity();
        activity.unregisterReceiver(mFolderReceiver);
        activity.unregisterReceiver(mEntryReceiver);
    }

    public void queryEntriesAync() {
        queryEntriesAync(1);
    }

    public void queryEntriesAync(int page) {
        mCurrentPage = page;

        FragmentActivity activity = getActivity();
        try {
            mFolder.setOwner(AccountManager.currentAccount());
            getEntriesInFolder(mEntryListService.get(), mFolder, page);

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

    /**
     * Called when {@link #queryEntriesAync(int)} is invoked. The default
     * implementation calls
     * {@link EntryListService#getEntriesInFolder(Folder, EntryTemplate, int, int)}
     * .
     * <p>
     * You may override this method to use other mechanisms, such as
     * {@link EntryListService#getEntriesBetweenDates(Folder, EntryTemplate, Date, Date, int, int)}
     */
    protected void getEntriesInFolder(EntryListService service, Folder folder, int page) throws NPException {
        service.getEntriesInFolder(mFolder, getTemplate(), page, PAGE_COUNT);
    }

    protected void onListLoaded(EntryList list) {
    }

    private void onListLoadedInternal(EntryList list) {
        if (mEntryList == null) {
            mEntryList = list;
        } else {
            expandEntryList(list);
        }
        onListLoaded(mEntryList);
        mCallback.onListLoaded(this, mEntryList);
    }

    /**
     * Add the entries to the current {@code EntryList} from another
     * {@code EntryList}.
     * 
     * @param o
     *            other {@code EntryList}
     */
    private void expandEntryList(EntryList o) {
        mEntryList.getEntries().addAll(o.getEntries());
        mEntryList.setPageId(o.getPageId());
    }

    protected boolean hasNextPage() {
        final EntryList list = mEntryList;
        if (list != null) {
            return list.getEntries().size() == (list.getCountPerPage() * list.getPageId());
        }
        return false;
    }

    public void deleteEntry(NPEntry entry) {
        getEntryService().safeDeleteEntry(getActivity(), entry);
    }

    protected FoldersAdapter newFoldersAdapter(EntryList list) {
        FoldersAdapter foldersAdapter = new FoldersAdapter(getActivity(), list.getFolder().getSubFolders());
        OnFolderMenuClickListener listener = new OnFolderMenuClickListener(getListView(), mFolder, getFolderService());
        foldersAdapter.setOnMenuClickListener(listener);
        return foldersAdapter;
    }

    /**
     * @see CompoundAdapter
     * @return an adapter that is used to indicate it is loading more entries at
     *         the end of the lit
     */
    protected SingleAdapter<View> getLoadMoreAdapter() {
        return mLoadMoreAdapter.get();
    }

    public EntryList getEntryList() {
        return mEntryList;
    }

    /**
     * @deprecated use {@link #setListAdapter(BaseAdapter)}
     * @throws UnsupportedOperationException
     *             every time this method is invoked
     */
    @Override
    @Deprecated
    public void setListAdapter(ListAdapter adapter) {
        throw new UnsupportedOperationException("You must use setListAdapter(BaseAdapter).");
    }

    public void setListAdapter(BaseAdapter adapter) {
        super.setListAdapter(adapter);
    }

    @Override
    public BaseAdapter getListAdapter() {
        return (BaseAdapter)super.getListAdapter();
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
            if ( (fragment != null) && fragment.isAdded()) {
                fragment.onListLoadedInternal(list);
            }
        }

        @Override
        public void failureCallback(ServiceError error) {
            EntriesFragment fragment = mEntriesFragment.get();
            if ( (fragment != null) && fragment.isAdded()) {
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
            if ( (fragment != null) && fragment.isAdded()) {
                if (fragment.mEntryList != null) {
                    fragment.mEntryList.getFolder()
                            .removeSubFolder(fragment.getFolderService().getJustDeletedFolder());
                }
                fragment.getListAdapter().notifyDataSetChanged();
            }
        }

        @Override
        public void failureCallback(ServiceError error) {
            EntriesFragment fragment = mEntriesFragment.get();
            if ( (fragment != null) && fragment.isAdded()) {
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
            if ( (fragment != null) && fragment.isAdded()) {
                if (fragment.mEntryList != null) {
                    fragment.mEntryList.getEntries()
                            .remove(fragment.getEntryService().getJustDeletedEntry());
                }
                fragment.getListAdapter().notifyDataSetChanged();
            }
        }

        @Override
        public void failureCallback(ServiceError error) {
            EntriesFragment fragment = mEntriesFragment.get();
            if ( (fragment != null) && fragment.isAdded()) {
                Toast.makeText(fragment.getActivity(), R.string.err_network, Toast.LENGTH_LONG).show();
            }
        }
    }
}
