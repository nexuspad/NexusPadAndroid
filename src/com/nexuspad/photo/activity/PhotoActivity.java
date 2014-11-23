/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.photo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.nexuspad.R;
import com.nexuspad.common.Constants;
import com.nexuspad.common.activity.SinglePaneActivity;
import com.nexuspad.common.annotation.ParentActivity;
import com.nexuspad.common.fragment.EntriesFragment;
import com.nexuspad.service.datamodel.EntryList;
import com.nexuspad.service.datamodel.NPFolder;
import com.nexuspad.service.datamodel.NPPhoto;
import com.nexuspad.photo.fragment.PhotoFragment;

import java.util.ArrayList;

/**
 * @author Ren
 */
@ParentActivity(PhotosActivity.class)
public class PhotoActivity extends SinglePaneActivity implements EntriesFragment.ActivityCallback, PhotoFragment.PhotoDisplayCallback {

	/**
	 * Implement the callback when photo is deleted.
	 *
	 * @param f
	 * @param photo
	 */
	@Override
	public void onDeleting(PhotoFragment f, NPPhoto photo) {
		finish();
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

	    mFolder = intent.getParcelableExtra(Constants.KEY_FOLDER);
        if (mFolder == null) {
            throw new IllegalStateException("you must pass in a Folder with KEY_FOLDER");
        }

        mPhoto = intent.getParcelableExtra(Constants.KEY_PHOTO);
        mPhotos = intent.getParcelableArrayListExtra(Constants.KEY_PHOTOS);

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
