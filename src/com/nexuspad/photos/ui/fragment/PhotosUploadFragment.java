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
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockListFragment;
import com.edmondapps.utils.android.Logs;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.edmondapps.utils.android.service.FileUploadService;
import com.nexuspad.R;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.photos.Request;
import com.nexuspad.photos.service.PhotoUploadService;

import java.util.ArrayList;
import java.util.List;

import static com.edmondapps.utils.android.view.ViewUtils.findView;
import static com.nexuspad.photos.service.PhotoUploadService.PhotosUploadBinder;

/**
 * @author Edmond
 */
@FragmentName(PhotosUploadFragment.TAG)
public class PhotosUploadFragment extends SherlockListFragment {
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
    private final List<Request> mPendingRequests = new ArrayList<Request>();
    private PhotosUploadBinder mBinder;
    private boolean mViewCreated;
    private PhotosUploadAdapter mAdapter;

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
        activity.bindService(new Intent(activity, PhotoUploadService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mBinder != null) {
            mBinder.removeCallback(mCallback);
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
        private FileUploadService.Callback callback = new FileUploadService.Callback() {
            @Override
            public boolean onProgress(long progress, long total) {
                progressbar.setMax((int) total);
                progressbar.setProgress((int) progress);
                int percent = (int) ((double) progress / total);
                subTitle.setText(subTitle.getResources().getString(R.string.formatted_progress, percent, progress, total));
                return true;
            }

            @Override
            public void onDone(boolean success) {
                Logs.d("ViewHolder", "onDone: " + success);
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
            final ViewHolder holder;
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_upload_photo, parent, false);

                holder = new ViewHolder();
                holder.icon = findView(convertView, android.R.id.icon);
                holder.title = findView(convertView, android.R.id.text1);
                holder.subTitle = findView(convertView, android.R.id.text2);
                holder.progressbar = findView(convertView, android.R.id.progress);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final Request request = getItem(position);
            request.setCallback(holder.callback);

            holder.title.setText(request.getUri().toString());

            return convertView;
        }
    }
}
