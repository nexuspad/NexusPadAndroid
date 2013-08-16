/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.photos.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.edmondapps.utils.android.activity.SinglePaneActivity;
import com.nexuspad.R;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.photos.ui.fragment.PhotosUploadFragment;

import java.util.ArrayList;
import java.util.List;

import static com.nexuspad.dataservice.ServiceConstants.PHOTO_MODULE;

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
    protected int onCreateLayoutId() {
        return R.layout.no_padding_activity;
    }

    private List<Uri> mUris = new ArrayList<Uri>();

    @Override
    protected void onCreate(Bundle savedState) {
        final Intent intent = getIntent();
        Uri uri = intent.getData();
        if (uri != null) {
            addIfNeeded(uri);
        }

        uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (uri != null) {
            addIfNeeded(uri);
        }

        final List<Uri> list = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (list != null) {
            for (Uri theUri : list) {
                addIfNeeded(theUri);
            }
        }

        super.onCreate(savedState);
    }

    private void addIfNeeded(Uri uri) {
        if (!mUris.contains(uri)) {
            mUris.add(uri);
        }
    }

    @Override
    protected Fragment onCreateFragment() {
        Folder folder = getIntent().getParcelableExtra(KEY_FOLDER);
        if (folder == null) {
            folder = Folder.rootFolderOf(PHOTO_MODULE, this);
        }
        final PhotosUploadFragment fragment = new PhotosUploadFragment();
        for (Uri uri : mUris) {
            fragment.uploadPhoto(uri, folder);
        }
        return fragment;
    }
}
