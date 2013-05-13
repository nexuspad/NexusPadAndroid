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
import com.nexuspad.ui.activity.EntryActivity;

/**
 * @author Edmond
 * 
 */
@ParentActivity(BookmarksActivity.class)
public class BookmarkActivity extends EntryActivity<Bookmark> implements BookmarkFragment.Callback {

    public static void startWithBookmark(Bookmark b, Context c) {
        Intent intent = new Intent(c, BookmarkActivity.class);
        intent.putExtra(KEY_ENTRY, b);
        c.startActivity(intent);
    }

    @Override
    protected Fragment onCreateFragment() {
        return BookmarkFragment.of(getEntry());
    }

    @Override
    public void onEdit(BookmarkFragment f, Bookmark b) {
        NewBookmarkActivity.startWithBookmark(b, this);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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
