package com.nexuspad.calendar.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import com.nexuspad.calendar.fragment.EventEditFragment;
import com.nexuspad.common.activity.EntryEditActivity;
import com.nexuspad.common.annotation.ParentActivity;
import com.nexuspad.common.annotation.ModuleId;
import com.nexuspad.service.datamodel.EntryTemplate;
import com.nexuspad.service.datamodel.NPEvent;
import com.nexuspad.service.datamodel.NPFolder;

import static com.nexuspad.service.dataservice.ServiceConstants.CALENDAR_MODULE;

/**
 * Author: edmond
 */
@ParentActivity(EventsActivity.class)
@ModuleId(moduleId = CALENDAR_MODULE, template = EntryTemplate.EVENT)
public class EventEditActivity extends EntryEditActivity<NPEvent> {

    public static void startWithFolder(Context context, NPFolder folder) {
        context.startActivity(EventEditActivity.of(context, folder, null));
    }

    public static void startWithEvent(Context context, NPFolder folder, NPEvent event) {
        context.startActivity(EventEditActivity.of(context, folder, event));
    }

    public static Intent of(Context context, NPFolder folder, NPEvent event) {
        final Intent intent = new Intent(context, EventEditActivity.class);
        intent.putExtra(KEY_ENTRY, event);
        intent.putExtra(KEY_FOLDER, folder);
        return intent;
    }

    @Override
    protected Fragment onCreateFragment() {
        return EventEditFragment.of(getEntry(), getFolder());
    }
}
