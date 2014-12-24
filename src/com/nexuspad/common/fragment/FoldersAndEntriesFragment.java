package com.nexuspad.common.fragment;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.BaseAdapter;
import com.google.common.collect.Iterables;
import com.nexuspad.Manifest;
import com.nexuspad.common.adapters.FoldersAndEntriesAdapter;
import com.nexuspad.common.adapters.ListFoldersAdapter;
import com.nexuspad.common.adapters.OnFolderMenuClickListener;
import com.nexuspad.service.account.AccountManager;
import com.nexuspad.service.datamodel.EntryList;
import com.nexuspad.service.datamodel.NPFolder;
import com.nexuspad.service.dataservice.FolderService;
import com.nexuspad.service.dataservice.NPException;
import com.nexuspad.service.dataservice.ServiceError;

import java.util.List;

/**
 * Created by ren on 7/24/14.
 */
public class FoldersAndEntriesFragment extends EntriesFragment {
	private static final String TAG = FoldersAndEntriesFragment.class.getSimpleName();

	private FoldersAndEntriesAdapter mFolderEntryCombinedAdapter;

	private FolderService mFolderService = null;

	/**
	 * Handles folder update/delete
	 */
	private final FolderService.FolderReceiver mFolderReceiver = new FolderService.FolderReceiver() {
		@Override
		protected void onNew(Context c, Intent i, NPFolder f) {
			final List<NPFolder> subFolders = getSubFolders();
			if (mFolder.getFolderId() == f.getParentId()) {
				if (!Iterables.tryFind(subFolders, f.filterById()).isPresent()) {
					if (subFolders.size() == 0) {
						subFolders.add(f);
					} else {
						subFolders.add(0, f);
					}
					refreshUIAfterUpdatingEntryList();
				} else {
					Log.w(TAG, "folder created on the server, but ID already exists in the list, updating instead: " + f);
					onUpdate(c, i, f);
				}
			}
		}

		@Override
		protected void onDelete(Context c, Intent i, NPFolder folder) {
		}

		@Override
		protected void onUpdate(Context c, Intent i, NPFolder folder) {
			if (mFolder.getFolderId() == folder.getParentId()) {
				final List<NPFolder> subFolders = getSubFolders();
				final int index = Iterables.indexOf(subFolders, folder.filterById());
				if (index >= 0) {
					subFolders.remove(index);
					subFolders.add(index, folder);
					refreshUIAfterUpdatingEntryList();
				} else {
					Log.w(TAG, "cannot find the updated entry in the list; folder: " + folder);
				}
			}
		}

		@Override
		protected void onError(Context context, Intent intent, ServiceError error) {
			super.onError(context, intent, error);
			Log.e(TAG, error.toString());
			handleServiceError(error);
		}
	};


	@Override
	public BaseAdapter getAdapter() {
		return mFolderEntryCombinedAdapter;
	}

	@Override
	public void setAdapter(BaseAdapter adapter) {
		mFolderEntryCombinedAdapter = (FoldersAndEntriesAdapter)adapter;
	}

	@Override
	protected void doSearch(String keyword) {
		showProgressIndicator();
		mCurrentSearchKeyword = keyword;

		try {
			mEntryListService.searchEntriesInFolder(keyword, mFolder, mModuleInfo.template(), 0, 99);
		} catch (NPException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void onSearchLoaded(EntryList list) {
		mFolderEntryCombinedAdapter.getEntriesAdapter().setDisplayEntryList(list);
	}

	@Override
	protected void reDisplayListEntries() {
		hideProgressIndicatorAndShowMainList();

		// Need to reset the scroll listener.
		mLoadMoreScrollListener.reset();

		mFolderEntryCombinedAdapter.getEntriesAdapter().setDisplayEntryList(mEntryList);
		mFolderEntryCombinedAdapter.notifyDataSetChanged();
	}


	@Override
	public void onResume() {
		super.onResume();

		getActivity().registerReceiver(mFolderReceiver,
				FolderService.FolderReceiver.getIntentFilter(),
				Manifest.permission.LISTEN_FOLDER_CHANGES,
				null);
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(mFolderReceiver);
	}


	public FolderService getFolderService() {
		if (mFolderService == null) {
			mFolderService = FolderService.getInstance(getActivity());
		}
		return mFolderService;
	}

	protected ListFoldersAdapter newFoldersAdapter() {
		ListFoldersAdapter foldersAdapter = new ListFoldersAdapter(getActivity(), getSubFolders());
		OnFolderMenuClickListener listener = new OnFolderMenuClickListener(getListView(), mFolder, getFolderService(), getUndoBarController());
		foldersAdapter.setOnMenuClickListener(listener);
		return foldersAdapter;
	}


	/**
	 * Folder specific undo bar actions.
	 *
	 * @param token
	 */
	@Override
	public void onUndoBarFinishShowing(Intent token) {
		if (token != null) {
			final String action = token.getAction();
			final NPFolder folder = token.getParcelableExtra(FolderService.KEY_FOLDER);
			final FolderService service = getFolderService();

			if (FolderService.ACTION_DELETE.equals(action)) {
				try {
					folder.setOwner(AccountManager.currentAccount());
					service.deleteFolder(folder);
				} catch (NPException e) {
					Log.e(TAG, e.toString());
				}
			} else {
				super.onUndoBarFinishShowing(token);
			}
		}
	}

}
