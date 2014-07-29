/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.photo.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.nexuspad.R;
import com.nexuspad.common.activity.SinglePaneActivity;
import com.nexuspad.common.annotation.ParentActivity;
import com.nexuspad.common.fragment.EntriesFragment;
import com.nexuspad.datamodel.EntryList;
import com.nexuspad.datamodel.NPFolder;
import com.nexuspad.datamodel.NPPhoto;
import com.nexuspad.photo.fragment.PhotoFragment;

import java.util.ArrayList;

/**
 * @author Edmond
 */
@ParentActivity(PhotosActivity.class)
public class PhotoActivity extends SinglePaneActivity implements EntriesFragment.Callback {

    private static final String KEY_FOLDER = "key_folder";
    private static final String KEY_PHOTO = "key_photo";
    private static final String KEY_PHOTOS = "key_photos";

    public static void startWithFolder(NPFolder f, NPPhoto photo, ArrayList<NPPhoto> photos, Activity c) {
        c.startActivity(PhotoActivity.of(f, photo, photos, c));
        c.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public static Intent of(NPFolder f, NPPhoto photo, ArrayList<NPPhoto> photos, Context c) {
        Intent intent = new Intent(c, PhotoActivity.class);
        intent.putExtra(KEY_FOLDER, f);
        intent.putExtra(KEY_PHOTO, photo);
        intent.putParcelableArrayListExtra(KEY_PHOTOS, photos);
        return intent;
    }

    private NPFolder mFolder;
    private NPPhoto mPhoto;
    private ArrayList<? extends NPPhoto> mPhotos;

    @Override
    protected int onCreateLayoutId() {
        return R.layout.np_padding_activity;
    }

    @Override
    protected void onCreate(Bundle savedState) {
        final Intent intent = getIntent();
        mFolder = intent.getParcelableExtra(KEY_FOLDER);
        if (mFolder == null) {
            throw new IllegalStateException("you must pass in a Folder with KEY_FOLDER");
        }
        mPhoto = intent.getParcelableExtra(KEY_PHOTO);
        mPhotos = intent.getParcelableArrayListExtra(KEY_PHOTOS);

        super.onCreate(savedState);
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
