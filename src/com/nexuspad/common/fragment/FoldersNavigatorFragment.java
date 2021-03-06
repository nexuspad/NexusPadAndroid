/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.common.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.google.common.collect.Iterables;
import com.nexuspad.Manifest;
import com.nexuspad.R;
import com.nexuspad.app.App;
import com.nexuspad.common.Constants;
import com.nexuspad.common.activity.UpdateFolderActivity;
import com.nexuspad.common.adapters.FolderNavigatorAdapter;
import com.nexuspad.common.adapters.OnFolderMenuClickListener;
import com.nexuspad.common.annotation.FragmentName;
import com.nexuspad.service.account.AccountManager;
import com.nexuspad.service.datamodel.NPFolder;
import com.nexuspad.service.dataservice.FolderService;
import com.nexuspad.service.dataservice.FolderService.FolderReceiver;
import com.nexuspad.service.dataservice.NPException;
import com.nexuspad.service.dataservice.ServiceError;

import java.util.List;

/**
 * The Fragment for folder selector.
 *
 * @author Edmond
 */
@FragmentName(FoldersNavigatorFragment.TAG)
public class FoldersNavigatorFragment extends UndoBarFragment implements SwipeRefreshLayout.OnRefreshListener {
	public static final String TAG = "FoldersFragment";
	private static final String KEY_LIST_POS = "key_list_pos";

	private NavigationCallback mCallback;
	private FolderService mFolderService;
	private NPFolder mParentFolder;
	private List<NPFolder> mSubFolders;

	protected View mListFrame;
	private View mQuickReturnV;
	private ListView mListView;
	private FolderNavigatorAdapter mFoldersAdapter;

	public interface NavigationCallback {
		void onFolderClicked(NPFolder folder);

		void onSubFolderClicked(NPFolder folder);

		/**
		 * Called when the "up" folder is clicked, the activity should navigate to the parent of the current parent folder
		 */
		void onUpFolderClicked();
	}

	private final FolderReceiver mFolderReceiver = new FolderReceiver() {
		@Override
		protected void onGotAll(Context c, Intent i, List<NPFolder> folders) {
			mSubFolders = folders;
			initFolderNavigatorList(folders);
		}

		@Override
		protected void onDelete(Context c, Intent i, NPFolder folder) {
		}

		@Override
		protected void onNew(Context c, Intent i, NPFolder f) {
			if (!Iterables.tryFind(mSubFolders, f.filterById()).isPresent()) {
				mSubFolders.add(f);
				mFoldersAdapter.notifyDataSetChanged();
			} else {
				Log.w(TAG, "folder created on the server, but ID already exists in the list, updating instead: " + f);
				onUpdate(c, i, f);
			}
		}

		@Override
		protected void onUpdate(Context c, Intent i, NPFolder folder) {
			final int index = Iterables.indexOf(mSubFolders, folder.filterById());

			if (index >= 0) {
				mSubFolders.remove(index);
				mSubFolders.add(index, folder);
				mFoldersAdapter.notifyDataSetChanged();
			} else {
				Log.w(TAG, "cannot find the updated entry in the list; folder: " + folder);
			}
		}

		@Override
		protected void onError(Context context, Intent intent, ServiceError error) {
			super.onError(context, intent, error);
			Log.e(TAG, error.toString());
		}
	};


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mCallback = App.getCallbackOrThrow(activity, NavigationCallback.class);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.folders_topmenu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.new_folder:
				UpdateFolderActivity.startWithParentFolder(mParentFolder, getActivity());
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle arguments = getArguments();
		if (arguments != null) {
			mParentFolder = arguments.getParcelable(Constants.KEY_PARENT_FOLDER);
		}

		mFolderService = FolderService.getInstance(getActivity());
	}

	@Override
	public void onResume() {
		super.onResume();

		getActivity().registerReceiver(
				mFolderReceiver,
				FolderReceiver.getIntentFilter(),
				Manifest.permission.LISTEN_FOLDER_CHANGES,
				null);

		try {
			mParentFolder.setOwner(AccountManager.currentAccount());
			mFolderService.getSubFolders(mParentFolder);

		} catch (NPException e) {
			Log.e(TAG, e.toString());
			Toast.makeText(getActivity(), R.string.err_network, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(mFolderReceiver);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.main_list, container, false);
	}


	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		/*
		 * Init the loading UI manager.
		 */
		mListFrame = view.findViewById(R.id.main_list_frame);
		final View progressFrame = view.findViewById(R.id.frame_progress);
		final View retryFrame = view.findViewById(R.id.frame_retry);

		final View emptyFolderView = view.findViewById(R.id.frame_empty_folder);

		if (mListFrame != null && progressFrame != null && retryFrame != null) {
			mLoadingUiManager = new LoadingUiManager(mListFrame, emptyFolderView, retryFrame, progressFrame, new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mLoadingUiManager.showProgressView();
					onRetryClicked(v);
				}
			});
		}

		if (mListFrame instanceof android.support.v4.widget.SwipeRefreshLayout) {
			SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout)mListFrame;
			swipeLayout.setOnRefreshListener(this);
		}

		mQuickReturnV = view.findViewById(R.id.bottom_overlay);

		final View theView = view.findViewById(R.id.list_view);

		if (theView instanceof ListView) {
			mListView = (ListView) theView;
		}

		if (mListView != null) {
			/*
			 * click on folder to open entries.
			 */
			mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					mCallback.onFolderClicked(mFoldersAdapter.getItem(position));
				}
			});

			mListView.setItemsCanFocus(true);
			mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		}
	}


	/**
	 * Called when the sub-folders are retrieved.
	 *
	 * @param folders same as {@link #getSubFolders()}
	 */
	private void initFolderNavigatorList(List<NPFolder> folders) {
		mFoldersAdapter = new FolderNavigatorAdapter(getActivity(), folders, mParentFolder, mCallback);

		mFoldersAdapter.setOnMenuClickListener(new OnFolderMenuClickListener(mListView, mParentFolder, mFolderService, getUndoBarController()) {
			@Override
			public void onClick(View v) {
				final int pos = getListView().getPositionForView(v);
				onFolderClick(mFoldersAdapter.getItem(pos), pos, v);
			}
		});

		mFoldersAdapter.setOnSubFolderClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final int pos = mListView.getPositionForView(v);
				final NPFolder folder = mFoldersAdapter.getItem(pos);
				mCallback.onSubFolderClicked(folder);
			}
		});

		mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				return position != 0 && mFoldersAdapter.onItemLongClick(parent, view, position, id);
			}
		});

		mListView.setAdapter(mFoldersAdapter);

		hideProgressIndicatorAndShowMainList();
		((SwipeRefreshLayout)mListFrame).setRefreshing(false);
	}


	@Override
	public void onUndoButtonClicked(Intent token) {
		if (token != null) {
			final String action = token.getAction();
			final NPFolder folder = token.getParcelableExtra(FolderService.KEY_FOLDER);
			final int position = token.getIntExtra(KEY_LIST_POS, 0);

			if (FolderService.ACTION_DELETE.equals(action)) {
				mSubFolders.add(position, folder);
				mFoldersAdapter.notifyDataSetChanged();
			}
		}
	}

	@Override
	public void onUndoBarShown(Intent token) {
		if (token != null) {
			final String action = token.getAction();
			final NPFolder folder = token.getParcelableExtra(FolderService.KEY_FOLDER);

			if (FolderService.ACTION_DELETE.equals(action)) {
				final int i = Iterables.indexOf(mSubFolders, folder.filterById());
				if (i >= 0) {
					mSubFolders.remove(i);
					token.putExtra(KEY_LIST_POS, i);
					mFoldersAdapter.notifyDataSetChanged();
				} else {
					Log.w(TAG, "deleting folder, but no matching ID found in the list. " + folder);
				}
			}
		}
	}

	@Override
	public void onUndoBarFinishShowing(Intent token) {
		if (token != null) {
			final String action = token.getAction();
			final NPFolder folder = token.getParcelableExtra(FolderService.KEY_FOLDER);

			final FolderService service = getFolderService();
			try {
				folder.setOwner(AccountManager.currentAccount());
			} catch (NPException e) {
				Log.e(TAG, e.toString());
				Context context = mListView.getContext();
				String msg = context.getString(R.string.formatted_err_delete_failed, folder.getDisplayName());
				Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
			}

			if (FolderService.ACTION_DELETE.equals(action)) {
				service.deleteFolder(folder);
			}
		}
	}


	/**
	 * SwipeRefresh handler.
	 */
	@Override
	public void onRefresh() {
		if (mListFrame instanceof SwipeRefreshLayout) {
			((SwipeRefreshLayout)mListFrame).setRefreshing(true);
			try {
				mFolderService.getSubFolders(mParentFolder);
			} catch (NPException e) {
			}
		}
	}

	protected FolderService getFolderService() {
		return mFolderService;
	}

	protected List<NPFolder> getSubFolders() {
		return mSubFolders;
	}

}
