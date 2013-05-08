/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.bookmark.ui.activity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.edmondapps.utils.android.annotaion.ParentActivity;
import com.nexuspad.R;
import com.nexuspad.bookmark.ui.fragment.BookmarksFragment;
import com.nexuspad.datamodel.Bookmark;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.home.ui.activity.DashboardActivity;
import com.nexuspad.ui.activity.EntriesActivity;

/**
 * @author Edmond
 * 
 */
@ParentActivity(DashboardActivity.class)
public class BookmarksActivity extends EntriesActivity implements BookmarksFragment.Callback {
    public static final String TAG = "BookmarksActivity";

    public static void startWithFolder(Folder f, Context c) {
        Intent intent = new Intent(c, BookmarksActivity.class);
        intent.putExtra(KEY_FOLDER, f);
        intent.putExtra(KEY_PARENT, c.getClass());
        c.startActivity(intent);
    }

    @Override
    protected Fragment onCreateFragment() {
        return BookmarksFragment.of(getFolder());
    }

    @Override
    public void onBookmarkClick(BookmarksFragment f, Bookmark bookmark) {
        Uri uri = Uri.parse(bookmark.getWebAddress());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.err_invalid_bookmark, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onFolderClick(BookmarksFragment f, Folder folder) {
        startWithFolder(folder, this);
    }
}
