/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.bookmark.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.edmondapps.utils.android.annotaion.ParentActivity;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.bookmark.ui.fragment.BookmarksFragment;
import com.nexuspad.datamodel.Bookmark;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.home.ui.activity.DashboardActivity;
import com.nexuspad.ui.activity.EntriesActivity;

/**
 * @author Edmond
 * 
 */
@ParentActivity(DashboardActivity.class)
@ModuleId(moduleId = ServiceConstants.BOOKMARK_MODULE)
public class BookmarksActivity extends EntriesActivity implements BookmarksFragment.Callback {
    public static final String TAG = "BookmarksActivity";

    public static void startWithFolder(Folder f, Context c) {
        Intent intent = new Intent(c, BookmarksActivity.class);
        intent.putExtra(KEY_FOLDER, f);
        intent.putExtra(KEY_PARENT_ACTIVITY, c.getClass());
        c.startActivity(intent);
    }

    @Override
    protected Fragment onCreateFragment() {
        return BookmarksFragment.of(getFolder());
    }

    @Override
    public void onBookmarkClick(BookmarksFragment f, Bookmark bookmark) {
        BookmarkActivity.startWithBookmark(bookmark, getFolder(), this);
    }

    @Override
    public void onEditBookmark(BookmarksFragment f, Bookmark bookmark) {
        NewBookmarkActivity.startWithBookmark(bookmark, getFolder(), this);
    }

    @Override
    public void onFolderClick(BookmarksFragment f, Folder folder) {
        startWithFolder(folder, this);
    }
}
