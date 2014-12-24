/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.common.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.google.common.base.Strings;
import com.nexuspad.common.Constants;
import com.nexuspad.common.activity.FoldersNavigatorActivity;
import com.nexuspad.common.annotation.ModuleInfo;
import com.nexuspad.service.account.AccountManager;
import com.nexuspad.service.datamodel.NPEntry;
import com.nexuspad.service.datamodel.NPFolder;
import com.nexuspad.service.dataservice.*;

/**
 * Annotate it with {@link com.nexuspad.common.annotation.ModuleInfo}.
 *
 * @author Edmond
 */
public abstract class EntryEditFragment<T extends NPEntry> extends EntryFragment<T> {
	public static final String TAG = EntryEditFragment.class.getSimpleName();

	protected static final int REQ_FOLDER = 1;

	/**
	 * @return if calling {@link #getEntryFromEditor()} would return a valid entry
	 */
	public abstract boolean isEditedEntryValid();

	protected TextView mFolderView;

	/**
	 * Callers of this method should first check with
	 * {@link #isEditedEntryValid()} to guarantee the validity of the edited
	 * entry.<p>
	 * Do not modify the detail entry here, create a copy and use {@link #setEntry(NPEntry)} instead.
	 * Or else equality checks will fail, and no entries will be updated.<p>
	 *
	 * @return an edited entry that reflects the user's changes
	 */
	public abstract T getEntryFromEditor();

	private ModuleInfo mModuleId;

	/**
	 * @return one of the {@code *_MODULE} constants in {@link ServiceConstants}
	 */
	protected int getModule() {
		if (mModuleId == null) {
			throw new IllegalStateException("You must annotate the class with ModuleId, or override this method.");
		}
		return mModuleId.moduleId();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mModuleId = ((Object) this).getClass().getAnnotation(ModuleInfo.class);
	}

	protected void updateFolderView() {
		String folderName = getFolder().getFolderName();
		if (Strings.isNullOrEmpty(folderName)) {
			folderName = getString(NPFolder.getFolderNameFor(getFolder().getModuleId()));
		}
		mFolderView.setText(folderName);
	}

	@Override
	protected void onFolderUpdated(NPFolder folder) {
		updateFolderView();
	}

	public final void addEntry() {
		if (isEditedEntryValid()) {
			T entry = getEntryFromEditor();
			try {
				entry.setOwner(AccountManager.currentAccount());
			} catch (NPException e) {
				throw new AssertionError("WTF, I thought I am logged in!");
			}
			onAddEntry(entry);
		} else {
			onEntryUpdateFailed(new ServiceError(ErrorCode.MISSING_PARAM, "entry is not valid"));
		}
	}

	/**
	 * Called when the entry is proven valid, and owner info is set correctly.
	 * <p/>
	 * The default implementation calls {@link EntryService#addEntry(NPEntry)}.
	 *
	 * @param entry the edited entry
	 * @see #isEditedEntryValid()
	 * @see #getEntryFromEditor()
	 */
	protected void onAddEntry(T entry) {
		Log.i(TAG, entry.toString());

		try {
			getEntryService().addEntry(entry);
		} catch (NPException e) {
			throw new RuntimeException(e);
		}
	}

	public final void updateEntry() {
		if (isEditedEntryValid()) {
			final T entry = getEntryFromEditor();

			try {
				entry.setOwner(AccountManager.currentAccount());
			} catch (NPException e) {
				throw new AssertionError("Account information missing!");
			}
			onUpdateEntry(entry);
		}
	}

	/**
	 * Called when the entry is proven valid, and owner info is set correctly.
	 * <p/>
	 * The default implementation calls {@link EntryService#updateEntry(NPEntry)}.
	 *
	 * @param entry the edited entry
	 * @see #isEditedEntryValid()
	 * @see #getEntryFromEditor()
	 */
	protected void onUpdateEntry(T entry) {
		try {
			getEntryService().updateEntry(entry);
		} catch (NPException e) {
			throw new RuntimeException(e);
		}
	}

	protected void installFolderSelectorListener(View v) {
		v.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final FragmentActivity activity = getActivity();
				final NPFolder folder = NPFolder.rootFolderOf(getModule(), activity);
				final Intent intent = FoldersNavigatorActivity.ofParentFolder(activity, folder);
				startActivityForResult(intent, REQ_FOLDER);
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
			case REQ_FOLDER:
				if (resultCode == Activity.RESULT_OK) {
					final NPFolder folder = data.getParcelableExtra(Constants.KEY_FOLDER);
					setFolder(folder);
				}
				break;
		}
	}
}
