package com.nexuspad.contacts.ui.fragment;

import com.edmondapps.utils.android.annotaion.FragmentName;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.Contact;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.ui.fragment.NewEntryFragment;

/**
 * Author: edmond
 */
@FragmentName(NewContactFragment.TAG)
@ModuleId(moduleId = ServiceConstants.CONTACT_MODULE, template = EntryTemplate.CONTACT)
public class NewContactFragment extends NewEntryFragment<Contact> {
    public static final String TAG = "NewContactFragment";


    @Override
    public boolean isEditedEntryValid() {
        return false;
    }

    @Override
    public Contact getEditedEntry() {
        return null;
    }
}
