/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.bookmark.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import com.nexuspad.bookmark.fragment.BookmarkEditFragment;
import com.nexuspad.common.Constants;
import com.nexuspad.common.activity.EntryEditActivity;
import com.nexuspad.common.annotation.ParentActivity;
import com.nexuspad.common.annotation.ModuleId;
import com.nexuspad.datamodel.NPBookmark;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.NPFolder;
import com.nexuspad.dataservice.ServiceConstants;

/**
 * @author Edmond
 */
@ParentActivity(BookmarksActivity.class)
@ModuleId(moduleId = ServiceConstants.BOOKMARK_MODULE, template = EntryTemplate.BOOKMARK)
public class BookmarkEditEditActivity extends EntryEditActivity<NPBookmark> {

    public static void startWithFolder(Context c, NPFolder f) {
        BookmarkEditEditActivity.startWithBookmark(c, f, null);
    }

    public static void startWithBookmark(Context c, NPFolder f, NPBookmark b) {
        Intent intent = new Intent(c, BookmarkEditEditActivity.class);
        intent.putExtra(KEY_ENTRY, b);
        intent.putExtra(KEY_FOLDER, f);
        c.startActivity(intent);
    }

    @Override
    protected Fragment onCreateFragment() {
        return BookmarkEditFragment.of(getEntry(), getFolder());
    }

    @Override
    protected void handleIntent(Intent i) {
        super.handleIntent(i);

        if (Intent.ACTION_SEND.equals(i.getAction())) {
            NPBookmark bookmark = new NPBookmark(getFolder());

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
    protected Intent getGoBackIntent(Class<?> activity) {
        return super.getGoBackIntent(activity).putExtra(Constants.KEY_FOLDER, getFolder());
    }

    @Override
    protected BookmarkEditFragment getFragment() {
        return (BookmarkEditFragment) super.getFragment();
    }
}