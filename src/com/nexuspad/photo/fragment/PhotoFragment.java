/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.photo.fragment;

import android.app.ActionBar;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.*;
import android.widget.ImageView;
import com.google.common.collect.Iterables;
import com.nexuspad.R;
import com.nexuspad.common.Constants;
import com.nexuspad.common.annotation.FragmentName;
import com.nexuspad.common.annotation.ModuleId;
import com.nexuspad.common.fragment.EntriesFragment;
import com.nexuspad.common.view.ZoomableImageView;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.NPFolder;
import com.nexuspad.datamodel.NPPhoto;
import com.nexuspad.dataservice.NPException;
import com.nexuspad.dataservice.NPWebServiceUtil;
import com.nexuspad.dataservice.ServiceConstants;
import com.squareup.picasso.Picasso;
import uk.co.senab.photoview.PhotoViewAttacher;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams;

/**
 * @author Edmond
 */
@FragmentName(PhotoFragment.TAG)
@ModuleId(moduleId = ServiceConstants.PHOTO_MODULE, template = EntryTemplate.PHOTO)
public class PhotoFragment extends EntriesFragment {
    public static final String TAG = "PhotoFragment";

    public static final String KEY_PHOTO = "key_photo";

    private static List<? extends NPPhoto> sPhotos;

    private ViewPager mViewPager;
    private Picasso mPicasso;
    private int mInitialPhotoIndex = -1;

    public static PhotoFragment of(NPFolder f, NPPhoto photo, List<? extends NPPhoto> photos) {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.KEY_FOLDER, f);
        bundle.putParcelable(KEY_PHOTO, photo);

        sPhotos = new ArrayList<NPPhoto>(photos); // parceling is too slow

        final PhotoFragment fragment = new PhotoFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedState) {
        setHasOptionsMenu(true);

        super.onCreate(savedState);
        final Bundle arguments = getArguments();

        mPicasso = Picasso.with(getActivity());
        final NPPhoto photo = arguments.getParcelable(KEY_PHOTO);
        mInitialPhotoIndex = Iterables.indexOf(sPhotos, photo.filterById());
    }

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		setHasOptionsMenu(true);
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
                final NPPhoto photo = sPhotos.get(mViewPager.getCurrentItem());
                deleteEntry(photo);
                sPhotos.remove(photo);
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

        mViewPager = (ViewPager)view.findViewById(R.id.view_pager);

        mViewPager.setAdapter(newPagerAdapter());
        mViewPager.setCurrentItem(mInitialPhotoIndex, false);

        final Resources resources = getResources();
        mViewPager.setBackgroundColor(resources.getColor(android.R.color.background_dark));
        mViewPager.setPageMargin(resources.getDimensionPixelSize(R.dimen.np_padding_medium));
    }

    private void stableNotifyAdapter() {
        final int prevPos = mViewPager.getCurrentItem();
        mViewPager.getAdapter().notifyDataSetChanged();
        mViewPager.setCurrentItem(prevPos);
    }

    private PagerAdapter newPagerAdapter() {
        return new PagerAdapter() {
            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                final NPPhoto photo = sPhotos.get(position);

                final FragmentActivity activity = getActivity();
                final ActionBar actionBar = activity.getActionBar();
                final LayoutInflater inflater = LayoutInflater.from(activity);

                final View frame = inflater.inflate(R.layout.photo_layout, container, false);

                final ZoomableImageView imageView = (ZoomableImageView)frame.findViewById(android.R.id.icon);
                imageView.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
                    @Override
                    public void onViewTap(View view, float x, float y) {
                        if (actionBar != null) {
                            if (actionBar.isShowing()) {
                                actionBar.hide();
                            } else {
                                actionBar.show();
                            }
                        }
                    }
                });

                try {
                    final String url = NPWebServiceUtil.fullUrlWithAuthenticationTokens(photo.getPhotoUrl(), getActivity());

                    mPicasso.load(url)
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.ic_launcher)
                            .into(imageView, new ZoomableImageViewCallback(imageView));

                } catch (NPException e) {
                    // TODO handle error
                }

                container.addView(frame, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                return frame;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                final ImageView imageView = (ImageView) ((ViewGroup) object).getChildAt(0);
                mPicasso.cancelRequest(imageView);
                container.removeView(imageView);
            }

            @Override
            public void setPrimaryItem(ViewGroup container, int position, Object object) {
                super.setPrimaryItem(container, position, object);
                final NPPhoto photo = sPhotos.get(position);
                final ActionBar actionBar = getActivity().getActionBar();
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
                return sPhotos.size();
            }
        };
    }

    private static class ZoomableImageViewCallback implements com.squareup.picasso.Callback {

        private WeakReference<ZoomableImageView> mReference;

        private ZoomableImageViewCallback(ZoomableImageView imageView) {
            mReference = new WeakReference<ZoomableImageView>(imageView);
        }

        @Override
        public void onSuccess() {
            update();
        }

        @Override
        public void onError() {
            update();
        }

        private void update() {
            final ZoomableImageView view = mReference.get();
            if (view != null) {
                view.update();
            }
        }
    }
}
