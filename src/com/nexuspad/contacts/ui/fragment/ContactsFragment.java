package com.nexuspad.contacts.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.edmondapps.utils.android.view.ViewUtils;
import com.edmondapps.utils.java.WrapperList;
import com.mobsandgeeks.adapters.Sectionizer;
import com.mobsandgeeks.adapters.SimpleSectionAdapter;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.EntryList;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.datamodel.Person;
import com.nexuspad.dataservice.ServiceConstants;
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
    protected void onListLoaded(EntryList list) {
        super.onListLoaded(list);

        mContacts = new WrapperList<Person>(list.getEntries());
        Collections.sort(mContacts);
        final BaseAdapter adapter = getListAdapter();
        if (adapter == null) {
            final ContactsAdapter contactsAdapter = new ContactsAdapter();
            setListAdapter(new SimpleSectionAdapter<Person>(getActivity(), contactsAdapter,
                    android.R.layout.simple_list_item_1, android.R.id.text1, new PersonSectionizer()));
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onNewFolder(Context c, Intent i, Folder f) {
        throw new UnsupportedOperationException();
    }

    private class PersonSectionizer implements Sectionizer<Person> {
        @Override
        public String getSectionTitleForItem(Person person) {
            final String lastName = person.getLastName();
            if (TextUtils.isEmpty(lastName)) {
                return getString(R.string.others);
            }
            if (lastName.length() > 1) {
                return lastName.substring(0, 1); // first letter
            }
            return lastName;
        }
    }

    private static class ViewHolder {
        TextView name;
    }

    private class ContactsAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mContacts.size();
        }

        @Override
        public Person getItem(int position) {
            return mContacts.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(android.R.layout.simple_list_item_1, parent, false);

                holder = new ViewHolder();
                holder.name = ViewUtils.findView(convertView, android.R.id.text1);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final Person p = getItem(position);
            holder.name.setText(getString(R.string.formatted_person_name, p.getLastName(), p.getFirstName(), p.getMiddleName()));

            return convertView;
        }
    }
}
