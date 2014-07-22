/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.common.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import com.nexuspad.common.annotaion.FragmentName;
import com.nexuspad.R;
import com.nexuspad.datamodel.NPFolder;
import com.nexuspad.common.activity.FoldersActivity;

/**
 * @author Edmond
 */
@FragmentName(NewFolderFragment.TAG)
public class NewFolderFragment extends DialogFragment {
	public static final String TAG = "NewFolderFragment";

	private static final String KEY_FOLDER = "key_folder";
	private static final String KEY_ORIGINAL_FOLDER_NAME = "key_original_folder_name";

	// request code for startActivityForResult(Intent, int)
	private static final int REQ_FOLDER = 1;

	/**
	 * @return a {@link NewFolderFragment} with a parent folder
	 */
	public static NewFolderFragment of(NPFolder parent) {
		return NewFolderFragment.of(parent, null);
	}

	/**
	 * @return a {@link NewFolderFragment} with a default folder name
	 */
	public static NewFolderFragment of(NPFolder parent, NPFolder folder) {
		Bundle bundle = new Bundle();
		bundle.putParcelable(KEY_FOLDER, parent);
		bundle.putParcelable(KEY_ORIGINAL_FOLDER_NAME, folder);

		NewFolderFragment fragment = new NewFolderFragment();
		fragment.setArguments(bundle);

		return fragment;
	}

	private TextView mFolderV;
	private EditText mFolderNameV;

	private NPFolder mParentFolder;
	private NPFolder mOriginalFolder;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle arguments = getArguments();
		if (arguments != null) {
			mParentFolder = arguments.getParcelable(KEY_FOLDER);
			mOriginalFolder = arguments.getParcelable(KEY_ORIGINAL_FOLDER_NAME);
		}

		if (mParentFolder == null) {
			throw new IllegalStateException("You must pass in a parent Folder with KEY_FOLDER.");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.folder_new_frag, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mFolderNameV = (EditText)view.findViewById(R.id.txt_folder_name);
		mFolderV = (TextView)view.findViewById(R.id.lbl_folder);

		installListeners();
		updateUI();
	}

	private void installListeners() {
		mFolderV.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final FragmentActivity activity = getActivity();
				NPFolder root = NPFolder.rootFolderOf(mParentFolder.getModuleId(), activity);
				Intent intent = FoldersActivity.ofParentFolder(activity, root);
				startActivityForResult(intent, REQ_FOLDER);
			}
		});
	}

	private void updateUI() {
		mFolderV.setText(mParentFolder.getFolderName());
		if (mOriginalFolder != null) {
			mFolderNameV.setText(mOriginalFolder.getFolderName());
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != Activity.RESULT_OK) {
			return;
		}

		switch (requestCode) {
			case REQ_FOLDER:
				mParentFolder = data.getParcelableExtra(FoldersActivity.KEY_FOLDER);
				mFolderV.setText(mParentFolder.getFolderName());
				break;
			default:
				throw new AssertionError("unknown requestCode: " + requestCode);
		}
	}

	public boolean isEditedFolderValid() {
		boolean isEmpty = TextUtils.isEmpty(mFolderNameV.getText().toString());
		if (isEmpty) {
			mFolderNameV.setError(mFolderNameV.getContext().getText(R.string.err_empty_field));
			mFolderNameV.requestFocus();
		}
		return isEmpty;
	}

	public NPFolder getEditedFolder() {
		NPFolder folder = mOriginalFolder == null ? new NPFolder(mParentFolder.getModuleId()) : new NPFolder(mOriginalFolder);
		folder.setParent(mParentFolder);
		folder.setFolderName(mFolderNameV.getText().toString());
		return folder;
	}

	public NPFolder getParentFolder() {
		return mParentFolder;
	}
}
