/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui;

import static com.edmondapps.utils.android.view.ViewUtils.findView;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nexuspad.R;
import com.nexuspad.datamodel.Folder;

/**
 * @author Edmond
 */
public class FoldersAdapter extends BaseAdapter implements OnItemLongClickListener {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_FOLDER = 1;
    public static final int TYPE_EMPTY_FOLDER = 2;

    private final LayoutInflater mInflater;
    private final List<? extends Folder> mSubFolders;
    private final boolean mUseSubFolderButtons;

    private OnClickListener mOnMenuClickListener;
    private OnClickListener mOnSubFolderClickListener;

    public FoldersAdapter(Activity a, List<? extends Folder> folders) {
        this(a, folders, false);
    }

    public FoldersAdapter(Activity a, List<? extends Folder> folders, boolean useSubFolderButtons) {
        mUseSubFolderButtons = useSubFolderButtons;
        mInflater = a.getLayoutInflater();
        mSubFolders = folders;
    }

    private static class ViewHolder {
        ImageView icon;
        ImageButton icon2;
        TextView text1;
        ImageButton menu;
    }

    private static ViewHolder getHolder(View convertView) {
        ViewHolder holder = (ViewHolder)convertView.getTag();
        if (holder == null) {
            holder = new ViewHolder();
            holder.icon = findView(convertView, android.R.id.icon);
            holder.icon2 = findView(convertView, android.R.id.icon2);
            holder.text1 = findView(convertView, android.R.id.text1);
            holder.menu = findView(convertView, R.id.menu);

            holder.menu.setFocusable(false);
            if (holder.icon2 != null) {
                holder.icon2.setFocusable(false);
            }

            convertView.setTag(holder);
        }
        return holder;
    }

    protected View getFolderView(Folder folder, int position, View c, ViewGroup p) {
        if (c == null) {
            int layout = mUseSubFolderButtons ? R.layout.list_item_icon_2 : R.layout.list_item_icon;
            c = getLayoutInflater().inflate(layout, p, false);
        }
        ViewHolder holder = getHolder(c);

        holder.icon.setImageResource(R.drawable.ic_np_folder);
        holder.text1.setText(folder.getFolderName());
        holder.menu.setOnClickListener(getOnMenuClickListener());

        if (mUseSubFolderButtons && !folder.getSubFolders().isEmpty()) {
            holder.icon2.setImageResource(R.drawable.subfolder);
            holder.icon2.setOnClickListener(mOnSubFolderClickListener);
        }

        return c;
    }

    protected View getEmptyFolderView(LayoutInflater i, View c, ViewGroup p) {
        ViewHolder holder;
        if (c == null) {
            c = i.inflate(R.layout.layout_img_caption, p, false);

            holder = new ViewHolder();
            holder.text1 = findView(c, android.R.id.text1);

            c.setTag(holder);
        } else {
            holder = (ViewHolder)c.getTag();
        }
        holder.text1.setText(R.string.empty_folder);
        holder.text1.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.empty_subfolders, 0, 0);
        return c;
    }

    protected boolean isSubfoldersEmpty() {
        return mSubFolders.isEmpty();
    }

    @Override
    public boolean isEmpty() {
        return isSubfoldersEmpty();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        ViewHolder holder = getHolder(view);
        if (mOnMenuClickListener != null) {
            mOnMenuClickListener.onClick(holder.menu);
        }
        return true;
    }

    @Override
    public int getCount() {
        return mSubFolders.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if ( (position == 0)) {
            return TYPE_HEADER;
        }
        return isSubfoldersEmpty() ? TYPE_EMPTY_FOLDER : TYPE_FOLDER;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        switch (getItemViewType(position)) {
            case TYPE_FOLDER:
                return true;
            case TYPE_HEADER:
            case TYPE_EMPTY_FOLDER:
                return false;
            default:
                throw new AssertionError("unknown view type: " + getItemViewType(position) + " at position: " + position);
        }
    }

    @Override
    public Folder getItem(int position) {
        return mSubFolders.get(position - 1);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        switch (getItemViewType(position)) {
            case TYPE_HEADER:
                return getHeaderView(position, convertView, parent);
            case TYPE_FOLDER:
                return getFolderView(getItem(position), position, convertView, parent);
            case TYPE_EMPTY_FOLDER:
                return getEmptyFolderView(mInflater, convertView, parent);
            default:
                throw new AssertionError("unknown view type: " + getItemViewType(position) + " at position: " + position);
        }
    }

    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_header, parent, false);

            holder = new ViewHolder();
            holder.text1 = findView(convertView, android.R.id.text1);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        holder.text1.setText(R.string.folders);

        return convertView;
    }

    public final void setOnSubFolderClickListener(OnClickListener onSubFolderClickListener) {
        mOnSubFolderClickListener = onSubFolderClickListener;
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
