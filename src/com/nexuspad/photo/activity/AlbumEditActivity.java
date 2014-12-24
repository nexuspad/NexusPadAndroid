package com.nexuspad.photo.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.nexuspad.common.Constants;
import com.nexuspad.common.activity.EntryEditActivity;
import com.nexuspad.common.annotation.ModuleInfo;
import com.nexuspad.common.annotation.ParentActivity;
import com.nexuspad.service.datamodel.NPAlbum;
import com.nexuspad.service.datamodel.EntryTemplate;
import com.nexuspad.service.datamodel.NPFolder;
import com.nexuspad.service.dataservice.ServiceConstants;
import com.nexuspad.photo.fragment.AlbumEditFragment;
import com.nexuspad.common.fragment.EntryEditFragment;

@ModuleInfo(moduleId = ServiceConstants.PHOTO_MODULE, template = EntryTemplate.ALBUM)
@ParentActivity(PhotosActivity.class)
public class AlbumEditActivity extends EntryEditActivity<NPAlbum> {

    public static Intent of(Context context, NPFolder folder) {
        return AlbumEditActivity.of(context, folder, null);
    }

    public static Intent of(Context context, NPFolder folder, NPAlbum album) {
        final Intent intent = new Intent(context, AlbumEditActivity.class);
        intent.putExtra(Constants.KEY_ENTRY, album);
        intent.putExtra(Constants.KEY_FOLDER, folder);
        return intent;
    }

    @Override
    protected Fragment onCreateFragment() {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.KEY_ENTRY, mEntry);
        bundle.putParcelable(Constants.KEY_FOLDER, mFolder);

        final AlbumEditFragment fragment = new AlbumEditFragment();
        fragment.setArguments(bundle);

        return fragment;
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
