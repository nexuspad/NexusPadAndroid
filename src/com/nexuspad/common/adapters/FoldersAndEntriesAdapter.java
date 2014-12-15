package com.nexuspad.common.adapters;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import com.nexuspad.service.datamodel.EntryList;

/**
 * Created by ren on 7/18/14.
 */
public class FoldersAndEntriesAdapter<T extends EntriesAdapter<?>> extends BaseAdapter implements AdapterView.OnItemLongClickListener {
	private final ListFoldersAdapter mFolderAdapter;
	private final T mEntriesAdapter;

	public FoldersAndEntriesAdapter(ListFoldersAdapter folderAdapter, T entriesAdapter) {
		mFolderAdapter = folderAdapter;
		mEntriesAdapter = entriesAdapter;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		if (isPositionFolder(position)) {
			return mFolderAdapter.onItemLongClick(parent, view, position, id);
		} else {
			return mEntriesAdapter.onItemLongClick(parent, view, position - mFolderAdapter.getCount(), id);
		}
	}

	public void setDisplayFoldersAndEntries(EntryList entryList) {
		mFolderAdapter.setSubFolders(entryList.getFolder().getSubFolders());
		mFolderAdapter.notifyDataSetChanged();
		mEntriesAdapter.setDisplayEntryList(entryList);
	}

	@Override
	public int getCount() {
		if (mEntriesAdapter.isEmpty()) {
			if (mFolderAdapter.isEmpty()) {
				return 1;
			} else {
				return mFolderAdapter.getCount();
			}
		}

		return mFolderAdapter.getCount() + mEntriesAdapter.getCount();
	}

	public void setShouldHideFolders(boolean shouldHideFolders) {
		mFolderAdapter.setShouldHide(shouldHideFolders);
		notifyDataSetChanged();
	}

	public boolean getShouldHideFolders() {
		return mFolderAdapter.getShouldHide();
	}

	@Override
	public boolean hasStableIds() {
		if (!mFolderAdapter.hasStableIds() || !mEntriesAdapter.hasStableIds()) {
			return false;
		}
		return true;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		mFolderAdapter.registerDataSetObserver(observer);
		mEntriesAdapter.registerDataSetObserver(observer);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		mFolderAdapter.unregisterDataSetObserver(observer);
		mEntriesAdapter.unregisterDataSetObserver(observer);
	}

	@Override
	public void notifyDataSetChanged() {
		mFolderAdapter.notifyDataSetChanged();
		mEntriesAdapter.notifyDataSetChanged();
	}

	@Override
	public void notifyDataSetInvalidated() {
		mFolderAdapter.notifyDataSetInvalidated();
		mEntriesAdapter.notifyDataSetInvalidated();
	}

	@Override
	public boolean areAllItemsEnabled() {
		if (!mFolderAdapter.areAllItemsEnabled() || !mEntriesAdapter.areAllItemsEnabled()) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isEnabled(int position) {
		if (isPositionFolder(position)) {
			return mFolderAdapter.isEnabled(position);
		}
		return mEntriesAdapter.isEnabled(position);
	}

	@Override
	public int getItemViewType(int position) {
		if (isPositionFolder(position)) {
			return mFolderAdapter.getItemViewType(position);
		}

		return mEntriesAdapter.getItemViewType(position);
	}

	@Override
	public int getViewTypeCount() {
		return mFolderAdapter.getViewTypeCount() + mEntriesAdapter.getViewTypeCount();
	}

	@Override
	public boolean isEmpty() {
		if (mFolderAdapter.isEmpty() && mEntriesAdapter.isEmpty()) {
			return true;
		}
		return false;
	}

	@Override
	public Object getItem(int position) {
		if (isPositionFolder(position)) {
			return mFolderAdapter.getItem(position);
		} else {
			position = position - mFolderAdapter.getCount();
			return mEntriesAdapter.getItem(position);
		}
	}

	@Override
	public long getItemId(int position) {
		if (isPositionFolder(position)) {
			return mFolderAdapter.getItemId(position);
		} else {
			position = position - mFolderAdapter.getCount();
			return mEntriesAdapter.getItemId(position);
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (isPositionFolder(position)) {
			return mFolderAdapter.getView(position, convertView, parent);
		} else {
			position = position - mFolderAdapter.getCount();
			return mEntriesAdapter.getView(position, convertView, parent);
		}
	}

	public boolean isPositionFolder(int position) {
		if (mFolderAdapter.getCount() == 0) {
			return false;
		} else {
			if (position < mFolderAdapter.getCount()) {
				return true;
			}
		}
		return false;
	}

	public boolean isPositionEntries(int position) {
		return mEntriesAdapter.getCount() != 0 && !isPositionFolder(position);
	}

	private boolean isPositionLoadMore(int position) {
		if (position >= (getCount() - 1) && mEntriesAdapter.hasMoreToLoad()) {               // Shouldn't be greater
			return true;
		}
		return false;
	}

	public final ListFoldersAdapter getFoldersAdapter() {
		return mFolderAdapter;
	}

	public final T getEntriesAdapter() {
		return mEntriesAdapter;
	}

}
