package com.nexuspad.calendar.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import com.nexuspad.common.annotation.ParentActivity;
import com.nexuspad.calendar.fragment.EventFragment;
import com.nexuspad.datamodel.NPEvent;
import com.nexuspad.datamodel.NPFolder;
import com.nexuspad.common.activity.EntryActivity;

/**
 * Author: edmond
 */
@ParentActivity(EventsActivity.class)
public class EventActivity extends EntryActivity<NPEvent> {
    public static void startWith(Context context, NPEvent event, NPFolder folder) {
        context.startActivity(EventActivity.of(context, event, folder));
    }

    public static Intent of(Context context, NPEvent event, NPFolder folder) {
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
