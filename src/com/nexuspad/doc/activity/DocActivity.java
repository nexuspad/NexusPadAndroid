/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.doc.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import com.nexuspad.common.annotaion.ParentActivity;
import com.nexuspad.datamodel.NPDoc;
import com.nexuspad.datamodel.NPFolder;
import com.nexuspad.doc.fragment.DocFragment;
import com.nexuspad.common.activity.EntryActivity;

/**
 * @author Edmond
 */
@ParentActivity(DocsActivity.class)
public class DocActivity extends EntryActivity<NPDoc> {

    public static Intent of(Context c, NPDoc doc, NPFolder folder) {
        Intent intent = new Intent(c, DocActivity.class);
        intent.putExtra(KEY_ENTRY, doc);
        intent.putExtra(KEY_FOLDER, folder);
        return intent;
    }

    @Override
    protected Fragment onCreateFragment() {
        return DocFragment.of(getEntry(), getFolder());
    }
}
