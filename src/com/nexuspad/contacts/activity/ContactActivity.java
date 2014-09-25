package com.nexuspad.contacts.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.nexuspad.common.Constants;
import com.nexuspad.common.activity.EntryActivity;
import com.nexuspad.common.annotation.ParentActivity;
import com.nexuspad.contacts.fragment.ContactFragment;
import com.nexuspad.datamodel.NPFolder;
import com.nexuspad.datamodel.NPPerson;

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
        intent.putExtra(Constants.KEY_ENTRY, contact);
        intent.putExtra(Constants.KEY_FOLDER, folder);
        return intent;
    }

    private NPPerson mContact;

    @Override
    protected void onCreate(Bundle savedState) {
        mContact = getIntent().getParcelableExtra(Constants.KEY_ENTRY);

        super.onCreate(savedState);
    }

    @Override
    protected Fragment onCreateFragment() {
        return ContactFragment.of(mContact, getFolder());
    }
}