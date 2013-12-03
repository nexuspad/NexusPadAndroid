/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.doc.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import com.edmondapps.utils.android.annotaion.ParentActivity;
import com.nexuspad.datamodel.Doc;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.doc.ui.fragment.DocFragment;
import com.nexuspad.ui.activity.EntryActivity;

/**
 * @author Edmond
 */
@ParentActivity(DocsActivity.class)
public class DocActivity extends EntryActivity<Doc> {

    public static Intent of(Context c, Doc doc, Folder folder) {
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
