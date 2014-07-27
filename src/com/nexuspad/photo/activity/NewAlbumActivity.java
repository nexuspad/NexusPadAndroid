package com.nexuspad.photo.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import com.nexuspad.common.annotaion.ParentActivity;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.NPAlbum;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.NPFolder;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.photo.fragment.NewAlbumFragment;
import com.nexuspad.common.activity.UpdateEntryActivity;
import com.nexuspad.common.fragment.UpdateEntryFragment;

@ModuleId(moduleId = ServiceConstants.PHOTO_MODULE, template = EntryTemplate.ALBUM)
@ParentActivity(PhotosActivity.class)
public class NewAlbumActivity extends UpdateEntryActivity<NPAlbum> {

    public static Intent of(Context context, NPFolder folder) {
        return NewAlbumActivity.of(context, folder, null);
    }

    public static Intent of(Context context, NPFolder folder, NPAlbum album) {
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
        UpdateEntryFragment<NPAlbum> fragment = getFragment();
        if (fragment.isEditedEntryValid()) {
            fragment.updateEntry();
            finish();
        }
    }
}
