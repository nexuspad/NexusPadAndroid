/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.bookmark.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.nexuspad.bookmark.fragment.BookmarksFragment;
import com.nexuspad.common.Constants;
import com.nexuspad.common.activity.EntriesActivity;
import com.nexuspad.common.annotation.ModuleInfo;
import com.nexuspad.home.activity.DashboardActivity;
import com.nexuspad.service.datamodel.EntryTemplate;
import com.nexuspad.service.datamodel.NPBookmark;
import com.nexuspad.service.datamodel.NPFolder;
import com.nexuspad.service.dataservice.ServiceConstants;

/**
 * @author Edmond
 */
@ModuleInfo(moduleId = ServiceConstants.BOOKMARK_MODULE, template = EntryTemplate.BOOKMARK)
public class BookmarksActivity extends EntriesActivity implements BookmarksFragment.Callback {
    public static final String TAG = BookmarksActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedState) {
        mParentActivity = DashboardActivity.class;

        super.onCreate(savedState);
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
        BookmarkEditActivity.startWithBookmark(this, mFolder, bookmark);
    }

    @Override
    public void onFolderClick(BookmarksFragment f, NPFolder folder) {
        Intent intent = new Intent(this, BookmarksActivity.class);
        intent.putExtra(Constants.KEY_FOLDER, folder);
        intent.putExtra(KEY_PARENT_ACTIVITY, ((Object)this).getClass());
        startActivity(intent);
    }
}
