package com.nexuspad.contacts.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.edmondapps.utils.android.annotaion.ParentActivity;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.contacts.ui.fragment.ContactFragment;
import com.nexuspad.contacts.ui.fragment.NewContactFragment;
import com.nexuspad.datamodel.Contact;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.ui.activity.EntryActivity;
import com.nexuspad.ui.activity.NewEntryActivity;

/**
 * Author: edmond
 */
@ParentActivity(ContactsActivity.class)
@ModuleId(moduleId = ServiceConstants.CONTACT_MODULE, template = EntryTemplate.CONTACT)
public class NewContactActivity extends NewEntryActivity<Contact> {

    public static void startWith(Context context, Contact contact, Folder folder) {
       context.startActivity(NewContactActivity.of(context, contact, folder));
    }

    public static Intent of(Context context, Contact contact, Folder folder) {
        final Intent intent = new Intent(context, NewContactActivity.class);
        intent.putExtra(KEY_ENTRY, contact);
        intent.putExtra(KEY_FOLDER, folder);
        return intent;
    }

    private Contact mContact;

    @Override
    protected void onCreate(Bundle savedState) {
        mContact = getIntent().getParcelableExtra(KEY_ENTRY);

        super.onCreate(savedState);
    }

    @Override
    protected Fragment onCreateFragment() {
        return NewContactFragment.of(mContact, getFolder());
    }
}