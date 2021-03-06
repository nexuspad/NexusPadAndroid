/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.common.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import com.nexuspad.R;
import com.nexuspad.service.account.AccountManager;
import com.nexuspad.common.fragment.UpdateFolderFragment;
import com.nexuspad.service.datamodel.NPFolder;
import com.nexuspad.service.dataservice.FolderService;
import com.nexuspad.service.dataservice.NPException;

/**
 * @author Edmond
 */
public class UpdateFolderActivity extends DoneDiscardActivity {
	public static final String TAG = "NewFolderActivity";
	public static final String KEY_FOLDER = "key_folder";
	public static final String KEY_ORIGINAL_FOLDER = "key_original_folder";

	/**
	 * Start a new {@link UpdateFolderActivity} with the default parent folder
	 * {@code f}.
	 */
	public static void startWithParentFolder(NPFolder f, Context c) {
		startWithParentFolder(f, null, c);
	}

	/**
	 * Start a new {@link UpdateFolderActivity} with the default parent folder
	 * {@code p} and default folder {@code f}.
	 */
	public static void startWithParentFolder(NPFolder p, NPFolder f, Context c) {
		Intent intent = new Intent(c, UpdateFolderActivity.class);
		intent.putExtra(KEY_FOLDER, p);
		intent.putExtra(KEY_ORIGINAL_FOLDER, f);
		c.startActivity(intent);
	}

	private FolderService mFolderService;

	private NPFolder mParentFolder;
	private NPFolder mOriginalFolder;

	@Override
	protected int onCreateLayoutId() {
		return R.layout.np_padding_activity;
	}

	@Override
	protected void onCreate(Bundle savedState) {
		mFolderService = FolderService.getInstance(this);

		final Intent intent = getIntent();
		mParentFolder = intent.getParcelableExtra(KEY_FOLDER);
		mOriginalFolder = intent.getParcelableExtra(KEY_ORIGINAL_FOLDER);

		super.onCreate(savedState);
	}

	@Override
	protected Fragment onCreateFragment() {
		return UpdateFolderFragment.of(mParentFolder, mOriginalFolder);
	}

	@Override
	protected UpdateFolderFragment getFragment() {
		return (UpdateFolderFragment) super.getFragment();
	}

	@Override
	protected void onDonePressed() {
		UpdateFolderFragment fragment = getFragment();

		if (fragment.isEditedFolderValid()) {
			final NPFolder editedFolder = fragment.getEditedFolder();
			if (!editedFolder.equals(mOriginalFolder)) {
				try {
					editedFolder.setOwner(AccountManager.currentAccount());
				} catch (NPException e) {
					throw new AssertionError("I thought I'm logged in.");
				}
				mFolderService.updateFolder(editedFolder);
			} else {
				Log.w(TAG, "folder not updated because no changes when made: " + editedFolder);
			}
			finish();
		}
	}
}
