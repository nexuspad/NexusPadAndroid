/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.edmondapps.utils.android.activity.SinglePaneActivity;
import com.nexuspad.R;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.ui.fragment.FoldersFragment;

/**
 * @author Edmond
 * 
 */
public class FoldersActivity extends SinglePaneActivity implements FoldersFragment.Callback {
    public static final String KEY_FOLDER = "com.nexuspad.ui.activity.FoldersActivity.folder";

    private static final int REQ_FOLDER = 1;

    /**
     * 
     * @param a
     *            the {@code Activity Context}, it will also become the parent
     *            {@code Activity}
     * @param folder
     *            the parent folder of the folders list
     * @return an {@code Intent} with the correct extras.
     */
    public static Intent ofParentFolder(Activity a, Folder folder) {
        Intent intent = new Intent(a, FoldersActivity.class);
        intent.putExtra(KEY_FOLDER, folder);
        intent.putExtra(KEY_PARENT_ACTIVITY, a.getClass());
        return intent;
    }

    private Folder mParentFolder;

    @Override
    protected int onCreateLayoutId() {
        return R.layout.no_padding_activity;
    }

    @Override
    protected void onCreate(Bundle savedState) {
        mParentFolder = getIntent().getParcelableExtra(KEY_FOLDER);

        setResult(RESULT_CANCELED);
        super.onCreate(savedState);
    }

    @Override
    protected Fragment onCreateFragment() {
        return FoldersFragment.of(mParentFolder);
    }

    @Override
    public void onFolderClicked(FoldersFragment f, Folder folder) {
        setResult(RESULT_OK, new Intent().putExtra(KEY_FOLDER, folder));
        finish();
    }

    @Override
    public void onSubFolderClicked(FoldersFragment f, Folder folder) {
        Intent intent = FoldersActivity.ofParentFolder(this, folder);
        // intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        startActivityForResult(intent, REQ_FOLDER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQ_FOLDER:
                setResult(RESULT_OK, data);
                finish();
                break;
            default:
                throw new AssertionError("unknown requestCode: " + requestCode);
        }
    }
}
