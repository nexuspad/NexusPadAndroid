package com.nexuspad.calendar.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import com.edmondapps.utils.android.annotaion.ParentActivity;
import com.nexuspad.calendar.ui.fragment.EventFragment;
import com.nexuspad.datamodel.Event;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.ui.activity.EntryActivity;

/**
 * Author: edmond
 */
@ParentActivity(EventsActivity.class)
public class EventActivity extends EntryActivity<Event> {
    public static void startWith(Context context, Event event, Folder folder) {
        context.startActivity(EventActivity.of(context, event, folder));
    }

    public static Intent of(Context context, Event event, Folder folder) {
        final Intent intent = new Intent(context, EventActivity.class);
        intent.putExtra(KEY_ENTRY, event);
        intent.putExtra(KEY_FOLDER, folder);
        return intent;
    }

    @Override
    protected Fragment onCreateFragment() {
        return EventFragment.of(getEntry(), getFolder());
    }
}
