/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.photos.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.ArrayAdapter;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.edmondapps.utils.android.annotaion.ParentActivity;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.home.ui.activity.DashboardActivity;
import com.nexuspad.photos.ui.fragment.AlbumsFragment;
import com.nexuspad.photos.ui.fragment.PhotosFragment;
import com.nexuspad.ui.activity.EntriesActivity;

import java.util.ArrayList;
import java.util.List;

import static com.edmondapps.utils.android.Utils.findFragment;
import static com.nexuspad.dataservice.ServiceConstants.PHOTO_MODULE;

/**
 * @author Edmond
 */
@ParentActivity(DashboardActivity.class)
@ModuleId(moduleId = PHOTO_MODULE, template = EntryTemplate.PHOTO)
public class PhotosActivity extends EntriesActivity implements OnNavigationListener {
    public static final String KEY_SPINNER_INDEX = "key_spinner_index";

    public static void startWithFolder(Folder f, Context c) {
        c.startActivity(PhotosActivity.of(f, c));
    }

    public static void startWithFolderAndIndex(Folder f, Context c, int initialSpinnerIndex) {
        c.startActivity(PhotosActivity.of(f, c, initialSpinnerIndex));
    }

    public static Intent of(Folder f, Context c) {
        return PhotosActivity.of(f, c, -1);
    }

    public static Intent of(Folder f, Context c, int initialSpinnerIndex) {
        Intent intent = new Intent(c, PhotosActivity.class);
        intent.putExtra(KEY_FOLDER, f);
        intent.putExtra(KEY_SPINNER_INDEX, initialSpinnerIndex);
        return intent;
    }

    private Fragment mPhotosFragment;
    private Fragment mAlbumsFragment;

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        final List<String> list = new ArrayList<String>();
        list.add(getString(R.string.photos));
        list.add(getString(R.string.albums));
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
            mPhotosFragment = findFragment(getSupportFragmentManager(), PhotosFragment.TAG);
            mAlbumsFragment = findFragment(getSupportFragmentManager(), AlbumsFragment.TAG);
        }
    }

    @Override
    protected Fragment onCreateFragment() {
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
