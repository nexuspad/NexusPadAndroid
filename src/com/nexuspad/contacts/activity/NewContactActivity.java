package com.nexuspad.contacts.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import com.nexuspad.common.annotaion.ParentActivity;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.contacts.fragment.NewContactFragment;
import com.nexuspad.datamodel.NPFolder;
import com.nexuspad.datamodel.NPPerson;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.common.activity.NewEntryActivity;

/**
 * Author: edmond
 */
@ParentActivity(ContactsActivity.class)
@ModuleId(moduleId = ServiceConstants.CONTACT_MODULE, template = EntryTemplate.CONTACT)
public class NewContactActivity extends NewEntryActivity<NPPerson> {

    public static void startWithFolder(Context context, NPFolder folder) {
        context.startActivity(NewContactActivity.of(context, folder, null));
    }

    public static void startWithContact(Context context, NPFolder folder, NPPerson contact) {
        context.startActivity(NewContactActivity.of(context, folder, contact));
    }

    public static Intent of(Context context, NPFolder folder, NPPerson contact) {
        final Intent intent = new Intent(context, NewContactActivity.class);
        intent.putExtra(KEY_ENTRY, contact);
        intent.putExtra(KEY_FOLDER, folder);
        return intent;
    }

    @Override
    protected Fragment onCreateFragment() {
        return NewContactFragment.of(getEntry(), getFolder());
    }
}