/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.common.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import com.nexuspad.R;
import com.nexuspad.app.App;
import com.nexuspad.common.utils.Lazy;
import com.nexuspad.core.Manifest;
import com.nexuspad.datamodel.NPEntry;
import com.nexuspad.datamodel.NPFolder;
import com.nexuspad.dataservice.EntryService;
import com.nexuspad.dataservice.EntryService.EntryReceiver;
import com.nexuspad.dataservice.NPException;
import com.nexuspad.dataservice.ServiceError;

/**
 * You must pass in a {@code Folder} with the key {@link EntryFragment#KEY_FOLDER}
 *
 * @author Edmond
 */
public abstract class EntryFragment<T extends NPEntry> extends DialogFragment {
	public static final String KEY_ENTRY = "com.nexuspad.ui.fragment.EntryFragment.entry";
	public static final String KEY_FOLDER = "com.nexuspad.ui.fragment.EntryFragment.folder";

	private static final String TAG = "EntryFragment";

	public interface Callback<T extends NPEntry> {
		void onDeleting(EntryFragment<T> f, T entry);
	}

	private final Lazy<EntryService> mEntryService = new Lazy<EntryService>() {
		@Override
		protected EntryService onCreate() {
			return EntryService.getInstance(getActivity());
		}
	};

	private final EntryReceiver mEntryReceiver = new EntryReceiver() {
		@Override
		protected void onUpdate(Context context, Intent intent, NPEntry entry) {
			onEntryUpdatedInternal(entry);
		}

		@Override
		protected void onError(Context context, Intent intent, ServiceError error) {
			super.onError(context, intent, error);
			Log.e(TAG, error.toString());
		}

		@Override
		@SuppressWarnings("unchecked")
		protected void onGet(Context context, Intent intent, NPEntry entry) {
			super.onGet(context, intent, entry);
			setEntry((T) entry);
		}
	};

	private T mEntry;
	private NPFolder mFolder;
	private Callback<T> mCallback;

	@Override
	@SuppressWarnings("unchecked")
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mCallback = App.getCallbackOrThrow(activity, Callback.class);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Bundle bundle = savedInstanceState == null ? getArguments() : savedInstanceState;
		if (bundle != null) {
			mEntry = bundle.getParcelable(KEY_ENTRY);
			mFolder = bundle.getParcelable(KEY_FOLDER);
		}

		if (mFolder == null) {
			throw new IllegalArgumentException("you must pass in a Folder with KEY_FOLDER");
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (savedInstanceState == null) {
			updateUI();
		}
		if (shouldGetDetailEntry()) {
			try {
				final T entry = getEntry();
				if (entry != null) {
					getEntryService().getEntry(entry);
				}
			} catch (NPException e) {
				Log.e(TAG, e.toString());
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle b) {
		super.onSaveInstanceState(b);
		b.putParcelable(KEY_ENTRY, mEntry);
		b.putParcelable(KEY_FOLDER, mFolder);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		T entry = getEntry();
		switch (item.getItemId()) {
			case R.id.delete:
				getEntryService().safeDeleteEntry(getActivity(), entry);
				mCallback.onDeleting(this, entry);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		getActivity().registerReceiver(
				mEntryReceiver,
				EntryReceiver.getIntentFilter(),
				Manifest.permission.LISTEN_ENTRY_CHANGES,
				null);
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(mEntryReceiver);
	}

	public void setEntry(T entry) {
		if (mEntry != entry) {
			mEntry = entry;
			onEntryUpdated(entry);
		}
	}

	/**
	 * Calling this method will also invoke {@link NPEntry#setFolder(com.nexuspad.datamodel.NPFolder)}
	 * for the simple entry and the detail entry.
	 */
	public void setFolder(NPFolder folder) {
		if (mFolder != folder) {
			mFolder = folder;
			onFolderUpdatedInternal(folder);
		}
	}

	private void onFolderUpdatedInternal(NPFolder folder) {
		if (mEntry != null) {
			mEntry.setFolder(folder);
		}
		onFolderUpdated(folder);
	}

	/**
	 * @return the original entry passed in by {@link #KEY_ENTRY} or
	 * {@link #setEntry(T)}
	 */
	public T getEntry() {
		return mEntry;
	}

	public NPFolder getFolder() {
		return mFolder;
	}

	public EntryService getEntryService() {
		return mEntryService.get();
	}

	protected boolean shouldGetDetailEntry() {
		return false;
	}

	protected void deleteEntry() {
		getEntryService().safeDeleteEntry(getActivity(), mEntry);
		mCallback.onDeleting(this, mEntry);
	}

	/**
	 * Called when a folder is updated (usually from the result of selecting a
	 * folder).
	 * <p/>
	 * Default implementation calls {@link #updateUI()}.
	 *
	 * @param folder the new {@link com.nexuspad.datamodel.NPFolder}
	 * @see com.nexuspad.common.activity.FoldersNavigatorActivity
	 */
	protected void onFolderUpdated(NPFolder folder) {
		updateUI();
	}

	/**
	 * Called when the original entry is updated.
	 * <p/>
	 * Default implementation calls {@link #updateUI()}.
	 *
	 * @param entry the new entry
	 */
	protected void onEntryUpdated(T entry) {
		updateUI();
	}

	/**
	 * Called when the UI requires an update (entry/detail/folder entry updated)
	 */
	protected void updateUI() {
	}

	protected void onEntryUpdateFailed(ServiceError error) {
	}

	@SuppressWarnings("unchecked")
	private void onEntryUpdatedInternal(NPEntry e) {
		T entry = (T) e;
		mEntry = entry;

		onEntryUpdated(entry);
	}
}
