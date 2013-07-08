/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.photos.ui.fragment;

import java.util.concurrent.PriorityBlockingQueue;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.edmondapps.utils.android.annotaion.FragmentName;

/**
 * @author Edmond
 * 
 */
@FragmentName(PhotosUploadFragment.TAG)
public class PhotosUploadFragment extends SherlockListFragment {
    public static final String TAG = "PhotosUploadFragment";
    public static final String KEY_URI = "key_uri";

    public static PhotosUploadFragment of(Uri uri) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_URI, uri);

        PhotosUploadFragment fragment = new PhotosUploadFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    private static final PriorityBlockingQueue<Request> mQueue = new PriorityBlockingQueue<Request>();

    private Uri mUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUri = getArguments().getParcelable(KEY_URI);

        Request request = new Request(mUri);
        if (!mQueue.contains(request)) {
            mQueue.add(request);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
    }
}
