/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui;

import static com.edmondapps.utils.android.view.ViewUtils.findView;

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nexuspad.R;
import com.nexuspad.datamodel.EntryList;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.datamodel.NPEntry;

/**
 * @author Edmond
 * 
 */
public abstract class EntriesAdapter<T extends NPEntry> extends BaseAdapter {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_FOLDER = 1;
    public static final int TYPE_ENTRY = 2;
    public static final int TYPE_EMPTY_FOLDER = 3;
    public static final int TYPE_EMPTY_ENTRY = 4;

    private final EntryList mEntryList;
    private final LayoutInflater mInflater;
    private final int mFolderHeaderPos = 0;
    private final ArrayList<Folder> mSubFolders;

    private int mEntryHeaderPos;
    private OnClickListener mOnMenuClickListener;

    public EntriesAdapter(Activity a, EntryList list) {
        mEntryList = list;
        mInflater = a.getLayoutInflater();
        mSubFolders = list.getFolder().getSubFolders();
        updateHeadersPositions();
    }

    protected boolean isSubfoldersEmpty() {
        return mSubFolders.isEmpty();
    }

    protected boolean isEntriesEmpty() {
        return mEntryList.getEntries().size() == 0;
    }

    protected abstract View getFolderView(Folder folder, int position, View convertView, ViewGroup parent);

    protected abstract View getEntryView(T entry, int position, View convertView, ViewGroup parent);

    protected abstract View getEmptySubfoldersView(LayoutInflater inflater, View convertView, ViewGroup parent);

    protected abstract View getEmptyEntryView(LayoutInflater inflater, View convertView, ViewGroup parent);

    protected abstract int getEntryStringId();

    private static class ViewHolder {
        TextView header;
    }

    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        updateHeadersPositions();
    }

    private void updateHeadersPositions() {
        int entryHeaderPos = mFolderHeaderPos + mSubFolders.size() + 1;
        // a space for "no folders" view
        mEntryHeaderPos = isSubfoldersEmpty() ? entryHeaderPos + 1 : entryHeaderPos;
    }

    @Override
    public int getCount() {
        int count = mEntryList.getEntries().size() + mSubFolders.size() + 2;// headers
        // a space for "no subfolders" view
        count = isSubfoldersEmpty() ? count + 1 : count;
        // a space for "no entries" view
        count = isEntriesEmpty() ? count + 1 : count;
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        if ( (position == 0) || (position == mEntryHeaderPos)) {
            return TYPE_HEADER;
        } else if (position < mEntryHeaderPos) {
            return isSubfoldersEmpty() ? TYPE_EMPTY_FOLDER : TYPE_FOLDER;
        } else /* if (position > mEntryHeaderPos) */{
            return isEntriesEmpty() ? TYPE_EMPTY_ENTRY : TYPE_ENTRY;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 5;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        switch (getItemViewType(position)) {
            case TYPE_FOLDER:
            case TYPE_ENTRY:
                return true;
            case TYPE_HEADER:
            case TYPE_EMPTY_FOLDER:
            case TYPE_EMPTY_ENTRY:
                return false;
            default:
                throw new AssertionError("unknow view type: " + getItemViewType(position) + " at position: " + position);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getItem(int position) {
        return (T)mEntryList.getEntries().get(position - (mEntryHeaderPos + 1));
    }

    public Folder getFolder(int position) {
        return mSubFolders.get(position - (mFolderHeaderPos + 1));
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        switch (getItemViewType(position)) {
            case TYPE_HEADER:
                return getHeaderView(position, convertView, parent);
            case TYPE_FOLDER:
                return getFolderView(getFolder(position), position, convertView, parent);
            case TYPE_ENTRY:
                return getEntryView(getItem(position), position, convertView, parent);
            case TYPE_EMPTY_FOLDER:
                return getEmptySubfoldersView(mInflater, convertView, parent);
            case TYPE_EMPTY_ENTRY:
                return getEmptyEntryView(mInflater, convertView, parent);
            default:
                throw new AssertionError("unknow view type: " + getItemViewType(position) + " at position: " + position);
        }
    }

    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_header, parent, false);

            holder = new ViewHolder();
            holder.header = findView(convertView, android.R.id.text1);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        if (position == mFolderHeaderPos) {
            holder.header.setText(R.string.folders);
        } else if (position == mEntryHeaderPos) {
            holder.header.setText(getEntryStringId());
        } else {
            throw new AssertionError("unknown header for position: " + position);
        }

        return convertView;
    }

    public final void setOnMenuClickListener(OnClickListener onMenuClickListener) {
        mOnMenuClickListener = onMenuClickListener;
    }

    public final OnClickListener getOnMenuClickListener() {
        return mOnMenuClickListener;
    }

    protected final LayoutInflater getLayoutInflater() {
        return mInflater;
    }
}
