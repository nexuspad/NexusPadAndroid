/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.bookmark.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.nexuspad.bookmark.fragment.BookmarkEditFragment;
import com.nexuspad.common.Constants;
import com.nexuspad.common.activity.EntryEditActivity;
import com.nexuspad.common.annotation.ModuleInfo;
import com.nexuspad.service.datamodel.EntryTemplate;
import com.nexuspad.service.datamodel.NPBookmark;
import com.nexuspad.service.datamodel.NPFolder;
import com.nexuspad.service.dataservice.ServiceConstants;

/**
 * @author Edmond
 */
@ModuleInfo(moduleId = ServiceConstants.BOOKMARK_MODULE, template = EntryTemplate.BOOKMARK)
public class BookmarkEditActivity extends EntryEditActivity<NPBookmark> {

    public static void startWithFolder(Context c, NPFolder f) {
        BookmarkEditActivity.startWithBookmark(c, f, null);
    }

    public static void startWithBookmark(Context c, NPFolder f, NPBookmark b) {
        Intent intent = new Intent(c, BookmarkEditActivity.class);
        intent.putExtra(Constants.KEY_ENTRY, b);
        intent.putExtra(Constants.KEY_FOLDER, f);
        c.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedState) {
        mParentActivity = BookmarksActivity.class;
        super.onCreate(savedState);
    }

    @Override
    protected Fragment onCreateFragment() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.KEY_ENTRY, mEntry);
        bundle.putParcelable(Constants.KEY_FOLDER, mFolder);

        BookmarkEditFragment fragment = new BookmarkEditFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    protected void handleIntent(Intent i) {
        super.handleIntent(i);

        if (Intent.ACTION_SEND.equals(i.getAction())) {
            NPBookmark bookmark = new NPBookmark(mFolder);

            Uri uri = i.getData();
            String text = i.getStringExtra(Intent.EXTRA_TEXT);

            if (uri != null) {
                bookmark.setWebAddress(uri.toString());
            } else if (text != null) {
                bookmark.setWebAddress(text);
            }
        }
    }

    @Override
    protected Intent getGoBackIntent(Class<?> activity) {
        return super.getGoBackIntent(activity).putExtra(Constants.KEY_FOLDER, mFolder);
    }

    @Override
    protected BookmarkEditFragment getFragment() {
        return (BookmarkEditFragment) super.getFragment();
    }
}
