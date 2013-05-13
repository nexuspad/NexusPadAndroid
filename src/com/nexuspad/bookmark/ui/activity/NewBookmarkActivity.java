/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.bookmark.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.edmondapps.utils.android.annotaion.ParentActivity;
import com.edmondapps.utils.android.view.LoadingViews;
import com.edmondapps.utils.java.Lazy;
import com.nexuspad.R;
import com.nexuspad.bookmark.ui.fragment.NewBookmarkFragment;
import com.nexuspad.datamodel.Bookmark;
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

    public static void startWithBookmark(Bookmark b, Context c) {
        Intent intent = new Intent(c, NewBookmarkActivity.class);
        intent.putExtra(KEY_ENTRY, b);
        c.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
    }

    @Override
    protected Fragment onCreateFragment() {
        return NewBookmarkFragment.of(getEntry());
    }

    @Override
    protected Intent getUpIntent(Class<?> activity) {
        Bookmark entry = getFragment().getEditedEntry();
        return super.getUpIntent(activity).putExtra(BookmarkActivity.KEY_ENTRY, entry);
    }

    @Override
    protected NewBookmarkFragment getFragment() {
        return (NewBookmarkFragment)super.getFragment();
    }

    @Override
    protected void onDonePressed() {
        super.onDonePressed();
        getFragment().updateEntry();

        mLoadingViews.get().startLoading();
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
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
