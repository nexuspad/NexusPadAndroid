/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.bookmark.ui.fragment;

import static com.edmondapps.utils.android.view.ViewUtils.findView;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.edmondapps.utils.android.ui.SingleAdapter;
import com.edmondapps.utils.android.view.LoadingViews;
import com.edmondapps.utils.java.Lazy;
import com.edmondapps.utils.java.WrapperList;
import com.nexuspad.Manifest;
import com.nexuspad.R;
import com.nexuspad.bookmark.ui.BookmarksAdapter;
import com.nexuspad.bookmark.ui.activity.NewBookmarkActivity;
import com.nexuspad.datamodel.Bookmark;
import com.nexuspad.datamodel.EntryList;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.datamodel.NPEntry;
import com.nexuspad.dataservice.EntryService;
import com.nexuspad.dataservice.EntryService.EntryReceiver;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.ui.FolderEntriesAdapter;
import com.nexuspad.ui.FoldersAdapter;
import com.nexuspad.ui.OnEntryMenuClickListener;
import com.nexuspad.ui.activity.NewEntryActivity.Mode;
import com.nexuspad.ui.fragment.EntriesFragment;

/**
 * @author Edmond
 * 
 */
@FragmentName(BookmarksFragment.TAG)
public class BookmarksFragment extends EntriesFragment {
    public static final String TAG = "BookmarksFragment";

    public static BookmarksFragment of(Folder f) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_FOLDER, f);

        BookmarksFragment fragment = new BookmarksFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    public interface Callback extends EntriesFragment.Callback {
        void onBookmarkClick(BookmarksFragment f, Bookmark bookmark);

        void onEditBookmark(BookmarksFragment f, Bookmark bookmark);

        void onFolderClick(BookmarksFragment f, Folder folder);
    }

    private final Lazy<SingleAdapter<View>> mLoadMoreAdapter = new Lazy<SingleAdapter<View>>() {
        @Override
        protected SingleAdapter<View> onCreate() {
            View view = getActivity().getLayoutInflater()
                    .inflate(R.layout.list_item_load_more, null, false);

            LoadingViews loadingViews = LoadingViews.of(
                    findView(view, android.R.id.text1), findView(view, android.R.id.progress));

            view.setTag(loadingViews);
            return new SingleAdapter<View>(view);
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

    private Callback mCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof Callback) {
            mCallback = (Callback)activity;
        } else {
            throw new IllegalStateException(activity + " must implement Callback.");
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().registerReceiver(
                mEntryReceiver,
                EntryService.getEntryReceiverIntentFilter(),
                Manifest.permission.LISTEN_ENTRY_CHANGES,
                null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mEntryReceiver);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.bookmarks_frag, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_bookmark:
                NewBookmarkActivity.startWithFolder(getFolder(), Mode.NEW, getActivity());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onNewFolder(Context c, Intent i, Folder f) {
        // TODO bug in ActionResult.getUpdatedFolder()
        if (true) {
            return;
        }
        getEntryList().getFolder().getSubFolders().add(f);
        getListAdapter().notifyDataSetChanged();
    }

    @Override
    protected void onListLoaded(EntryList list) {
        super.onListLoaded(list);

        clearLoadMore();

        ListView listView = getListView();

        BookmarksAdapter bookmarksAdapter = newBookmarksAdapter(list);
        FoldersAdapter foldersAdapter = newFoldersAdapter(list);

        FolderBookmarksAdapter adapter;

        if (hasNextPage()) {
            adapter = new FolderBookmarksAdapter(foldersAdapter, bookmarksAdapter, mLoadMoreAdapter.get());
        } else {
            adapter = new FolderBookmarksAdapter(foldersAdapter, bookmarksAdapter);
        }

        setListAdapter(adapter);
        listView.setOnItemLongClickListener(adapter);
    }

    private void clearLoadMore() {
        LoadingViews loadingViews = (LoadingViews)mLoadMoreAdapter.get().getView().getTag();
        loadingViews.doneLoading();
    }

    private BookmarksAdapter newBookmarksAdapter(EntryList list) {
        BookmarksAdapter bookmarksAdapter = new BookmarksAdapter(getActivity(), new WrapperList<Bookmark>(list.getEntries()));
        bookmarksAdapter.setOnMenuClickListener(new BookmarkMenuClickListener(getListView(), getEntryService()));
        return bookmarksAdapter;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        FolderBookmarksAdapter adapter = getListAdapter();

        if (adapter.isPositionFolder(position)) {
            int pos = adapter.getPositionForAdapter(position);
            mCallback.onFolderClick(this, adapter.getFoldersAdapter().getItem(pos));

        } else if (adapter.isPositionEntries(position)) {
            int pos = adapter.getPositionForAdapter(position);
            mCallback.onBookmarkClick(this, adapter.getEntriesAdapter().getItem(pos));

        } else {
            LoadingViews loadingViews = (LoadingViews)v.getTag();
            loadingViews.startLoading();
            queryEntriesAync(getCurrentPage() + 1);
        }
    }

    @Override
    protected int getModule() {
        return ServiceConstants.BOOKMARK_MODULE;
    }

    @Override
    protected EntryTemplate getTemplate() {
        return EntryTemplate.BOOKMARK;
    }

    @Override
    public FolderBookmarksAdapter getListAdapter() {
        return (FolderBookmarksAdapter)super.getListAdapter();
    }

    private class BookmarkMenuClickListener extends OnEntryMenuClickListener<Bookmark> {
        public BookmarkMenuClickListener(ListView listView, EntryService entryService) {
            super(listView, entryService);
        }

        @Override
        protected boolean onEntryMenuClick(Bookmark entry, int menuId) {
            switch (menuId) {
                case R.id.edit:
                    mCallback.onEditBookmark(BookmarksFragment.this, entry);
                    return true;
                default:
                    return super.onEntryMenuClick(entry, menuId);
            }
        }
    }

    private static class FolderBookmarksAdapter extends FolderEntriesAdapter<BookmarksAdapter> {
        private FolderBookmarksAdapter(FoldersAdapter folderAdapter, BookmarksAdapter entriesAdapter) {
            super(folderAdapter, entriesAdapter);
        }

        public FolderBookmarksAdapter(FoldersAdapter folderAdapter, BookmarksAdapter entriesAdapter, BaseAdapter... others) {
            super(folderAdapter, entriesAdapter, others);
        }
    }
}
