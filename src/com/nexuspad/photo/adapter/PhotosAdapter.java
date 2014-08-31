package com.nexuspad.photo.adapter;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.nexuspad.R;
import com.nexuspad.common.adapters.EntriesAdapter;
import com.nexuspad.datamodel.EntryList;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.NPFolder;
import com.nexuspad.datamodel.NPPhoto;
import com.nexuspad.dataservice.EntryListService;
import com.squareup.picasso.Picasso;

public class PhotosAdapter extends EntriesAdapter<NPPhoto> {

	private final Activity mActivity;
	private final Picasso mPicasso;

	/**
	 * use this constructor if you want filtering abilities
	 *
	 * @param a
	 * @param entryList
	 * @param folder
	 * @param service
	 * @param template
	 */
	public PhotosAdapter(Activity a, EntryList entryList, NPFolder folder, EntryListService service, EntryTemplate template) {
		super(a, entryList, folder, service, template);
		mActivity = a;
		mPicasso = Picasso.with(a);
		mPicasso.setIndicatorsEnabled(true);
	}

	@Override
	protected View getEntryView(NPPhoto entry, int position, View convertView, ViewGroup parent) {
		final ImageView view;

		if (convertView == null) {
			LayoutInflater inflater = mActivity.getLayoutInflater();
			view = (ImageView) inflater.inflate(R.layout.layout_photo_grid, parent, false);
		} else {
			view = (ImageView) convertView;
		}

		Log.i(TAG, getItem(position).getTnUrl());

		mPicasso.load(getItem(position).getTnUrl())
				.placeholder(R.drawable.placeholder)
				.error(R.drawable.ic_launcher)
				.resizeDimen(R.dimen.photo_grid_width, R.dimen.photo_grid_height)
				.centerCrop()
				.into(view);

		return view;
	}

	@Override
	protected String getEntriesHeaderText() {
		return null;
	}
}