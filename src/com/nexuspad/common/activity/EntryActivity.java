/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.common.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import com.nexuspad.R;
import com.nexuspad.common.Constants;
import com.nexuspad.common.fragment.EntryFragment;
import com.nexuspad.datamodel.NPEntry;
import com.nexuspad.datamodel.NPFolder;

/**
 * @author Edmond
 */
public abstract class EntryActivity<T extends NPEntry> extends SinglePaneActivity implements EntryFragment.Callback<T> {
	private T mEntry;
	private NPFolder mFolder;

	@Override
	protected int onCreateLayoutId() {
		return R.layout.np_padding_activity;
	}

	@Override
	protected void onCreate(Bundle savedState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		handleIntent(getIntent());
		super.onCreate(savedState);

		getActionBar().setIcon(R.drawable.ic_ab);
	}

	@Override
	public void onNewIntent(Intent i) {
		super.onNewIntent(i);
		handleIntent(i);
	}

	private void handleIntent(Intent i) {
		mFolder = i.getParcelableExtra(Constants.KEY_FOLDER);

		if (mFolder == null) {
			throw new IllegalArgumentException("you must pass in a Folder with KEY_FOLDER");
		}

		T entry = i.getParcelableExtra(Constants.KEY_ENTRY);
		if (mEntry != entry) {
			mEntry = entry;
			onNewEntry(entry);
		}
	}

	/**
	 * Called when the entry has changed, usually a result of
	 * {@link #onCreate(Bundle)} or {@link #onNewIntent(Intent)}.
	 *
	 * @param entry the new entry, same as {@link #getEntry()}
	 */
	protected void onNewEntry(T entry) {
		setTitle(entry.getTitle());
	}

	@Override
	public void onDeleting(EntryFragment<T> f, T entry) {
		finish();
	}

	protected T getEntry() {
		return mEntry;
	}

	protected NPFolder getFolder() {
		return mFolder;
	}
}
