/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.photos.ui.fragment;

import android.content.Context;
import android.content.Intent;

import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.ui.fragment.EntriesFragment;

/**
 * @author Edmond
 * 
 */
@ModuleId(moduleId = ServiceConstants.PHOTO_MODULE, template = EntryTemplate.ALBUM)
public class AlbumsFragment extends EntriesFragment {

    @Override
    protected void onNewFolder(Context c, Intent i, Folder f) {
        throw new UnsupportedOperationException();
    }
}
