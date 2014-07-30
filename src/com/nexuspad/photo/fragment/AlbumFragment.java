package com.nexuspad.photo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.*;
import android.widget.AdapterView;
import android.widget.GridView;
import com.nexuspad.R;
import com.nexuspad.common.annotation.FragmentName;
import com.nexuspad.common.fragment.EntryFragment;
import com.nexuspad.datamodel.*;
import com.nexuspad.dataservice.EntryListService;
import com.nexuspad.photo.activity.AlbumEditActivity;
import com.nexuspad.photo.activity.PhotoActivity;
import com.nexuspad.photo.adapter.PhotosAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Edmond
 */

@FragmentName(AlbumFragment.TAG)
public class AlbumFragment extends EntryFragment<NPAlbum> implements AdapterView.OnItemClickListener {
	public static final String TAG = "AlbumFragment";
	private GridView mGridView;

	public static AlbumFragment of(NPAlbum album, NPFolder folder) {
		final Bundle bundle = new Bundle();
		bundle.putParcelable(KEY_ENTRY, album);
		bundle.putParcelable(KEY_FOLDER, folder);

		final AlbumFragment fragment = new AlbumFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	private EntryList mPhotosList = new EntryList();
	private PhotosAdapter mPhotosAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.album_frag, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.edit:
				onEdit();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected boolean shouldGetDetailEntry() {
		return true;
	}

	private void onEdit() {
		final Intent intent = AlbumEditActivity.of(getActivity(), getFolder(), getEntry());
		startActivity(intent);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.album_frag, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		mGridView = (GridView)view.findViewById(R.id.grid_view);
		mGridView.setOnItemClickListener(this);
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	protected void updateUI() {
		final NPAlbum album = getEntry();

		if (album != null) {
			if (album.getPhotos() != null) {
				for (NPPhoto p : album.getPhotos()) {
					mPhotosList.getEntries().add(p);
				}
			}

			final List<NPUpload> attachments = album.getAttachments();
			if (attachments != null) {
				for (NPUpload npUpload : attachments) {
					mPhotosList.getEntries().add(new NPPhoto(npUpload));
				}
			}
		}

		if (mPhotosAdapter == null) {
			final FragmentActivity a = getActivity();
			mPhotosAdapter = new PhotosAdapter(a, mPhotosList, getFolder(), EntryListService.getInstance(a), EntryTemplate.PHOTO);
			mGridView.setAdapter(mPhotosAdapter);
		} else {
			mPhotosAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		final NPPhoto photo = mPhotosAdapter.getItem(position);
		ArrayList<NPPhoto> photos = new ArrayList<NPPhoto>();
		for (NPEntry e : (List<? extends NPEntry>)mPhotosList.getEntries()) {
			photos.add(NPPhoto.fromEntry(e));
		}
		PhotoActivity.startWithFolder(getFolder(), photo, photos, getActivity());
	}
}
