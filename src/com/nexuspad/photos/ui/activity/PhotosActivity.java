/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.photos.ui.activity;

import static com.nexuspad.dataservice.ServiceConstants.PHOTO_MODULE;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.edmondapps.utils.android.annotaion.ParentActivity;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.home.ui.activity.DashboardActivity;
import com.nexuspad.photos.ui.fragment.PhotosFragment;
import com.nexuspad.ui.activity.EntriesActivity;

/**
 * @author Edmond
 * 
 */
@ParentActivity(DashboardActivity.class)
@ModuleId(moduleId = PHOTO_MODULE, template = EntryTemplate.PHOTO)
public class PhotosActivity extends EntriesActivity implements OnNavigationListener {

    public static void startWithFolder(Folder f, Context c) {
        c.startActivity(PhotosActivity.of(f, c));
    }

    public static Intent of(Folder f, Context c) {
        Intent intent = new Intent(c, PhotosActivity.class);
        intent.putExtra(KEY_FOLDER, f);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        List<String> list = new ArrayList<String>();
        list.add(getString(R.string.photos));
        list.add(getString(R.string.albums));
        ArrayAdapter<?> adapter = new ArrayAdapter<String>(actionBar.getThemedContext(), R.layout.list_item_spinner, android.R.id.text1, list);

        actionBar.setListNavigationCallbacks(adapter, this);
        actionBar.setDisplayShowTitleEnabled(false);
    }

    @Override
    protected Fragment onCreateFragment() {
        return PhotosFragment.of(getFolder());
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        Toast.makeText(this, "spinner navigation not ready yet", Toast.LENGTH_SHORT).show();
        Fragment fragment;
        switch (itemPosition) {
            case 0:
                fragment = PhotosFragment.of(getFolder());
                return true;
            case 1:

                break;
            default:
                throw new AssertionError("unknown position: " + itemPosition);
        }
        return false;
    }

}
