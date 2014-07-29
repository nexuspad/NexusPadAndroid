package com.nexuspad.contacts.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItemCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import com.google.common.base.Strings;
import com.nexuspad.R;
import com.nexuspad.common.annotation.ModuleId;
import com.nexuspad.common.activity.FoldersNavigatorActivity;
import com.nexuspad.common.adapters.EntriesAdapter;
import com.nexuspad.common.adapters.ListViewHolder;
import com.nexuspad.common.annotation.FragmentName;
import com.nexuspad.common.fragment.EntriesFragment;
import com.nexuspad.common.listeners.OnEntryMenuClickListener;
import com.nexuspad.common.utils.EntriesLocalSearchFilter;
import com.nexuspad.contacts.activity.ContactActivity;
import com.nexuspad.contacts.activity.ContactsActivity;
import com.nexuspad.contacts.activity.UpdateContactActivity;
import com.nexuspad.datamodel.*;
import com.nexuspad.dataservice.EntryListService;
import com.nexuspad.dataservice.ServiceConstants;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

@FragmentName(ContactsFragment.TAG)
@ModuleId(moduleId = ServiceConstants.CONTACT_MODULE, template = EntryTemplate.CONTACT)
public final class ContactsFragment extends EntriesFragment {
	public static final String TAG = "ContactsFragment";

	private SortTask mSortTask;

	private StickyListHeadersListView mStickyHeaderContactListView;

	public static ContactsFragment of(NPFolder folder) {
		final Bundle bundle = new Bundle();
		bundle.putParcelable(KEY_FOLDER, folder);

		final ContactsFragment fragment = new ContactsFragment();
		fragment.setArguments(bundle);

		return fragment;
	}

	private final EntriesLocalSearchFilter.OnFilterDoneListener<NPPerson> mFilterDoneListener = new EntriesLocalSearchFilter.OnFilterDoneListener<NPPerson>() {
		@Override
		public void onFilterDone(List<NPPerson> persons) {
			EntryList filteredResult = new EntryList();

			for (NPPerson p : persons) {
				filteredResult.getEntries().add(p);
			}

			((ContactsAdapter)getAdapter()).setDisplayEntries(filteredResult);
			getAdapter().notifyDataSetChanged();
		}
	};


	@Override
	protected boolean isAutoLoadMoreEnabled() {
		// we are loading everything from the start
		return false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.contacts_frag, menu);
		setUpSearchView(menu.findItem(R.id.search));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.new_contact:
				UpdateContactActivity.startWithFolder(getActivity(), getFolder());
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

		final View theView = view.findViewById(R.id.main_list_view);

		mStickyHeaderContactListView = (StickyListHeadersListView)theView;

		mStickyHeaderContactListView.setFastScrollEnabled(false);     // not ready for the first release

		// set the folder selector view bar
		mStickyHeaderContactListView.setOnScrollListener(newDirectionalScrollListener(null));

		// set the listener for folder selector
		initFolderSelector(ACTIVITY_REQ_CODE_FOLDER_SELECTOR);

		mStickyHeaderContactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (getAdapter() != null) {
					final NPPerson contact = ((ContactsAdapter)getAdapter()).getItem(position);
					ContactActivity.startWith(getActivity(), contact, getFolder());
				}
			}
		});

		//mStickyHeaderContactListView.setItemsCanFocus(true);
		mStickyHeaderContactListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	}

	@Override
	protected void onListLoaded(EntryList list) {
		Log.i(TAG, "Receiving entry list.");

		if (mSortTask != null) {
			mSortTask.cancel(true);
		}
		mSortTask = new SortTask(mEntryList, this);
		mSortTask.execute((Void[]) null);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case ACTIVITY_REQ_CODE_FOLDER_SELECTOR:
				if (resultCode == Activity.RESULT_OK) {
					final FragmentActivity activity = getActivity();
					final NPFolder folder = data.getParcelableExtra(FoldersNavigatorActivity.KEY_FOLDER);
					ContactsActivity.startWithFolder(folder, activity);
					activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
				}
				break;
			default:
				throw new AssertionError("unknown requestCode: " + requestCode);
		}
	}

	@Override
	public void onDestroy() {
		if (mSortTask != null) {
			mSortTask.cancel(true);
		}
		super.onDestroy();
	}


	private ContactsAdapter newContactsAdapter(EntryList entryList) {
		final ContactsAdapter a = new ContactsAdapter(getActivity(), entryList, getFolder(), getEntryListService(), getTemplate(), mFilterDoneListener);

		a.setOnMenuClickListener(new OnEntryMenuClickListener<NPPerson>(mStickyHeaderContactListView, getEntryService(), getUndoBarController()) {
			@Override
			public void onClick(View v) {
				final int i = mStickyHeaderContactListView.getPositionForView(v);
				final NPPerson contact = a.getItem(i);
				onEntryClick(contact, i, v);
			}

			@Override
			protected boolean onEntryMenuClick(NPPerson contact, int pos, int menuId) {
				switch (menuId) {
					case R.id.edit:
						UpdateContactActivity.startWithContact(getActivity(), getFolder(), contact);
						return true;
					default:
						return super.onEntryMenuClick(contact, pos, menuId);
				}
			}
		});

		mStickyHeaderContactListView.setOnItemLongClickListener(a);
		return a;
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
						((ContactsAdapter)getAdapter()).doSearch(query);
					}
					return true;
				}

				@Override
				public boolean onQueryTextChange(String newText) {
					// For Contact module, enable the instant search feedback since the search is done locally.
					if (Strings.isNullOrEmpty(newText)) {
						reDisplayListEntries();
					} else {
						((ContactsAdapter)getAdapter()).doSearch(newText);
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

	@Override
	protected void reDisplayListEntries() {
		fadeInListFrame();
		((ContactsAdapter)getAdapter()).setDisplayEntries(mEntryList);
		getAdapter().notifyDataSetChanged();
	}

	/**
	 * Override common ListEntriesAdapter for Contact list.
	 */
	public static class ContactsAdapter extends EntriesAdapter<NPPerson> implements StickyListHeadersAdapter {
		private final EntriesLocalSearchFilter mFilter;

		private ContactsAdapter(Activity a, EntryList entryList, NPFolder folder, EntryListService service, EntryTemplate template, EntriesLocalSearchFilter.OnFilterDoneListener<NPPerson> onFilterDoneListener) {
			super(a, entryList, folder, service, template);
			mFilter = new EntriesLocalSearchFilter(entryList, onFilterDoneListener);
		}

		private String getDisplayString(int position) {
			return getItem(position).getTitle();
		}

		@Override
		protected View getEntryView(NPPerson p, int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.list_item_icon, parent, false);
			}
			final ListViewHolder holder = getHolder(convertView);

//            postponed for the first release
//            final String profileImageUrl = p.getProfileImageUrl();
//            if (!TextUtils.isEmpty(profileImageUrl)) {
//                try {
//                    final String url = NPWebServiceUtil.fullUrlWithAuthenticationTokens(profileImageUrl, getActivity());
//
//                    mPicasso.load(url)
//                            .placeholder(R.drawable.placeholder)
//                            .error(R.drawable.ic_launcher)
//                            .into(holder.icon);
//
//                } catch (NPException e) {
//                    throw new RuntimeException(e);
//                }
//            }

			holder.getText1().setText(getDisplayString(position));
			holder.getMenu().setOnClickListener(getOnMenuClickListener());

			return convertView;
		}

		@Override
		protected View getEmptyEntryView(LayoutInflater i, View c, ViewGroup p) {
			return getCaptionView(i, c, p, R.string.empty_contacts, R.drawable.empty_folder);
		}

		@Override
		public View getHeaderView(int position, View convertView, ViewGroup parent) {
			if (isEmpty()) return new View(getLayoutInflater().getContext()); // empty view (no header)
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.list_header, parent, false);
				convertView.setBackgroundColor(Color.argb(127, 255, 255, 255));
			}
			final ListViewHolder holder = getHolder(convertView);

			final String string = getDisplayString(position);
			if (!TextUtils.isEmpty(string) && string.length() > 1) {
				holder.getText1().setText(string.substring(0, 1));
			} else {
				holder.getText1().setText(R.string.others);
			}

			return convertView;
		}

		@Override
		public long getHeaderId(int position) {
			if (isEmpty()) return -1;
			final String string = getDisplayString(position);
			if (!TextUtils.isEmpty(string) && string.length() > 1) {
				return string.substring(0, 1).toUpperCase().charAt(0);
			}
			return 0;
		}

		@Override
		protected String getEntriesHeaderText() {
			return null;
		}

		@Override
		public void doSearch(String string) {
			mFilter.filter(string);
		}
	}

	private static class SortTask extends AsyncTask<Void, Void, Void> {
		private final EntryList mContactList;
		private final WeakReference<ContactsFragment> mFragment;

		private SortTask(EntryList contactList, ContactsFragment fragment) {
			mContactList = contactList;
			mFragment = new WeakReference<ContactsFragment>(fragment);
		}

		@Override
		protected Void doInBackground(Void... params) {
			Collections.sort(mContactList.getEntries(), NPEntry.ORDERING_BY_TITLE);
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			final ContactsFragment fragment = mFragment.get();
			if (fragment != null && fragment.isAdded()) {
				if (fragment.getAdapter() == null) {
					fragment.setAdapter(fragment.newContactsAdapter(mContactList));
					fragment.mStickyHeaderContactListView.setAdapter((ContactsAdapter)fragment.getAdapter());
				}

				fragment.fadeInListFrame();
				fragment.getAdapter().notifyDataSetChanged();
			}
		}
	}
}
