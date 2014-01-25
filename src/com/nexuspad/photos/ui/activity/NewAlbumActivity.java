package com.nexuspad.photos.ui.activity;

import android.content.Context;
import android.content.Intent;
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

    public static Intent of(Context context, Folder folder) {
        return NewAlbumActivity.of(context, folder, null);
    }

    public static Intent of(Context context, Folder folder, Album album) {
        final Intent intent = new Intent(context, NewAlbumActivity.class);
        intent.putExtra(KEY_ENTRY, album);
        intent.putExtra(KEY_FOLDER, folder);
        return intent;
    }

    @Override
    protected Fragment onCreateFragment() {
        return NewAlbumFragment.of(getEntry(), getFolder());
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
