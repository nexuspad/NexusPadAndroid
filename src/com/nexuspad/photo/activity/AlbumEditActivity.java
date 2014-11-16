package com.nexuspad.photo.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import com.nexuspad.common.activity.EntryEditActivity;
import com.nexuspad.common.annotation.ParentActivity;
import com.nexuspad.common.annotation.ModuleId;
import com.nexuspad.service.datamodel.NPAlbum;
import com.nexuspad.service.datamodel.EntryTemplate;
import com.nexuspad.service.datamodel.NPFolder;
import com.nexuspad.service.dataservice.ServiceConstants;
import com.nexuspad.photo.fragment.AlbumEditFragment;
import com.nexuspad.common.fragment.EntryEditFragment;

@ModuleId(moduleId = ServiceConstants.PHOTO_MODULE, template = EntryTemplate.ALBUM)
@ParentActivity(PhotosActivity.class)
public class AlbumEditActivity extends EntryEditActivity<NPAlbum> {

    public static Intent of(Context context, NPFolder folder) {
        return AlbumEditActivity.of(context, folder, null);
    }

    public static Intent of(Context context, NPFolder folder, NPAlbum album) {
        final Intent intent = new Intent(context, AlbumEditActivity.class);
        intent.putExtra(KEY_ENTRY, album);
        intent.putExtra(KEY_FOLDER, folder);
        return intent;
    }

    @Override
    protected Fragment onCreateFragment() {
        return AlbumEditFragment.of(getEntry(), getFolder());
    }

    @Override
    protected void onDoneEditing() {
        EntryEditFragment<NPAlbum> fragment = getFragment();
        if (fragment.isEditedEntryValid()) {
            fragment.updateEntry();
            finish();
        }
    }
}
