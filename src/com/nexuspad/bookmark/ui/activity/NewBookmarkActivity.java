/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.bookmark.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;

import com.edmondapps.utils.android.annotaion.ParentActivity;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.bookmark.ui.fragment.NewBookmarkFragment;
import com.nexuspad.datamodel.Bookmark;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.ui.activity.NewEntryActivity;

/**
 * @author Edmond
 */
@ParentActivity(BookmarksActivity.class)
@ModuleId(moduleId = ServiceConstants.BOOKMARK_MODULE)
public class NewBookmarkActivity extends NewEntryActivity<Bookmark> {

    public static void startWithBookmark(Bookmark b, Folder f, Mode m, Context c) {
        Intent intent = new Intent(c, NewBookmarkActivity.class);
        intent.putExtra(KEY_ENTRY, b);
        intent.putExtra(KEY_FOLDER, f);
        intent.putExtra(KEY_MODE, m);
        c.startActivity(intent);
    }

    public static void startWithFolder(Folder f, Mode m, Context c) {
        Intent intent = new Intent(c, NewBookmarkActivity.class);
        intent.putExtra(KEY_FOLDER, f);
        intent.putExtra(KEY_MODE, m);
        c.startActivity(intent);
    }

    @Override
    protected Fragment onCreateFragment() {
        return NewBookmarkFragment.of(getEntry(), getFolder());
    }

    @Override
    protected void handleIntent(Intent i) {
        super.handleIntent(i);

        if (Intent.ACTION_SEND.equals(i.getAction())) {
            Bookmark bookmark = new Bookmark(getFolder());

            Uri uri = i.getData();
            String text = i.getStringExtra(Intent.EXTRA_TEXT);

            if (uri != null) {
                bookmark.setWebAddress(uri.toString());
            } else if (text != null) {
                bookmark.setWebAddress(text);
            }

            setEntry(bookmark);
        }
    }

    @Override
    protected Intent getUpIntent(Class<?> activity) {
        return super.getUpIntent(activity).putExtra(BookmarkActivity.KEY_FOLDER, getFolder());
    }

    @Override
    protected NewBookmarkFragment getFragment() {
        return (NewBookmarkFragment)super.getFragment();
    }
}
