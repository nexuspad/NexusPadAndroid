/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.photo.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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
import com.nexuspad.common.activity.FoldersNavigatorActivity;
import com.nexuspad.common.adapters.OnPagingListEndListener;
import com.nexuspad.common.annotation.FragmentName;
import com.nexuspad.common.annotation.ModuleId;
import com.nexuspad.common.fragment.EntriesFragment;
import com.nexuspad.datamodel.*;
import com.nexuspad.photo.activity.PhotoActivity;
import com.nexuspad.photo.activity.PhotosActivity;
import com.nexuspad.photo.adapter.PhotosAdapter;

import java.util.ArrayList;
import java.util.List;

import static com.nexuspad.dataservice.ServiceConstants.PHOTO_MODULE;

/**
 * @author Edmond
 */
@FragmentName(PhotosFragment.TAG)
@ModuleId(moduleId = PHOTO_MODULE, template = EntryTemplate.PHOTO)
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

	@Override
	protected void onListLoaded(EntryList list) {
		Log.i(TAG, "Receiving photo list.");

		PhotosAdapter a = (PhotosAdapter)mGridView.getAdapter();
		if (a != null) {
			stableNotifyAdapter(a);
			dismissProgressIndicator();
			return;
		}

		a = new PhotosAdapter(getActivity(), mEntryList, getFolder(), getEntryListService(), getTemplate());

		mGridView.setAdapter(a);
		stableNotifyAdapter(a);

		dismissProgressIndicator();
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
		intent.putExtra(PhotoActivity.KEY_PHOTO, photo);
		intent.putParcelableArrayListExtra(PhotoActivity.KEY_PHOTOS, photos);

		activity.startActivity(intent);
		activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

	@Override
	protected void onNewEntry(NPEntry entry) {
		if (entry instanceof NPPhoto) {  // or album, which will be handled at AlbumsFragment
			final NPPhoto photo = (NPPhoto) entry;
            mEntryList.getEntries().add(photo);
		}
		super.onNewEntry(entry);
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
	protected void onEntryListUpdated() {
		super.onEntryListUpdated();
		BaseAdapter adapter = (BaseAdapter) mGridView.getAdapter();
		stableNotifyAdapter(adapter);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case REQ_FOLDER:
				if (resultCode == Activity.RESULT_OK) {
					final FragmentActivity activity = getActivity();
					final NPFolder folder = data.getParcelableExtra(FoldersNavigatorActivity.KEY_FOLDER);
					PhotosActivity.startWithFolder(folder, activity);
					activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
				}
				break;
			default:
				throw new AssertionError("unknown requestCode: " + requestCode);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.photos_frag, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		mGridView = (GridView)view.findViewById(R.id.grid_view);

		mGridView.setOnScrollListener(
			newDirectionalScrollListener(
				new OnPagingListEndListener() {
					@Override
					protected void onListBottom(int page) {
						queryEntriesInFolderByPage(getCurrentPage() + 1);
					}
				}
			)
		);

		mGridView.setOnItemClickListener(this);

		super.onViewCreated(view, savedInstanceState);

		initFolderSelector(REQ_FOLDER);

		if (mEntryList == null) {
			queryEntriesAsync();
		} else {
			onListLoaded(mEntryList);
		}
	}


	private void stableNotifyAdapter(BaseAdapter adapter) {
		final int prevPos = mGridView.getFirstVisiblePosition();
		adapter.notifyDataSetChanged();
		mGridView.setSelection(prevPos);
	}
}
