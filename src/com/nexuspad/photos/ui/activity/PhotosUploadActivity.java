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

/**
 * @author Edmond
 * 
 */
public class PhotosUploadActivity extends SinglePaneActivity {
    public static void startWith(Uri uri, Context c) {
        c.startActivity(PhotosUploadActivity.of(uri, c));
    }

    public static Intent of(Uri uri, Context c) {
        Intent intent = new Intent(c, PhotosUploadActivity.class);
        intent.setData(uri);
        return intent;
    }

    @Override
    protected void onCreate(Bundle ss) {
        super.onCreate(ss);
    }

    @Override
    protected Fragment onCreateFragment() {
        return null;
    }
}
