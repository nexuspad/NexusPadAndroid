/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.photos.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.datamodel.Photo;
import com.nexuspad.dataservice.NPService;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.ui.fragment.EntriesFragment;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import uk.co.senab.photoview.PhotoView;

import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams;
import static com.edmondapps.utils.android.view.ViewUtils.findView;

/**
 * @author Edmond
 */
@FragmentName(PhotoFragment.TAG)
@ModuleId(moduleId = ServiceConstants.PHOTO_MODULE, template = EntryTemplate.PHOTO)
public class PhotoFragment extends EntriesFragment {
    public static final String TAG = "PhotoFragment";

    public static final String KEY_PHOTOS = "key_photos";
    public static final String KEY_PHOTO = "key_photo";

    private ViewPager mViewPager;

    private List<Photo> mPhotos;
    private int mPhotoIndex;

    public static PhotoFragment of(Folder f, Photo photo, ArrayList<? extends Photo> photos) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_FOLDER, f);
        bundle.putParcelableArrayList(KEY_PHOTOS, photos);
        bundle.putParcelable(KEY_PHOTO, photo);

        PhotoFragment fragment = new PhotoFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    protected boolean isLoadListEnabled() {
        // we have the list
        return false;
    }

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        final Bundle arguments = getArguments();

        mPhotos = arguments.getParcelableArrayList(KEY_PHOTOS);
        final Photo photo = arguments.getParcelable(KEY_PHOTO);
        mPhotoIndex = mPhotos.indexOf(photo);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.photo_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedState) {
        super.onViewCreated(view, savedState);

        mViewPager = findView(view, R.id.view_pager);

        initViews();
    }

    private void initViews() {
        mViewPager.setAdapter(newPagerAdapter());
        mViewPager.setCurrentItem(mPhotoIndex, false);
    }

    private PagerAdapter newPagerAdapter() {
        return new PagerAdapter() {
            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                Photo photo = mPhotos.get(position);

                Activity activity = getActivity();
                LayoutInflater inflater = LayoutInflater.from(activity);

                View frame = inflater.inflate(R.layout.photo_layout, container, false);

                final PhotoView imageView = findView(frame, android.R.id.icon);
                final String url = NPService.addAuthToken(photo.getPhotoUrl());

                Picasso.with(getActivity())
                        .load(url)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.ic_launcher)
                        .into(imageView);

                container.addView(frame, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                return frame;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                ImageView view = (ImageView) ((ViewGroup)object).getChildAt(0);
                Picasso.with(getActivity()).cancelRequest(view);
                container.removeView(view);
            }

            @Override
            public boolean isViewFromObject(View v, Object o) {
                return v == o;
            }

            @Override
            public int getCount() {
                return mPhotos.size();
            }
        };
    }

    @Override
    protected void onNewFolder(Context c, Intent i, Folder f) {
        throw new UnsupportedOperationException();
    }
}
