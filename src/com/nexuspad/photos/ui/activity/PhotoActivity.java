/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.photos.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.view.Window;
import com.edmondapps.utils.android.activity.SinglePaneActivity;
import com.edmondapps.utils.android.annotaion.ParentActivity;
import com.nexuspad.R;
import com.nexuspad.datamodel.EntryList;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.photos.ui.fragment.PhotoFragment;
import com.nexuspad.ui.fragment.EntriesFragment;

/**
 * @author Edmond
 * 
 */
@ParentActivity(PhotosActivity.class)
public class PhotoActivity extends SinglePaneActivity implements EntriesFragment.Callback {

    private static final String KEY_FOLDER = "key_folder";

    public static void startWithFolder(Folder f, Activity c) {
        c.startActivity(PhotoActivity.of(f, c));
        c.overridePendingTransition(0, 0);
    }

    public static Intent of(Folder f, Context c) {
        Intent intent = new Intent(c, PhotoActivity.class);
        intent.putExtra(KEY_FOLDER, f);
        return intent;
    }

    private Folder mFolder;

    @Override
    protected int onCreateLayoutId() {
        return R.layout.no_padding_activity;
    }

    @Override
    protected Fragment onCreateFragment() {
        return PhotoFragment.of(mFolder);
    }

    @Override
    protected void onCreate(Bundle savedState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        mFolder = getIntent().getParcelableExtra(KEY_FOLDER);
        if (mFolder == null) {
            throw new IllegalStateException("you must pass in a Folder with KEY_FOLDER");
        }

        super.onCreate(savedState);
        getSupportActionBar().hide();
    }

    @Override
    public void onBackPressed() {
        // exit animation
        super.onBackPressed();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public void onListLoaded(EntriesFragment f, EntryList list) {
        // i don't give a ____ :P
    }
}
