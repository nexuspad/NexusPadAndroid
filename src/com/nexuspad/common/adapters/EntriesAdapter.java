/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.common.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemLongClickListener;
import com.google.common.base.Strings;
import com.nexuspad.R;
import com.nexuspad.service.datamodel.EntryList;
import com.nexuspad.service.datamodel.NPEntry;

/**
 * Common Adapter to be used in entries list view.
 *
 * @author Edmond
 */
public abstract class EntriesAdapter<T extends NPEntry> extends BaseAdapter implements OnItemLongClickListener {
	public static final String TAG = EntriesAdapter.class.getSimpleName();

	public static final int TYPE_HEADER = 0;
	public static final int TYPE_ENTRY = 1;

	private final LayoutInflater mInflater;
	private final String mEntryHeaderText;

	protected EntryList mDisplayEntryList;

	private OnClickListener mOnMenuClickListener;

	/**
	 * use this constructor if you want filtering abilities
	 */
	public EntriesAdapter(Activity a, EntryList entryList) {
		mDisplayEntryList = entryList;
		mInflater = a.getLayoutInflater();
		mEntryHeaderText = getEntriesHeaderText();
	}

	protected abstract View getEntryView(T entry, int position, View convertView, ViewGroup parent);

	/**
	 * Swap out the current entries with the specified one. The original entries passed in the constructor will
	 * be preserved.
	 *
	 * @param entryList the new entries to be displayed
	 */
	public void setDisplayEntryList(EntryList entryList) {
		mDisplayEntryList = entryList;
		notifyDataSetChanged();
	}

	public EntryList getDisplayEntryList() {
		return mDisplayEntryList;
	}

	/**
	 * @return the string id; or 0 if no headers should be used
	 */
	protected abstract String getEntriesHeaderText();

	private boolean isHeaderEnabled() {
		return !Strings.isNullOrEmpty(mEntryHeaderText);
	}

	protected static ListViewHolder getHolder(View convertView) {
		ListViewHolder holder = (ListViewHolder) convertView.getTag();
		if (holder == null) {
			holder = new ListViewHolder();
			holder.icon = (ImageView)convertView.findViewById(android.R.id.icon);
			holder.text1 = (TextView)convertView.findViewById(android.R.id.text1);
			holder.menu = (ImageButton)convertView.findViewById(R.id.menu);
			if (holder.menu != null) {
				holder.menu.setFocusable(false);
			}

			convertView.setTag(holder);
		}
		return holder;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		ListViewHolder holder = getHolder(view);
		OnClickListener listener = getOnMenuClickListener();
		if (listener != null) {
			listener.onClick(holder.menu);
		}
		return true;
	}

	@Override
	public boolean isEmpty() {
		return mDisplayEntryList == null || mDisplayEntryList.getEntries().isEmpty();
	}

	@Override
	public int getCount() {
		if (mDisplayEntryList == null || mDisplayEntryList.getEntries().isEmpty()) {
			// One view - empty view
			return 1;
		}

		if (isHeaderEnabled()) {
			// Header view and entry views
			return mDisplayEntryList.getEntries().size() + 1;
		}

		// Entry views
		return mDisplayEntryList.getEntries().size();
	}

	@Override
	public int getItemViewType(int position) {
		if (position == 0 && isHeaderEnabled()) {
			return TYPE_HEADER;
		}
		return TYPE_ENTRY;
	}

	@Override
	public int getViewTypeCount() {
		return 2; // header and entry
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
				return false;
			default:
				throw new AssertionError("unknown view type: " + getItemViewType(position) + " at position: " + position);
		}
	}

	@Override
	public T getItem(int position) {
		if (mDisplayEntryList == null || mDisplayEntryList.getEntries().isEmpty()) {
			return null;
		}
		return (T)mDisplayEntryList.getEntries().get(isHeaderEnabled() ? position - 1 : position);
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
			default:
				throw new AssertionError("unknown view type: " + getItemViewType(position) + " at position: " + position);
		}
	}

	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		View header = mInflater.inflate(R.layout.list_header, parent, false);
		TextView headerText = (TextView)header.findViewById(android.R.id.text1);
		headerText.setText(getEntriesHeaderText());

		return header;
	}

	protected static View getCaptionView(LayoutInflater i, View c, ViewGroup p, int stringId, int drawableId) {
		ListViewHolder holder;
		if (c == null) {
			c = i.inflate(R.layout.layout_img_caption, p, false);

			holder = new ListViewHolder();
			holder.text1 = (TextView)c.findViewById(android.R.id.text1);

			c.setTag(holder);
		} else {
			holder = (ListViewHolder) c.getTag();
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


	public boolean hasMoreToLoad() {
		if (mDisplayEntryList.getEntries().size() < mDisplayEntryList.getTotalCount()) {
			return true;
		}
		return false;
	}

}
