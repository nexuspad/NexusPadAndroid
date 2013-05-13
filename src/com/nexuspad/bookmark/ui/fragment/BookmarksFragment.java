/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.bookmark.ui.fragment;

import static com.edmondapps.utils.android.view.ViewUtils.findView;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.edmondapps.utils.android.annotaion.FragmentName;
import com.edmondapps.utils.android.ui.SingleAdapter;
import com.edmondapps.utils.android.view.LoadingViews;
import com.edmondapps.utils.java.Lazy;
import com.edmondapps.utils.java.WrapperList;
import com.nexuspad.R;
import com.nexuspad.bookmark.ui.BookmarksAdapter;
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

    private Callback mCallback;

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
    protected void onListLoaded(EntryList list) {
        super.onListLoaded(list);

        clearLoadMore();

        FragmentActivity a = getActivity();
        ListView listView = getListView();

        BookmarksAdapter bookmarksAdapter = newBookmarksAdapter(list, a, listView);
        FoldersAdapter foldersAdapter = newFoldersAdapter(list, a, listView);

        FolderBookmarksAdapter adapter =
                new FolderBookmarksAdapter(foldersAdapter, bookmarksAdapter, mLoadMoreAdapter.get());
        setListAdapter(adapter);

        listView.setOnItemLongClickListener(adapter);
    }

    private void clearLoadMore() {
        LoadingViews loadingViews = (LoadingViews)mLoadMoreAdapter.get().getView().getTag();
        loadingViews.doneLoading();
    }

    private BookmarksAdapter newBookmarksAdapter(EntryList list, FragmentActivity a, ListView listView) {
        BookmarksAdapter bookmarksAdapter = new BookmarksAdapter(a, new WrapperList<Bookmark>(list.getEntries()));
        bookmarksAdapter.setOnMenuClickListener(new BookmarkMenuClickListener(listView, getEntryService()));
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
