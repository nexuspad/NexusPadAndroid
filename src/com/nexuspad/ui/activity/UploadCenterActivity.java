/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.edmondapps.utils.android.activity.SinglePaneActivity;
import com.nexuspad.R;
import com.nexuspad.app.Request;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.datamodel.NPEntry;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.ui.fragment.UploadCenterFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Edmond
 */
public class UploadCenterActivity extends SinglePaneActivity {
    public static final String KEY_FOLDER = "key_folder";
    public static final String KEY_ENTRY = "key_entry";

	/**
	 * Upload files to a folder.
	 *
	 * @param uris
	 * @param folder
	 * @param c
	 */
    public static void startWith(ArrayList<Uri> uris, Folder folder, Context c) {
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
    public static Intent of(ArrayList<Uri> uris, Folder folder, Context c) {
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
        return R.layout.no_padding_activity;
    }

    private List<Uri> mUris = new ArrayList<Uri>();

    @Override
    protected void onCreate(Bundle savedState) {
        final Intent intent = getIntent();
        Uri uri = intent.getData();
        if (uri != null) {
            addIfNeeded(uri);
        }

        uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
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
            Folder folder = intent.getParcelableExtra(KEY_FOLDER);
            if (folder == null) {
                folder = Folder.rootFolderOf(ServiceConstants.PHOTO_MODULE, this);
            }
            for (Uri uri : mUris) {
                fragment.addRequest(Request.forFolder(uri, folder, null));
            }
        } else {
            for (Uri uri : mUris) {
                fragment.addRequest(Request.forEntry(uri, entry, null));
            }
        }
        return fragment;
    }
}
