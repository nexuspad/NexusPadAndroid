package com.nexuspad.ui.adapters;

import android.database.DataSetObserver;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import com.nexuspad.ui.EntriesAdapter;
import com.nexuspad.ui.FoldersAdapter;

/**
 * Created by ren on 7/18/14.
 */
public class FoldersEntriesListAdapter<T extends EntriesAdapter<?>> extends BaseAdapter implements AdapterView.OnItemLongClickListener {
	private final FoldersAdapter mFolderAdapter;
	private final T mEntriesAdapter;
	private final BaseAdapter mLoadMoreAdapter;

	public FoldersEntriesListAdapter(FoldersAdapter folderAdapter, T entriesAdapter) {
		mFolderAdapter = folderAdapter;
		mEntriesAdapter = entriesAdapter;
		mLoadMoreAdapter = null;
	}

	public FoldersEntriesListAdapter(FoldersAdapter folderAdapter, T entriesAdapter, BaseAdapter loadMoreAdapter) {
		mFolderAdapter = folderAdapter;
		mEntriesAdapter = entriesAdapter;
		mLoadMoreAdapter = loadMoreAdapter;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		if (isPositionFolder(position)) {
			return mFolderAdapter.onItemLongClick(parent, view, position, id);
		} else {
			return mEntriesAdapter.onItemLongClick(parent, view, position - mFolderAdapter.getCount(), id);
		}
	}

	@Override
	public int getCount() {
		if (mEntriesAdapter.isEmpty()) {
			return 1;
		}

		if (mLoadMoreAdapter == null) {
			return mFolderAdapter.getCount() + mEntriesAdapter.getCount();
		}

		return mFolderAdapter.getCount() + mEntriesAdapter.getCount() + 1;
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
		} else if (isPositionLoadMore(position)) {
			return mLoadMoreAdapter.isEnabled(position);
		}
		return mEntriesAdapter.isEnabled(position);
	}

	@Override
	public int getItemViewType(int position) {
		if (isPositionFolder(position)) {
			return mFolderAdapter.getItemViewType(position);
		} else if (isPositionLoadMore(position)) {
			return mLoadMoreAdapter.getItemViewType(0);
		}

		return mEntriesAdapter.getItemViewType(position);
	}

	@Override
	public int getViewTypeCount() {
		return mFolderAdapter.getViewTypeCount() + mEntriesAdapter.getViewTypeCount() + (mLoadMoreAdapter == null ? 0 : 1);
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
		} else if (isPositionLoadMore(position)) {
			return mLoadMoreAdapter.getItem(0);
		} else {
			position = position - mFolderAdapter.getCount();
			return mEntriesAdapter.getItem(position);
		}
	}

	@Override
	public long getItemId(int position) {
		if (isPositionFolder(position)) {
			return mFolderAdapter.getItemId(position);
		} else if (isPositionLoadMore(position)) {
			return mLoadMoreAdapter.getItemId(0);
		} else {
			position = position - mFolderAdapter.getCount();
			return mEntriesAdapter.getItemId(position);
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (isPositionFolder(position)) {
			return mFolderAdapter.getView(position, convertView, parent);
		} else if (isPositionLoadMore(position)) {
			return mLoadMoreAdapter.getView(0, convertView, parent);
		} else {
			position = position - mFolderAdapter.getCount();
			return mEntriesAdapter.getView(position, convertView, parent);
		}
	}

	private boolean isPositionFolder(int position) {
		if (mFolderAdapter.getCount() == 0) {
			return false;
		} else {
			if (position < mFolderAdapter.getCount()) {
				return true;
			}
		}
		return false;
	}

	private boolean isPositionEntries(int position) {
		if (mEntriesAdapter.getCount() == 0) {
			return false;
		} else {
			if (position < mEntriesAdapter.getCount()) {
				return true;
			}
		}
		return false;
	}

	private boolean isPositionLoadMore(int position) {
		if (position >= getCount() && mLoadMoreAdapter != null) {               // Shouldn't be greater
			return true;
		}
		return false;
	}

	public final FoldersAdapter getFoldersAdapter() {
		return mFolderAdapter;
	}

	public final T getEntriesAdapter() {
		return mEntriesAdapter;
	}

}
