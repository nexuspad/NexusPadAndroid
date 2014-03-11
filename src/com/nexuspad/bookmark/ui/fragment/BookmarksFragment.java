/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.bookmark.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.edmondapps.utils.java.WrapperList;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.app.App;
import com.nexuspad.bookmark.ui.BookmarksAdapter;
import com.nexuspad.bookmark.ui.activity.NewBookmarkActivity;
import com.nexuspad.datamodel.Bookmark;
import com.nexuspad.datamodel.EntryList;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.dataservice.EntryService;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.ui.FolderEntriesAdapter;
import com.nexuspad.ui.FoldersAdapter;
import com.nexuspad.ui.OnEntryMenuClickListener;
import com.nexuspad.ui.fragment.EntriesFragment;

/**
 * @author Edmond
 */
@FragmentName(BookmarksFragment.TAG)
@ModuleId(moduleId = ServiceConstants.BOOKMARK_MODULE, template = EntryTemplate.BOOKMARK)
public class BookmarksFragment extends EntriesFragment {
    public static final String TAG = "BookmarksFragment";
    private MenuItem mSearchItem;

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

    private Callback mCallback;
    private SearchView mSearchView;
    private FolderBookmarksAdapter mPreSearchAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = App.getCallback(activity, Callback.class);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.bookmarks_frag, menu);
        mSearchItem = menu.findItem(R.id.search);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_bookmark:
                NewBookmarkActivity.startWithFolder(getActivity(), getFolder());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onListLoaded(EntryList list) {
        super.onListLoaded(list);

        FolderBookmarksAdapter a = getListAdapter();
        if (a != null) {
            a.notifyDataSetChanged();
            if (!hasNextPage()) {
                a.removeAdapter(getLoadMoreAdapter());
            }
            return;
        }

        ListView listView = getListView();

        BookmarksAdapter bookmarksAdapter = newBookmarksAdapter(list);
        FoldersAdapter foldersAdapter = newFoldersAdapter();

        FolderBookmarksAdapter adapter;

        if (hasNextPage()) {
            adapter = new FolderBookmarksAdapter(foldersAdapter, bookmarksAdapter, getLoadMoreAdapter());
        } else {
            adapter = new FolderBookmarksAdapter(foldersAdapter, bookmarksAdapter);
        }

        setListAdapter(adapter);
        listView.setOnItemLongClickListener(adapter);
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
        }
    }

    @Override
    public FolderBookmarksAdapter getListAdapter() {
        return (FolderBookmarksAdapter) super.getListAdapter();
    }

    private class BookmarkMenuClickListener extends OnEntryMenuClickListener<Bookmark> {
        public BookmarkMenuClickListener(ListView listView, EntryService entryService) {
            super(listView, entryService, getUndoBarController());
        }

        @Override
        protected boolean onEntryMenuClick(Bookmark entry, int pos, int menuId) {
            switch (menuId) {
                case R.id.edit:
                    mCallback.onEditBookmark(BookmarksFragment.this, entry);
                    return true;
                default:
                    return super.onEntryMenuClick(entry, pos, menuId);
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
