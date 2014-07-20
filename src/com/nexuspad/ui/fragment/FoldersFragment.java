/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.edmondapps.utils.android.Logs;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.google.common.collect.Iterables;
import com.nexuspad.Manifest;
import com.nexuspad.R;
import com.nexuspad.account.AccountManager;
import com.nexuspad.app.App;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.dataservice.FolderService;
import com.nexuspad.dataservice.FolderService.FolderReceiver;
import com.nexuspad.dataservice.NPException;
import com.nexuspad.dataservice.ServiceError;
import com.nexuspad.ui.OnFolderMenuClickListener;
import com.nexuspad.ui.activity.NewFolderActivity;
import com.nexuspad.ui.adapters.FolderNavigatorAdapter;

import java.util.List;

/**
 *
 * You must pass in a moduleId with {@link FoldersFragment#KEY_PARENT_FOLDER} as an argument or use
 * the static factory method {@link #of(Folder)}.
 *
 * @author Edmond
 */
@FragmentName(FoldersFragment.TAG)
public class FoldersFragment extends FadeListFragment {
	public static final String TAG = "FoldersFragment";
	public static final String KEY_PARENT_FOLDER = "com.nexuspad.ui.fragment.FoldersFragment.parent_folder";
	private static final String KEY_LIST_POS = "key_list_pos";

	private Callback mCallback;
	private FolderService mFolderService;
	private Folder mParentFolder;
	private List<Folder> mSubFolders;

	private ListView mListView;
	private FolderNavigatorAdapter mFoldersAdapter;


	/**
	 * @param folder the parent folder of the folders list
	 */
	public static FoldersFragment of(Folder folder) {
		Bundle bundle = new Bundle();
		bundle.putParcelable(KEY_PARENT_FOLDER, folder);

		FoldersFragment fragment = new FoldersFragment();
		fragment.setArguments(bundle);

		return fragment;
	}

	@Override
	public void onUndoButtonClicked(Intent token) {
		if (token != null) {
			final String action = token.getAction();
			final Folder folder = token.getParcelableExtra(FolderService.KEY_FOLDER);
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
			final Folder folder = token.getParcelableExtra(FolderService.KEY_FOLDER);

			if (FolderService.ACTION_DELETE.equals(action)) {
				final int i = Iterables.indexOf(mSubFolders, folder.filterById());
				if (i >= 0) {
					mSubFolders.remove(i);
					token.putExtra(KEY_LIST_POS, i);
					mFoldersAdapter.notifyDataSetChanged();
				} else {
					Logs.w(TAG, "deleting folder, but no matching ID found in the list. " + folder);
				}
			}
		}
	}

	@Override
	public void onUndoBarHidden(Intent token) {
		if (token != null) {
			final String action = token.getAction();
			final Folder folder = token.getParcelableExtra(FolderService.KEY_FOLDER);

			final FolderService service = getFolderService();
			try {
				folder.setOwner(AccountManager.currentAccount());
			} catch (NPException e) {
				Logs.e(TAG, e);
				Context context = mListView.getContext();
				String msg = context.getString(R.string.formatted_err_delete_failed, folder.getDisplayName());
				Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
			}

			if (FolderService.ACTION_DELETE.equals(action)) {
				service.deleteFolder(folder);
			}
		}
	}

	public interface Callback {
		void onFolderClicked(Folder folder);

		void onSubFolderClicked(Folder folder);

		/**
		 * Called when the "up" folder is clicked, the activity should navigate to the parent of the current parent folder
		 *
		 */
		void onUpFolderClicked();
	}

	private final FolderReceiver mFolderReceiver = new FolderReceiver() {
		@Override
		protected void onGotAll(Context c, Intent i, List<Folder> folders) {
			mSubFolders = folders;
			initFolderNavigatorList(folders);
		}

		@Override
		protected void onDelete(Context c, Intent i, Folder folder) {
		}

		@Override
		protected void onNew(Context c, Intent i, Folder f) {
			if (!Iterables.tryFind(mSubFolders, f.filterById()).isPresent()) {
				mSubFolders.add(f);
				mFoldersAdapter.notifyDataSetChanged();
			} else {
				Logs.w(TAG, "folder created on the server, but ID already exists in the list, updating instead: " + f);
				onUpdate(c, i, f);
			}
		}

		@Override
		protected void onUpdate(Context c, Intent i, Folder folder) {
			final int index = Iterables.indexOf(mSubFolders, folder.filterById());
			if (index >= 0) {
				mSubFolders.remove(index);
				mSubFolders.add(index, folder);
				mFoldersAdapter.notifyDataSetChanged();
			} else {
				Logs.w(TAG, "cannot find the updated entry in the list; folder: " + folder);
			}
		}

		@Override
		protected void onError(Context context, Intent intent, ServiceError error) {
			super.onError(context, intent, error);
			Logs.e(TAG, error.toString());
		}
	};


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mCallback = App.getCallbackOrThrow(activity, Callback.class);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.folders_frag, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.new_folder:
				NewFolderActivity.startWithParentFolder(mParentFolder, getActivity());
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
			mParentFolder = arguments.getParcelable(KEY_PARENT_FOLDER);
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
			Logs.e(TAG, e);
			Toast.makeText(getActivity(), R.string.err_network, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(mFolderReceiver);
	}

	/**
	 * Called when the sub-folders are retrieved.
	 *
	 * @param folders same as {@link #getSubFolders()}
	 */
	private void initFolderNavigatorList(List<Folder> folders) {
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
				final Folder folder = mFoldersAdapter.getItem(pos);
				mCallback.onSubFolderClicked(folder);
			}
		});

		mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if (position != 0) {
//					final int realPos = getListAdapter().getPositionForAdapter(position);
//					return getFoldersAdapter().onItemLongClick(parent, view, realPos, id);
					return mFoldersAdapter.onItemLongClick(parent, view, position, id);
				} else {
					return false;
				}
			}
		});
	}

//	/**
//	 * Creates an adapter with the given list of {@code Folder}s.
//	 *
//	 * @param folders the folders to be displayed
//	 * @return an instance of {@link FoldersAdapter}
//	 */
//	protected FoldersAdapter createFoldersAdapter(List<Folder> folders) {
//		final FoldersAdapter foldersAdapter = new NamedFoldersAdapter(getActivity(), folders, mParentFolder, mCallback);
//		ListView listView = getListView();
//		foldersAdapter.setOnMenuClickListener(new OnFolderMenuClickListener(listView, mParentFolder, mFolderService, getUndoBarController()) {
//			@Override
//			public void onClick(View v) {
//				final int pos = getListView().getPositionForView(v);
//				final int realPos = getListAdapter().getPositionForAdapter(pos);
//				onFolderClick(foldersAdapter.getItem(realPos), realPos, v);
//			}
//		});
//		foldersAdapter.setOnSubFolderClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				final int pos = getListView().getPositionForView(v);
//				final int realPos = getListAdapter().getPositionForAdapter(pos);
//				final Folder folder = foldersAdapter.getItem(realPos);
//				mCallback.onSubFolderClicked(folder);
//			}
//		});
//		return foldersAdapter;
//	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
//		if (position == 0) {
//			if (mParentFolder.getFolderId() == Folder.ROOT_FOLDER) {
//				mCallback.onFolderClicked(mParentFolder);
//			} else {
//				mCallback.onUpFolderClicked();
//			}
//		} else {
//			final int realPos = getListAdapter().getPositionForAdapter(position);
			mCallback.onFolderClicked(mFoldersAdapter.getItem(position));
//		}
	}


//	/**
//	 * This {@code Fragment} guarantees the use of {@link FoldersAdapter}.
//	 * <p/>
//	 * Override {@link #createFoldersAdapter(List)} instead.
//	 *
//	 * @throws UnsupportedOperationException every time this method is invoked
//	 */
//	@Override
//	@Deprecated
//	public void setListAdapter(ListAdapter adapter) {
//		throw new UnsupportedOperationException("NO");
//	}

//	@Override
//	public CompoundAdapter getListAdapter() {
//		return (CompoundAdapter) super.getListAdapter();
//	}

//	public FoldersAdapter getFoldersAdapter() {
//		return (FoldersAdapter) getListAdapter().getAdapter(1);
//	}

	protected FolderService getFolderService() {
		return mFolderService;
	}

	protected List<Folder> getSubFolders() {
		return mSubFolders;
	}

}
