package com.nexuspad.contacts.ui.fragment;

import com.edmondapps.utils.android.annotaion.FragmentName;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.Contact;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.ui.fragment.EntryFragment;

/**
 * Author: Edmond
 */
@FragmentName(ContactFragment.TAG)
@ModuleId(moduleId = ServiceConstants.CONTACT_MODULE, template = EntryTemplate.CONTACT)
public class ContactFragment extends EntryFragment<Contact> {
    public static final String TAG = "ContactFragment";
}
