package com.nexuspad.contacts.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.nexuspad.common.annotaion.ParentActivity;
import com.nexuspad.contacts.fragment.ContactFragment;
import com.nexuspad.datamodel.NPFolder;
import com.nexuspad.datamodel.NPPerson;
import com.nexuspad.common.activity.EntryActivity;

/**
 * Author: edmond
 */
@ParentActivity(ContactsActivity.class)
public class ContactActivity extends EntryActivity<NPPerson> {

    public static void startWith(Context context, NPPerson contact, NPFolder folder) {
        context.startActivity(ContactActivity.of(context, contact, folder));
    }

    public static Intent of(Context context, NPPerson contact, NPFolder folder) {
        final Intent intent = new Intent(context, ContactActivity.class);
        intent.putExtra(KEY_ENTRY, contact);
        intent.putExtra(KEY_FOLDER, folder);
        return intent;
    }

    private NPPerson mContact;

    @Override
    protected void onCreate(Bundle savedState) {
        mContact = getIntent().getParcelableExtra(KEY_ENTRY);

        super.onCreate(savedState);
    }

    @Override
    protected Fragment onCreateFragment() {
        return ContactFragment.of(mContact, getFolder());
    }
}