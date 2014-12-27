/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.photo.fragment;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import com.nexuspad.R;
import com.nexuspad.common.Constants;
import com.nexuspad.common.adapters.EntriesAdapter;
import com.nexuspad.common.annotation.FragmentName;
import com.nexuspad.common.annotation.ModuleInfo;
import com.nexuspad.common.fragment.EntriesFragment;
import com.nexuspad.common.listeners.OnPagingListEndListener;
import com.nexuspad.photo.activity.PhotoActivity;
import com.nexuspad.photo.adapter.PhotosAdapter;
import com.nexuspad.service.datamodel.*;

import java.util.ArrayList;
import java.util.List;

import static com.nexuspad.service.dataservice.ServiceConstants.PHOTO_MODULE;

/**
 * @author Edmond
 */
@FragmentName(PhotosFragment.TAG)
@ModuleInfo(moduleId = PHOTO_MODULE, template = EntryTemplate.PHOTO)
public class PhotosFragment extends EntriesFragment implements OnItemClickListener {
	public static final String TAG = "PhotosFragment";

	private static final int REQ_FOLDER = 1;

	private GridView mGridView;

	public static PhotosFragment of(NPFolder f) {
		Bundle bundle = new Bundle();
		bundle.putParcelable(Constants.KEY_FOLDER, f);

		PhotosFragment fragment = new PhotosFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	protected OnPagingListEndListener mLoadMoreScrollListener = new OnPagingListEndListener() {
		@Override
		protected void onListBottom(int page) {
			PhotosAdapter adapter = (PhotosAdapter)getAdapter();
			if (adapter.hasMoreToLoad()) {
				queryEntriesInFolderByPage(getCurrentPage() + 1);
			}
		}
	};


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.photos_frag, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		mGridView = (GridView)view.findViewById(R.id.grid_view);

		mGridView.setOnScrollListener(mLoadMoreScrollListener);
		mGridView.setOnItemClickListener(this);

		super.onViewCreated(view, savedInstanceState);

		initFolderSelector(REQ_FOLDER);

		if (mEntryList == null) {
			queryEntriesAsync();
		} else {
			onListLoaded(mEntryList);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case REQ_FOLDER:
				if (resultCode == Activity.RESULT_OK) {
					mFolder = data.getParcelableExtra(Constants.KEY_FOLDER);
					queryEntriesAsync();
				}

				// Refresh Fragment list content after selecting the folder from folder navigator.
				// Since Activity remains the same, we need to update the title in Action bar.
				final ActionBar actionBar = getActivity().getActionBar();
				actionBar.setTitle(mFolder.getFolderName());

				break;

			default:
				throw new AssertionError("unknown requestCode: " + requestCode);
		}
	}

	/**
	 * SwipeRefresh handler.
	 */
	@Override
	public void onRefresh() {
		if (mListFrame instanceof SwipeRefreshLayout) {
			((SwipeRefreshLayout)mListFrame).setRefreshing(true);
			queryEntriesAsync();
		}
	}

	@Override
	protected void onListLoaded(EntryList newListToDisplay) {
		Log.i(TAG, "Receiving photo list.");

		super.onListLoaded(newListToDisplay);

		mLoadMoreScrollListener.setCurrentPage(newListToDisplay.getPageId());

		PhotosAdapter a = (PhotosAdapter)mGridView.getAdapter();

		if (a == null) {
			a = new PhotosAdapter(getActivity(), newListToDisplay, getFolder(), getEntryListService(), getTemplate());

			setAdapter(a);

			mGridView.setAdapter(a);
			stableNotifyAdapter(a);

		} else {
			a.setDisplayEntryList(newListToDisplay);
			stableNotifyAdapter(a);
		}

		if (newListToDisplay.isEmpty()) {
			hideProgressIndicatorAndShowEmptyFolder();
		} else {
			hideProgressIndicatorAndShowMainList();
		}
	}

	@Override
	protected void onSearchLoaded(EntryList list) {
		((EntriesAdapter)getAdapter()).setDisplayEntryList(list);
	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		PhotosAdapter adapter = (PhotosAdapter) mGridView.getAdapter();
		NPPhoto photo = adapter.getItem(position);

		ArrayList<NPPhoto> photos = new ArrayList<NPPhoto>();
		for (NPEntry e : (List<? extends NPEntry>)mEntryList.getEntries()) {
			photos.add(NPPhoto.fromEntry(e));
		}

		FragmentActivity activity = getActivity();

		Intent intent = new Intent(activity, PhotoActivity.class);
		intent.putExtra(Constants.KEY_FOLDER, getFolder());
		intent.putExtra(Constants.KEY_PHOTO, photo);
		intent.putParcelableArrayListExtra(Constants.KEY_PHOTOS, photos);

		activity.startActivity(intent);
		activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

	@Override
	protected void onUpdateEntry(NPEntry entry) {
		if (entry instanceof NPPhoto) {  // or album, which will be handled at AlbumsFragment
			final NPPhoto photo = (NPPhoto) entry;
            mEntryList.getEntries().add(photo);
		}
		super.onUpdateEntry(entry);
	}

	@Override
	protected void onDeleteEntry(NPEntry entry) {
		if (entry instanceof NPPhoto) {
			final NPPhoto photo = (NPPhoto) entry;
			mEntryList.getEntries().remove(photo);
		}
		super.onDeleteEntry(entry);
	}

	@Override
	protected void refreshUIAfterUpdatingEntryList() {
		super.refreshUIAfterUpdatingEntryList();
		BaseAdapter adapter = (BaseAdapter) mGridView.getAdapter();
		stableNotifyAdapter(adapter);
	}


	private void stableNotifyAdapter(BaseAdapter adapter) {
		final int prevPos = mGridView.getFirstVisiblePosition();
		adapter.notifyDataSetChanged();
		mGridView.setSelection(prevPos);
	}

	@Override
	protected void reDisplayListEntries() {
		super.reDisplayListEntries();

		// Need to reset the scroll listener.
		mLoadMoreScrollListener.reset();
	}

}
