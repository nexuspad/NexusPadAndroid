/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemLongClickListener;
import com.nexuspad.R;
import com.nexuspad.datamodel.NPEntry;

import java.util.List;

import static com.edmondapps.utils.android.view.ViewUtils.findView;

/**
 * @author Edmond
 */
public abstract class EntriesAdapter<T extends NPEntry> extends BaseAdapter implements OnItemLongClickListener {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ENTRY = 1;
    public static final int TYPE_EMPTY_ENTRY = 2;

    private final List<? extends T> mRawEntries;     // unfiltered, original entries
    private final LayoutInflater mInflater;
    private final int mEntryHeaderId;

    private List<? extends T> mDisplayEntries; // may be filtered entries displayed ons screen
    private OnClickListener mOnMenuClickListener;

    public EntriesAdapter(Activity a, List<? extends T> entries) {
        mRawEntries = entries;
        mDisplayEntries = entries;
        mInflater = a.getLayoutInflater();
        mEntryHeaderId = getEntryStringId();
    }

    protected abstract View getEntryView(T entry, int position, View convertView, ViewGroup parent);

    protected abstract View getEmptyEntryView(LayoutInflater inflater, View convertView, ViewGroup parent);

    /**
     * Swap out the current entries with the specified one. The original entries passed in the constructor will
     * be preserved.
     *
     * @param displayEntries the new entries to be displayed
     */
    protected void setDisplayEntries(List<? extends T> displayEntries) {
        mDisplayEntries = displayEntries;
        notifyDataSetChanged();
    }

    /**
     * Reset the adapter to display the original, unfiltered entries passed from the constructor.
     */
    public void showRawEntries() {
        mDisplayEntries = mRawEntries;
        notifyDataSetChanged();
    }

    /**
     * @return the string id; or 0 if no headers should be used
     */
    protected abstract int getEntryStringId();

    private boolean isHeaderEnabled() {
        return mEntryHeaderId > 0;
    }

    protected static class ViewHolder {
        public ImageView icon;
        public TextView text1;
        public ImageButton menu;
    }

    protected static ViewHolder getHolder(View convertView) {
        ViewHolder holder = (ViewHolder) convertView.getTag();
        if (holder == null) {
            holder = new ViewHolder();
            holder.icon = findView(convertView, android.R.id.icon);
            holder.text1 = findView(convertView, android.R.id.text1);
            holder.menu = findView(convertView, R.id.menu);
            if (holder.menu != null) {
                holder.menu.setFocusable(false);
            }

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

    @Override
    public boolean isEmpty() {
        return mDisplayEntries.isEmpty();
    }

    @Override
    public int getCount() {
        if (mDisplayEntries.isEmpty()) {
            return 1; // empty view
        }
        if (isHeaderEnabled()) {
            return mDisplayEntries.size() + 1; // header and entries
        }
        return mDisplayEntries.size(); // entries
    }

    @Override
    public int getItemViewType(int position) {
        if (mDisplayEntries.isEmpty()) {
            return TYPE_EMPTY_ENTRY;
        }
        if (position == 0 && isHeaderEnabled()) {
            return TYPE_HEADER;
        }
        return TYPE_ENTRY;
    }

    @Override
    public int getViewTypeCount() {
        if (mDisplayEntries.isEmpty()) {
            return 1; // empty view
        }
        if (isHeaderEnabled()) {
            return 2; // header and the entries
        }
        return 1; // entries
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
        return mDisplayEntries.get(isHeaderEnabled() ? position - 1 : position);
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
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_header, parent, false);
        }
        ViewHolder holder = getHolder(convertView);

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
            holder = (ViewHolder) c.getTag();
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
