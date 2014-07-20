package com.nexuspad.ui.adapters;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.nexuspad.R;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.ui.FoldersAdapter;
import com.nexuspad.ui.fragment.FoldersFragment;
import com.nexuspad.ui.fragment.FoldersFragment.Callback;


import java.util.List;

import static com.edmondapps.utils.android.view.ViewUtils.findView;

/**
 * Created by ren on 7/16/14.
 */
public class FolderNavigatorAdapter extends FoldersAdapter {

	private final Folder mParent;
	private FoldersFragment.Callback mCallback;

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

	public FolderNavigatorAdapter(Activity a, List<? extends Folder> folders, Folder parent, Callback callback) {
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
			holder.setText1((TextView) findView(convertView, android.R.id.text1));

			convertView.setTag(holder);
		} else {
			holder = (FolderNavigatorViewHolder) convertView.getTag();
		}

		holder.getText1().setText(parent.getResources().getString(R.string.formatted_sub_folders, mParent.getFolderName()));

		holder.getText1().setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mParent.getFolderId() == Folder.ROOT_FOLDER) {
					mCallback.onFolderClicked(mParent);
				} else {
					mCallback.onUpFolderClicked();
				}
			}
		});

		return convertView;
	}

}
