/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.photos.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import com.edmondapps.utils.android.activity.SinglePaneActivity;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.photos.ui.fragment.PhotosUploadFragment;

/**
 * @author Edmond
 */
public class PhotosUploadActivity extends SinglePaneActivity {
    public static final String KEY_FOLDER = "key_folder";

    public static void startWith(Uri uri, Folder folder, Context c) {
        c.startActivity(PhotosUploadActivity.of(uri, folder, c));
    }

    public static Intent of(Uri uri, Folder folder, Context c) {
        Intent intent = new Intent(c, PhotosUploadActivity.class);
        intent.setData(uri);
        intent.putExtra(KEY_FOLDER, folder);
        return intent;
    }

    @Override
    protected void onCreate(Bundle ss) {
        super.onCreate(ss);
    }

    @Override
    protected Fragment onCreateFragment() {
        final Intent intent = getIntent();
        final Folder folder = intent.getParcelableExtra(KEY_FOLDER);
        final Uri uri = intent.getData();
        final PhotosUploadFragment fragment = new PhotosUploadFragment();
        fragment.uploadPhoto(uri, folder);
        return fragment;
    }
}
