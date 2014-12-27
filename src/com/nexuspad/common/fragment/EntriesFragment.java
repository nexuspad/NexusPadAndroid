/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.common.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.nexuspad.Manifest;
import com.nexuspad.R;
import com.nexuspad.app.App;
import com.nexuspad.common.Constants;
import com.nexuspad.common.activity.FoldersNavigatorActivity;
import com.nexuspad.common.activity.UpdateFolderActivity;
import com.nexuspad.common.adapters.EntriesAdapter;
import com.nexuspad.common.annotation.ModuleInfo;
import com.nexuspad.common.listeners.DirectionalScrollListener;
import com.nexuspad.home.activity.LoginActivity;
import com.nexuspad.service.account.AccountManager;
import com.nexuspad.service.datamodel.*;
import com.nexuspad.service.dataservice.*;
import com.nexuspad.service.dataservice.EntryService.EntryReceiver;

import java.util.Collections;
import java.util.List;

import static com.google.common.base.Strings.nullToEmpty;
import static com.nexuspad.service.dataservice.EntryListService.EntryListReceiver;

/**
 * The base class for EntryList Fragments.
 *
 * Manages an EntryList.
 * <p/>
 * You may use {@link com.nexuspad.common.annotation.ModuleInfo} annotation on the {@code Fragment} class, if you
 * don't, you must override {@link #getTemplate()} and {@link #getModule()}.
 *
 */
public abstract class EntriesFragment <T extends EntriesAdapter> extends UndoBarFragment
		implements SwipeRefreshLayout.OnRefreshListener {
	private static final String TAG = EntriesFragment.class.getSimpleName();

	public static final int ACTIVITY_REQ_CODE_FOLDER_SELECTOR = 1;

	public static final String KEY_LIST_POS = "key_list_pos";

	public static final int PAGE_COUNT = 20;

	protected EntryService mEntryService = null;
	protected EntryListService mEntryListService = null;

	protected EntryList mEntryList;

	private ActivityCallback mCallback;

	private int mCurrentPage;

	protected NPFolder mFolder;

	protected ModuleInfo mModuleInfo;

	protected View mListFrame;
	protected View mBottomOverlayView;
	protected TextView mFolderSelectorView;
	protected ListView mListView;

	protected String mCurrentSearchKeyword;

	/** Keep private to force using getAdapter and setAdapter */
	private EntriesAdapter mEntriesAdapter;

	public BaseAdapter getAdapter() {
		return mEntriesAdapter;
	}

	public void setAdapter(BaseAdapter adapter) {
		mEntriesAdapter = (T)adapter;
	}

	public interface ActivityCallback {
		void onListLoaded(EntriesFragment f, EntryList list);
	}


	/**
	 * Override BroadcastReceiver to display entries on screen.
	 */
	private final EntryListReceiver mEntryListReceiver = new EntryListReceiver() {
		@Override
		protected void onReceiveFolderListing(Context c, Intent i, EntryTemplate entryTemplate, String key) {
			if (mModuleInfo.template().equals(entryTemplate)) {
				EntryList newEntryList = getEntryListService().getEntryListFromKey(key);

				if (mEntryList == null) {
					mEntryList = newEntryList;
					mEntryList.setEntryUpdated(true);

				} else {

					if (mEntryList.getFolder().getFolderId() != newEntryList.getFolder().getFolderId()) {
						mEntryList = newEntryList;

					} else {

						/*
						 * Handles loading next pages.
						 */
						final List<NPEntry> existingEntries = mEntryList.getEntries();
						List<NPEntry> newEntries = newEntryList.getEntries();

						boolean entryAdded = false;
						for (NPEntry newEntry : newEntries) {
							if (!existingEntries.contains(newEntry)) {
								existingEntries.add(EntryFactory.copyEntry(newEntry));
								entryAdded = true;
							}
						}

						if (entryAdded) {
							mEntryList.setEntryUpdated(true);
						} else {
							mEntryList.setEntryUpdated(false);
						}

						mEntryList.setPageId(newEntryList.getPageId());
					}
				}

				if (mModuleInfo.moduleId() == NPModule.JOURNAL) {
					onListLoaded(mEntryList);

				} else {
					if (mModuleInfo.moduleId() == NPModule.CONTACT) {
						Collections.sort(mEntryList.getEntries(), NPPerson.ORDERING_BY_LAST_NAME);
					} else if (mModuleInfo.moduleId() == NPModule.CALENDAR) {
						Collections.sort(mEntryList.getEntries(), NPEvent.ORDERING_BY_STARTING_TIME);
					} else {
						Collections.sort(mEntryList.getEntries(), NPEntry.ORDERING_BY_UPDATE_TIME);
					}

					onListLoaded(mEntryList);
				}
			}
		}

		@Override
		protected void onReceiveSearchResult(Context c, Intent i, EntryTemplate entryTemplate, String key) {
			if (mModuleInfo.template().equals(entryTemplate)) {
				final EntryList entryList = getEntryListService().getEntryListFromKey(key);
				mCurrentSearchKeyword = nullToEmpty(entryList.getKeyword());

				onSearchLoaded(entryList);
				hideProgressIndicatorAndShowMainList();
			}
		}

		@Override
		protected void onError(Context context, Intent intent, ServiceError error) {
			Log.e(TAG, error.toString());
			handleServiceError(error);
		}
	};


	/**
	 * Handles adding/updating/deleting entry
	 */
	private final EntryReceiver mEntryReceiver = new EntryReceiver() {
		@Override
		protected void onGet(Context context, Intent intent, NPEntry entry) {
			onGetEntry(entry);
		}

		@Override
		public void onDelete(Context context, Intent intent, NPEntry entry) {
			onDeleteEntry(entry);
		}

		@Override
		public void onNew(Context context, Intent intent, NPEntry entry) {
			onUpdateEntry(entry);
		}

		@Override
		public void onUpdate(Context context, Intent intent, NPEntry entry) {
			onUpdateEntry(entry);
		}

		@Override
		protected void onError(Context context, Intent intent, ServiceError error) {
			super.onError(context, intent, error);
			Log.e(TAG, error.toString());
			handleServiceError(error);
		}
	};


	/**
	 * set up {@code OnQueryTextListener}, {@code OnCloseListener}, and {@code OnActionExpandListener}.
	 *
	 * @param searchItem
	 */
	protected void setUpSearchView(MenuItem searchItem) {
		final SearchView searchView = (SearchView) searchItem.getActionView();
		if (searchView != null) {
			searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
				@Override
				public boolean onQueryTextSubmit(String query) {
					if (Strings.isNullOrEmpty(query)) {
						reDisplayListEntries();
					} else {
						doSearch(query);
					}
					return true;
				}

				/**
				 * Placeholder method for searching while typing.
				 */
				@Override
				public boolean onQueryTextChange(String newText) {
					// For Contact module, enable the instant search feedback since the search is done locally.
					if (mFolder.getModuleId() == NPModule.CONTACT) {
						if (Strings.isNullOrEmpty(newText)) {
							reDisplayListEntries();
						} else {
							doSearch(newText);
						}
						return true;
					} else {
						return false;
					}
				}
			});

			searchView.setOnCloseListener(new SearchView.OnCloseListener() {
				@Override
				public boolean onClose() {
					mCurrentSearchKeyword = null;
					reDisplayListEntries();
					return true;
				}
			});

			MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
				@Override
				public boolean onMenuItemActionExpand(MenuItem item) {
					return true;
				}

				/**
				 * This is called when search is closed.
				 *
				 * @param item
				 * @return
				 */
				@Override
				public boolean onMenuItemActionCollapse(MenuItem item) {
					mCurrentSearchKeyword = null;
					reDisplayListEntries();
					return true;
				}
			});
		}
	}

	protected void doSearch(String keyword) {
		Log.i(TAG, "Search keyword: " + keyword);

		showProgressIndicator();
		mCurrentSearchKeyword = keyword;

		try {
			mEntryListService.searchEntriesInFolder(keyword, mFolder, mModuleInfo.template(), 0, 99);
		} catch (NPException e) {
			throw new RuntimeException(e);
		}
	}

	protected void reDisplayListEntries() {
		hideProgressIndicatorAndShowMainList();

		((EntriesAdapter)getAdapter()).setDisplayEntryList(mEntryList);
	}

	/**
	 * @return one of the {@code *_MODULE} constants in {@link ServiceConstants}
	 * @see #getTemplate()
	 */
	protected int getModule() {
		if (mModuleInfo == null) {
			throw new IllegalStateException("You must annotate the class with a ModuleId, or override this method.");
		}
		return mModuleInfo.moduleId();
	}

	/**
	 * @return should correspond with {@link #getModule()}
	 */
	protected EntryTemplate getTemplate() {
		if (mModuleInfo == null) {
			throw new IllegalStateException("You must annotate the class with a ModuleId, or override this method.");
		}
		return mModuleInfo.template();
	}

	protected List<NPFolder> getSubFolders() {
		return mEntryList.getFolder().getSubFolders();
	}

	protected void refreshUIAfterUpdatingEntryList() {
		if (getAdapter() != null) {
			getAdapter().notifyDataSetChanged();
		}
	}

	/**
	 * EntryReceiver get entry detail.
	 *
	 * @param entry
	 */
	protected void onGetEntry(NPEntry entry) {
	}

	/**
	 * EntryReceiver delete entry action result.
	 *
	 * @param entry
	 */
	protected void onDeleteEntry(NPEntry entry) {
		EntryList entryList = getEntryList();
		if (entryList != null) {
			if (entryList.removeEntryFromList(entry)) {
				Log.i(TAG, "Removed entry from the list.");
			} else {
				Log.w(TAG, "Entry deleted on the server, but ID does not exists in the list");
			}
			refreshUIAfterUpdatingEntryList();
		}
	}

	/**
	 * EntryReceiver update entry action result.
	 *
	 * @param newOrUpdatedEntry
	 */
	protected void onUpdateEntry(NPEntry newOrUpdatedEntry) {
		getEntryList().addOrUpdateEntry(newOrUpdatedEntry);
		refreshUIAfterUpdatingEntryList();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mCallback = App.getCallbackOrThrow(activity, ActivityCallback.class);
		mModuleInfo = ((Object) this).getClass().getAnnotation(ModuleInfo.class);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Bundle arguments = getArguments();

		if (arguments != null) {
			mFolder = arguments.getParcelable(Constants.KEY_FOLDER);
		}

		if (mFolder == null) {
			throw new IllegalArgumentException("you did not pass in a folder with KEY_FOLDER");
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		final FragmentActivity activity = getActivity();

		activity.registerReceiver(mEntryReceiver,
				EntryReceiver.getIntentFilter(),
				Manifest.permission.LISTEN_ENTRY_CHANGES,
				null);

		activity.registerReceiver(mEntryListReceiver,
				EntryListReceiver.getIntentFilter(),
				Manifest.permission.RECEIVE_ENTRY_LIST,
				null);
	}

	@Override
	public void onPause() {
		super.onPause();
		final FragmentActivity activity = getActivity();

		activity.unregisterReceiver(mEntryReceiver);
		activity.unregisterReceiver(mEntryListReceiver);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.main_list, container, false);
	}


	/**
	 * {@link #onListLoaded(EntryList)} may get called immediately in the
	 * method.
	 */
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		/*
		 * main list frame
		 */
		mListFrame = view.findViewById(R.id.main_list_frame);

		if (mListFrame instanceof android.support.v4.widget.SwipeRefreshLayout) {
			SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout)mListFrame;
			swipeLayout.setOnRefreshListener(this);
		}

		/*
		 * progress frame and retry button
		 */
		final View emptyFolderView = view.findViewById(R.id.frame_empty_folder);
		final View progressFrame = view.findViewById(R.id.frame_progress);
		final View retryFrame = view.findViewById(R.id.frame_retry);

		if (mListFrame != null && progressFrame != null && retryFrame != null) {
			if (mLoadingUiManager == null) {
				mLoadingUiManager = new LoadingUiManager(mListFrame, emptyFolderView, retryFrame, progressFrame, new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mLoadingUiManager.showProgressView();
						onRetryClicked(v);
					}
				});
			}
		}

		/*
		 * quick return
		 */
		if (mBottomOverlayView == null) {
			mBottomOverlayView = view.findViewById(R.id.bottom_overlay);
		}

		/*
		 * folder selector
		 */
		if (mFolderSelectorView == null) {
			mFolderSelectorView = (TextView) view.findViewById(R.id.lbl_folder);
		}

		/*
		 * main list view
		 */
		if (mListView == null) {
			final View theView = view.findViewById(R.id.list_view);

			if (theView instanceof ListView) {
				mListView = (ListView) theView;

				mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						onListItemClick(mListView, view, position, id);
					}
				});

				mListView.setItemsCanFocus(true);
				mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			}
		}
	}

	protected boolean isAutoLoadMoreEnabled() {
		return true;
	}

	@Override
	protected void onRetryClicked(View button) {
		super.onRetryClicked(button);
		queryEntriesAsync();
	}

	/**
	 * SwipeRefresh handler.
	 */
	@Override
	public void onRefresh() {
		Log.i(TAG, "Implement swipe refresh....");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.new_folder:
				UpdateFolderActivity.startWithParentFolder(getFolder(), getActivity());
				return true;
			case R.id.search:
				// When the search view displays, hide the bottom overlay.
				if (mBottomOverlayView != null && mBottomOverlayView.getHeight() > 0) {
					mBottomOverlayView.animate().translationY(mBottomOverlayView.getHeight()).setDuration(100L);
				}
				return super.onOptionsItemSelected(item);
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Query the entry list by folder.
	 * Override in Fragments for different behavior.
	 */
	protected void queryEntriesAsync() {
		queryEntriesInFolderByPage(1);
	}

	public void queryEntriesInFolderByPage(int page) {
		mCurrentPage = page;

		try {
			mFolder.setOwner(AccountManager.currentAccount());
			getEntryListService().getEntriesInFolder(mFolder, getTemplate(), page, PAGE_COUNT);

		} catch (NPException e) {
			Log.e(TAG, e.toString());
			handleServiceError(e.getServiceError());
		}
	}

	protected void handleServiceError(ServiceError error) {
		final ErrorCode errorCode = error.getErrorCode();

		if (shouldKickToLogin(errorCode)) {
			// Clear the token
			AccountManager.logout();
			kickToLoginScreen();

		} else {
			displayRetry();
		}
	}

	private boolean shouldKickToLogin(ErrorCode errorCode) {
		return errorCode == ErrorCode.INVALID_USER_TOKEN
				|| errorCode == ErrorCode.INVALID_LOGIN
				|| errorCode == ErrorCode.NOT_LOGGED_IN
				|| errorCode == ErrorCode.FAILED_REGISTRATION
				|| errorCode == ErrorCode.FAILED_REGISTRATION_ACCT_EXISTS
				|| errorCode == ErrorCode.FAILED_DELETE_ACCOUNT
				|| errorCode == ErrorCode.LOGIN_NO_USER
				|| errorCode == ErrorCode.LOGIN_ACCT_PROBLEM
				|| errorCode == ErrorCode.LOGIN_FAILED;
	}

	private void kickToLoginScreen() {
		final FragmentActivity activity = getActivity();
		final Intent intent = new Intent(activity, LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
		activity.finish();
	}

	/**
	 * Place holder. Subclass should override.
	 *
	 * @param newListToDisplay
	 */
	protected void onListLoaded(EntryList newListToDisplay) {
		if (mListFrame instanceof SwipeRefreshLayout) {
			((SwipeRefreshLayout)mListFrame).setRefreshing(false);
		}
	}


	/**
	 * The search result is here. Use {@link EntryList#getKeyword()} to see the original search string.
	 *
	 * @param list the filtered entries
	 */
	protected void onSearchLoaded(EntryList list) {
		Log.e(TAG, "No onSearchLoaded implementation. Must be implemented in subclass.");
	}

	public void deleteEntry(NPEntry entry) {
		getEntryService().safeDeleteEntry(getActivity(), entry);
	}


	/**
	 * Decide to show the folder selector bar when list is scrolled to the bottom.
	 *
	 * @param other
	 * @return
	 */
	protected DirectionalScrollListener newDirectionalScrollListener(final AbsListView.OnScrollListener other) {
		return new DirectionalScrollListener(0, other) {
			@Override
			public void onScrollDirectionChanged(final boolean showing) {
				if (mBottomOverlayView != null) {
					final int height = showing ? 0 : mBottomOverlayView.getHeight();
					mBottomOverlayView.animate()
							.translationY(height)
							.setDuration(200L)
							.withEndAction(new Runnable() {
								@Override
								public void run() {
									final View folderSelectorV = getFolderSelectorView();
									folderSelectorV.setClickable(showing);
									folderSelectorV.setFocusable(showing);
								}
							});
				}
			}
		};
	}

	protected void initFolderSelector(final int reqCode) {
		final TextView folderSelectorView = getFolderSelectorView();
		final int module = getModule();

		if (mFolder.getFolderId() == NPFolder.ROOT_FOLDER) {
			if (module == NPModule.CALENDAR) {
				folderSelectorView.setText(R.string.calendars);
			} else {
				folderSelectorView.setText(R.string.folders);
			}

		} else {
			folderSelectorView.setText(mFolder.getDisplayName());
		}

		folderSelectorView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final FragmentActivity activity = getActivity();
				final Intent intent = FoldersNavigatorActivity.ofParentFolder(activity, NPFolder.rootFolderOf(module, activity));
				startActivityForResult(intent, reqCode);
				activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
			}
		});
	}

	public TextView getFolderSelectorView() {
		if (mFolderSelectorView == null) {
			throw new IllegalStateException("that is no view with id R.id.lbl_folder");
		}
		return mFolderSelectorView;
	}


	public EntryList getEntryList() {
		if (mEntryList == null) {
			mEntryList = new EntryList();
		}
		return mEntryList;
	}

	public ListView getListView() {
		return mListView;
	}

	public NPFolder getFolder() {
		if (mFolder == null) {
			mFolder = NPFolder.rootFolderOf(mModuleInfo.moduleId());
		}
		return mFolder;
	}

	public final int getCurrentPage() {
		return mCurrentPage;
	}

	public EntryService getEntryService() {
		if (mEntryService == null) {
			mEntryService = EntryService.getInstance(getActivity());
		}
		return mEntryService;
	}

	public final EntryListService getEntryListService() {
		if (mEntryListService == null) {
			mEntryListService = EntryListService.getInstance(getActivity());
		}
		return mEntryListService;
	}


	@Override
	public void onUndoButtonClicked(Intent token) {
		if (token != null) {
			final String action = token.getAction();
			final NPEntry entry = token.getParcelableExtra(Constants.KEY_ENTRY);
			final NPFolder folder = token.getParcelableExtra(FolderService.KEY_FOLDER);
			final int position = token.getIntExtra(KEY_LIST_POS, 0);

			if (EntryService.ACTION_DELETE.equals(action)) {
				final List<NPEntry> entries = getEntryList().getEntries();
				entries.add(position, entry);
				refreshUIAfterUpdatingEntryList();

			} else if (FolderService.ACTION_DELETE.equals(action)) {
				final List<NPFolder> subFolders = getSubFolders();
				subFolders.add(position, folder);
				refreshUIAfterUpdatingEntryList();
			}
		}
	}

	@Override
	public void onUndoBarShown(Intent token) {
		if (token != null) {
			final String action = token.getAction();
			final NPEntry entry = token.getParcelableExtra(Constants.KEY_ENTRY);
			final NPFolder folder = token.getParcelableExtra(FolderService.KEY_FOLDER);

			if (EntryService.ACTION_DELETE.equals(action)) {
				final EntryList entryList = getEntryList();
				if (entryList != null) {
					final List<NPEntry> entries = entryList.getEntries();
					final int i = Iterables.indexOf(entries, entry.filterById());
					if (i >= 0) {
						entries.remove(i);
						token.putExtra(KEY_LIST_POS, i);
						refreshUIAfterUpdatingEntryList();
					} else {
						Log.w(TAG, "deleting entry, but no matching ID exists in the list. " + entry.getEntryId());
					}
				}
			} else if (FolderService.ACTION_DELETE.equals(action)) {
				if (mFolder.getFolderId() == folder.getParentId()) {
					final List<NPFolder> subFolders = getSubFolders();
					final int i = Iterables.indexOf(subFolders, folder.filterById());
					if (i >= 0) {
						subFolders.remove(i);
						token.putExtra(KEY_LIST_POS, i);
						refreshUIAfterUpdatingEntryList();
					} else {
						Log.w(TAG, "deleting folder, but no matching ID found in the list. " + folder);
					}
				}
			}
		}
	}

	/**
	 * When the unbar is finally hidden, call the service API to delete the entry.
	 *
	 * @param token
	 */
	@Override
	public void onUndoBarFinishShowing(Intent token) {
		if (token != null) {
			final String action = token.getAction();
			final NPEntry entry = token.getParcelableExtra(Constants.KEY_ENTRY);

			if (EntryService.ACTION_DELETE.equals(action)) {
				getEntryService().safeDeleteEntry(getActivity(), entry);
			}
		}
	}

}
