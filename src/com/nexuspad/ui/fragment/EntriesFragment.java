/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItemCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.edmondapps.utils.android.Logs;
import com.edmondapps.utils.android.ui.CompoundAdapter;
import com.edmondapps.utils.android.ui.SingleAdapter;
import com.edmondapps.utils.java.Lazy;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
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
import com.nexuspad.ui.DirectionalScrollListener;
import com.nexuspad.ui.FoldersAdapter;
import com.nexuspad.ui.OnFolderMenuClickListener;
import com.nexuspad.ui.OnListEndListener;
import com.nexuspad.ui.activity.FoldersActivity;
import com.nexuspad.ui.activity.NewFolderActivity;

import java.util.Iterator;
import java.util.List;

import static com.edmondapps.utils.android.view.ViewUtils.findView;
import static com.google.common.base.Strings.nullToEmpty;
import static com.nexuspad.dataservice.EntryListService.EntryListReceiver;

/**
 * Manages an EntryList.
 * <p/>
 * You may use {@link ModuleId} annotation on the {@code Fragment} class, if you
 * don't, you must override {@link #getTemplate()} and {@link #getModule()}.
 *
 * @author Edmond
 */
public abstract class EntriesFragment extends FadeListFragment {
    public static final String KEY_FOLDER = "key_folder";

    private static final int PAGE_COUNT = 20;
    private static final String TAG = "EntriesFragment";
    private static final String KEY_LIST_POS = "key_list_pos";

    public interface Callback {
        void onListLoaded(EntriesFragment f, EntryList list);
    }

    public interface FilterableAdapter {
        void showRawEntries();

        void filter(String newText);

        void setDisplayEntries(EntryList entries);
    }

    private final Lazy<FolderService> mFolderService = new Lazy<FolderService>() {
        @Override
        protected FolderService onCreate() {
            return FolderService.getInstance(getActivity());
        }
    };

    private final Lazy<EntryService> mEntryService = new Lazy<EntryService>() {
        @Override
        protected EntryService onCreate() {
            return EntryService.getInstance(getActivity());
        }
    };

    private final Lazy<EntryListService> mEntryListService = new Lazy<EntryListService>() {
        @Override
        protected EntryListService onCreate() {
            return EntryListService.getInstance(getActivity());
        }
    };

    private final EntryListReceiver mEntryListReceiver = new EntryListReceiver() {
        @Override
        protected void onGotAll(Context c, Intent i, EntryTemplate entryTemplate, String key) {
            if (mModuleId.template().equals(entryTemplate)) {
                onListLoadedInternal(mEntryListService.get().getEntryListFromKey(key));
            }
        }

        @Override
        protected void onGotSearch(Context c, Intent i, EntryTemplate entryTemplate, String key) {
            if (mModuleId.template().equals(entryTemplate)) {
                final EntryList entryList = mEntryListService.get().getEntryListFromKey(key);
                final String searchKeyword = nullToEmpty(entryList.getKeyword());
                if (searchKeyword.equals(mCurrentSearchKeyword)) {
                    onSearchLoadedInternal(entryList);
                }
            }
        }

        @Override
        protected void onError(Context context, Intent intent, ServiceError error) {
            Logs.e(TAG, error.toString());
            handleServiceError(error);
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

        @Override
        protected void onError(Context context, Intent intent, ServiceError error) {
            super.onError(context, intent, error);
            Logs.e(TAG, error.toString());
            handleServiceError(error);
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

        @Override
        protected void onError(Context context, Intent intent, ServiceError error) {
            super.onError(context, intent, error);
            Logs.e(TAG, error.toString());
            handleServiceError(error);
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

    private View mQuickReturnV;
    private TextView mFolderSelectorV;

    private String mCurrentSearchKeyword;

    /**
     * For subclass to implement. return the entries adapter to use the filtering features
     *
     * @return the underlying entries adapter; return a non-null adapter to use the default behaviours in methods like {@link #onSearchLoaded(EntryList)}
     */
    protected FilterableAdapter getFilterableAdapter() {
        throw new IllegalStateException("you must implement this method to use any goodies from EntriesFragment");
    }

    /**
     * set up {@code OnQueryTextListener}, {@code OnCloseListener}, and {@code OnActionExpandListener}.
     * <p></p>
     * it will not be expanded if {@link #getListAdapter()} is null; must implement {@link #getFilterableAdapter()}
     *
     * @param searchItem
     */
    protected void setUpSearchView(MenuItem searchItem) {
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (Strings.isNullOrEmpty(newText)) {
                    showRawEntries();
                } else {
                    filter(newText);
                }
                return true;  // i got this
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                showRawEntries();
                return true;
            }
        });
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return getListAdapter() != null;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                showRawEntries();
                return true;
            }
        });
    }

    private void filter(String newText) {
        fadeInProgressFrame();
        mCurrentSearchKeyword = newText;
        getFilterableAdapter().filter(newText);
    }

    private void showRawEntries() {
        fadeInListFrame();
        getFilterableAdapter().showRawEntries();
    }

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

    protected void onUpdateEntry(NPEntry updatedEntry) {
        final EntryList entryList = getEntryList();
        if (entryList != null) {
            final List<NPEntry> entries = entryList.getEntries();
            final Iterator<NPEntry> iterator = entries.iterator();
            final Predicate<NPEntry> predicate = updatedEntry.filterById();

            for (int index = 0; iterator.hasNext(); ++index) {
                final NPEntry current = iterator.next();
                if (predicate.apply(current)) {
                    entries.remove(index);

                    final Folder folder = current.getFolder();
                    final Folder updatedEntryF = updatedEntry.getFolder();

                    if (folder.filterById().apply(updatedEntryF)) { // same folder, put it back in
                        entries.add(index, updatedEntry);
                    }

                    onEntryListUpdated();
                    return;
                }
            }
            Logs.w(TAG, "cannot find the updated entry in the list; entry: " + updatedEntry);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = App.getCallbackOrThrow(activity, Callback.class);
        mModuleId = getClass().getAnnotation(ModuleId.class);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle arguments = getArguments();

        if (arguments != null) {
            mFolder = arguments.getParcelable(KEY_FOLDER);
        }

        if (mFolder == null) {
            throw new IllegalArgumentException("you did not pass in a folder with KEY_FOLDER");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        final FragmentActivity activity = getActivity();

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
    public void onPause() {
        super.onPause();
        final FragmentActivity activity = getActivity();

        activity.unregisterReceiver(mFolderReceiver);
        activity.unregisterReceiver(mEntryReceiver);
        activity.unregisterReceiver(mEntryListReceiver);
    }

    /**
     * {@link #onListLoaded(EntryList)} may get called immediately in the
     * method.
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mQuickReturnV = findView(view, R.id.quick_return);
        mFolderSelectorV = findView(view, R.id.lbl_folder);

        ListView listView = getListView();
        if (listView != null) {
            listView.setItemsCanFocus(true);
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            if (isAutoLoadMoreEnabled()) {
                listView.setOnScrollListener(new OnListEndListener() {
                    @Override
                    protected void onListEnd(int page) {
                        queryEntriesAsync(getCurrentPage() + 1);
                    }
                });
            }
        }

        if (isLoadListEnabled()) {
            queryEntriesAsync();
        }
    }

    @Override
    protected void onRetryClicked(View button) {
        super.onRetryClicked(button);
        if (isRetryEnabled()) {
            queryEntriesAsync();
        }
    }

    @Override
    public void onUndoButtonClicked(Intent token) {
        if (token != null) {
            final String action = token.getAction();
            final NPEntry entry = token.getParcelableExtra(EntryService.KEY_ENTRY);
            final Folder folder = token.getParcelableExtra(FolderService.KEY_FOLDER);
            final int position = token.getIntExtra(KEY_LIST_POS, 0);

            if (EntryService.ACTION_DELETE.equals(action)) {
                final List<NPEntry> entries = getEntryList().getEntries();
                entries.add(position, entry);
                onEntryListUpdated();

            } else if (FolderService.ACTION_DELETE.equals(action)) {
                final List<Folder> subFolders = getSubFolders();
                subFolders.add(position, folder);
                onEntryListUpdated();
            }
        }
    }

    @Override
    public void onUndoBarShown(Intent token) {
        if (token != null) {
            final String action = token.getAction();
            final NPEntry entry = token.getParcelableExtra(EntryService.KEY_ENTRY);
            final Folder folder = token.getParcelableExtra(FolderService.KEY_FOLDER);

            if (EntryService.ACTION_DELETE.equals(action)) {
                final EntryList entryList = getEntryList();
                if (entryList != null) {
                    final List<NPEntry> entries = entryList.getEntries();
                    final int i = Iterables.indexOf(entries, entry.filterById());
                    if (i >= 0) {
                        entries.remove(i);
                        token.putExtra(KEY_LIST_POS, i);
                        onEntryListUpdated();
                    } else {
                        Logs.w(TAG, "deleting entry, but no matching ID exists in the list. " + entry.getEntryId());
                    }
                }
            } else if (FolderService.ACTION_DELETE.equals(action)) {
                if (mFolder.getFolderId() == folder.getParentId()) {
                    final List<Folder> subFolders = getSubFolders();
                    final int i = Iterables.indexOf(subFolders, folder.filterById());
                    if (i >= 0) {
                        subFolders.remove(i);
                        token.putExtra(KEY_LIST_POS, i);
                        onEntryListUpdated();
                    } else {
                        Logs.w(TAG, "deleting folder, but no matching ID found in the list. " + folder);
                    }
                }
            }
        }
    }

    @Override
    public void onUndoBarHidden(Intent token) {
        if (token != null) {
            final String action = token.getAction();
            final NPEntry entry = token.getParcelableExtra(EntryService.KEY_ENTRY);
            final Folder folder = token.getParcelableExtra(FolderService.KEY_FOLDER);
            final FolderService service = getFolderService();

            if (EntryService.ACTION_DELETE.equals(action)) {
                getEntryService().safeDeleteEntry(getActivity(), entry);
            } else if (FolderService.ACTION_DELETE.equals(action)) {
                try {
                    folder.setOwner(AccountManager.currentAccount());
                    service.deleteFolder(folder);
                } catch (NPException e) {
                    Logs.e(TAG, e);
                }
            }
        }
    }

    /**
     * @return true if this Fragment should call {@link #queryEntriesAsync()}
     * automatically after view is created; false to disable it
     */
    protected boolean isLoadListEnabled() {
        return true;
    }

    /**
     * @return true if this Fragment should call {@link #queryEntriesAsync()}
     * automatically after the retry button is clicked; false to disable it
     */
    protected boolean isRetryEnabled() {
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

    public void queryEntriesAsync() {
        queryEntriesAsync(1);
    }

    public void queryEntriesAsync(int page) {
        mCurrentPage = page;

        try {
            mFolder.setOwner(AccountManager.currentAccount());
            getEntriesInFolder(mEntryListService.get(), mFolder, page);

        } catch (NPException e) {
            Logs.e(TAG, e);
            handleServiceError(e.getServiceError());
        }
    }

    private void handleServiceError(ServiceError error) {
        final ErrorCode errorCode = error.getErrorCode();
        if (shouldKickToLogin(errorCode)) {
            kickToLoginScreen();
        } else {
            fadeInRetryFrame();
        }
    }

    private boolean shouldKickToLogin(ErrorCode errorCode) {
        return errorCode == ErrorCode.INVALID_USER_TOKEN
                || errorCode == ErrorCode.INVALID_LOGIN
                || errorCode == ErrorCode.NOT_LOGGED_IN
                || errorCode == ErrorCode.FAILED_REGISTRATION
                || errorCode == ErrorCode.FAILED_REGISTRATION_ACCT_EXISTS
                || errorCode == ErrorCode.FAILED_DELETE_ACCOUNT
                || errorCode == ErrorCode.LOGIN_NO_USER
                || errorCode == ErrorCode.LOGIN_ACCT_PROBLEM
                || errorCode == ErrorCode.LOGIN_FAILED;
    }

    private void kickToLoginScreen() {
        final FragmentActivity activity = getActivity();
        final Intent intent = new Intent(activity, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        activity.finish();
    }

    /**
     * Called when {@link #queryEntriesAsync(int)} is invoked. The default
     * implementation calls
     * {@link EntryListService#getEntriesInFolder(Folder, EntryTemplate, int, int)}
     * .
     * <p/>
     * You may override this method to use other mechanisms, such as
     */
    protected void getEntriesInFolder(EntryListService service, Folder folder, int page) throws NPException {
        service.getEntriesInFolder(mFolder, getTemplate(), page, getEntriesCountPerPage());
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
     * The search result is here. Use {@link EntryList#getKeyword()} to see the original search string.
     *
     * @param list the filtered entries
     */
    protected void onSearchLoaded(EntryList list) {
        getFilterableAdapter().setDisplayEntries(list);
    }

    private void onSearchLoadedInternal(EntryList list) {
        onSearchLoaded(list);
        fadeInListFrame();
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
        OnFolderMenuClickListener listener = new OnFolderMenuClickListener(getListView(), mFolder, getFolderService(), getUndoBarController());
        foldersAdapter.setOnMenuClickListener(listener);
        return foldersAdapter;
    }

    /**
     * set up the quick return listener (hiding when scroll down, and vice versa)
     * <p/>
     * This will replace the OnScrollListener in the {@code AbsListView} inside {@code ListViewManager}, use {@code other} if you want to include
     * another {@code OnScrollListener}.
     *
     * @param listViewManager the listview manager
     * @param other           the other scroll listener; optional
     */
    protected void setQuickReturnListener(ListViewManager listViewManager, AbsListView.OnScrollListener other) {
        listViewManager.setOnScrollListener(newDirectionalScrollListener(other));
    }

    /**
     * set up the quick return listener (hiding when scroll down, and vice versa)
     * <p/>
     * This will replace the OnScrollListener in the {@code AbsListView}, use {@code other} if you want to include
     * another {@code OnScrollListener}.
     *
     * @param gridView the scrolling view (commonly ListView or GridView)
     * @param other    the other scroll listener; optional
     */
    protected void setQuickReturnListener(GridView gridView, AbsListView.OnScrollListener other) {
        gridView.setOnScrollListener(newDirectionalScrollListener(other));
    }

    private DirectionalScrollListener newDirectionalScrollListener(final AbsListView.OnScrollListener other) {
        return new DirectionalScrollListener(0, other) {
            @Override
            public void onScrollDirectionChanged(final boolean showing) {
                final View quickReturnV = getQuickReturnView();
                final int height = showing ? 0 : quickReturnV.getHeight();
                quickReturnV.animate()
                        .translationY(height)
                        .setDuration(200L)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                final View folderSelectorV = getFolderSelectorView();
                                folderSelectorV.setClickable(showing);
                                folderSelectorV.setFocusable(showing);
                            }
                        });
            }
        };
    }

    protected void setOnFolderSelectedClickListener(final int reqCode) {
        final TextView folderSelectorView = getFolderSelectorView();
        final int module = getModule();

        folderSelectorView.setText(getFolder().getFolderName());
        folderSelectorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FragmentActivity activity = getActivity();
                final Intent intent = FoldersActivity.ofParentFolder(activity, Folder.rootFolderOf(module, activity));
                startActivityForResult(intent, reqCode);
                activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    protected View getQuickReturnView() {
        if (mQuickReturnV == null) {
            throw new IllegalStateException("that is no view with id R.id.quick_return");
        }
        return mQuickReturnV;
    }

    public TextView getFolderSelectorView() {
        if (mFolderSelectorV == null) {
            throw new IllegalStateException("that is no view with id R.id.lbl_folder");
        }
        return mFolderSelectorV;
    }

    /**
     * @return an adapter that is used to indicate it is loading more entries at
     * the end of the lit
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
