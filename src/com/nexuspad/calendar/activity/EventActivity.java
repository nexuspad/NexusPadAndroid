package com.nexuspad.calendar.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.nexuspad.calendar.fragment.EventFragment;
import com.nexuspad.common.Constants;
import com.nexuspad.common.activity.EntryActivity;
import com.nexuspad.service.datamodel.NPEvent;
import com.nexuspad.service.datamodel.NPFolder;

public class EventActivity extends EntryActivity<NPEvent> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mParentActivity = EventsActivity.class;
        super.onCreate(savedInstanceState);
    }

    public static void startWith(Context context, NPEvent event, NPFolder folder) {
        context.startActivity(EventActivity.of(context, event, folder));
    }

    public static Intent of(Context context, NPEvent event, NPFolder folder) {
        final Intent intent = new Intent(context, EventActivity.class);
        intent.putExtra(Constants.KEY_ENTRY, event);
        intent.putExtra(Constants.KEY_FOLDER, folder);
        return intent;
    }

    @Override
    protected Fragment onCreateFragment() {
        return EventFragment.of(getEntry(), getFolder());
    }
}
