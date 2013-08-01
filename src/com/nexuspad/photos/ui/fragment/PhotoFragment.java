/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.photos.ui.fragment;

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
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
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
import com.squareup.picasso.Target;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

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

    private Picasso mPicasso;
    private List<Photo> mPhotos;
    private int mInitialPhotoIndex;

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
        setHasOptionsMenu(true);

        super.onCreate(savedState);
        final Bundle arguments = getArguments();

        mPicasso = Picasso.with(getActivity());
        mPhotos = arguments.getParcelableArrayList(KEY_PHOTOS);
        final Photo photo = arguments.getParcelable(KEY_PHOTO);
        mInitialPhotoIndex = mPhotos.indexOf(photo);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.photo_frag, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                final Photo photo = mPhotos.get(mViewPager.getCurrentItem());
                deleteEntry(photo);
                mPhotos.remove(photo);
                stableNotifyAdapter();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        mViewPager.setCurrentItem(mInitialPhotoIndex, false);
    }

    private void stableNotifyAdapter() {
        final int prevPos = mViewPager.getCurrentItem();
        mViewPager.getAdapter().notifyDataSetChanged();
        mViewPager.setCurrentItem(prevPos);
    }

    private void cancelTargetFor(ImageView view) {
        Target target = (Target) view.getTag();
        if (target != null) {
            mPicasso.cancelRequest(target);
        }
    }

    private Target getTargetFor(final ImageView view) {
        Target target = (Target) view.getTag();
        if (target == null) {
            target = new Target() {
                @Override
                public void onSuccess(Bitmap bitmap) {
                    view.setImageBitmap(bitmap);
                }

                @Override
                public void onError() {
                    view.setImageResource(R.drawable.ic_launcher);
                }
            };
            view.setTag(target);
        } else {
            mPicasso.cancelRequest(target);
        }
        return target;
    }

    private PagerAdapter newPagerAdapter() {
        return new PagerAdapter() {
            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                final Photo photo = mPhotos.get(position);

                final SherlockFragmentActivity activity = getSherlockActivity();
                final ActionBar actionBar = getSherlockActivity().getSupportActionBar();
                final LayoutInflater inflater = LayoutInflater.from(activity);

                final View frame = inflater.inflate(R.layout.photo_layout, container, false);

                final PhotoView imageView = findView(frame, android.R.id.icon);
                imageView.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
                    @Override
                    public void onViewTap(View view, float x, float y) {
                        if (actionBar.isShowing()) {
                            actionBar.hide();
                        } else {
                            actionBar.show();
                        }
                    }
                });
                final String url = NPService.addAuthToken(photo.getPhotoUrl());

                mPicasso.load(url)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.ic_launcher)
                        .into(getTargetFor(imageView));

                container.addView(frame, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                return frame;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                final ImageView imageView = (ImageView) ((ViewGroup) object).getChildAt(0);
                cancelTargetFor(imageView);
                container.removeView(imageView);
            }

            @Override
            public void setPrimaryItem(ViewGroup container, int position, Object object) {
                super.setPrimaryItem(container, position, object);
                final Photo photo = mPhotos.get(position);
                final ActionBar actionBar = getSherlockActivity().getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setTitle(photo.getTitle());
                }
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
