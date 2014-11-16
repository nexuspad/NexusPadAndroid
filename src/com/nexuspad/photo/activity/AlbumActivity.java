/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.photo.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.nexuspad.common.activity.SinglePaneActivity;
import com.nexuspad.common.annotation.ParentActivity;
import com.nexuspad.R;
import com.nexuspad.service.datamodel.NPAlbum;
import com.nexuspad.service.datamodel.NPFolder;
import com.nexuspad.photo.fragment.AlbumFragment;
import com.nexuspad.common.fragment.EntryFragment;

/**
 * @author Edmond
 */
@ParentActivity(PhotosActivity.class)
public class AlbumActivity extends SinglePaneActivity implements EntryFragment.EntryDetailCallback<NPAlbum> {

    private static final String KEY_FOLDER = "key_folder";
    private static final String KEY_ALBUM = "key_album";

    public static void startWith(NPAlbum album, NPFolder f, Context c) {
        c.startActivity(AlbumActivity.of(album, f, c));
    }

    public static Intent of(NPAlbum album, NPFolder f, Context c) {
        Intent intent = new Intent(c, AlbumActivity.class);
        intent.putExtra(KEY_FOLDER, f);
        intent.putExtra(KEY_ALBUM, album);
        return intent;
    }

    private NPFolder mFolder;
    private NPAlbum mAlbum;

    @Override
    protected void onCreate(Bundle savedState) {
        final Intent intent = getIntent();
        mFolder = intent.getParcelableExtra(KEY_FOLDER);
        mAlbum = intent.getParcelableExtra(KEY_ALBUM);

        setTitle(mAlbum.getTitle());

        super.onCreate(savedState);
    }

    @Override
    protected int onCreateLayoutId() {
        return R.layout.np_padding_activity;
    }

    @Override
    protected Fragment onCreateFragment() {
        return AlbumFragment.of(mAlbum, mFolder);
    }

    @Override
    public void onDeleting(EntryFragment<NPAlbum> f, NPAlbum entry) {
        finish();
    }
}
