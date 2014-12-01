/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.common.activity;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MenuItem;
import com.nexuspad.R;
import com.nexuspad.common.Constants;
import com.nexuspad.common.fragment.FoldersNavigatorFragment;
import com.nexuspad.service.datamodel.NPFolder;
import com.nexuspad.service.dataservice.FolderService;

/**
 * @author Edmond
 */
public class FoldersNavigatorActivity extends SinglePaneActivity implements FoldersNavigatorFragment.NavigationCallback {
	public static final String TAG = FoldersNavigatorActivity.class.getSimpleName();
	private static final int REQ_FOLDER = 1;

	/**
	 * @param c      the {@code Context}
	 * @param folder the parent folder of the folders list
	 * @return an {@code Intent} with the correct extras.
	 */
	public static Intent ofParentFolder(Context c, NPFolder folder) {
		Intent intent = new Intent(c, FoldersNavigatorActivity.class);
		intent.putExtra(Constants.KEY_FOLDER, folder);
		return intent;
	}

	private NPFolder mParentFolder;

	@Override
	protected int onCreateLayoutId() {
		return R.layout.np_padding_activity;
	}

	@Override
	protected void onCreate(Bundle savedState) {
		mParentFolder = getIntent().getParcelableExtra(Constants.KEY_FOLDER);

		setResult(RESULT_CANCELED);
		super.onCreate(savedState);

		final ActionBar actionBar = getActionBar();
		actionBar.setIcon(R.drawable.ic_ab);
		actionBar.setDisplayHomeAsUpEnabled(true);
		setTitle(mParentFolder.getFolderName());
	}

	@Override
	protected Fragment onCreateFragment() {
		Bundle bundle = new Bundle();
		bundle.putParcelable(Constants.KEY_PARENT_FOLDER, mParentFolder);

		FoldersNavigatorFragment fragment = new FoldersNavigatorFragment();
		fragment.setArguments(bundle);

		return fragment;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onFolderClicked(NPFolder folder) {
		setResult(RESULT_OK, new Intent().putExtra(Constants.KEY_FOLDER, folder));
		finish();
	}

	@Override
	public void onSubFolderClicked(NPFolder folder) {
		final Intent intent = FoldersNavigatorActivity.ofParentFolder(this, folder);
		startActivityForResult(intent, REQ_FOLDER);
	}

	@Override
	public void onUpFolderClicked() {
		final int moduleId = mParentFolder.getModuleId();
		final int folderId = mParentFolder.getFolderId();

		if (folderId == NPFolder.ROOT_FOLDER) {
			finish();
		}

		final FolderService service = FolderService.getInstance(this);
		final NPFolder fullFolder = service.getFolder(moduleId, folderId);

		NPFolder upFolder = null;
		if (fullFolder != null) {
			final int parentId = fullFolder.getParentId();
			upFolder = service.getFolder(moduleId, parentId);
		}
		if (upFolder == null) {
			upFolder = NPFolder.rootFolderOf(moduleId, this);
		}

		final Intent upIntent = getGoBackIntent(FoldersNavigatorActivity.class);
		upIntent.putExtra(Constants.KEY_FOLDER, upFolder);
		startActivity(upIntent);
		finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK) {
			return;
		}
		switch (requestCode) {
			case REQ_FOLDER:
				setResult(RESULT_OK, data);
				finish();
				break;
			default:
				throw new AssertionError("unknown requestCode: " + requestCode);
		}
	}
}
