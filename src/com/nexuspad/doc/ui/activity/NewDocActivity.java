/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.doc.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.edmondapps.utils.android.annotaion.ParentActivity;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.Doc;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.doc.ui.fragment.NewDocFragment;
import com.nexuspad.ui.activity.NewEntryActivity;

/**
 * @author Edmond
 * 
 */
@ParentActivity(DocsActivity.class)
@ModuleId(moduleId = ServiceConstants.DOC_MODULE)
public class NewDocActivity extends NewEntryActivity<Doc> {

    public static void startWithDoc(Doc doc, Folder f, Mode m, Context c) {
        Intent intent = new Intent(c, NewDocActivity.class);
        intent.putExtra(KEY_ENTRY, doc);
        intent.putExtra(KEY_FOLDER, f);
        intent.putExtra(KEY_MODE, m);
        c.startActivity(intent);
    }

    public static void startWithFolder(Folder f, Mode m, Context c) {
        Intent intent = new Intent(c, NewDocActivity.class);
        intent.putExtra(KEY_FOLDER, f);
        intent.putExtra(KEY_MODE, m);
        c.startActivity(intent);
    }

    @Override
    protected Fragment onCreateFragment() {
        return NewDocFragment.of(getEntry(), getFolder());
    }

    @Override
    protected Intent getUpIntent(Class<?> activity) {
        return super.getUpIntent(activity).putExtra(DocActivity.KEY_FOLDER, getFolder());
    }

    @Override
    protected NewDocFragment getFragment() {
        return (NewDocFragment)super.getFragment();
    }
}