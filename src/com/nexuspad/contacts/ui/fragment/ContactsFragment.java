package com.nexuspad.contacts.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import com.edmondapps.utils.java.WrapperList;
import com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.EntryList;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.datamodel.Person;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.ui.EntriesAdapter;
import com.nexuspad.ui.fragment.EntriesFragment;

import java.util.Collections;
import java.util.List;

@ModuleId(moduleId = ServiceConstants.CONTACT_MODULE, template = EntryTemplate.CONTACT)
public class ContactsFragment extends EntriesFragment {

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
        Collections.sort(mContacts, Person.ORDERING_BY_LAST_NAME);
        final BaseAdapter adapter = getListAdapter();
        if (adapter == null) {
            final ContactsAdapter contactsAdapter = new ContactsAdapter();
            setListAdapter(contactsAdapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onNewFolder(Context c, Intent i, Folder f) {
        throw new UnsupportedOperationException();
    }

    private class ContactsAdapter extends EntriesAdapter<Person> implements StickyListHeadersAdapter {
        public ContactsAdapter() {
            super(getActivity(), mContacts);
        }

        @Override
        protected View getEntryView(Person p, int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_icon, parent, false);
            }
            final ViewHolder holder = getHolder(convertView);

            holder.text1.setText(getString(R.string.formatted_person_name, p.getLastName(), p.getFirstName(), p.getMiddleName()));

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

            final String lastName = getItem(position).getLastName();
            if (!TextUtils.isEmpty(lastName) && lastName.length() > 1) {
                holder.text1.setText(lastName.substring(0, 1));
            }

            return convertView;
        }

        @Override
        protected int getEntryStringId() {
            return 0;
        }

        @Override
        public long getHeaderId(int position) {
            return getHeaderChar(position);
        }

        private char getHeaderChar(int position) {
            final String lastName = getItem(position).getLastName();
            if (!TextUtils.isEmpty(lastName) && lastName.length() > 1) {
                return lastName.substring(0, 1).charAt(0);
            }
            return 0;
        }
    }
}
