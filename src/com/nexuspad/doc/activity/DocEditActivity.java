/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.doc.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.nexuspad.common.Constants;
import com.nexuspad.common.activity.EntryEditActivity;
import com.nexuspad.common.annotation.ModuleInfo;
import com.nexuspad.common.annotation.ParentActivity;
import com.nexuspad.doc.fragment.DocEditFragment;
import com.nexuspad.service.datamodel.NPDoc;
import com.nexuspad.service.datamodel.EntryTemplate;
import com.nexuspad.service.datamodel.NPFolder;
import com.nexuspad.service.datamodel.NPUpload;
import com.nexuspad.service.dataservice.ServiceConstants;
import com.nexuspad.common.activity.UploadCenterActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Edmond
 */
@ParentActivity(DocsActivity.class)
@ModuleInfo(moduleId = ServiceConstants.DOC_MODULE, template = EntryTemplate.DOC)
public class DocEditActivity extends EntryEditActivity<NPDoc> implements DocEditFragment.DocDetailCallback {

    public static void startWithFolder(Context c, NPFolder f) {
        DocEditActivity.startWithDoc(c, f, null);
    }

    public static void startWithDoc(Context c, NPFolder f, NPDoc doc) {
        Intent intent = new Intent(c, DocEditActivity.class);
        intent.putExtra(Constants.KEY_ENTRY, doc);
        intent.putExtra(Constants.KEY_FOLDER, f);
        c.startActivity(intent);
    }

    @Override
    protected void onDoneEditing() {
        DocEditFragment fragment = getFragment();
        if (fragment.isEditedEntryValid()) {
            fragment.updateEntry();
            // don't go up, it is handled at onEntryChangedAndUpdateList(…)
        }
    }

    @Override
    public void onUpdateEntry(NPDoc entry) {
        final List<NPUpload> attachments = entry.getAttachments();
        final ArrayList<Uri> uris = new ArrayList<Uri>(attachments.size());  // for parcelling
        for (NPUpload attachment : attachments) {
            if (attachment.isJustCreated()) {
                uris.add(Uri.parse(attachment.getDownloadLink()));
            }
        }

        if (uris.size() > 0) {
            UploadCenterActivity.startWith(uris, entry, this);
        } else {
            goUp();
        }
    }

    @Override
    protected Fragment onCreateFragment() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.KEY_ENTRY, mEntry);
        bundle.putParcelable(Constants.KEY_FOLDER, mFolder);

        DocEditFragment fragment = new DocEditFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    protected Intent getGoBackIntent(Class<?> activity) {
        return super.getGoBackIntent(activity).putExtra(Constants.KEY_FOLDER, mFolder);
    }

    @Override
    protected DocEditFragment getFragment() {
        return (DocEditFragment) super.getFragment();
    }
}
