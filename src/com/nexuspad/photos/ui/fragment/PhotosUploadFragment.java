/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.photos.ui.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.edmondapps.utils.android.service.FileUploadService;
import com.edmondapps.utils.android.view.PopupMenu;
import com.nexuspad.R;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.photos.Request;
import com.nexuspad.photos.service.PhotoUploadService;
import com.nexuspad.ui.fragment.ListFragment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.edmondapps.utils.android.view.ViewUtils.findView;
import static com.nexuspad.photos.service.PhotoUploadService.PhotosUploadBinder;

/**
 * @author Edmond
 */
@FragmentName(PhotosUploadFragment.TAG)
public class PhotosUploadFragment extends ListFragment {
    public static final String TAG = "PhotosUploadFragment";

    private final PhotoUploadService.Callback mCallback = new PhotoUploadService.Callback() {
        @Override
        public void onNewRequest(Request request) {
            mRequests.add(request);
            updateUI();
        }
    };
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (PhotosUploadBinder) service;

            mBinder.addRequests(mPendingRequests);
            mPendingRequests.clear();

            mRequests.addAll(mBinder.peekRequests());

            mBinder.addCallback(mCallback);
            updateUI();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBinder = null;
        }
    };
    private final List<Request> mRequests = new ArrayList<Request>();
    private final Set<Request> mPendingRequests = new HashSet<Request>();
    private PhotosUploadBinder mBinder;
    private boolean mViewCreated;
    private PhotosUploadAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_content, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewCreated = true;
        updateUI();
    }

    private void updateUI() {
        if (mViewCreated) {
            if (mAdapter == null) {
                mAdapter = new PhotosUploadAdapter();
                setListAdapter(mAdapter);
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final FragmentActivity activity = getActivity();
        final Intent service = new Intent(activity, PhotoUploadService.class);
        activity.startService(service);
        activity.bindService(service, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mBinder != null) {
            mBinder.removeCallback(mCallback);
        }
        getActivity().unbindService(mConnection);
    }

    public void uploadPhotos(Iterable<? extends Uri> uris, Folder folder) {
        for (Uri uri : uris) {
            uploadPhoto(uri, folder);
        }
    }

    public void uploadPhoto(Uri uri, Folder folder) {
        if (mBinder != null) {
            mBinder.addRequest(uri, folder);
        } else {
            mPendingRequests.add(new Request(uri, folder));
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

    private class PhotosUploadAdapter extends BaseAdapter {
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
            final FragmentActivity activity = getActivity();
            final ViewHolder holder;
            if (convertView == null) {
                convertView = activity.getLayoutInflater().inflate(R.layout.list_item_upload_photo, parent, false);

                holder = new ViewHolder();
                holder.icon = findView(convertView, android.R.id.icon);
                holder.title = findView(convertView, android.R.id.text1);
                holder.subTitle = findView(convertView, android.R.id.text2);
                holder.progressbar = findView(convertView, android.R.id.progress);
                holder.menu = findView(convertView, R.id.menu);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final Request request = getItem(position);
            request.setCallback(holder.callback);

            Picasso.with(activity)
                    .load(request.getUri())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.ic_launcher)
                    .resizeDimen(R.dimen.photo_grid_width, R.dimen.photo_grid_height)
                    .centerCrop()
                    .into(holder.icon);

            holder.title.setText(request.getFile(activity).getName());
            holder.menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final PopupMenu popupMenu = PopupMenu.newInstance(activity, v);
                    popupMenu.inflate(R.menu.photos_upload);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(int menuId) {
                            switch (menuId) {
                                case R.id.cancel:
                                    holder.cancelled = true;
                                    return true;
                                default:
                                    throw new AssertionError("unexpected menuId: " + menuId);
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
