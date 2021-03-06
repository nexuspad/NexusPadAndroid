/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.bookmark.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.nexuspad.bookmark.fragment.BookmarkFragment;
import com.nexuspad.common.activity.EntryActivity;
import com.nexuspad.service.datamodel.NPBookmark;

/**
 * @author Edmond
 */
public class BookmarkActivity extends EntryActivity<NPBookmark> implements BookmarkFragment.BookmarkDetailCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mParentActivity = BookmarksActivity.class;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected Fragment onCreateFragment() {
        return BookmarkFragment.of(getEntry(), getFolder());
    }

    @Override
    public void onEdit(BookmarkFragment f, NPBookmark b) {
        BookmarkEditActivity.startWithBookmark(this, getFolder(), b);
    }

    @Override
    protected void onNewEntry(NPBookmark entry) {
        super.onNewEntry(entry);
        BookmarkFragment fragment = getFragment();
        if (fragment != null) {
            fragment.setEntry(entry);
        }
    }

    @Override
    protected BookmarkFragment getFragment() {
        return (BookmarkFragment) super.getFragment();
    }
}
