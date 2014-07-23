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
import android.widget.ArrayAdapter;
import com.google.common.collect.ImmutableList;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.common.activity.EntriesActivity;
import com.nexuspad.common.activity.UploadCenterActivity;
import com.nexuspad.common.annotaion.ParentActivity;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.NPFolder;
import com.nexuspad.home.activity.DashboardActivity;
import com.nexuspad.photo.fragment.AlbumsFragment;
import com.nexuspad.photo.fragment.PhotosFragment;

import java.util.ArrayList;
import java.util.List;

import static com.nexuspad.dataservice.ServiceConstants.PHOTO_MODULE;

/**
 * @author Edmond
 */
@ParentActivity(DashboardActivity.class)
@ModuleId(moduleId = PHOTO_MODULE, template = EntryTemplate.PHOTO)
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
        intent.putExtra(KEY_FOLDER, f);
        intent.putExtra(KEY_SPINNER_INDEX, initialSpinnerIndex);
        return intent;
    }

    private Fragment mPhotosFragment;
    private Fragment mAlbumsFragment;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.photos_frag, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_photos:
                final Intent intent = PhotosSelectActivity.of(this);
                startActivityForResult(intent, REQ_CHOOSE_FILE);
                return true;
            case R.id.new_albums:
                final Intent albumIntent = NewAlbumActivity.of(this, getFolder());
                startActivity(albumIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        final List<String> list = ImmutableList.of(getString(R.string.photos), getString(R.string.albums));

        final ArrayAdapter<?> adapter = new ArrayAdapter<String>(
                actionBar.getThemedContext(), R.layout.list_item_spinner, android.R.id.text1, list);

        actionBar.setListNavigationCallbacks(adapter, this);
        actionBar.setDisplayShowTitleEnabled(false);

        if (savedState == null) {
            final int spinnerIndex = getIntent().getIntExtra(KEY_SPINNER_INDEX, -1);

            mPhotosFragment = PhotosFragment.of(getFolder());
            mAlbumsFragment = AlbumsFragment.of(getFolder());

            final int containerViewId = getFragmentId();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(containerViewId, mPhotosFragment, PhotosFragment.TAG)
                    .add(containerViewId, mAlbumsFragment, AlbumsFragment.TAG)
                    .hide(spinnerIndex == 1 ? mPhotosFragment : mAlbumsFragment)
                    .commit();

            actionBar.setSelectedNavigationItem(spinnerIndex);
        } else {
            mPhotosFragment = getSupportFragmentManager().findFragmentByTag(PhotosFragment.TAG);
            mAlbumsFragment = getSupportFragmentManager().findFragmentByTag(AlbumsFragment.TAG);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CHOOSE_FILE:
                if (resultCode == Activity.RESULT_OK) {
                    final ArrayList<Uri> uris = data.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
	                UploadCenterActivity.startWith(uris, getFolder(), this);
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
                showPhotosFragment();
                break;
            case 1:
                showAlbumsFragment();
                break;
            default:
                throw new AssertionError("unknown position: " + itemPosition);
        }
        return true;
    }

    private void showPhotosFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .hide(mAlbumsFragment)
                .show(mPhotosFragment)
                .commit();
    }

    private void showAlbumsFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .hide(mPhotosFragment)
                .show(mAlbumsFragment)
                .commit();
    }
}
