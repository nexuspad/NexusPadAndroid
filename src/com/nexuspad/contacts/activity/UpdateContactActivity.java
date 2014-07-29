package com.nexuspad.contacts.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import com.nexuspad.common.annotation.ParentActivity;
import com.nexuspad.common.annotation.ModuleId;
import com.nexuspad.contacts.fragment.UpdateContactFragment;
import com.nexuspad.datamodel.NPFolder;
import com.nexuspad.datamodel.NPPerson;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.common.activity.UpdateEntryActivity;

/**
 * Author: edmond
 */
@ParentActivity(ContactsActivity.class)
@ModuleId(moduleId = ServiceConstants.CONTACT_MODULE, template = EntryTemplate.CONTACT)
public class UpdateContactActivity extends UpdateEntryActivity<NPPerson> {

    public static void startWithFolder(Context context, NPFolder folder) {
        context.startActivity(UpdateContactActivity.of(context, folder, null));
    }

    public static void startWithContact(Context context, NPFolder folder, NPPerson contact) {
        context.startActivity(UpdateContactActivity.of(context, folder, contact));
    }

    public static Intent of(Context context, NPFolder folder, NPPerson contact) {
        final Intent intent = new Intent(context, UpdateContactActivity.class);
        intent.putExtra(KEY_ENTRY, contact);
        intent.putExtra(KEY_FOLDER, folder);
        return intent;
    }

    @Override
    protected Fragment onCreateFragment() {
        return UpdateContactFragment.of(getEntry(), getFolder());
    }
}