package com.nexuspad.calendar.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.nexuspad.calendar.fragment.EventEditFragment;
import com.nexuspad.common.Constants;
import com.nexuspad.common.activity.EntryEditActivity;
import com.nexuspad.common.annotation.ModuleInfo;
import com.nexuspad.common.annotation.ParentActivity;
import com.nexuspad.service.datamodel.EntryTemplate;
import com.nexuspad.service.datamodel.NPEvent;
import com.nexuspad.service.datamodel.NPFolder;

import static com.nexuspad.service.dataservice.ServiceConstants.CALENDAR_MODULE;

/**
 * Author: edmond
 */
@ParentActivity(EventsActivity.class)
@ModuleInfo(moduleId = CALENDAR_MODULE, template = EntryTemplate.EVENT)
public class EventEditActivity extends EntryEditActivity<NPEvent> {

    public static void startWithFolder(Context context, NPFolder folder) {
        context.startActivity(EventEditActivity.of(context, folder, null));
    }

    public static void startWithEvent(Context context, NPFolder folder, NPEvent event) {
        context.startActivity(EventEditActivity.of(context, folder, event));
    }

    public static Intent of(Context context, NPFolder folder, NPEvent event) {
        final Intent intent = new Intent(context, EventEditActivity.class);
        intent.putExtra(Constants.KEY_ENTRY, event);
        intent.putExtra(Constants.KEY_FOLDER, folder);
        return intent;
    }

    @Override
    protected Fragment onCreateFragment() {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.KEY_ENTRY, mEntry);
        bundle.putParcelable(Constants.KEY_FOLDER, mFolder);

        final EventEditFragment fragment = new EventEditFragment();
        fragment.setArguments(bundle);

        return fragment;
    }
}
