/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.home.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import com.nexuspad.R;
import com.nexuspad.account.AccountManager;
import com.nexuspad.bookmark.activity.BookmarksActivity;
import com.nexuspad.calendar.activity.EventsActivity;
import com.nexuspad.common.activity.SinglePaneActivity;
import com.nexuspad.common.activity.UploadCenterActivity;
import com.nexuspad.contacts.activity.ContactsActivity;
import com.nexuspad.datamodel.NPFolder;
import com.nexuspad.datamodel.NPModule;
import com.nexuspad.dataservice.NPException;
import com.nexuspad.doc.activity.DocsActivity;
import com.nexuspad.home.fragment.DashboardFragment;
import com.nexuspad.journal.activity.JournalsActivity;
import com.nexuspad.photo.activity.PhotosActivity;

import java.io.File;
import java.util.ArrayList;

import static com.nexuspad.dataservice.ServiceConstants.*;

/**
 * @author Edmond
 */
public class DashboardActivity extends SinglePaneActivity implements DashboardFragment.Callback {
	public static final String TAG = DashboardActivity.class.getName();

	@Override
	protected int onCreateLayoutId() {
		return R.layout.np_padding_activity;
	}

	@Override
	protected void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		try {
			setTitle(AccountManager.currentAccount().getFirstName());
		} catch (NPException e) {
			Log.e(TAG, e.toString());
		}
	}

	@Override
	protected Fragment onCreateFragment() {
		return new DashboardFragment();
	}

	@Override
	public void onModuleClicked(DashboardFragment f, int moduleType) {
		switch (moduleType) {
			case BOOKMARK_MODULE:
				startActivity(new Intent(this, BookmarksActivity.class));
				break;

			case CALENDAR_MODULE:
				startActivity(new Intent(this, EventsActivity.class));
				break;

			case DOC_MODULE:
				startActivity(new Intent(this, DocsActivity.class));
				break;

			case PHOTO_MODULE:
				startActivity(new Intent(this, PhotosActivity.class));
				break;

			case CONTACT_MODULE:
				startActivity(new Intent(this, ContactsActivity.class));
				break;

			case JOURNAL_MODULE:
				startActivity(new Intent(this, JournalsActivity.class));
				break;

			case 0:
				ArrayList<Uri> uris = new ArrayList<Uri>();
				uris.add(Uri.fromFile(new File("/storage/emulated/0/DCIM/Camera/IMG_20130630_140648.jpg")));
				uris.add(Uri.fromFile(new File("/storage/emulated/0/DCIM/Camera/xxxxxxx.jpg")));
				UploadCenterActivity.startWith(uris, NPFolder.rootFolderOf(NPModule.PHOTO_MODULE), this);

				break;

			default:
				Log.v(TAG, "moduleType: " + moduleType);
		}

	}
}
