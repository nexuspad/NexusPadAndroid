package com.nexuspad.contacts.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.nexuspad.common.Constants;
import com.nexuspad.common.annotation.ModuleInfo;
import com.nexuspad.common.annotation.ParentActivity;
import com.nexuspad.contacts.fragment.ContactsFragment;
import com.nexuspad.service.datamodel.EntryTemplate;
import com.nexuspad.service.datamodel.NPFolder;
import com.nexuspad.service.dataservice.ServiceConstants;
import com.nexuspad.home.activity.DashboardActivity;
import com.nexuspad.common.activity.EntriesActivity;

@ParentActivity(DashboardActivity.class)
@ModuleInfo(moduleId = ServiceConstants.CONTACT_MODULE, template = EntryTemplate.CONTACT)
public class ContactsActivity extends EntriesActivity {

    public static void startWithFolder(NPFolder folder, Context context) {
        context.startActivity(ContactsActivity.of(folder, context));
    }

    public static Intent of(NPFolder folder, Context context) {
        final Intent intent = new Intent(context, ContactsActivity.class);
        intent.putExtra(Constants.KEY_FOLDER, folder);
        return intent;
    }

    @Override
    protected Fragment onCreateFragment() {
	    final Bundle bundle = new Bundle();
	    bundle.putParcelable(Constants.KEY_FOLDER, mFolder);

	    final ContactsFragment fragment = new ContactsFragment();
	    fragment.setArguments(bundle);

	    return fragment;
    }
}
