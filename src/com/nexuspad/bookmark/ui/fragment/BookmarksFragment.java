/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.bookmark.ui.fragment;

import static com.nexuspad.dataservice.ServiceConstants.BOOKMARK_MODULE;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.edmondapps.utils.android.Logs;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.edmondapps.utils.android.ui.SingleAdapter;
import com.edmondapps.utils.java.Lazy;
import com.edmondapps.utils.java.WrapperList;
import com.nexuspad.R;
import com.nexuspad.bookmark.ui.BookmarksAdapter;
import com.nexuspad.datamodel.Bookmark;
import com.nexuspad.datamodel.EntryList;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.ui.FolderEntriesAdapter;
import com.nexuspad.ui.FoldersAdapter;
import com.nexuspad.ui.OnEntryMenuClickListener;
import com.nexuspad.ui.OnFolderMenuClickListener;
import com.nexuspad.ui.fragment.EntriesFragment;

/**
 * @author Edmond
 * 
 */
@FragmentName(BookmarksFragment.TAG)
public class BookmarksFragment extends EntriesFragment {
    public static final String TAG = "BookmarksFragment";
    private static final String KEY_FOLDER = "key_folder";

    public static BookmarksFragment of(Folder f) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_FOLDER, f);

        BookmarksFragment fragment = new BookmarksFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    public interface Callback extends EntriesFragment.Callback {
        void onBookmarkClick(BookmarksFragment f, Bookmark bookmark);

        void onFolderClick(BookmarksFragment f, Folder folder);
    }

    private final Lazy<SingleAdapter<View>> mLoadMoreAdapter = new Lazy<SingleAdapter<View>>() {
        @Override
        protected SingleAdapter<View> onCreate() {
            View view = getActivity().getLayoutInflater()
                    .inflate(R.layout.list_item_load_more, null, false);
            return new SingleAdapter<View>(view);
        }
    };

    private Folder mFolder;
    private Callback mCallback;
    private final int mPageCount = 1;

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
            mFolder = Folder.initReservedFolder(BOOKMARK_MODULE, Folder.ROOT_FOLDER);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        queryEntriesAync(mFolder, mPageCount);
        getListView().setItemsCanFocus(true);
    }

    @Override
    protected void onListLoaded(EntryList list) {
        super.onListLoaded(list);

        FragmentActivity a = getActivity();
        ListView listView = getListView();

        BookmarksAdapter bookmarksAdapter = newBookmarksAdapter(list, a, listView);
        FoldersAdapter foldersAdapter = newFoldersAdapter(list, a, listView);

        FolderBookmarksAdapter adapter =
                new FolderBookmarksAdapter(foldersAdapter, bookmarksAdapter, mLoadMoreAdapter.get());
        setListAdapter(adapter);

        listView.setOnItemLongClickListener(adapter);
    }

    private BookmarksAdapter newBookmarksAdapter(EntryList list, FragmentActivity a, ListView listView) {
        BookmarksAdapter bookmarksAdapter = new BookmarksAdapter(a, new WrapperList<Bookmark>(list.getEntries()));
        bookmarksAdapter.setOnMenuClickListener(new OnEntryMenuClickListener<Bookmark>(listView, getEntryService()));
        return bookmarksAdapter;
    }

    private FoldersAdapter newFoldersAdapter(EntryList list, FragmentActivity a, ListView listView) {
        FoldersAdapter foldersAdapter = new FoldersAdapter(a, list.getFolder().getSubFolders());
        foldersAdapter.setOnMenuClickListener(new OnFolderMenuClickListener(listView, getFolderService()));
        return foldersAdapter;
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
            Logs.d(TAG, "position: " + position);
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

    private static class FolderBookmarksAdapter extends FolderEntriesAdapter<BookmarksAdapter> {
        private FolderBookmarksAdapter(FoldersAdapter folderAdapter, BookmarksAdapter entriesAdapter) {
            super(folderAdapter, entriesAdapter);
        }

        public FolderBookmarksAdapter(FoldersAdapter folderAdapter, BookmarksAdapter entriesAdapter, BaseAdapter... others) {
            super(folderAdapter, entriesAdapter, others);
        }
    }
}
