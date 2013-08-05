package com.nexuspad.contacts.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.edmondapps.utils.java.WrapperList;
import com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.*;
import com.nexuspad.dataservice.NPService;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.ui.EntriesAdapter;
import com.nexuspad.ui.OnEntryMenuClickListener;
import com.nexuspad.ui.fragment.EntriesFragment;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

@FragmentName(ContactsFragment.TAG)
@ModuleId(moduleId = ServiceConstants.CONTACT_MODULE, template = EntryTemplate.CONTACT)
public class ContactsFragment extends EntriesFragment {
    public static final String TAG = "ContactsFragment";

    public static ContactsFragment of(Folder folder) {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_FOLDER, folder);

        final ContactsFragment fragment = new ContactsFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    private List<Person> mContacts;

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
        return inflater.inflate(R.layout.list_content_sticky, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setFastScrollEnabled(true);
    }

    @Override
    protected void onListLoaded(EntryList list) {
        super.onListLoaded(list);

        mContacts = new WrapperList<Person>(list.getEntries());

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Collections.sort(mContacts, NPEntry.ORDERING_BY_TITLE);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (isAdded()) {
                    final BaseAdapter adapter = getListAdapter();
                    if (adapter == null) {
                        setListAdapter(newContactsAdapter());
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }.execute((Void[]) null);
    }

    private ContactsAdapter newContactsAdapter() {
        final ContactsAdapter a = new ContactsAdapter(getActivity());
        final ListView listView = getListView();
        a.setOnMenuClickListener(new OnEntryMenuClickListener<Person>(listView, getEntryService()) {
            @Override
            public void onClick(View v) {
                final int i = listView.getPositionForView(v);
                final Person person = a.getItem(i);
                onEntryClick(person, i, v);
            }
        });
        listView.setOnItemLongClickListener(a);
        return a;
    }

    private class ContactsAdapter extends EntriesAdapter<Person> implements StickyListHeadersAdapter {
        private final Picasso mPicasso;

        private ContactsAdapter(Activity a) {
            super(a, mContacts);
            mPicasso = Picasso.with(a);
        }

        private String getDisplayString(int position) {
            final Person p = getItem(position);

            if (true) {
                return p.getTitle();
            }

            final String lastName = p.getLastName();
            final String firstName = p.getFirstName();
            final String middleName = p.getMiddleName();

            if (lastName == null || firstName == null) {
                return getString(R.string.others);
            }

            return getString(R.string.formatted_person_name, lastName, firstName, middleName);
        }

        @Override
        protected View getEntryView(Person p, int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_icon, parent, false);
            }
            final ViewHolder holder = getHolder(convertView);

            final String profileImageUrl = p.getProfileImageUrl();
            if (!TextUtils.isEmpty(profileImageUrl)) {
                final String url = NPService.addAuthToken(profileImageUrl);
                mPicasso.load(url)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.ic_launcher)
                        .resizeDimen(R.dimen.magic_length, R.dimen.magic_length)
                        .centerCrop()
                        .into(holder.icon);
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
                return string.substring(0, 1).charAt(0);
            }
            return 0;
        }

        @Override
        protected int getEntryStringId() {
            return 0;
        }
    }
}
