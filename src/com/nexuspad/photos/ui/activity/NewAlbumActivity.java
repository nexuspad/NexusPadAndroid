package com.nexuspad.photos.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.edmondapps.utils.android.annotaion.ParentActivity;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.Album;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.photos.ui.fragment.NewAlbumFragment;
import com.nexuspad.ui.activity.NewEntryActivity;
import com.nexuspad.ui.fragment.NewEntryFragment;

@ModuleId(moduleId = ServiceConstants.PHOTO_MODULE, template = EntryTemplate.ALBUM)
@ParentActivity(PhotosActivity.class)
public class NewAlbumActivity extends NewEntryActivity<Album> {

    public static Intent of(Folder folder, Context context) {
        return NewAlbumActivity.of(null, folder, context);
    }

    public static Intent of(Album album, Folder folder, Context context) {
        final Intent intent = new Intent(context, NewAlbumActivity.class);
        intent.putExtra(KEY_ENTRY, album);
        intent.putExtra(KEY_FOLDER, folder);
        return intent;
    }

    private Album mAlbum;
    private Folder mFolder;

    @Override
    protected void onCreate(Bundle savedState) {
        final Intent intent = getIntent();
        mAlbum = intent.getParcelableExtra(KEY_ENTRY);
        mFolder = intent.getParcelableExtra(KEY_FOLDER);

        super.onCreate(savedState);
    }

    @Override
    protected Fragment onCreateFragment() {
        return NewAlbumFragment.of(mAlbum, mFolder);
    }

    @Override
    protected void onDoneEditing() {
        NewEntryFragment<Album> fragment = getFragment();
        if (fragment.isEditedEntryValid()) {
            fragment.updateEntry();
            finish();
        }
    }
}
