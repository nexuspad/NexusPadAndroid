/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.photos.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.edmondapps.utils.android.activity.SinglePaneActivity;
import com.edmondapps.utils.android.annotaion.ParentActivity;
import com.nexuspad.R;
import com.nexuspad.datamodel.Album;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.photos.ui.fragment.AlbumFragment;
import com.nexuspad.ui.fragment.EntryFragment;

/**
 * @author Edmond
 */
@ParentActivity(PhotosActivity.class)
public class AlbumActivity extends SinglePaneActivity implements EntryFragment.Callback<Album> {

    private static final String KEY_FOLDER = "key_folder";
    private static final String KEY_ALBUM = "key_album";

    public static void startWith(Album album, Folder f, Context c) {
        c.startActivity(AlbumActivity.of(album, f, c));
    }

    public static Intent of(Album album, Folder f, Context c) {
        Intent intent = new Intent(c, AlbumActivity.class);
        intent.putExtra(KEY_FOLDER, f);
        intent.putExtra(KEY_ALBUM, album);
        return intent;
    }

    private Folder mFolder;
    private Album mAlbum;

    @Override
    protected void onCreate(Bundle savedState) {
        final Intent intent = getIntent();
        mFolder = intent.getParcelableExtra(KEY_FOLDER);
        mAlbum = intent.getParcelableExtra(KEY_ALBUM);

        super.onCreate(savedState);
    }

    @Override
    protected int onCreateLayoutId() {
        return R.layout.no_padding_activity;
    }

    @Override
    protected Fragment onCreateFragment() {
        return AlbumFragment.of(mAlbum, mFolder);
    }

    @Override
    public void onDeleting(EntryFragment<Album> f, Album entry) {
        // nothing
    }
}
