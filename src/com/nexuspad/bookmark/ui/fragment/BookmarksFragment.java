/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.bookmark.ui.fragment;

import static com.nexuspad.dataservice.ServiceConstants.BOOKMARK_MODULE;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.edmondapps.utils.android.annotaion.FragmentName;
import com.nexuspad.bookmark.ui.BookmarksAdapter;
import com.nexuspad.datamodel.Bookmark;
import com.nexuspad.datamodel.EntryList;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.ui.EntriesAdapter;
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

    public interface Callback {
        void onBookmarkClick(BookmarksFragment f, Bookmark bookmark);

        void onFolderClick(BookmarksFragment f, Folder folder);
    }

    private OnClickListener mOnMenuClickListener;

    private Folder mFolder;
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
        queryEntriesAync(mFolder);
        getListView().setItemsCanFocus(true);
    }

    @Override
    protected void onListLoaded(EntryList list) {
        super.onListLoaded(list);
        mOnMenuClickListener = new OnBookmarkMenuClickListener(getListView(), getEntryService(), getFolderService());

        final BookmarksAdapter adapter = new BookmarksAdapter(getActivity(), list);
        adapter.setOnMenuClickListener(mOnMenuClickListener);

        getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return adapter.onItemLongClick(parent, view, position, id);
            }
        });

        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        EntriesAdapter<Bookmark> adapter = getListAdapter();
        switch (adapter.getItemViewType(position)) {
            case EntriesAdapter.TYPE_ENTRY:
                mCallback.onBookmarkClick(this, adapter.getItem(position));
                break;
            case EntriesAdapter.TYPE_FOLDER:
                mCallback.onFolderClick(this, adapter.getFolder(position));
                break;
            default:
                throw new AssertionError("unexpected view type: " + adapter.getItemViewType(position) + ", at position: " + position);
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
    public BookmarksAdapter getListAdapter() {
        return (BookmarksAdapter)super.getListAdapter();
    }
}
