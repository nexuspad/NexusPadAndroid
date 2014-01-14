/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.home.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.edmondapps.utils.android.Logs;
import com.edmondapps.utils.android.activity.SinglePaneActivity;
import com.nexuspad.R;
import com.nexuspad.bookmark.ui.activity.BookmarksActivity;
import com.nexuspad.calendar.ui.activity.EventsActivity;
import com.nexuspad.contacts.ui.activity.ContactsActivity;
import com.nexuspad.doc.ui.activity.DocsActivity;
import com.nexuspad.home.ui.fragment.DashboardFragment;
import com.nexuspad.journal.ui.activity.JournalActivity;
import com.nexuspad.photos.ui.activity.PhotosActivity;

import static com.nexuspad.dataservice.ServiceConstants.*;

/**
 * @author Edmond
 */
public class DashboardActivity extends SinglePaneActivity implements DashboardFragment.Callback {
    public static final String TAG = "MainPhoneActivity";

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        getActionBar().setIcon(R.drawable.back_to_dashboard);
    }

    @Override
    protected int onCreateLayoutId() {
        return R.layout.no_padding_activity;
    }

    @Override
    protected Fragment onCreateFragment() {
        return new DashboardFragment();
    }

    @Override
    public void onModuleClicked(DashboardFragment f, int moduleType) {
        Class<?> activity = getActivityForModule(moduleType);
        if (activity != null) {
            startActivity(new Intent(this, activity));
        }
    }

    private static Class<?> getActivityForModule(int moduleType) {
        switch (moduleType) {
            case BOOKMARK_MODULE:
                return BookmarksActivity.class;
            case CALENDAR_MODULE:
                return EventsActivity.class;
            case DOC_MODULE:
                return DocsActivity.class;
            case PHOTO_MODULE:
                return PhotosActivity.class;
            case CONTACT_MODULE:
                return ContactsActivity.class;
            case JOURNAL_MODULE:
                return JournalActivity.class;
            default:
                Logs.v(TAG, "moduleType: " + moduleType);
                return null;
        }
    }
}
