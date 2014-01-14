/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import com.edmondapps.utils.android.ui.CompoundAdapter;

/**
 * @author Edmond
 */
public class FolderEntriesAdapter<T extends EntriesAdapter<?>> extends CompoundAdapter implements OnItemLongClickListener {
    private final FoldersAdapter mFolderAdapter;
    private final T mEntriesAdapter;

    public FolderEntriesAdapter(FoldersAdapter folderAdapter, T entriesAdapter) {
        super(folderAdapter, entriesAdapter);
        mFolderAdapter = folderAdapter;
        mEntriesAdapter = entriesAdapter;
    }

    public FolderEntriesAdapter(FoldersAdapter folderAdapter, T entriesAdapter, BaseAdapter... others) {
        super(folderAdapter, entriesAdapter, others);
        mFolderAdapter = folderAdapter;
        mEntriesAdapter = entriesAdapter;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (isPositionFolder(position)) {
            return mFolderAdapter.onItemLongClick(parent, view, getPositionForAdapter(position), id);
        } else if (isPositionEntries(position)) {
            return mEntriesAdapter.onItemLongClick(parent, view, getPositionForAdapter(position), id);
        }
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public final boolean isPositionFolder(int position) {
        // identity check
        return mFolderAdapter == getAdapter(position);
    }

    public final boolean isPositionEntries(int position) {
        // identity check
        return mEntriesAdapter == getAdapter(position);
    }

    public final FoldersAdapter getFoldersAdapter() {
        return mFolderAdapter;
    }

    public final T getEntriesAdapter() {
        return mEntriesAdapter;
    }
}
