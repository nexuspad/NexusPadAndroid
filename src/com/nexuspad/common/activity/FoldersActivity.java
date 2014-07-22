/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.common.activity;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.nexuspad.R;
import com.nexuspad.common.fragment.FoldersFragment;
import com.nexuspad.datamodel.NPFolder;
import com.nexuspad.dataservice.FolderService;

/**
 * @author Edmond
 */
public class FoldersActivity extends SinglePaneActivity implements FoldersFragment.Callback {
    public static final String TAG = "FoldersActivity";
    public static final String KEY_FOLDER = "com.nexuspad.ui.activity.FoldersActivity.folder";

    private static final int REQ_FOLDER = 1;

    /**
     * @param c      the {@code Context}
     * @param folder the parent folder of the folders list
     * @return an {@code Intent} with the correct extras.
     */
    public static Intent ofParentFolder(Context c, NPFolder folder) {
        Intent intent = new Intent(c, FoldersActivity.class);
        intent.putExtra(KEY_FOLDER, folder);
        return intent;
    }

    private NPFolder mParentFolder;

    @Override
    protected int onCreateLayoutId() {
        return R.layout.no_padding_activity;
    }

    @Override
    protected void onCreate(Bundle savedState) {
        mParentFolder = getIntent().getParcelableExtra(KEY_FOLDER);

        setResult(RESULT_CANCELED);
        super.onCreate(savedState);

        final ActionBar actionBar = getActionBar();
        actionBar.setIcon(R.drawable.ic_ab);
        actionBar.setDisplayHomeAsUpEnabled(true);
        setTitle(mParentFolder.getFolderName());
    }

    @Override
    protected Fragment onCreateFragment() {
        return FoldersFragment.of(mParentFolder);
    }

    @Override
    public void onFolderClicked(NPFolder folder) {
        setResult(RESULT_OK, new Intent().putExtra(KEY_FOLDER, folder));
        finish();
    }

    @Override
    public void onSubFolderClicked(NPFolder folder) {
        final Intent intent = FoldersActivity.ofParentFolder(this, folder);
        startActivityForResult(intent, REQ_FOLDER);
    }

    @Override
    public void onUpFolderClicked() {
        onUpPressed();
    }

    @Override
    protected boolean onUpPressed() {
        final int moduleId = mParentFolder.getModuleId();
        final int folderId = mParentFolder.getFolderId();

        final FolderService service = FolderService.getInstance(this);
        final NPFolder fullFolder = service.getFolder(moduleId, folderId);

        NPFolder upFolder = null;
        if (fullFolder != null) {
            final int parentId = fullFolder.getParentId();
            upFolder = service.getFolder(moduleId, parentId);
        }
        if (upFolder == null) {
            upFolder = NPFolder.rootFolderOf(moduleId, this);
        }

        final Intent upIntent = getUpIntent(FoldersActivity.class);
        upIntent.putExtra(KEY_FOLDER, upFolder);
        startActivity(upIntent);
        finish();
        return true;
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
