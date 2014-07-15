/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemLongClickListener;
import com.edmondapps.utils.java.WrapperList;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.nexuspad.R;
import com.nexuspad.app.App;
import com.nexuspad.datamodel.EntryList;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.datamodel.NPEntry;
import com.nexuspad.dataservice.EntryListService;
import com.nexuspad.dataservice.NPException;
import com.nexuspad.ui.fragment.EntriesFragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.edmondapps.utils.android.view.ViewUtils.findView;

/**
 * Common Adapter to be used in entries list view.
 *
 * @author Edmond
 */
public abstract class EntriesAdapter<T extends NPEntry> extends BaseAdapter implements OnItemLongClickListener, EntriesFragment.FilterableAdapter {
	public static final int TYPE_HEADER = 0;
	public static final int TYPE_ENTRY = 1;
	public static final int TYPE_EMPTY_ENTRY = 2;

	private final ImmutableList<T> mRawEntries;     // unfiltered, original entries
	private final LayoutInflater mInflater;
	private final String mEntryHeaderText;

	private final Folder mFolder;
	private final EntryListService mService;
	private final EntryTemplate mTemplate;


	private List<T> mDisplayEntries;  // may be filtered entries displayed ons screen
	private OnClickListener mOnMenuClickListener;
	private boolean mResetConvertView;

	/**
	 * use this constructor if you want filtering abilities
	 */
	public EntriesAdapter(Activity a, List<T> entries, Folder folder, EntryListService service, EntryTemplate template) {
		mFolder = folder;
		mService = service;
		mTemplate = template;
		mRawEntries = ImmutableList.copyOf(entries);
		mDisplayEntries = entries;
		mInflater = a.getLayoutInflater();
		mEntryHeaderText = getEntriesHeaderText();
	}

	protected abstract View getEntryView(T entry, int position, View convertView, ViewGroup parent);

	protected abstract View getEmptyEntryView(LayoutInflater inflater, View convertView, ViewGroup parent);

	/**
	 * Swap out the current entries with the specified one. The original entries passed in the constructor will
	 * be preserved.
	 *
	 * @param displayEntries the new entries to be displayed
	 */
	public void setDisplayEntries(List<T> displayEntries) {
		mDisplayEntries = displayEntries;
		mResetConvertView = true;
		notifyDataSetChanged();
	}

	@Override
	public void setDisplayEntries(EntryList entries) {
		setDisplayEntries(new WrapperList<T>(entries.getEntries()));
	}

	/**
	 * Reset the adapter to display the original, unfiltered entries passed from the constructor.
	 */
	@Override
	public void showRawEntries() {
		mDisplayEntries.clear();
		mDisplayEntries.addAll(mRawEntries);
		mResetConvertView = true;
		notifyDataSetChanged();
	}

	/**
	 * @return the string id; or 0 if no headers should be used
	 */
	protected abstract String getEntriesHeaderText();

	private boolean isHeaderEnabled() {
		return !Strings.isNullOrEmpty(mEntryHeaderText);
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
			// One view - empty view
			return 1;
		}
		if (isHeaderEnabled()) {
			// Header view and entry views
			return mDisplayEntries.size() + 1;
		}

		// Entry views
		return mDisplayEntries.size();
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
		return 3; // header, entries, and empty view
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
		if (mResetConvertView) {
			convertView = null;
			mResetConvertView = false;
		}
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

		holder.text1.setText(getEntriesHeaderText());

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

	public ImmutableList<T> getRawEntries() {
		return mRawEntries;
	}

	/**
	 * default implementation calls {@link #filterWithWeb(String)}
	 *
	 * @param string
	 */
	@Override
	public void filter(String string) {
		filterWithWeb(string);
	}

	public void filterWithWeb(String string) {
		try {
			mService.searchEntriesInFolder(string, mFolder, mTemplate, 0, 99);
		} catch (NPException e) {
			throw new RuntimeException(e);
		}
	}

	public interface OnFilterDoneListener<E extends NPEntry> {
		void onFilterDone(List<E> displayEntries);
	}

	public class EntriesAdapterLocalFilter extends Filter {

		final WeakReference<OnFilterDoneListener<T>> mOnFilterDoneListener;

		public EntriesAdapterLocalFilter() {
			mOnFilterDoneListener = null;
		}

		/**
		 * @param onFilterDoneListener weak-referenced, do not use anonymous inner class
		 */
		public EntriesAdapterLocalFilter(OnFilterDoneListener<T> onFilterDoneListener) {
			mOnFilterDoneListener = new WeakReference<OnFilterDoneListener<T>>(onFilterDoneListener);
		}

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			final List<T> willDisplayEntries = new ArrayList<T>();

			willDisplayEntries.clear();
			if (TextUtils.isEmpty(constraint)) {
				willDisplayEntries.addAll(getRawEntries());
				return null;
			}
			final Pattern pattern = App.createSearchPattern(constraint.toString().trim());
			for (T entry : getRawEntries()) {
				if (entry.filterByPattern(pattern)) {
					willDisplayEntries.add(entry);
				}
			}

			final FilterResults results = new FilterResults();
			results.count = willDisplayEntries.size();
			results.values = willDisplayEntries;

			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			@SuppressWarnings("unchecked")
			final List<T> list = (List<T>) results.values;
			setDisplayEntries(list);

			if (mOnFilterDoneListener != null) {
				final OnFilterDoneListener<T> listener = mOnFilterDoneListener.get();
				if (listener != null) {
					listener.onFilterDone(list);
				}
			}
		}
	}
}
