/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.doc.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import com.nexuspad.common.Constants;
import com.nexuspad.common.activity.EntriesActivity;
import com.nexuspad.common.annotation.ModuleId;
import com.nexuspad.service.datamodel.EntryTemplate;
import com.nexuspad.service.datamodel.NPDoc;
import com.nexuspad.service.datamodel.NPFolder;
import com.nexuspad.service.dataservice.ServiceConstants;
import com.nexuspad.doc.fragment.DocsFragment;

/**
 * @author Edmond
 */
@ModuleId(moduleId = ServiceConstants.DOC_MODULE, template = EntryTemplate.DOC)
public class DocsActivity extends EntriesActivity implements DocsFragment.Callback {

    /**
     * @param a becomes the parent {@code Activity}
     * @return an {@code Intent} that starts with the specified {@code Folder}.
     */
    public static Intent of(Activity a, NPFolder parent) {
        Intent intent = new Intent(a, DocsActivity.class);
        intent.putExtra(Constants.KEY_FOLDER, parent);
        intent.putExtra(KEY_PARENT_ACTIVITY, a.getClass());
        return intent;
    }

    @Override
    protected Fragment onCreateFragment() {
        return DocsFragment.of(mFolder);
    }

    @Override
    public void onFolderClick(DocsFragment f, NPFolder folder) {
        startActivity(DocsActivity.of(this, folder));
    }

    @Override
    public void onDocClick(DocsFragment f, NPDoc doc) {
        startActivity(DocActivity.of(this, doc, mFolder));
    }
}
