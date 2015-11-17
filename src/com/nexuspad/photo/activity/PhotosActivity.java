/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.photo.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.nexuspad.R;
import com.nexuspad.common.Constants;
import com.nexuspad.common.activity.EntriesActivity;
import com.nexuspad.common.activity.UploadCenterActivity;
import com.nexuspad.common.annotation.ModuleInfo;
import com.nexuspad.home.activity.DashboardActivity;
import com.nexuspad.photo.fragment.PhotosFragment;
import com.nexuspad.service.datamodel.EntryTemplate;
import com.nexuspad.service.datamodel.NPFolder;

import java.util.ArrayList;

import static com.nexuspad.service.dataservice.ServiceConstants.PHOTO_MODULE;

/**
 * @author Edmond
 */
@ModuleInfo(moduleId = PHOTO_MODULE, template = EntryTemplate.PHOTO)
public class PhotosActivity extends EntriesActivity implements ActionBar.OnNavigationListener {
    public static final String TAG = "PhotosActivity";
    public static final String KEY_SPINNER_INDEX = "key_spinner_index";

    private static final int REQ_CHOOSE_FILE = 2;

    public static void startWithFolder(NPFolder f, Context c) {
        c.startActivity(PhotosActivity.of(f, c));
    }

    public static void startWithFolderAndIndex(NPFolder f, Context c, int initialSpinnerIndex) {
        c.startActivity(PhotosActivity.of(f, c, initialSpinnerIndex));
    }

    public static Intent of(NPFolder f, Context c) {
        return PhotosActivity.of(f, c, -1);
    }

    public static Intent of(NPFolder f, Context c, int initialSpinnerIndex) {
        Intent intent = new Intent(c, PhotosActivity.class);
        intent.putExtra(Constants.KEY_FOLDER, f);
        intent.putExtra(KEY_SPINNER_INDEX, initialSpinnerIndex);
        return intent;
    }

    private Fragment mPhotosFragment;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.photos_topmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_photos:
                final Intent intent = PhotosSelectActivity.of(this);
                startActivityForResult(intent, REQ_CHOOSE_FILE);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedState) {
        mParentActivity = DashboardActivity.class;

        super.onCreate(savedState);

        if (savedState == null) {
            mPhotosFragment = PhotosFragment.of(mFolder);

            final int containerViewId = getFragmentId();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(containerViewId, mPhotosFragment, PhotosFragment.TAG)
                    .commit();
        } else {
            mPhotosFragment = getSupportFragmentManager().findFragmentByTag(PhotosFragment.TAG);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CHOOSE_FILE:
                if (resultCode == Activity.RESULT_OK) {
                    final ArrayList<Uri> uris = data.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
	                UploadCenterActivity.startWith(uris, mFolder, this);
                }
                break;
            default:
                // probably from fragments
                Log.w(TAG, "unknown requestCode: " + requestCode);
        }
    }

    @Override
    protected Fragment onCreateFragment() {
        // we manage fragments on our own
        return null;
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        switch (itemPosition) {
            case 0:
                break;
            case 1:
                break;
            default:
                throw new AssertionError("unknown position: " + itemPosition);
        }
        return true;
    }
}
