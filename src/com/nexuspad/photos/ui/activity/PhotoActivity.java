/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.photos.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Window;
import com.edmondapps.utils.android.activity.SinglePaneActivity;
import com.edmondapps.utils.android.annotaion.ParentActivity;
import com.nexuspad.R;
import com.nexuspad.datamodel.EntryList;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.datamodel.Photo;
import com.nexuspad.photos.ui.fragment.PhotoFragment;
import com.nexuspad.ui.fragment.EntriesFragment;

import java.util.ArrayList;

/**
 * @author Edmond
 * 
 */
@ParentActivity(PhotosActivity.class)
public class PhotoActivity extends SinglePaneActivity implements EntriesFragment.Callback {

    private static final String KEY_FOLDER = "key_folder";
    private static final String KEY_PHOTO = "key_photo";
    private static final String KEY_PHOTOS = "key_photos";

    public static void startWithFolder(Folder f, Photo photo, ArrayList<? extends Photo> photos, Activity c) {
        c.startActivity(PhotoActivity.of(f, photo, photos, c));
        c.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public static Intent of(Folder f, Photo photo, ArrayList<? extends Photo> photos, Context c) {
        Intent intent = new Intent(c, PhotoActivity.class);
        intent.putExtra(KEY_FOLDER, f);
        intent.putExtra(KEY_PHOTO, photo);
        intent.putExtra(KEY_PHOTOS, photos);
        return intent;
    }

    private Folder mFolder;
    private Photo mPhoto;
    private ArrayList<? extends Photo> mPhotos;

    @Override
    protected int onCreateLayoutId() {
        return R.layout.no_padding_activity;
    }

    @Override
    protected void onCreate(Bundle savedState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        final Intent intent = getIntent();
        mFolder = intent.getParcelableExtra(KEY_FOLDER);
        if (mFolder == null) {
            throw new IllegalStateException("you must pass in a Folder with KEY_FOLDER");
        }
        mPhoto = intent.getParcelableExtra(KEY_PHOTO);
        mPhotos = intent.getParcelableArrayListExtra(KEY_PHOTOS);

        super.onCreate(savedState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
            actionBar.setIcon(R.drawable.back_to_dashboard);
        }
    }

    @Override
    protected Fragment onCreateFragment() {
        return PhotoFragment.of(mFolder, mPhoto, mPhotos);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, android.R.anim.fade_out);
    }

    @Override
    public void onListLoaded(EntriesFragment f, EntryList list) {
        // do nothing
    }
}
