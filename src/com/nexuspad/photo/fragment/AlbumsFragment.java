/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.photo.fragment;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.koushikdutta.ion.Ion;
import com.nexuspad.R;
import com.nexuspad.common.Constants;
import com.nexuspad.common.adapters.EntriesAdapter;
import com.nexuspad.common.annotation.FragmentName;
import com.nexuspad.common.annotation.ModuleInfo;
import com.nexuspad.common.fragment.EntriesFragment;
import com.nexuspad.photo.activity.AlbumActivity;
import com.nexuspad.service.datamodel.EntryList;
import com.nexuspad.service.datamodel.EntryTemplate;
import com.nexuspad.service.datamodel.NPAlbum;
import com.nexuspad.service.datamodel.NPFolder;
import com.nexuspad.service.dataservice.ServiceConstants;

/**
 * @author Edmond
 */
@FragmentName(AlbumsFragment.TAG)
@ModuleInfo(moduleId = ServiceConstants.PHOTO_MODULE, template = EntryTemplate.ALBUM)
public class AlbumsFragment extends EntriesFragment {
	public static final String TAG = "AlbumsFragment";

	public static AlbumsFragment of(NPFolder folder) {
		final Bundle bundle = new Bundle();
		bundle.putParcelable(Constants.KEY_FOLDER, folder);

		final AlbumsFragment fragment = new AlbumsFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.albums_frag, container, false);
	}

	@Override
	protected void onListLoaded(EntryList newListToDisplay) {
		Log.i(TAG, "Receiving entry list.");

		super.onListLoaded(newListToDisplay);

		AlbumsAdapter a = (AlbumsAdapter)getAdapter();

		if (a == null) {
			a = new AlbumsAdapter();
			setAdapter(a);
			mListView.setAdapter(a);

		} else {
			a.setDisplayEntryList(newListToDisplay);
		}
	}

	@Override
	protected void onSearchLoaded(EntryList list) {
		((EntriesAdapter)getAdapter()).setDisplayEntryList(list);
	}


	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		getListView().setOnScrollListener(newDirectionalScrollListener(null));

		initFolderSelector(ACTIVITY_REQ_CODE_FOLDER_SELECTOR);

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
			case ACTIVITY_REQ_CODE_FOLDER_SELECTOR:
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
				throw new AssertionError("unexpected requestCode: " + requestCode);
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

        final NPAlbum album = ((AlbumsAdapter) getAdapter()).getItem(position);
        AlbumActivity.startWith(album, getFolder(), getActivity());
	}

	private static class ViewHolder {
		ImageView thumbnail;
		TextView title;
	}

	private class AlbumsAdapter extends EntriesAdapter<NPAlbum> {

		public AlbumsAdapter() {
			super(getActivity(), mEntryList);
		}

		@Override
		protected View getEntryView(NPAlbum entry, int position, View convertView, ViewGroup parent) {
			final AlbumsFragment.ViewHolder holder;
			if (convertView == null) {
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_album, parent, false);

				holder = new AlbumsFragment.ViewHolder();
				holder.thumbnail = (ImageView)convertView.findViewById(android.R.id.icon);
				holder.title = (TextView)convertView.findViewById(android.R.id.text1);

				convertView.setTag(holder);
			} else {
				holder = (AlbumsFragment.ViewHolder) convertView.getTag();
			}

			final NPAlbum album = getItem(position);
			holder.title.setText(album.getTitle());
			final String tnUrl = album.getTnUrl();
			if (!TextUtils.isEmpty(tnUrl)) {
				Ion.with(getActivity())
						.load(tnUrl)
						.withBitmap()
						.placeholder(R.drawable.placeholder)
						.error(R.drawable.ic_launcher)
//				.animateLoad(spinAnimation)
//				.animateIn(fadeInAnimation)
						.intoImageView(holder.thumbnail);
			} else {
				holder.thumbnail.setImageResource(R.drawable.placeholder);
			}

			return convertView;
		}

		@Override
		protected String getEntriesHeaderText() {
			return null;
		}
	}
}
