package com.nexuspad.common.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.nexuspad.R;
import com.nexuspad.app.Request;
import com.nexuspad.app.service.UploadService;
import com.nexuspad.common.annotation.FragmentName;
import com.nexuspad.dataservice.FileUploadService;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: edmond
 */
@FragmentName(UploadCenterFragment.TAG)
public class UploadCenterFragment extends NPBaseFragment {
	public static final String TAG = "UploadCenterFragment";

	private final List<Request> mPendingRequests = new ArrayList<Request>();

	private ListView mListView;

	private UploadRequestAdapter mAdapter;
	private UploadService.UploadBinder mBinder;

	private final UploadService.OnNewRequestListener mOnNewRequestListener = new UploadService.OnNewRequestListener() {
		@Override
		public void onNewRequest(Request request) {
			mPendingRequests.add(request);
			mAdapter.notifyDataSetChanged();
		}
	};

	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBinder = (UploadService.UploadBinder) service;

			mBinder.addRequests(mPendingRequests);
			mBinder.addCallback(mOnNewRequestListener);

			mAdapter = new UploadRequestAdapter(getActivity(), mPendingRequests, R.string.photos);

			mAdapter.notifyDataSetChanged();

			dismissProgressIndicator();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBinder = null;
		}
	};


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAdapter = new UploadRequestAdapter(getActivity(), mPendingRequests, R.string.photos);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list_content, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mListView = (ListView)view.findViewById(android.R.id.list);
		mListView.setAdapter(mAdapter);

		mAdapter.notifyDataSetChanged();
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		final FragmentActivity activity = getActivity();
		final Intent service = new Intent(activity, UploadService.class);
		activity.startService(service);
		activity.bindService(service, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (mBinder != null) {
			mBinder.removeCallback(mOnNewRequestListener);
		}
		getActivity().unbindService(mConnection);
	}

	public void addRequest(Request request) {
		if (mBinder == null) {
			mPendingRequests.add(request);
		} else {
			mBinder.addRequest(request);
		}
	}

	private static class ViewHolder {
		private ImageView icon;
		private TextView title;
		private TextView subTitle;
		private ProgressBar progressbar;
		private ImageButton menu;
		private boolean cancelled;

		private FileUploadService.Callback callback = new FileUploadService.Callback() {
			@Override
			public boolean onProgress(long progress, long total) {
				progressbar.setMax((int) total);
				progressbar.setProgress((int) progress);
				int percent = (int) ((double) progress / total * 100);
				subTitle.setText(subTitle.getResources().getString(R.string.formatted_progress, percent, progress, total));
				return !cancelled;
			}

			@Override
			public void onDone(boolean success) {
				progressbar.setVisibility(View.GONE);
				final String rawString = subTitle.getResources()
						.getString(success ? R.string.upload_completed : R.string.upload_failed);
				final String color = success ? "green" : "red";
				subTitle.setText(Html.fromHtml("<b><font color='" + color + "'>" + rawString + "</font></b>"));
			}
		};
	}


	private static class UploadRequestAdapter extends BaseAdapter {
		private static final int VIEW_TYPE_REQUEST = 1;

		private final Context mContext;
		private final List<Request> mRequests;
		private final int mHeaderRes;

		public UploadRequestAdapter(Context context, List<Request> requests, int headerRes) {
			mContext = context;
			mRequests = requests;
			mHeaderRes = headerRes;
		}

		@Override
		public int getViewTypeCount() {
			return 1;
		}

		@Override
		public int getItemViewType(int position) {
			return VIEW_TYPE_REQUEST;
		}

		@Override
		public boolean isEmpty() {
			return mRequests.isEmpty();
		}

		@Override
		public int getCount() {
			return mRequests.size();
		}

		@Override
		public Request getItem(int position) {
			return mRequests.get(position);
		}

		@Override
		public long getItemId(int position) {
			return getItem(position).getTimeStamp();
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return getPhotoRequestView(position, convertView, parent);
		}

		private View getPhotoRequestView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_upload_photo, parent, false);

				holder = new ViewHolder();
				holder.icon = (ImageView)convertView.findViewById(android.R.id.icon);
				holder.title = (TextView)convertView.findViewById(android.R.id.text1);
				holder.subTitle = (TextView)convertView.findViewById(android.R.id.text2);
				holder.progressbar = (ProgressBar)convertView.findViewById(android.R.id.progress);
				holder.menu = (ImageButton)convertView.findViewById(R.id.menu);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			final Request request = getItem(position);
			request.setCallback(holder.callback);

			Picasso.with(mContext)
					.load(request.getUri())
					.placeholder(R.drawable.placeholder)
					.error(R.drawable.ic_launcher)
					.resizeDimen(R.dimen.photo_grid_width, R.dimen.photo_grid_height)
					.centerCrop()
					.into(holder.icon);

			holder.title.setText(request.getFile(mContext).getName());
			holder.menu.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final PopupMenu popupMenu = new PopupMenu(mContext, v);
					popupMenu.inflate(R.menu.photos_upload);
					popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
						@Override
						public boolean onMenuItemClick(MenuItem menuItem) {
							switch (menuItem.getItemId()) {
								case R.id.cancel:
									holder.cancelled = true;
									return true;
								default:
									throw new AssertionError("unexpected menuId: " + menuItem.getItemId());
							}
						}
					});
					popupMenu.show();
				}
			});

			return convertView;
		}
	}
}