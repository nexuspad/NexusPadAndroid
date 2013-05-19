/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.bookmark.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.edmondapps.utils.android.annotaion.ParentActivity;
import com.edmondapps.utils.android.view.LoadingViews;
import com.edmondapps.utils.java.Lazy;
import com.nexuspad.R;
import com.nexuspad.bookmark.ui.fragment.NewBookmarkFragment;
import com.nexuspad.datamodel.Bookmark;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.dataservice.ErrorCode;
import com.nexuspad.dataservice.ServiceError;
import com.nexuspad.ui.activity.NewEntryActivity;
import com.nexuspad.ui.fragment.NewEntryFragment;

/**
 * @author Edmond
 * 
 */
@ParentActivity(BookmarkActivity.class)
public class NewBookmarkActivity extends NewEntryActivity<Bookmark> implements NewBookmarkFragment.Callback {

    private final Lazy<LoadingViews> mLoadingViews = new Lazy<LoadingViews>() {
        @Override
        protected LoadingViews onCreate() {
            return LoadingViews.of(findViewById(R.id.content), findViewById(android.R.id.progress));
        }
    };

    public static void startWithBookmark(Bookmark b, Mode m, Context c) {
        Intent intent = new Intent(c, NewBookmarkActivity.class);
        intent.putExtra(KEY_ENTRY, b);
        intent.putExtra(KEY_FOLDER, b.getFolder());
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
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
    }

    @Override
    protected Fragment onCreateFragment() {
        Bookmark bookmark = getEntry();
        if (bookmark != null) {
            return NewBookmarkFragment.of(bookmark);
        } else {
            return NewBookmarkFragment.of(getFolder());
        }
    }

    @Override
    protected Intent getUpIntent(Class<?> activity) {
        Intent upIntent = super.getUpIntent(activity);

        NewBookmarkFragment fragment = getFragment();

        upIntent.putExtra(KEY_FOLDER, getFolder());

        if (fragment.isEditedEntryValid()) {
            Bookmark entry = fragment.getEditedEntry();
            upIntent.putExtra(KEY_ENTRY, entry);
        } else {
            upIntent.setClass(this, BookmarksActivity.class);
        }

        return upIntent;
    }

    @Override
    protected NewBookmarkFragment getFragment() {
        return (NewBookmarkFragment)super.getFragment();
    }

    @Override
    protected void onDonePressed() {
        super.onDonePressed();

        mLoadingViews.get().startLoading();

        switch (getMode()) {
            case EDIT:
                getFragment().updateEntry();
                break;
            case NEW:
            default:
                getFragment().addEntry();
                break;
        }
    }

    @Override
    protected void onDiscardPressed() {
        startActivity(getUpIntent(getUpActivity()));
        finish();
    }

    @Override
    public void onEntryUpdated(NewEntryFragment<Bookmark> f, Bookmark entry) {
        startActivity(getUpIntent(getUpActivity()));
        finish();
    }

    @Override
    public void onEntryUpdateFailed(NewEntryFragment<Bookmark> f, ServiceError error) {
        mLoadingViews.get().doneLoading();
        if (ErrorCode.MISSING_PARAM != error.getErrorCode()) {
            Toast.makeText(this, R.string.err_internal, Toast.LENGTH_LONG).show();
        }
    }
}
