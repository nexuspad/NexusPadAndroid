/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import com.edmondapps.utils.android.ui.CompoundAdapter;

/**
 * @deprecated
 *
 * Handles the list that contains both folders and entries.
 *
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

    @Override
    public int getCount() {
        if (mEntriesAdapter.isEmpty()) {
            return 1;
        }

        final int folderCount = mFolderAdapter.isEmpty() ? 0 : mFolderAdapter.getCount();
        final int entriesCount = mEntriesAdapter.getCount();  // always non-empty (returned 1 above if it is empty)
        final int localCount = folderCount + entriesCount;
        final int superCount = super.getCount();

	    //Log.i("CHECK COUNT: ", "folder cnt:" + String.valueOf(folderCount) + " entry cnt:" + String.valueOf(entriesCount) + " " + String.valueOf(localCount) + " " + String.valueOf(superCount));

	    return localCount;

        // superCount will be 0 for empty adapters, use local in such case
        //return localCount > superCount ? localCount : superCount;
    }

    public void setShouldHideFolders(boolean shouldHideFolders) {
        mFolderAdapter.setShouldHide(shouldHideFolders);
        notifyDataSetChanged();
    }

    public boolean getShouldHideFolders() {
        return mFolderAdapter.getShouldHide();
    }

    @Override
    public int getViewTypeCount() {
        if (mEntriesAdapter.isEmpty()) {
            return mEntriesAdapter.getViewTypeCount(); // should be 1
        }
        return super.getViewTypeCount();
    }

    @Override
    public int getItemViewType(int position) {
        if (mEntriesAdapter.isEmpty()) {
            return mEntriesAdapter.getItemViewType(position);
        }
        return super.getItemViewType(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
	    if (mEntriesAdapter.isEmpty()) {
            return mEntriesAdapter.getView(position, convertView, parent);
        }
        return super.getView(position, convertView, parent);
    }

    @Override
    public boolean areAllItemsEnabled() {
        //noinspection SimplifiableIfStatement
        if (mEntriesAdapter.isEmpty()) {
            return false;
        }
        return super.areAllItemsEnabled();
    }

    @Override
    public boolean isEnabled(int position) {
        //noinspection SimplifiableIfStatement
        if (mEntriesAdapter.isEmpty()) {
            return false;
        }
        return super.isEnabled(position);
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
