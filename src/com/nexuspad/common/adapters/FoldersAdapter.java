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
import com.nexuspad.R;
import com.nexuspad.datamodel.NPFolder;

import java.util.List;

/**
 * @author Edmond
 */
public class FoldersAdapter extends BaseAdapter implements OnItemLongClickListener {
	public static final int TYPE_HEADER = 0;
	public static final int TYPE_FOLDER = 1;
	public static final int TYPE_EMPTY_FOLDER = 2;

	protected final LayoutInflater mInflater;

	private final List<? extends NPFolder> mSubFolders;
	private final boolean mUseSubFolderButtons;

	private OnClickListener mOnMenuClickListener;
	private OnClickListener mOnSubFolderClickListener;
	private boolean mShouldHide;

	public FoldersAdapter(Activity a, List<? extends NPFolder> folders) {
		this(a, folders, false);
	}

	public FoldersAdapter(Activity a, List<? extends NPFolder> folders, boolean useSubFolderButtons) {
		mUseSubFolderButtons = useSubFolderButtons;
		mInflater = a.getLayoutInflater();
		mSubFolders = folders;
	}

	public void setShouldHide(boolean shouldHide) {
		mShouldHide = shouldHide;
		notifyDataSetChanged();
	}

	public boolean getShouldHide() {
		return mShouldHide;
	}

	public static class FolderViewHolder {
		ImageView icon;
		ImageButton icon2;
		TextView text1;
		ImageButton menu;

		public TextView getText1() {
			return text1;
		}

		public void setText1(TextView textView) {
			text1 = textView;
		}
	}

	private static FolderViewHolder getHolder(View convertView) {
		FolderViewHolder holder = (FolderViewHolder) convertView.getTag();
		if (holder == null) {
			holder = new FolderViewHolder();
			holder.icon = (ImageView)convertView.findViewById(android.R.id.icon);
			holder.icon2 = (ImageButton)convertView.findViewById(android.R.id.icon2);
			holder.text1 = (TextView)convertView.findViewById(android.R.id.text1);
			holder.menu = (ImageButton)convertView.findViewById(R.id.menu);

			holder.menu.setFocusable(false);
			if (holder.icon2 != null) {
				holder.icon2.setFocusable(false);
			}

			convertView.setTag(holder);
		}
		return holder;
	}

	protected View getFolderView(NPFolder folder, int position, View c, ViewGroup p) {
		if (c == null) {
			int layout = mUseSubFolderButtons ? R.layout.list_item_with_2_icon : R.layout.list_item_with_icon;
			c = getLayoutInflater().inflate(layout, p, false);
		}
		FolderViewHolder holder = getHolder(c);

		holder.icon.setImageResource(R.drawable.ic_np_folder);
		holder.text1.setText(folder.getFolderName());
		holder.menu.setOnClickListener(getOnMenuClickListener());

		if (mUseSubFolderButtons) {
			if (!folder.getSubFolders().isEmpty()) {
				holder.icon2.setVisibility(View.VISIBLE);
				holder.icon2.setImageResource(R.drawable.subfolder);
				holder.icon2.setOnClickListener(mOnSubFolderClickListener);
			} else {
				holder.icon2.setVisibility(View.INVISIBLE);
			}
		}

		return c;
	}

	protected View getEmptyFolderView(LayoutInflater i, View c, ViewGroup p) {
		FolderViewHolder holder;
		if (c == null) {
			c = i.inflate(R.layout.layout_img_caption, p, false);

			holder = new FolderViewHolder();
			holder.text1 = (TextView)c.findViewById(android.R.id.text1);

			c.setTag(holder);
		} else {
			holder = (FolderViewHolder) c.getTag();
		}
		holder.text1.setText(R.string.empty_folder);
		holder.text1.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.empty_subfolders, 0, 0);
		return c;
	}

	@Override
	public boolean isEmpty() {
		return getShouldHide() || mSubFolders.isEmpty();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		FolderViewHolder holder = getHolder(view);
		if (mOnMenuClickListener != null) {
			mOnMenuClickListener.onClick(holder.menu);
		}
		return true;
	}

	@Override
	public int getCount() {
		return (isEmpty() || getShouldHide()) ? 0 : mSubFolders.size() + 1;
	}

	@Override
	public int getItemViewType(int position) {
		if (mSubFolders.isEmpty()) {
			return TYPE_EMPTY_FOLDER;
		}
		if (position == 0) {
			return TYPE_HEADER;
		}
		return TYPE_FOLDER;
	}

	@Override
	public int getViewTypeCount() {
		return mSubFolders.isEmpty() ? 1 : 3;
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
	public NPFolder getItem(int position) {
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
				View headerView = getHeaderView(position, convertView, parent);
				return headerView;

			case TYPE_FOLDER:
				return getFolderView(getItem(position), position, convertView, parent);

			case TYPE_EMPTY_FOLDER:
				return getEmptyFolderView(mInflater, convertView, parent);

			default:
				throw new AssertionError("unknown view type: " + getItemViewType(position) + " at position: " + position);
		}
	}

	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		FolderViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_header, parent, false);

			holder = new FolderViewHolder();
			holder.text1 = (TextView)convertView.findViewById(android.R.id.text1);

			convertView.setTag(holder);
		} else {
			holder = (FolderViewHolder) convertView.getTag();
		}

		holder.text1.setText(getHeaderText(position, convertView, parent));

		return convertView;
	}

	protected CharSequence getHeaderText(int position, View convertView, ViewGroup parent) {
		return parent.getResources().getText(R.string.folders);
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
