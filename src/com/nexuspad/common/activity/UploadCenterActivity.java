/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.common.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.nexuspad.R;
import com.nexuspad.app.UploadRequest;
import com.nexuspad.common.fragment.UploadCenterFragment;
import com.nexuspad.service.datamodel.NPFolder;
import com.nexuspad.service.datamodel.NPEntry;
import com.nexuspad.service.dataservice.ServiceConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Edmond
 */
public class UploadCenterActivity extends SinglePaneActivity {
    public static final String KEY_FOLDER = "key_folder";
    public static final String KEY_ENTRY = "key_entry";

	private List<Uri> mUris = new ArrayList<Uri>();

	/**
	 * Upload files to a folder.
	 *
	 * @param uris
	 * @param folder
	 * @param c
	 */
    public static void startWith(ArrayList<Uri> uris, NPFolder folder, Context c) {
        c.startActivity(UploadCenterActivity.of(uris, folder, c));
    }

	/**
	 * Create Intent for uploading to folder.
	 *
	 * @param uris
	 * @param folder
	 * @param c
	 * @return
	 */
    public static Intent of(ArrayList<Uri> uris, NPFolder folder, Context c) {
        Intent intent = new Intent(c, UploadCenterActivity.class);
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        intent.putExtra(KEY_FOLDER, folder);
        return intent;
    }


	/**
	 * Attach files to entry.
	 *
	 * @param uris
	 * @param entry
	 * @param c
	 */
    public static void startWith(ArrayList<Uri> uris, NPEntry entry, Context c) {
        c.startActivity(UploadCenterActivity.of(uris, entry, c));
    }

	/**
	 * Create Intent for attaching files to entry.
	 *
 	 * @param uris
	 * @param entry
	 * @param c
	 * @return
	 */
    public static Intent of(ArrayList<Uri> uris, NPEntry entry, Context c) {
        Intent intent = new Intent(c, UploadCenterActivity.class);
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        intent.putExtra(KEY_ENTRY, entry);
        return intent;
    }

    @Override
    protected int onCreateLayoutId() {
        return R.layout.np_padding_activity;
    }

    @Override
    protected void onCreate(Bundle savedState) {
        final Intent intent = getIntent();
        Uri uri = intent.getData();
        if (uri != null) {
            addIfNeeded(uri);
        }

        final List<Uri> list = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (list != null) {
            for (Uri theUri : list) {
                addIfNeeded(theUri);
            }
        }

        super.onCreate(savedState);
    }

    private void addIfNeeded(Uri uri) {
        if (!mUris.contains(uri)) {
            mUris.add(uri);
        }
    }

    @Override
    protected Fragment onCreateFragment() {
        final UploadCenterFragment fragment = new UploadCenterFragment();
        if (mUris.isEmpty()) {
            return fragment;
        }

        final Intent intent = getIntent();
        final NPEntry entry = intent.getParcelableExtra(KEY_ENTRY);
        if (entry == null) {
            NPFolder folder = intent.getParcelableExtra(KEY_FOLDER);
            if (folder == null) {
                folder = NPFolder.rootFolderOf(ServiceConstants.PHOTO_MODULE, this);
            }
            for (Uri uri : mUris) {
                fragment.addRequest(UploadRequest.forFolder(uri, folder, null));
            }
        } else {
            for (Uri uri : mUris) {
                fragment.addRequest(UploadRequest.forEntry(uri, entry, null));
            }
        }
        return fragment;
    }
}
