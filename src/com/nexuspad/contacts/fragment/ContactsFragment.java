package com.nexuspad.contacts.fragment;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import com.google.common.base.Strings;
import com.koushikdutta.ion.Ion;
import com.nexuspad.R;
import com.nexuspad.common.Constants;
import com.nexuspad.common.adapters.EntriesAdapter;
import com.nexuspad.common.adapters.ListViewHolder;
import com.nexuspad.common.annotation.FragmentName;
import com.nexuspad.common.annotation.ModuleInfo;
import com.nexuspad.common.fragment.EntriesFragment;
import com.nexuspad.common.listeners.OnEntryMenuClickListener;
import com.nexuspad.common.utils.EntriesLocalSearchFilter;
import com.nexuspad.contacts.activity.ContactActivity;
import com.nexuspad.contacts.activity.ContactEditActivity;
import com.nexuspad.service.datamodel.EntryList;
import com.nexuspad.service.datamodel.EntryTemplate;
import com.nexuspad.service.datamodel.NPFolder;
import com.nexuspad.service.datamodel.NPPerson;
import com.nexuspad.service.dataservice.EntryListService;
import com.nexuspad.service.dataservice.NPException;
import com.nexuspad.service.dataservice.NPWebServiceUtil;
import com.nexuspad.service.dataservice.ServiceConstants;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import java.util.List;

@FragmentName(ContactsFragment.TAG)
@ModuleInfo(moduleId = ServiceConstants.CONTACT_MODULE, template = EntryTemplate.CONTACT)
public final class ContactsFragment extends EntriesFragment {
	public static final String TAG = "ContactsFragment";

	private StickyListHeadersListView mStickyHeaderContactListView;

	private final EntriesLocalSearchFilter.OnFilterDoneListener<NPPerson> mFilterDoneListener = new EntriesLocalSearchFilter.OnFilterDoneListener<NPPerson>() {
		@Override
		public void onFilterDone(List<NPPerson> persons) {
			EntryList filteredResult = new EntryList();

			for (NPPerson p : persons) {
				filteredResult.getEntries().add(p);
			}

			((ContactsAdapter)getAdapter()).setDisplayEntryList(filteredResult);
			getAdapter().notifyDataSetChanged();
		}
	};


	@Override
	protected boolean isAutoLoadMoreEnabled() {
		// we are loading everything from the start
		return false;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.contacts_topmenu, menu);
		setUpSearchView(menu.findItem(R.id.search));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.new_contact:
				ContactEditActivity.startWithFolder(getActivity(), getFolder());
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.contacts_frag, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// set the listener for folder selector
		initFolderSelector(ACTIVITY_REQ_CODE_FOLDER_SELECTOR);

		final View theView = view.findViewById(R.id.list_view);

		mStickyHeaderContactListView = (StickyListHeadersListView)theView;

		mStickyHeaderContactListView.setFastScrollEnabled(false);     // not ready for the first release

		// set the folder selector view bar
		mStickyHeaderContactListView.setOnScrollListener(newDirectionalScrollListener(null));

		mStickyHeaderContactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (getAdapter() != null) {
					final NPPerson contact = ((ContactsAdapter)getAdapter()).getItem(position);
					ContactActivity.startWith(getActivity(), contact, getFolder());
				}
			}
		});

		mStickyHeaderContactListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);


		if (mEntryList == null) {
			queryEntriesAsync();
		} else {
			onListLoaded(mEntryList);
		}
	}

	@Override
	protected void onListLoaded(EntryList newListToDisplay) {
		Log.i(TAG, "Receiving contact entry list.");

		super.onListLoaded(newListToDisplay);

		ContactsAdapter adapter = (ContactsAdapter)getAdapter();

		if (adapter == null) {
			adapter = new ContactsAdapter(getActivity(), newListToDisplay, getFolder(), getEntryListService(), getTemplate(), mFilterDoneListener);

			adapter.setOnMenuClickListener(new OnEntryMenuClickListener<NPPerson>(mStickyHeaderContactListView, getEntryService(), getUndoBarController()) {
				@Override
				public void onClick(View v) {
					final int i = mStickyHeaderContactListView.getPositionForView(v);
					final NPPerson contact = (NPPerson) getAdapter().getItem(i);
					onEntryClick(contact, i, v);
				}

				@Override
				protected boolean onEntryMenuClick(NPPerson contact, int pos, int menuId) {
					switch (menuId) {
						case R.id.edit:
							ContactEditActivity.startWithContact(getActivity(), getFolder(), contact);
							return true;
						default:
							return super.onEntryMenuClick(contact, pos, menuId);
					}
				}
			});

			mStickyHeaderContactListView.setAdapter(adapter);
			mStickyHeaderContactListView.setOnItemLongClickListener(adapter);
			setAdapter(adapter);

		} else {
			adapter.setDisplayEntryList(newListToDisplay);
		}

		if (newListToDisplay.isEmpty()) {
			hideProgressIndicatorAndShowEmptyFolder();
		} else {
			hideProgressIndicatorAndShowMainList();
		}
	}

	@Override
	protected void onSearchLoaded(EntryList list) {
		((EntriesAdapter)getAdapter()).setDisplayEntryList(list);
	}

	@Override
	protected void reDisplayListEntries() {
		((ContactsAdapter)getAdapter()).setDisplayEntryList(mEntryList);
		getAdapter().notifyDataSetChanged();
	}

	/**
	 * SwipeRefresh handler.
	 */
	@Override
	public void onRefresh() {
		if (mListFrame instanceof SwipeRefreshLayout) {
			((SwipeRefreshLayout)mListFrame).setRefreshing(true);
			queryEntriesAsync();
		}
	}

	/**
	 * Handles activity result:
	 *      - Folder navigator
	 *
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case ACTIVITY_REQ_CODE_FOLDER_SELECTOR:
				if (resultCode == Activity.RESULT_OK) {
//					final FragmentActivity activity = getActivity();
//					final NPFolder folder = data.getParcelableExtra(Constants.KEY_FOLDER);
//					ContactsActivity.startWithFolder(folder, activity);
//					activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

					mFolder = data.getParcelableExtra(Constants.KEY_FOLDER);
					queryEntriesAsync();

					// Refresh Fragment list content after selecting the folder from folder navigator.
					// Since Activity remains the same, we need to update the title in Action bar.
					final ActionBar actionBar = getActivity().getActionBar();
					actionBar.setTitle(mFolder.getFolderName());
				}
				break;

			default:
				throw new AssertionError("unknown requestCode: " + requestCode);
		}
	}


	@Override
	protected void setUpSearchView(MenuItem searchItem) {
		final SearchView searchView = (SearchView) searchItem.getActionView();
		if (searchView != null) {
			searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
				@Override
				public boolean onQueryTextSubmit(String query) {
					if (Strings.isNullOrEmpty(query)) {
						reDisplayListEntries();
					} else {
						((ContactsAdapter)getAdapter()).filter(query);
					}
					return true;
				}

				@Override
				public boolean onQueryTextChange(String newText) {
					// For Contact module, enable the instant search feedback since the search is done locally.
					if (Strings.isNullOrEmpty(newText)) {
						reDisplayListEntries();
					} else {
						((ContactsAdapter)getAdapter()).filter(newText);
					}
					return true;
				}
			});

			searchView.setOnCloseListener(new SearchView.OnCloseListener() {
				@Override
				public boolean onClose() {
					reDisplayListEntries();
					return true;
				}
			});

			MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
				@Override
				public boolean onMenuItemActionExpand(MenuItem item) {
					if (mEntryList.getEntries().size() > 0) {
						return true;
					}
					return false;
				}

				@Override
				public boolean onMenuItemActionCollapse(MenuItem item) {
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


	/**
	 * Override common ListEntriesAdapter for Contact list.
	 */
	public static class ContactsAdapter extends EntriesAdapter<NPPerson> implements StickyListHeadersAdapter {
		private final Activity mActivity;
		private final EntriesLocalSearchFilter mFilter;

		private ContactsAdapter(Activity a, EntryList entryList, NPFolder folder, EntryListService service, EntryTemplate template, EntriesLocalSearchFilter.OnFilterDoneListener<NPPerson> onFilterDoneListener) {
			super(a, entryList);
			mActivity = a;
			mFilter = new EntriesLocalSearchFilter(entryList, onFilterDoneListener);
		}

		private String getDisplayTitle(int position) {
			if (getItem(position) != null) {
				return getItem(position).getTitle();
			}

			return "";
		}

		private String getSortKey(int position) {
			if (getItem(position) != null) {
				if (!Strings.isNullOrEmpty(getItem(position).getLastName())) {
					return getItem(position).getLastName();
				} else {
					return getItem(position).getTitle();
				}
			} else {
				return "";
			}
		}

		@Override
		protected View getEntryView(NPPerson p, int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.list_item_with_icon, parent, false);
			}

			final ListViewHolder holder = getHolder(convertView);

            final String profileImageUrl = p.getProfileImageUrl();

            if (!TextUtils.isEmpty(profileImageUrl)) {
                try {
                    final String url = NPWebServiceUtil.fullUrlWithAuthenticationTokens(profileImageUrl, mActivity);

	                Ion.with(mActivity)
			                .load(url)
			                .withBitmap()
			                .placeholder(R.drawable.placeholder)
			                .intoImageView(holder.getIcon());

                } catch (NPException e) {
                    throw new RuntimeException(e);
                }
            } else {
	            holder.getIcon().setImageDrawable(mActivity.getResources().getDrawable(R.drawable.avatar));
            }

			holder.getText1().setText(getDisplayTitle(position));
			holder.getMenu().setOnClickListener(getOnMenuClickListener());

			return convertView;
		}

		@Override
		public View getHeaderView(int position, View convertView, ViewGroup parent) {
			if (isEmpty()) return new View(getLayoutInflater().getContext()); // empty view (no header)
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.list_header, parent, false);
				convertView.setBackgroundColor(Color.argb(127, 255, 255, 255));
			}
			final ListViewHolder holder = getHolder(convertView);

			final String sortKey = getSortKey(position);

			if (!TextUtils.isEmpty(sortKey) && sortKey.length() > 1) {
				holder.getText1().setText(sortKey.substring(0, 1));
			} else {
				holder.getText1().setText(R.string.others);
			}

			return convertView;
		}


		@Override
		public long getHeaderId(int position) {
			if (isEmpty()) return -1;

			final String sortKey = getSortKey(position);

			if (!TextUtils.isEmpty(sortKey) && sortKey.length() > 1) {
				return sortKey.substring(0, 1).toUpperCase().charAt(0);
			}

			return 0;
		}

		@Override
		protected String getEntriesHeaderText() {
			return null;
		}

		@Override
		public int getCount() {
			return mDisplayEntryList.getEntries().size();
		}

		@Override
		public NPPerson getItem(int position) {
			if (mDisplayEntryList == null || mDisplayEntryList.isEmpty()) {
				return null;
			}
			return (NPPerson)mDisplayEntryList.getEntries().get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		public void filter(String string) {
			mFilter.filter(string);
		}
	}

}
