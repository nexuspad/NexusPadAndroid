package com.nexuspad.contacts.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import com.edmondapps.utils.android.annotaion.ParentActivity;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.contacts.ui.fragment.ContactsFragment;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.home.ui.activity.DashboardActivity;
import com.nexuspad.ui.activity.EntriesActivity;

@ParentActivity(DashboardActivity.class)
@ModuleId(moduleId = ServiceConstants.CONTACT_MODULE, template = EntryTemplate.CONTACT)
public class ContactsActivity extends EntriesActivity {

    public static void startWithFolder(Folder folder, Context context) {
        context.startActivity(ContactsActivity.of(folder, context));
    }

    public static Intent of(Folder folder, Context context) {
        final Intent intent = new Intent(context, ContactsActivity.class);
        intent.putExtra(KEY_FOLDER, folder);
        return intent;
    }

    @Override
    protected Fragment onCreateFragment() {
        return ContactsFragment.of(getFolder());
    }
}
