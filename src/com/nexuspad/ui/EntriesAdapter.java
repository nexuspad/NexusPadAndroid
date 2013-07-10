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
import com.nexuspad.datamodel.NPEntry;

/**
 * @author Edmond
 * 
 */
public abstract class EntriesAdapter<T extends NPEntry> extends BaseAdapter implements OnItemLongClickListener {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ENTRY = 1;
    public static final int TYPE_EMPTY_ENTRY = 2;

    private final List<? extends T> mEntries;
    private final LayoutInflater mInflater;

    private OnClickListener mOnMenuClickListener;

    public EntriesAdapter(Activity a, List<? extends T> entries) {
        mEntries = entries;
        mInflater = a.getLayoutInflater();
    }

    protected abstract View getEntryView(T entry, int position, View convertView, ViewGroup parent);

    protected abstract View getEmptyEntryView(LayoutInflater inflater, View convertView, ViewGroup parent);

    protected abstract int getEntryStringId();

    protected static class ViewHolder {
        public ImageView icon;
        public TextView text1;
        public ImageButton menu;
    }

    protected static ViewHolder getHolder(View convertView) {
        ViewHolder holder = (ViewHolder)convertView.getTag();
        if (holder == null) {
            holder = new ViewHolder();
            holder.icon = findView(convertView, android.R.id.icon);
            holder.text1 = findView(convertView, android.R.id.text1);
            holder.menu = findView(convertView, R.id.menu);
            holder.menu.setFocusable(false);

            convertView.setTag(holder);
        }
        return holder;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        ViewHolder holder = getHolder(view);
        OnClickListener listener = getOnMenuClickListener();
        if (listener != null) {
            listener.onClick(holder.menu);
        }
        return true;
    }

    protected boolean isEntriesEmpty() {
        return mEntries.isEmpty();
    }

    @Override
    public boolean isEmpty() {
        return isEntriesEmpty();
    }

    @Override
    public int getCount() {
        return mEntries.size() + 1;// header
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        } else {
            return isEntriesEmpty() ? TYPE_EMPTY_ENTRY : TYPE_ENTRY;
        }
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
            case TYPE_ENTRY:
                return true;
            case TYPE_HEADER:
            case TYPE_EMPTY_ENTRY:
                return false;
            default:
                throw new AssertionError("unknown view type: " + getItemViewType(position) + " at position: " + position);
        }
    }

    @Override
    public T getItem(int position) {
        return mEntries.get(position - 1);
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
            case TYPE_ENTRY:
                return getEntryView(getItem(position), position, convertView, parent);
            case TYPE_EMPTY_ENTRY:
                return getEmptyEntryView(mInflater, convertView, parent);
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

        holder.text1.setText(getEntryStringId());

        return convertView;
    }

    protected static View getCaptionView(LayoutInflater i, View c, ViewGroup p, int stringId, int drawableId) {
        ViewHolder holder;
        if (c == null) {
            c = i.inflate(R.layout.layout_img_caption, p, false);

            holder = new ViewHolder();
            holder.text1 = findView(c, android.R.id.text1);

            c.setTag(holder);
        } else {
            holder = (ViewHolder)c.getTag();
        }
        holder.text1.setText(stringId);
        holder.text1.setCompoundDrawablesWithIntrinsicBounds(0, drawableId, 0, 0);
        return c;
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
