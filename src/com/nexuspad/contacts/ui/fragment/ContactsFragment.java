package com.nexuspad.contacts.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.*;
import android.widget.BaseAdapter;
import android.widget.ListView;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.edmondapps.utils.java.WrapperList;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.contacts.ui.activity.ContactActivity;
import com.nexuspad.contacts.ui.activity.ContactsActivity;
import com.nexuspad.contacts.ui.activity.NewContactActivity;
import com.nexuspad.datamodel.*;
import com.nexuspad.dataservice.EntryListService;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.ui.EntriesAdapter;
import com.nexuspad.ui.OnEntryMenuClickListener;
import com.nexuspad.ui.activity.FoldersActivity;
import com.nexuspad.ui.fragment.EntriesFragment;
import com.nexuspad.ui.fragment.FadeListFragment;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

@FragmentName(ContactsFragment.TAG)
@ModuleId(moduleId = ServiceConstants.CONTACT_MODULE, template = EntryTemplate.CONTACT)
public final class ContactsFragment extends EntriesFragment {
    public static final String TAG = "ContactsFragment";

    public static ContactsFragment of(Folder folder) {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_FOLDER, folder);

        final ContactsFragment fragment = new ContactsFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    private static final int REQ_FOLDER = 1;

    private List<Contact> mContacts;
    private SortTask mSortTask;
    private MenuItem mAddItem;

    private final EntriesAdapter.OnFilterDoneListener<Contact> mFilterDoneListener = new EntriesAdapter.OnFilterDoneListener<Contact>() {
        @Override
        public void onFilterDone(List<Contact> displayEntries) {
            fadeInListFrame();
        }
    };

    @Override
    protected int getEntriesCountPerPage() {
        return Integer.MAX_VALUE;
    }

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

        mAddItem = menu.findItem(R.id.menu_new);

        setUpSearchView(menu.findItem(R.id.search));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_contact:
                NewContactActivity.startWithFolder(getActivity(), getFolder());
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

        final FadeListFragment.ListViewManager manager = getListViewManager();
        manager.setFastScrollEnabled(false);     // not ready for the first release

        setQuickReturnListener(manager, null);
        setOnFolderSelectedClickListener(REQ_FOLDER);
    }

    @Override
    protected void onListLoaded(EntryList list) {
        super.onListLoaded(list);

        mContacts = new WrapperList<Contact>(list.getEntries());
        if (mSortTask != null) {
            mSortTask.cancel(true);
        }
        mSortTask = new SortTask(mContacts, this);
        mSortTask.execute((Void[]) null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_FOLDER:
                if (resultCode == Activity.RESULT_OK) {
                    final FragmentActivity activity = getActivity();
                    final Folder folder = data.getParcelableExtra(FoldersActivity.KEY_FOLDER);
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

    @Override
    public ContactsAdapter getListAdapter() {
        return (ContactsAdapter) super.getListAdapter();
    }

    private ContactsAdapter newContactsAdapter(List<Contact> contacts) {
        final ContactsAdapter a = new ContactsAdapter(getActivity(), contacts, getFolder(), getEntryListService(), getTemplate(), mFilterDoneListener);
        final ListView listView = getListView();
        a.setOnMenuClickListener(new OnEntryMenuClickListener<Contact>(listView, getEntryService(), getUndoBarController()) {
            @Override
            public void onClick(View v) {
                final int i = listView.getPositionForView(v);
                final Contact contact = a.getItem(i);
                onEntryClick(contact, i, v);
            }

            @Override
            protected boolean onEntryMenuClick(Contact contact, int pos, int menuId) {
                switch (menuId) {
                    case R.id.edit:
                        NewContactActivity.startWithContact(getActivity(), getFolder(), contact);
                        return true;
                    default:
                        return super.onEntryMenuClick(contact, pos, menuId);
                }
            }
        });
        listView.setOnItemLongClickListener(a);
        return a;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        final Contact contact = getListAdapter().getItem(position);
        ContactActivity.startWith(getActivity(), contact, getFolder());
    }

    @Override
    protected EntriesAdapter<?> getFilterableAdapter() {
        return getListAdapter();
    }


    /**
     * Override common EntriesAdapter for Contact list.
     */
    private static class ContactsAdapter extends EntriesAdapter<Contact> implements StickyListHeadersAdapter {
        private final EntriesAdapterLocalFilter mFilter;

        private ContactsAdapter(Activity a, List<Contact> contacts, Folder folder, EntryListService service, EntryTemplate template, OnFilterDoneListener<Contact> onFilterDoneListener) {
            super(a, contacts, folder, service, template);
            mFilter = new EntriesAdapterLocalFilter(onFilterDoneListener);
        }

        private String getDisplayString(int position) {
            return getItem(position).getTitle();
        }

        @Override
        protected View getEntryView(Contact p, int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_icon, parent, false);
            }
            final ViewHolder holder = getHolder(convertView);

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

            holder.text1.setText(getDisplayString(position));
            holder.menu.setOnClickListener(getOnMenuClickListener());

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
            final ViewHolder holder = getHolder(convertView);

            final String string = getDisplayString(position);
            if (!TextUtils.isEmpty(string) && string.length() > 1) {
                holder.text1.setText(string.substring(0, 1));
            } else {
                holder.text1.setText(R.string.others);
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
        protected int getEntryStringId() {
            return 0;
        }

        @Override
        public void filter(String string) {
            mFilter.filter(string);
        }
    }

    private static class SortTask extends AsyncTask<Void, Void, Void> {
        private final List<Contact> mContacts;
        private final WeakReference<ContactsFragment> mFragment;

        private SortTask(List<Contact> contacts, ContactsFragment fragment) {
            mContacts = contacts;
            mFragment = new WeakReference<ContactsFragment>(fragment);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Collections.sort(mContacts, NPEntry.ORDERING_BY_TITLE);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            final ContactsFragment fragment = mFragment.get();
            if (fragment != null && fragment.isAdded()) {
                final BaseAdapter adapter = fragment.getListAdapter();
                if (adapter == null) {
                    fragment.setListAdapter(fragment.newContactsAdapter(mContacts));
                    fragment.mAddItem.setVisible(true);
                    fragment.mAddItem.setEnabled(true);
                } else {
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }
}
