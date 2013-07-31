package com.nexuspad.photos.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.Album;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.photos.ui.fragment.NewAlbumFragment;
import com.nexuspad.ui.activity.NewEntryActivity;

@ModuleId(moduleId = ServiceConstants.PHOTO_MODULE, template = EntryTemplate.ALBUM)
public class NewAlbumActivity extends NewEntryActivity<Album> {

    public static Intent of(Folder folder, Context context) {
        final Intent intent = new Intent(context, NewAlbumActivity.class);
        intent.putExtra(KEY_FOLDER, folder);
        intent.putExtra(KEY_MODE, Mode.NEW);
        return intent;
    }

    private Folder mFolder;

    @Override
    protected void onCreate(Bundle savedState) {
        mFolder = getIntent().getParcelableExtra(KEY_FOLDER);

        super.onCreate(savedState);
    }

    @Override
    protected Fragment onCreateFragment() {
        return NewAlbumFragment.of(mFolder);
    }
}
