package com.nexuspad.calendar.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import com.nexuspad.common.activity.UpdateEntryActivity;
import com.nexuspad.common.annotation.ParentActivity;
import com.nexuspad.common.annotation.ModuleId;
import com.nexuspad.calendar.fragment.NewEventFragment;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.NPEvent;
import com.nexuspad.datamodel.NPFolder;

import static com.nexuspad.dataservice.ServiceConstants.CALENDAR_MODULE;

/**
 * Author: edmond
 */
@ParentActivity(EventsActivity.class)
@ModuleId(moduleId = CALENDAR_MODULE, template = EntryTemplate.EVENT)
public class NewEventActivity extends UpdateEntryActivity<NPEvent> {

    public static void startWithFolder(Context context, NPFolder folder) {
        context.startActivity(NewEventActivity.of(context, folder, null));
    }

    public static void startWithEvent(Context context, NPFolder folder, NPEvent event) {
        context.startActivity(NewEventActivity.of(context, folder, event));
    }

    public static Intent of(Context context, NPFolder folder, NPEvent event) {
        final Intent intent = new Intent(context, NewEventActivity.class);
        intent.putExtra(KEY_ENTRY, event);
        intent.putExtra(KEY_FOLDER, folder);
        return intent;
    }

    @Override
    protected Fragment onCreateFragment() {
        return NewEventFragment.of(getEntry(), getFolder());
    }
}