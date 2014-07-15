/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.bookmark.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.BaseAdapter;
import android.widget.ListView;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.edmondapps.utils.java.WrapperList;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.app.App;
import com.nexuspad.bookmark.ui.activity.NewBookmarkActivity;
import com.nexuspad.datamodel.Bookmark;
import com.nexuspad.datamodel.EntryList;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.dataservice.EntryListService;
import com.nexuspad.dataservice.EntryService;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.ui.EntriesAdapter;
import com.nexuspad.ui.FolderEntriesAdapter;
import com.nexuspad.ui.FoldersAdapter;
import com.nexuspad.ui.OnEntryMenuClickListener;
import com.nexuspad.ui.fragment.EntriesFragment;

import java.util.List;

/**
 * @author Edmond
 */
@FragmentName(BookmarksFragment.TAG)
@ModuleId(moduleId = ServiceConstants.BOOKMARK_MODULE, template = EntryTemplate.BOOKMARK)
public class BookmarksFragment extends EntriesFragment {
    public static final String TAG = "BookmarksFragment";

    public static BookmarksFragment of(Folder f) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_FOLDER, f);

        BookmarksFragment fragment = new BookmarksFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

	/**
	 * Callback methods that the Activity must implement.
	 */
    public interface Callback extends EntriesFragment.Callback {
        void onBookmarkClick(BookmarksFragment f, Bookmark bookmark);

        void onEditBookmark(BookmarksFragment f, Bookmark bookmark);

        void onFolderClick(BookmarksFragment f, Folder folder);
    }

    private Callback mCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = App.getCallbackOrThrow(activity, Callback.class);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.bookmarks_frag, menu);

	    setUpSearchView(menu.findItem(R.id.search));
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

	    final BookmarksAdapter bookmarksAdapter = new BookmarksAdapter(
			    getActivity(),
			    new WrapperList<Bookmark>(list.getEntries()));

	    bookmarksAdapter.setOnMenuClickListener(new BookmarkMenuClickListener(getListView(), getEntryService()));

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

    @Override
    protected void onSearchLoaded(EntryList list) {
        getListAdapter().setShouldHideFolders(true);
        super.onSearchLoaded(list);
    }

    @Override
    protected EntriesAdapter<?> getFilterableAdapter() {
        return getListAdapter().getEntriesAdapter();
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

    public class BookmarksAdapter extends EntriesAdapter<Bookmark> {
        public BookmarksAdapter(Activity a, List<Bookmark> entries) {
            super(a, entries, getFolder(), getEntryListService(), EntryTemplate.BOOKMARK);
        }

	    @Override
        protected View getEntryView(Bookmark entry, int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_icon, parent, false);
            }
            ViewHolder holder = getHolder(convertView);

            holder.icon.setImageResource(R.drawable.ic_bookmark);
            holder.text1.setText(entry.getTitle());
            holder.menu.setOnClickListener(getOnMenuClickListener());

            return convertView;
        }

        @Override
        protected String getEntriesHeaderText() {
            return getString(R.string.bookmarks);
        }

        @Override
        protected View getEmptyEntryView(LayoutInflater i, View c, ViewGroup p) {
            return getCaptionView(i, c, p, R.string.empty_bookmarks, R.drawable.empty_folder);
        }

        @Override
        public void showRawEntries() {
            getListAdapter().setShouldHideFolders(false);
            super.showRawEntries();
        }
    }
}
