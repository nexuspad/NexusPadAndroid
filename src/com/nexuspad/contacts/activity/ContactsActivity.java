package com.nexuspad.contacts.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.nexuspad.common.Constants;
import com.nexuspad.common.activity.EntriesActivity;
import com.nexuspad.common.annotation.ModuleInfo;
import com.nexuspad.contacts.fragment.ContactsFragment;
import com.nexuspad.home.activity.DashboardActivity;
import com.nexuspad.service.datamodel.EntryTemplate;
import com.nexuspad.service.dataservice.ServiceConstants;

@ModuleInfo(moduleId = ServiceConstants.CONTACT_MODULE, template = EntryTemplate.CONTACT)
public class ContactsActivity extends EntriesActivity {

	@Override
	protected void onCreate(Bundle savedState) {
		mParentActivity = DashboardActivity.class;
		super.onCreate(savedState);
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
