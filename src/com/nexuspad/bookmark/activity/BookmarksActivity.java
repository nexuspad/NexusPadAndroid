/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.bookmark.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import com.nexuspad.bookmark.fragment.BookmarksFragment;
import com.nexuspad.common.Constants;
import com.nexuspad.common.activity.EntriesActivity;
import com.nexuspad.common.annotation.ModuleId;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.NPBookmark;
import com.nexuspad.datamodel.NPFolder;
import com.nexuspad.dataservice.ServiceConstants;

/**
 * @author Edmond
 */
@ModuleId(moduleId = ServiceConstants.BOOKMARK_MODULE, template = EntryTemplate.BOOKMARK)
public class BookmarksActivity extends EntriesActivity implements BookmarksFragment.Callback {
    public static final String TAG = BookmarksActivity.class.getSimpleName();

    public static void startWithFolder(NPFolder f, Context c) {
        Intent intent = new Intent(c, BookmarksActivity.class);
        intent.putExtra(KEY_FOLDER, f);
        intent.putExtra(KEY_PARENT_ACTIVITY, c.getClass());
        c.startActivity(intent);
    }

    @Override
    protected Fragment onCreateFragment() {
        return BookmarksFragment.of(mFolder);
    }

    @Override
    public void onBookmarkClick(BookmarksFragment f, NPBookmark bookmark) {
	    Intent intent = new Intent(this, BookmarkActivity.class);
	    intent.putExtra(Constants.KEY_ENTRY, bookmark);
	    intent.putExtra(Constants.KEY_FOLDER, mFolder);
	    this.startActivity(intent);
    }

    @Override
    public void onEditBookmark(BookmarksFragment f, NPBookmark bookmark) {
        BookmarkEditEditActivity.startWithBookmark(this, mFolder, bookmark);
    }

    @Override
    public void onFolderClick(BookmarksFragment f, NPFolder folder) {
        startWithFolder(folder, this);
    }
}
