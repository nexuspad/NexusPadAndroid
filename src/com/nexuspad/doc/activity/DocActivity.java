/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.doc.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.nexuspad.common.Constants;
import com.nexuspad.common.activity.EntryActivity;
import com.nexuspad.doc.fragment.DocFragment;
import com.nexuspad.service.datamodel.NPDoc;
import com.nexuspad.service.datamodel.NPFolder;

public class DocActivity extends EntryActivity<NPDoc> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mParentActivity = DocsActivity.class;
        super.onCreate(savedInstanceState);
    }

    public static Intent of(Context c, NPDoc doc, NPFolder folder) {
        Intent intent = new Intent(c, DocActivity.class);
        intent.putExtra(Constants.KEY_ENTRY, doc);
        intent.putExtra(Constants.KEY_FOLDER, folder);
        return intent;
    }

    @Override
    protected Fragment onCreateFragment() {
        return DocFragment.of(getEntry(), getFolder());
    }
}
