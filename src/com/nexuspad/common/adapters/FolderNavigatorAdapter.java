package com.nexuspad.common.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.nexuspad.R;
import com.nexuspad.common.fragment.FoldersNavigatorFragment;
import com.nexuspad.service.datamodel.NPFolder;

import java.util.List;

/**
 * Created by ren on 7/16/14.
 */
public class FolderNavigatorAdapter extends FoldersAdapter {

	private final NPFolder mParent;
	private FoldersNavigatorFragment.NavigationCallback mCallback;

	/**
	 * View holder for folder navigator.
	 */
	public static class FolderNavigatorViewHolder {
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

	public FolderNavigatorAdapter(Activity a, List<? extends NPFolder> folders, NPFolder parent, FoldersNavigatorFragment.NavigationCallback callback) {
		super(a, folders, true);
		mParent = parent;
		mCallback = callback;
	}

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		FolderNavigatorViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.folder_nav_list_header, parent, false);

			holder = new FolderNavigatorViewHolder();
			holder.setText1((TextView) convertView.findViewById(android.R.id.text1));

			convertView.setTag(holder);
		} else {
			holder = (FolderNavigatorViewHolder) convertView.getTag();
		}

		holder.getText1().setText(parent.getResources().getString(R.string.formatted_sub_folders, mParent.getFolderName()));

		holder.getText1().setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mParent.getFolderId() == NPFolder.ROOT_FOLDER) {
					mCallback.onFolderClicked(mParent);
				} else {
					mCallback.onUpFolderClicked();
				}
			}
		});

		return convertView;
	}

}
