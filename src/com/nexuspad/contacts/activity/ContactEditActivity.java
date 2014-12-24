package com.nexuspad.contacts.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.nexuspad.common.Constants;
import com.nexuspad.common.activity.EntryEditActivity;
import com.nexuspad.common.annotation.ModuleInfo;
import com.nexuspad.common.annotation.ParentActivity;
import com.nexuspad.contacts.fragment.ContactEditFragment;
import com.nexuspad.service.datamodel.NPFolder;
import com.nexuspad.service.datamodel.NPPerson;
import com.nexuspad.service.datamodel.EntryTemplate;
import com.nexuspad.service.dataservice.ServiceConstants;

/**
 * Author: edmond
 */
@ParentActivity(ContactsActivity.class)
@ModuleInfo(moduleId = ServiceConstants.CONTACT_MODULE, template = EntryTemplate.CONTACT)
public class ContactEditActivity extends EntryEditActivity<NPPerson> {

    public static void startWithFolder(Context context, NPFolder folder) {
        context.startActivity(ContactEditActivity.of(context, folder, null));
    }

    public static void startWithContact(Context context, NPFolder folder, NPPerson contact) {
        context.startActivity(ContactEditActivity.of(context, folder, contact));
    }

    public static Intent of(Context context, NPFolder folder, NPPerson contact) {
        final Intent intent = new Intent(context, ContactEditActivity.class);
        intent.putExtra(Constants.KEY_ENTRY, contact);
        intent.putExtra(Constants.KEY_FOLDER, folder);
        return intent;
    }

    @Override
    protected Fragment onCreateFragment() {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.KEY_ENTRY, mEntry);
        bundle.putParcelable(Constants.KEY_FOLDER, mFolder);

        final ContactEditFragment fragment = new ContactEditFragment();
        fragment.setArguments(bundle);

        return fragment;
    }
}