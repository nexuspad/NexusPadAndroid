/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.doc.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import com.nexuspad.common.annotaion.ParentActivity;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.NPDoc;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.NPFolder;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.doc.fragment.DocsFragment;
import com.nexuspad.home.activity.DashboardActivity;
import com.nexuspad.common.activity.EntriesActivity;

/**
 * @author Edmond
 */
@ParentActivity(DashboardActivity.class)
@ModuleId(moduleId = ServiceConstants.DOC_MODULE, template = EntryTemplate.DOC)
public class DocsActivity extends EntriesActivity implements DocsFragment.Callback {

    /**
     * @param a becomes the parent {@code Activity}
     * @return an {@code Intent} that starts with the specified {@code Folder}.
     */
    public static Intent of(Activity a, NPFolder parent) {
        Intent intent = new Intent(a, DocsActivity.class);
        intent.putExtra(KEY_FOLDER, parent);
        intent.putExtra(KEY_PARENT_ACTIVITY, a.getClass());
        return intent;
    }

    @Override
    protected Fragment onCreateFragment() {
        return DocsFragment.of(getFolder());
    }

    @Override
    public void onFolderClick(DocsFragment f, NPFolder folder) {
        startActivity(DocsActivity.of(this, folder));
    }

    @Override
    public void onDocClick(DocsFragment f, NPDoc doc) {
        startActivity(DocActivity.of(this, doc, getFolder()));
    }
}
