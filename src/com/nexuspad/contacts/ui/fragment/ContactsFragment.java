package com.nexuspad.contacts.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import com.edmondapps.utils.android.Logs;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.edmondapps.utils.java.WrapperList;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.contacts.ui.activity.ContactActivity;
import com.nexuspad.contacts.ui.activity.ContactsActivity;
import com.nexuspad.contacts.ui.activity.NewContactActivity;
import com.nexuspad.datamodel.*;
import com.nexuspad.dataservice.NPException;
import com.nexuspad.dataservice.NPWebServiceUtil;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.photos.ui.activity.PhotosActivity;
import com.nexuspad.ui.EntriesAdapter;
import com.nexuspad.ui.OnEntryMenuClickListener;
import com.nexuspad.ui.activity.FoldersActivity;
import com.nexuspad.ui.fragment.EntriesFragment;
import com.squareup.picasso.Picasso;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

import java.lang.ref.WeakReference;
import java.util.*;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.contacts_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ListView listView = getListView();
        listView.setFastScrollEnabled(true);

        setQuickReturnListener(listView, null);
        setOnFolderSelectedClickListener(REQ_FOLDER);
    }

    @Override
    protected void onListLoaded(EntryList list) {
        super.onListLoaded(list);

        mContacts = new WrapperList<Contact>(list.getEntries());
        if (mSortTask == null) {
            mSortTask = new SortTask(mContacts, this, getString(R.string.others));
        } else {
            mSortTask.cancel(true);
        }
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
                    activity.finish();
                    activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
                break;
            default:
                throw new AssertionError("unknown requestCode: " + requestCode);
        }
    }

    @Override
    public void onDestroy() {
        mSortTask.cancel(true);
        super.onDestroy();
    }

    @Override
    public ContactsAdapter getListAdapter() {
        return (ContactsAdapter) super.getListAdapter();
    }

    private ContactsAdapter newContactsAdapter(Map<String, Integer> map) {
        final ContactsAdapter a = new ContactsAdapter(getActivity(), map);
        final ListView listView = getListView();
        a.setOnMenuClickListener(new OnEntryMenuClickListener<Contact>(listView, getEntryService()) {
            @Override
            public void onClick(View v) {
                final int i = listView.getPositionForView(v);
                final Contact contact = a.getItem(i);
                onEntryClick(contact, i, v);
            }

            @Override
            protected boolean onEntryMenuClick(Contact contact, int menuId) {
                switch (menuId) {
                    case R.id.edit:
                        NewContactActivity.startWith(getActivity(), contact, getFolder());
                        return true;
                    default:
                        return super.onEntryMenuClick(contact, menuId);
                }
            }
        });
        listView.setOnItemLongClickListener(a);
        return a;
    }

    private static String[] toArray(Collection<String> set) {
        final String[] strings = new String[set.size()];
        return set.toArray(strings);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        final Contact contact = getListAdapter().getItem(position);
        ContactActivity.startWith(getActivity(), contact, getFolder());
    }

    private class ContactsAdapter extends EntriesAdapter<Contact> implements StickyListHeadersAdapter, SectionIndexer {
        private final Picasso mPicasso;
        private final Map<String, Integer> mSectionMap;
        private final String[] mSections;

        private ContactsAdapter(Activity a, Map<String, Integer> sectionMap) {
            super(a, mContacts);
            mSectionMap = sectionMap;
            mSections = toArray(mSectionMap.keySet());
            mPicasso = Picasso.with(a);
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

            final String profileImageUrl = p.getProfileImageUrl();
            if (!TextUtils.isEmpty(profileImageUrl)) {
                try {
                    final String url = NPWebServiceUtil.fullUrlWithAuthenticationTokens(profileImageUrl, getActivity());

                    mPicasso.load(url)
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.ic_launcher)
                            .into(holder.icon);

                } catch (NPException e) {
                    throw new RuntimeException(e);
                }
            }

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
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_header, parent, false);
            }
            ViewHolder holder = getHolder(convertView);

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
        public Object[] getSections() {
            return mSections;
        }

        @Override
        public int getPositionForSection(int section) {
            return mSectionMap.get(mSections[section >= mSections.length ? mSections.length - 1 : section]);
        }

        @Override
        public int getSectionForPosition(int position) {
            final Integer boxedPos = position;
            Integer previous = 0;
            for (Integer integer : mSectionMap.values()) {
                if (previous.compareTo(boxedPos) > 0) {
                    return previous;
                }
                previous = integer;
            }
            Logs.w(TAG, "cannot find section for position: " + position);
            return previous;
        }
    }

    private static class SortTask extends AsyncTask<Void, Void, Void> {
        private final List<? extends NPEntry> mCollection;
        private final String mPlaceHolder;
        private final WeakReference<ContactsFragment> mFragment;
        private final Map<String, Integer> mMap = new LinkedHashMap<String, Integer>(26);

        private SortTask(List<? extends NPEntry> collection, ContactsFragment fragment, String placeHolder) {
            mCollection = collection;
            mFragment = new WeakReference<ContactsFragment>(fragment);
            mPlaceHolder = placeHolder;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Collections.sort(mCollection, NPEntry.ORDERING_BY_TITLE);

            for (int i = 0, mCollectionSize = mCollection.size(); i < mCollectionSize; i++) {
                NPEntry entry = mCollection.get(i);
                final String title = entry.getTitle();
                if (title != null && title.length() > 1) {
                    final String firstChar = title.substring(0, 1).toUpperCase();
                    putIfAbsent(mMap, firstChar, i);
                } else {
                    putIfAbsent(mMap, mPlaceHolder, i);
                }
                if (isCancelled()) return null;
            }

            return null;
        }

        private static <K, V> void putIfAbsent(Map<K, V> map, K key, V value) {
            if (!map.containsKey(key)) {
                map.put(key, value);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            final ContactsFragment fragment = mFragment.get();
            if (fragment != null && fragment.isAdded()) {
                final BaseAdapter adapter = fragment.getListAdapter();
                if (adapter == null) {
                    fragment.setListAdapter(fragment.newContactsAdapter(mMap));
                } else {
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }
}
