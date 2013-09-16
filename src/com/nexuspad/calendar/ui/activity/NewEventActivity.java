package com.nexuspad.calendar.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import com.edmondapps.utils.android.annotaion.ParentActivity;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.calendar.ui.fragment.NewEventFragment;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Event;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.ui.activity.NewEntryActivity;

import static com.nexuspad.dataservice.ServiceConstants.CALENDAR_MODULE;

/**
 * Author: edmond
 */
@ParentActivity(EventsActivity.class)
@ModuleId(moduleId = CALENDAR_MODULE, template = EntryTemplate.EVENT)
public class NewEventActivity extends NewEntryActivity<Event> {
    public static void startWith(Context context, Event event, Folder folder) {
        context.startActivity(NewEventActivity.of(context, event, folder));
    }

    public static Intent of(Context context, Event event, Folder folder) {
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
