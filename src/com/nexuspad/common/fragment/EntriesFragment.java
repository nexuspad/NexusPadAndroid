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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.nexuspad.Manifest;
import com.nexuspad.R;
import com.nexuspad.account.AccountManager;
import com.nexuspad.common.annotation.ModuleId;
import com.nexuspad.app.App;
import com.nexuspad.common.activity.FoldersNavigatorActivity;
import com.nexuspad.common.activity.UpdateFolderActivity;
import com.nexuspad.common.adapters.*;
import com.nexuspad.common.listeners.DirectionalScrollListener;
import com.nexuspad.common.utils.Lazy;
import com.nexuspad.datamodel.*;
import com.nexuspad.dataservice.*;
import com.nexuspad.dataservice.EntryService.EntryReceiver;
import com.nexuspad.home.activity.LoginActivity;

import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Strings.nullToEmpty;
import static com.nexuspad.dataservice.EntryListService.EntryListReceiver;

/**
 * The base class for EntryList Fragments.
 *
 * Manages an EntryList.
 * <p/>
 * You may use {@link ModuleId} annotation on the {@code Fragment} class, if you
 * don't, you must override {@link #getTemplate()} and {@link #getModule()}.
 *
 * @author Edmond
 */
public abstract class EntriesFragment <T extends EntriesAdapter> extends UndoBarFragment {
	private static final String TAG = EntriesFragment.class.getSimpleName();

	public static final int ACTIVITY_REQ_CODE_FOLDER_SELECTOR = 1;

	public static final String KEY_FOLDER = "key_folder";
	public static final String KEY_LIST_POS = "key_list_pos";

	public static final int PAGE_COUNT = 20;

	private EntryService mEntryService = null;
	private EntryListService mEntryListService = null;

	protected EntryList mEntryList;

	private ActivityCallback mCallback;

	private int mCurrentPage;

	protected NPFolder mFolder;

	private ModuleId mModuleId;

	protected View mQuickReturnView;
	protected TextView mFolderSelectorView;
	protected ListView mListView;

	protected View mEmptyFolderView;

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
			if (mModuleId.template().equals(entryTemplate)) {
				EntryList entryList = getEntryListService().getEntryListFromKey(key);

				if (mEntryList == null) {
					mEntryList = entryList;
				} else {
					final List<NPEntry> oldEntries = mEntryList.getEntries();
					final List<NPEntry> newEntries = entryList.getEntries();

					for (NPEntry newEntry : newEntries) {
						if (!Iterables.tryFind(oldEntries, newEntry.filterById()).isPresent()) {
							oldEntries.add(newEntry);
						} else {
							Log.i("ENTRIES FRAG: ", "entry is already in the list........");
						}
					}

					mEntryList.setPageId(entryList.getPageId());
				}

				if (mModuleId.moduleId() == NPModule.JOURNAL) {
					onListLoaded(mEntryList);

				} else {
					if (mEntryList.getEntries().size() == 0) {
						fadeInEmptyFolderView();
					} else {
						onListLoaded(mEntryList);
					}
				}
			}
		}

		@Override
		protected void onReceiveSearchResult(Context c, Intent i, EntryTemplate entryTemplate, String key) {
			if (mModuleId.template().equals(entryTemplate)) {
				final EntryList entryList = getEntryListService().getEntryListFromKey(key);
				mCurrentSearchKeyword = nullToEmpty(entryList.getKeyword());

				if (entryList.getEntries().size() == 0) {
					fadeInEmptyFolderView();
				} else {
					onSearchLoaded(entryList);
					dismissProgressIndicator();
				}
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
			onNewEntry(entry);
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

	protected final Lazy<SingleAdapter<View>> mLoadMoreAdapter = new Lazy<SingleAdapter<View>>() {
		@Override
		protected SingleAdapter<View> onCreate() {
			return new SingleAdapter<View>(getActivity().getLayoutInflater()
					.inflate(R.layout.list_item_load_more, null, false));
		}
	};

	protected OnListEndListener mLoadMoreScrollListener = new OnListEndListener() {
		@Override
		protected void onListEnd(int page) {
			queryEntriesInFolderByPage(getCurrentPage() + 1);
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
		displayProgressIndicator();
		mCurrentSearchKeyword = keyword;
		mEntriesAdapter.doSearch(keyword);
	}

	protected void reDisplayListEntries() {
		dismissProgressIndicator();

		// Need to reset the scroll listener.
		mLoadMoreScrollListener.reset();

		mEntriesAdapter.setDisplayEntries(mEntryList);
		mEntriesAdapter.notifyDataSetChanged();
	}

	/**
	 * @return one of the {@code *_MODULE} constants in {@link ServiceConstants}
	 * @see #getTemplate()
	 */
	protected int getModule() {
		if (mModuleId == null) {
			throw new IllegalStateException("You must annotate the class with a ModuleId, or override this method.");
		}
		return mModuleId.moduleId();
	}

	/**
	 * @return should correspond with {@link #getModule()}
	 */
	protected EntryTemplate getTemplate() {
		if (mModuleId == null) {
			throw new IllegalStateException("You must annotate the class with a ModuleId, or override this method.");
		}
		return mModuleId.template();
	}

	protected List<NPFolder> getSubFolders() {
		return mEntryList.getFolder().getSubFolders();
	}

	protected void onEntryListUpdated() {
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
			final List<NPEntry> entries = entryList.getEntries();
			if (Iterables.removeIf(entries, entry.filterById())) {
				onEntryListUpdated();
			} else {
				Log.w(TAG, "entry deleted on the server, but ID does not exists in the list");
			}
		}
	}

	/**
	 * EntryReceiver create new entry action result.
	 *
	 * @param entry
	 */
	protected void onNewEntry(NPEntry entry) {
		EntryList entryList = getEntryList();
		if (entryList != null) {
			final List<NPEntry> entries = entryList.getEntries();
			if (!Iterables.tryFind(entries, entry.filterById()).isPresent()) {
				if (entries.size() == 0) {
					entries.add(entry);
				} else {
					entries.add(0, entry);
				}
				onEntryListUpdated();
			} else {
				Log.w(TAG, "entry created on the server, but ID already exists in the list, updating instead: " + entry);
				onUpdateEntry(entry);
			}
		}
	}

	/**
	 * EntryReceiver update entry action result.
	 *
	 * @param updatedEntry
	 */
	protected void onUpdateEntry(NPEntry updatedEntry) {
		if (mEntryList != null) {
			final List<NPEntry> entries = mEntryList.getEntries();
			final Iterator<NPEntry> iterator = entries.iterator();
			final Predicate<NPEntry> predicate = updatedEntry.filterById();

			for (int index = 0; iterator.hasNext(); ++index) {
				final NPEntry current = iterator.next();
				if (predicate.apply(current)) {
					entries.remove(index);

					final NPFolder folder = current.getFolder();
					final NPFolder updatedEntryF = updatedEntry.getFolder();

					if (folder.filterById().apply(updatedEntryF)) { // same folder, put it back in
						entries.add(index, updatedEntry);
					}

					onEntryListUpdated();
					return;
				}
			}
			Log.w(TAG, "cannot find the updated entry in the list; entry: " + updatedEntry);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mCallback = App.getCallbackOrThrow(activity, ActivityCallback.class);
		mModuleId = ((Object) this).getClass().getAnnotation(ModuleId.class);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Bundle arguments = getArguments();

		if (arguments != null) {
			mFolder = arguments.getParcelable(KEY_FOLDER);
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
		return inflater.inflate(R.layout.list_content, container, false);
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
		final View listFrame = view.findViewById(R.id.main_list_frame);

		/*
		 * progress frame and retry button
		 */
		final View progressFrame = view.findViewById(R.id.frame_progress);
		final View retryFrame = view.findViewById(R.id.frame_retry);

		if (listFrame != null && progressFrame != null && retryFrame != null) {
			mLoadingUiManager = new LoadingUiManager(listFrame, retryFrame, progressFrame, new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mLoadingUiManager.fadeInProgressFrame();
					onRetryClicked(v);
				}
			});
		}

		/*
		 * quick return
		 */
		mQuickReturnView = view.findViewById(R.id.quick_return);

		/*
		 * empty folder
		 */
		if (mEmptyFolderView == null) {
			mEmptyFolderView = view.findViewById(R.id.frame_empty_folder);
		}
		if (mEmptyFolderView != null) {
			mEmptyFolderView.setVisibility(View.GONE);
		}

		/*
		 * folder selector
		 */
		mFolderSelectorView = (TextView)view.findViewById(R.id.lbl_folder);

		/*
		 * main list view
		 */
		final View theView = view.findViewById(R.id.list_view);

		if (theView instanceof ListView) {
			mListView = (ListView) theView;
		}

		if (mListView != null) {
			mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					onListItemClick(mListView, view, position, id);
				}
			});

			mListView.setItemsCanFocus(true);
			mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

			if (isAutoLoadMoreEnabled()) {
				mListView.setOnScrollListener(mLoadMoreScrollListener);
			}
		}

		queryEntriesAsync();
	}

	protected boolean isAutoLoadMoreEnabled() {
		return true;
	}

	@Override
	protected void onRetryClicked(View button) {
		super.onRetryClicked(button);
		queryEntriesAsync();
	}

	@Override
	public void onUndoButtonClicked(Intent token) {
		if (token != null) {
			final String action = token.getAction();
			final NPEntry entry = token.getParcelableExtra(EntryService.KEY_ENTRY);
			final NPFolder folder = token.getParcelableExtra(FolderService.KEY_FOLDER);
			final int position = token.getIntExtra(KEY_LIST_POS, 0);

			if (EntryService.ACTION_DELETE.equals(action)) {
				final List<NPEntry> entries = getEntryList().getEntries();
				entries.add(position, entry);
				onEntryListUpdated();

			} else if (FolderService.ACTION_DELETE.equals(action)) {
				final List<NPFolder> subFolders = getSubFolders();
				subFolders.add(position, folder);
				onEntryListUpdated();
			}
		}
	}

	@Override
	public void onUndoBarShown(Intent token) {
		if (token != null) {
			final String action = token.getAction();
			final NPEntry entry = token.getParcelableExtra(EntryService.KEY_ENTRY);
			final NPFolder folder = token.getParcelableExtra(FolderService.KEY_FOLDER);

			if (EntryService.ACTION_DELETE.equals(action)) {
				final EntryList entryList = getEntryList();
				if (entryList != null) {
					final List<NPEntry> entries = entryList.getEntries();
					final int i = Iterables.indexOf(entries, entry.filterById());
					if (i >= 0) {
						entries.remove(i);
						token.putExtra(KEY_LIST_POS, i);
						onEntryListUpdated();
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
						onEntryListUpdated();
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
	public void onUndoBarHidden(Intent token) {
		if (token != null) {
			final String action = token.getAction();
			final NPEntry entry = token.getParcelableExtra(EntryService.KEY_ENTRY);

			if (EntryService.ACTION_DELETE.equals(action)) {
				getEntryService().safeDeleteEntry(getActivity(), entry);
			}
		}
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.new_folder:
				UpdateFolderActivity.startWithParentFolder(getFolder(), getActivity());
				return true;
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
	 * @param list
	 */
	protected void onListLoaded(EntryList list) {
	}


	/**
	 * The search result is here. Use {@link EntryList#getKeyword()} to see the original search string.
	 *
	 * @param list the filtered entries
	 */
	protected void onSearchLoaded(EntryList list) {
		((EntriesAdapter)getAdapter()).setDisplayEntries(list);
	}

	public void deleteEntry(NPEntry entry) {
		getEntryService().safeDeleteEntry(getActivity(), entry);
	}

	protected void setQuickReturnListener(ListView view, AbsListView.OnScrollListener other) {
		view.setOnScrollListener(newDirectionalScrollListener(other));
	}

	/**
	 * set up the quick return listener (hiding when scroll down, and vice versa)
	 * <p/>
	 * This will replace the OnScrollListener in the {@code AbsListView}, use {@code other} if you want to include
	 * another {@code OnScrollListener}.
	 *
	 * @param gridView the scrolling view (commonly ListView or GridView)
	 * @param other    the other scroll listener; optional
	 */
	protected void setQuickReturnListener(GridView gridView, AbsListView.OnScrollListener other) {
		gridView.setOnScrollListener(newDirectionalScrollListener(other));
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
				final View quickReturnV = getQuickReturnView();
				final int height = showing ? 0 : quickReturnV.getHeight();
				if (quickReturnV != null) {
					quickReturnV.animate()
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

		folderSelectorView.setText(getFolder().getFolderName());
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

	protected View getQuickReturnView() {
		if (mQuickReturnView == null) {
			throw new IllegalStateException("that is no view with id R.id.quick_return");
		}
		return mQuickReturnView;
	}

	public TextView getFolderSelectorView() {
		if (mFolderSelectorView == null) {
			throw new IllegalStateException("that is no view with id R.id.lbl_folder");
		}
		return mFolderSelectorView;
	}

	protected void fadeInEmptyFolderView() {
		if (mEmptyFolderView != null) {
			mLoadingUiManager.fadeOutProgressFrame();
			mEmptyFolderView.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * @return an adapter that is used to indicate it is loading more entries at
	 * the end of the lit
	 */
	protected SingleAdapter<View> getLoadMoreAdapter() {
		return mLoadMoreAdapter.get();
	}

	public EntryList getEntryList() {
		return mEntryList;
	}

	public ListView getListView() {
		return mListView;
	}

	public NPFolder getFolder() {
		return mFolder;
	}

	public final int getCurrentPage() {
		return mCurrentPage;
	}

	public final EntryService getEntryService() {
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

}
