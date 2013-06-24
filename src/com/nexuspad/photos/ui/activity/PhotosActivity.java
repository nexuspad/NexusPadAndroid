/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.photos.ui.activity;

import static com.nexuspad.dataservice.ServiceConstants.PHOTO_MODULE;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.edmondapps.utils.android.annotaion.ParentActivity;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.home.ui.activity.DashboardActivity;
import com.nexuspad.photos.ui.fragment.PhotosFragment;
import com.nexuspad.ui.activity.EntriesActivity;

/**
 * @author Edmond
 * 
 */
@ParentActivity(DashboardActivity.class)
@ModuleId(moduleId = PHOTO_MODULE, template = EntryTemplate.PHOTO)
public class PhotosActivity extends EntriesActivity {

    public static void startWithFolder(Folder f, Context c) {
        c.startActivity(PhotosActivity.of(f, c));
    }

    public static Intent of(Folder f, Context c) {
        Intent intent = new Intent(c, PhotosActivity.class);
        intent.putExtra(KEY_FOLDER, f);
        return intent;
    }

    @Override
    protected Fragment onCreateFragment() {
        return PhotosFragment.of(getFolder());
    }
}
