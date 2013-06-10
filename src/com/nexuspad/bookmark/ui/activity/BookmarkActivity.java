/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.bookmark.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.edmondapps.utils.android.annotaion.ParentActivity;
import com.nexuspad.bookmark.ui.fragment.BookmarkFragment;
import com.nexuspad.datamodel.Bookmark;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.ui.activity.EntryActivity;
import com.nexuspad.ui.activity.NewEntryActivity.Mode;

/**
 * @author Edmond
 * 
 */
@ParentActivity(BookmarksActivity.class)
public class BookmarkActivity extends EntryActivity<Bookmark> implements BookmarkFragment.Callback {

    public static void startWithBookmark(Bookmark b, Folder f, Context c) {
        Intent intent = new Intent(c, BookmarkActivity.class);
        intent.putExtra(KEY_ENTRY, b);
        intent.putExtra(KEY_FOLDER, f);
        c.startActivity(intent);
    }

    @Override
    protected Fragment onCreateFragment() {
        return BookmarkFragment.of(getEntry(), getFolder());
    }

    @Override
    public void onEdit(BookmarkFragment f, Bookmark b) {
        NewBookmarkActivity.startWithBookmark(b, getFolder(), Mode.EDIT, this);
    }

    @Override
    protected void onNewEntry(Bookmark entry) {
        super.onNewEntry(entry);
        BookmarkFragment fragment = getFragment();
        if (fragment != null) {
            fragment.setEntry(entry);
        }
    }

    @Override
    protected BookmarkFragment getFragment() {
        return (BookmarkFragment)super.getFragment();
    }
}
