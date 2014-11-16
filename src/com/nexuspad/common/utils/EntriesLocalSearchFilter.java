package com.nexuspad.common.utils;

import android.text.TextUtils;
import android.widget.Filter;
import com.nexuspad.app.App;
import com.nexuspad.service.datamodel.EntryList;
import com.nexuspad.service.datamodel.NPEntry;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by ren on 7/23/14.
 */
public class EntriesLocalSearchFilter<T extends NPEntry> extends Filter {

	final WeakReference<OnFilterDoneListener<T>> mOnFilterDoneListener;
	private final EntryList mRawEntryList;

	/**
	 * Interface needs to be implemented in Fragments to handle the filtered result.
	 * @param <E>
	 */
	public interface OnFilterDoneListener<E extends NPEntry> {
		void onFilterDone(List<E> displayEntries);
	}

	public EntriesLocalSearchFilter() {
		mRawEntryList = null;
		mOnFilterDoneListener = null;
	}

	/**
	 * @param onFilterDoneListener weak-referenced, do not use anonymous inner class
	 */
	public EntriesLocalSearchFilter(EntryList entryList, OnFilterDoneListener<T> onFilterDoneListener) {
		mRawEntryList = entryList;
		mOnFilterDoneListener = new WeakReference<OnFilterDoneListener<T>>(onFilterDoneListener);
	}

	@Override
	protected FilterResults performFiltering(CharSequence constraint) {
		final List<NPEntry> willDisplayEntries = new ArrayList<NPEntry>();

		willDisplayEntries.clear();
		if (TextUtils.isEmpty(constraint)) {
			willDisplayEntries.addAll(mRawEntryList.getEntries());
			return null;
		}

		final Pattern pattern = App.createSearchPattern(constraint.toString().trim());

		for (NPEntry entry : (List<? extends NPEntry>)mRawEntryList.getEntries()) {
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

		if (mOnFilterDoneListener != null) {
			final OnFilterDoneListener<T> listener = mOnFilterDoneListener.get();
			if (listener != null) {
				listener.onFilterDone(list);
			}
		}
	}
}