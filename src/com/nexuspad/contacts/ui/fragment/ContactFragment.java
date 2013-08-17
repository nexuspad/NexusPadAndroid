package com.nexuspad.contacts.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.edmondapps.utils.android.view.ViewUtils;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.Contact;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.ui.fragment.EntryFragment;

import static com.edmondapps.utils.android.view.ViewUtils.findView;

/**
 * Author: Edmond
 */
@FragmentName(ContactFragment.TAG)
@ModuleId(moduleId = ServiceConstants.CONTACT_MODULE, template = EntryTemplate.CONTACT)
public class ContactFragment extends EntryFragment<Contact> {
    public static final String TAG = "ContactFragment";

    public static ContactFragment of(Contact contact, Folder folder) {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_ENTRY, contact);
        bundle.putParcelable(KEY_FOLDER, folder);

        final ContactFragment fragment = new ContactFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    private TextView mTitleV;
    private TextView mFirstNameV;
    private TextView mMiddleNameV;
    private TextView mLastNameV;
    private TextView mBussinessNameV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.contact_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mTitleV = findView(view, android.R.id.title);
        mFirstNameV = findView(view, R.id.lbl_first_name);
        mMiddleNameV = findView(view, R.id.lbl_middle_name);
        mLastNameV = findView(view, R.id.lbl_last_name);
        mBussinessNameV = findView(view, R.id.lbl_bussiness_name);

        updateUI();
        super.onViewCreated(view, savedInstanceState);
    }

    private void updateUI() {
        final Contact contact = getDetailEntryIfExist();
        if (contact != null) {
            mTitleV.setText(contact.getTitle());
            mFirstNameV.setText(contact.getFirstName());
            mMiddleNameV.setText(contact.getMiddleName());
            mLastNameV.setText(contact.getLastName());
            mBussinessNameV.setText(contact.getBusinessName());
        }
    }

    @Override
    protected void onEntryUpdated(Contact entry) {
        super.onEntryUpdated(entry);
        updateUI();
    }

    @Override
    protected void onDetailEntryUpdated(Contact entry) {
        super.onDetailEntryUpdated(entry);
        updateUI();
    }
}
