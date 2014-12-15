package com.nexuspad.photo.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.koushikdutta.ion.Ion;
import com.nexuspad.R;
import com.nexuspad.common.adapters.EntriesAdapter;
import com.nexuspad.service.datamodel.EntryList;
import com.nexuspad.service.datamodel.EntryTemplate;
import com.nexuspad.service.datamodel.NPFolder;
import com.nexuspad.service.datamodel.NPPhoto;
import com.nexuspad.service.dataservice.EntryListService;
import com.nexuspad.service.dataservice.NPException;
import com.nexuspad.service.dataservice.NPWebServiceUtil;

public class PhotosAdapter extends EntriesAdapter<NPPhoto> {

	private final Activity mActivity;

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
		super(a, entryList);
		mActivity = a;
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

		String tnUrl = null;

		if (getItem(position) != null) {
			try {
				tnUrl = NPWebServiceUtil.fullUrlWithAuthenticationTokens(getItem(position).getTnUrl(), mActivity);
			} catch (NPException e) {
				e.printStackTrace();
			}

//		Log.i(TAG, tnUrl);
//
//		mPicasso.setLoggingEnabled(true);
//		mPicasso.setIndicatorsEnabled(true);
//
//		mPicasso.with(mActivity)
//				.load(tnUrl)
//				.placeholder(R.drawable.placeholder)
//				.error(R.drawable.ic_launcher)
//				.resizeDimen(R.dimen.photo_grid_width, R.dimen.photo_grid_height)
//				.centerCrop()
//				.into(view, new Callback.EmptyCallback() {
//					@Override
//					public void onSuccess() {
//					}
//					@Override
//					public void onError() {
//					}
//				});

			Ion.with(mActivity)
					.load(tnUrl)
					.withBitmap()
					.placeholder(R.drawable.placeholder)
					.error(R.drawable.ic_launcher)
					.intoImageView(view);

		}

		return view;
	}

	@Override
	protected String getEntriesHeaderText() {
		return null;
	}
}