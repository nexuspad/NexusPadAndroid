/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui.fragment;

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
import com.google.common.collect.Iterables;
import com.nexuspad.Manifest;
import com.nexuspad.R;
import com.nexuspad.account.AccountManager;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.app.App;
import com.nexuspad.datamodel.EntryList;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.datamodel.NPEntry;
import com.nexuspad.dataservice.*;
import com.nexuspad.dataservice.EntryService.EntryReceiver;
import com.nexuspad.dataservice.FolderService.FolderReceiver;
import com.nexuspad.home.ui.activity.LoginActivity;
import com.nexuspad.ui.FoldersAdapter;
import com.nexuspad.ui.OnFolderMenuClickListener;
import com.nexuspad.ui.OnListEndListener;
import com.nexuspad.ui.activity.NewFolderActivity;

import java.util.List;

import static com.nexuspad.dataservice.EntryListService.EntryListReceiver;

/**
 * Manages an EntryList.
 * <p/>
 * You may use {@link ModuleId} annotation on the {@code Fragment} class, if you
 * don't, you must override {@link #getTemplate()} and {@link #getModule()}.
 *
 * @author Edmond
 */
public abstract class EntriesFragment extends ListFragment {
    public static final String KEY_FOLDER = "key_folder";

    private static final int PAGE_COUNT = 20;
    private static final String TAG = "EntriesFragment";
    private static EntryList sCachedEntryList;

    public interface Callback {
        void onListLoaded(EntriesFragment f, EntryList list);
    }

    private final Lazy<FolderService> mFolderService = new Lazy<FolderService>() {
        @Override
        protected FolderService onCreate() {
            return new FolderService(getActivity());
        }
    };

    private final Lazy<EntryService> mEntryService = new Lazy<EntryService>() {
        @Override
        protected EntryService onCreate() {
            return new EntryService(getActivity());
        }
    };

    private final Lazy<EntryListService> mEntryListService = new Lazy<EntryListService>() {
        @Override
        protected EntryListService onCreate() {
            return new EntryListService(getActivity());
        }
    };

    private final EntryListReceiver mEntryListReceiver = new EntryListReceiver() {
        @Override
        protected void onGotAll(Context c, Intent i, String key) {
            onListLoadedInternal(mEntryListService.get().getEntryListFromKey(key));
        }

        @Override
        protected void onError(Context context, Intent intent, ServiceError error) {
            Toast.makeText(getActivity(), R.string.err_internal, Toast.LENGTH_LONG).show();
        }
    };

    private final FolderReceiver mFolderReceiver = new FolderReceiver() {
        @Override
        protected void onNew(Context c, Intent i, Folder f) {
            final List<Folder> subFolders = getSubFolders();
            if (mFolder.getFolderId() == f.getParentId()) {
                if (!Iterables.tryFind(subFolders, f.filterById()).isPresent()) {
                    if (subFolders.size() == 0) {
                        subFolders.add(f);
                    } else {
                        subFolders.add(0, f);
                    }
                    onEntryListUpdated();
                } else {
                    Logs.w(TAG, "folder created on the server, but ID already exists in the list, updating instead: " + f);
                    onUpdate(c, i, f);
                }
            }
        }

        @Override
        protected void onDelete(Context c, Intent i, Folder folder) {
            if (mFolder.getFolderId() == folder.getParentId()) {
                if (Iterables.removeIf(getSubFolders(), folder.filterById())) {
                    onEntryListUpdated();
                } else {
                    Logs.w(TAG, "folder deleted from the server, but no matching ID found in the list. " + folder);
                }
            }
        }

        @Override
        protected void onUpdate(Context c, Intent i, Folder folder) {
            if (mFolder.getFolderId() == folder.getParentId()) {
                final List<Folder> subFolders = getSubFolders();
                final int index = Iterables.indexOf(subFolders, folder.filterById());
                if (index >= 0) {
                    subFolders.remove(index);
                    subFolders.add(index, folder);
                    onEntryListUpdated();
                } else {
                    Logs.w(TAG, "cannot find the updated entry in the list; folder: " + folder);
                }
            }
        }
    };

    private final EntryReceiver mEntryReceiver = new EntryReceiver() {
        @Override
        public void onDelete(Context context, Intent intent, NPEntry entry) {
            onDeleteEntry(entry);
        }

        @Override
        public void onNew(Context context, Intent intent, NPEntry entry) {
            onNewEntry(entry);
        }

        @Override
        public void onUpdate(Context context, Intent intent, NPEntry entry) {
            onUpdateEntry(entry);
        }
    };

    private final Lazy<SingleAdapter<View>> mLoadMoreAdapter = new Lazy<SingleAdapter<View>>() {
        @Override
        protected SingleAdapter<View> onCreate() {
            return new SingleAdapter<View>(getActivity().getLayoutInflater()
                    .inflate(R.layout.list_item_load_more, null, false));
        }
    };

    private EntryList mEntryList;

    private Callback mCallback;
    private int mCurrentPage;
    private Folder mFolder;
    private ModuleId mModuleId;

    /**
     * @return one of the {@code *_MODULE} constants in {@link ServiceConstants}
     * @see #getTemplate()
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

    protected List<Folder> getSubFolders() {
        return mEntryList.getFolder().getSubFolders();
    }

    protected void onEntryListUpdated() {
        final BaseAdapter adapter = getListAdapter();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    protected void onDeleteEntry(NPEntry entry) {
        EntryList entryList = getEntryList();
        if (entryList != null) {
            if (Iterables.removeIf(entryList.getEntries(), entry.filterById())) {
                onEntryListUpdated();
            } else {
                Logs.w(TAG, "entry deleted on the server, but no matching ID exists in the list. " + entry.getEntryId());
            }
        }
    }

    protected void onNewEntry(NPEntry entry) {
        EntryList entryList = getEntryList();
        if (entryList != null) {
            final List<NPEntry> entries = entryList.getEntries();
            if (!Iterables.tryFind(entries, entry.filterById()).isPresent()) {
                if (entries.size() == 0) {
                    entries.add(entry);
                } else {
                    entries.add(0, entry);
                }
                onEntryListUpdated();
            } else {
                Logs.w(TAG, "entry created on the server, but ID already exists in the list, updating instead: " + entry);
                onUpdateEntry(entry);
            }
        }
    }

    protected void onUpdateEntry(NPEntry entry) {
        final EntryList entryList = getEntryList();
        if (entryList != null) {
            final List<NPEntry> entries = entryList.getEntries();
            final int index = Iterables.indexOf(entries, entry.filterById());
            if (index >= 0) {
                entries.remove(index);
                entries.add(index, entry);
                onEntryListUpdated();
            } else {
                Logs.w(TAG, "cannot find the updated entry in the list; entry: " + entry);
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = App.getCallback(activity, Callback.class);
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
            mFolder = Folder.rootFolderOf(getModule(), activity);
        }

        activity.registerReceiver(mFolderReceiver,
                FolderReceiver.getIntentFilter(),
                Manifest.permission.LISTEN_FOLDER_CHANGES,
                null);

        activity.registerReceiver(mEntryReceiver,
                EntryReceiver.getIntentFilter(),
                Manifest.permission.LISTEN_ENTRY_CHANGES,
                null);

        activity.registerReceiver(mEntryListReceiver,
                EntryListReceiver.getIntentFilter(),
                Manifest.permission.RECEIVE_ENTRY_LIST,
                null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        sCachedEntryList = mEntryList;
    }

    /**
     * {@link #onListLoaded(EntryList)} may get called immediately in the
     * method.
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ListView listView = getListView();
        if (listView != null) {
            listView.setItemsCanFocus(true);
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            if (isAutoLoadMoreEnabled()) {
                listView.setOnScrollListener(new OnListEndListener() {
                    @Override
                    protected void onListEnd(int _) {
                        queryEntriesAync(getCurrentPage() + 1);
                    }
                });
            }
        }

        if (sCachedEntryList != null) {
            mEntryList = sCachedEntryList;
            onListLoadedInternal(mEntryList);
            sCachedEntryList = null;
        } else if (isLoadListEnabled()) {
            queryEntriesAync();
        }
    }

    /**
     * @return true if this Fragment should call {@link #queryEntriesAync()}
     *         automatically after view is created; false to disable it
     */
    protected boolean isLoadListEnabled() {
        return true;
    }

    /**
     * @return if the {@link EntryList} should be updated when the list reaches the end
     */
    protected boolean isAutoLoadMoreEnabled() {
        return true;
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
        activity.unregisterReceiver(mEntryListReceiver);
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
     * <p/>
     * You may override this method to use other mechanisms, such as
     * {@link EntryListService#getEntriesBetweenDates(Folder, EntryTemplate, long, long, int, int)}
     */
    protected void getEntriesInFolder(EntryListService service, Folder folder, int page) throws NPException {
        service.getEntriesInFolder(mFolder, getTemplate(), page, getEntriesCountPerPage());
    }

    // not ready
    protected void searchEntriesInFolder(String keyword, int page) throws NPException {
        getEntryListService().searchEntriesInFolder(keyword, getFolder(), getTemplate(), page, getEntriesCountPerPage());
    }

    protected int getEntriesCountPerPage() {
        return PAGE_COUNT;
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
     * @param o other {@code EntryList}
     */
    private void expandEntryList(EntryList o) {
        final List<NPEntry> oldEntries = mEntryList.getEntries();
        final List<NPEntry> newEntries = o.getEntries();

        for (NPEntry newEntry : newEntries) {
            if (!Iterables.tryFind(oldEntries, newEntry.filterById()).isPresent()) {
                oldEntries.add(newEntry);
            }
        }

        mEntryList.setPageId(o.getPageId());
    }

    protected boolean hasNextPage() {
        final EntryList list = mEntryList;
        return list != null && list.getEntries().size() == (list.getCountPerPage() * list.getPageId());
    }

    public void deleteEntry(NPEntry entry) {
        getEntryService().safeDeleteEntry(getActivity(), entry);
    }

    protected FoldersAdapter newFoldersAdapter() {
        FoldersAdapter foldersAdapter = new FoldersAdapter(getActivity(), getSubFolders());
        OnFolderMenuClickListener listener = new OnFolderMenuClickListener(getListView(), mFolder, getFolderService());
        foldersAdapter.setOnMenuClickListener(listener);
        return foldersAdapter;
    }

    /**
     * @return an adapter that is used to indicate it is loading more entries at
     *         the end of the lit
     * @see CompoundAdapter
     */
    protected SingleAdapter<View> getLoadMoreAdapter() {
        return mLoadMoreAdapter.get();
    }

    public EntryList getEntryList() {
        return mEntryList;
    }

    /**
     * @throws UnsupportedOperationException every time this method is invoked
     * @deprecated use {@link #setListAdapter(BaseAdapter)}
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
        return (BaseAdapter) super.getListAdapter();
    }

    public Folder getFolder() {
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
}
