/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.photos.ui.fragment;

import static com.edmondapps.utils.android.view.ViewUtils.findView;
import static com.nineoldandroids.view.ViewHelper.setPivotX;
import static com.nineoldandroids.view.ViewHelper.setPivotY;
import static com.nineoldandroids.view.ViewHelper.setScaleX;
import static com.nineoldandroids.view.ViewHelper.setScaleY;
import static com.nineoldandroids.view.ViewHelper.setTranslationX;
import static com.nineoldandroids.view.ViewHelper.setTranslationY;
import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.ImageView;

import com.edmondapps.utils.android.annotaion.FragmentName;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.datamodel.Photo;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.photos.service.PhotosService;
import com.nexuspad.photos.service.PhotosService.OnPhotosChangedListener;
import com.nexuspad.ui.fragment.EntriesFragment;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;

/**
 * @author Edmond
 * 
 */
@FragmentName(PhotoFragment.TAG)
@ModuleId(moduleId = ServiceConstants.PHOTO_MODULE, template = EntryTemplate.PHOTO)
public class PhotoFragment extends EntriesFragment {
    public static final String TAG = "PhotoFragment";

    public static final class BitmapInfo implements Parcelable {
        public static final Creator<BitmapInfo> CREATOR = new Creator<BitmapInfo>() {
            @Override
            public BitmapInfo createFromParcel(Parcel source) {
                return new BitmapInfo(source);
            }

            @Override
            public BitmapInfo[] newArray(int size) {
                return new BitmapInfo[size];
            }
        };

        private final Photo mPhoto;
        private final int mX;
        private final int mY;
        private final int mWidth;
        private final int mHeight;

        public BitmapInfo(Photo p, int[] positionOnScreen, int width, int height) {
            mPhoto = p;
            mX = positionOnScreen[0];
            mY = positionOnScreen[1];
            mWidth = width;
            mHeight = height;
        }

        private BitmapInfo(Parcel p) {
            mPhoto = p.readParcelable(Photo.class.getClassLoader());
            mX = p.readInt();
            mY = p.readInt();
            mWidth = p.readInt();
            mHeight = p.readInt();
        }

        @Override
        public final void writeToParcel(Parcel p, int flags) {
            p.writeParcelable(mPhoto, 0);
            p.writeInt(mX);
            p.writeInt(mY);
            p.writeInt(mWidth);
            p.writeInt(mHeight);
        }

        @Override
        public final int describeContents() {
            return 0;
        }

        public final Photo getPhoto() {
            return mPhoto;
        }

        public final int getX() {
            return mX;
        }

        public final int getY() {
            return mY;
        }

        public final int getWidth() {
            return mWidth;
        }

        public final int getHeight() {
            return mHeight;
        }
    }

    private ColorDrawable mBackground;
    private ViewPager mViewPager;
    private ImageView mImageV;

    private boolean mPlayedEnterAnimation;
    private BitmapInfo mBitmapInfo;
    private int mLeftDelta;
    private int mTopDelta;
    private float mWidthScale;
    private float mHeightScale;

    private List<Photo> mPhotos;
    private PhotosService mPhotosService;

    /**
     * With scale-up animation
     */
    public static PhotoFragment of(Folder f) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_FOLDER, f);

        PhotoFragment fragment = new PhotoFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    protected boolean isLoadListEnabled() {
        // we have the list from the PhotosService
        return false;
    }

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        mPhotosService = PhotosService.getInstance(getActivity());
        mBitmapInfo = mPhotosService.getActiveBitmapInfo();
        mPhotos = new ArrayList<Photo>(mPhotosService.getPhotos());
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
        installListeners();
    }

    @SuppressWarnings("deprecation")
    private void initViews() {
        mBackground = new ColorDrawable(Color.BLACK);

        mViewPager.setAdapter(newPagerAdapter());
        mViewPager.setCurrentItem(mPhotosService.getActivePhotoIndex(), false);
        mViewPager.setBackgroundDrawable(mBackground);
    }

    private void installListeners() {
        mPhotosService.setOnPhotosChangedListener(new OnPhotosChangedListener() {
            @Override
            public void onPhotosAdded(List<? extends Photo> addedPhotos) {
                super.onPhotosAdded(addedPhotos);
                mPhotos.addAll(addedPhotos);

                // "refreshes" the adapter
                PagerAdapter adapter = mViewPager.getAdapter();
                int currentItem = mViewPager.getCurrentItem();
                mViewPager.setAdapter(null);
                mViewPager.setAdapter(adapter);
                mViewPager.setCurrentItem(currentItem, false);
            }
        });
    }

    private PagerAdapter newPagerAdapter() {
        return new PagerAdapter() {
            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                Photo photo = mPhotos.get(position);

                Activity activity = getActivity();
                LayoutInflater inflater = LayoutInflater.from(activity);

                View frame = inflater.inflate(R.layout.photo_layout, container, false);

                ImageView imageView = findView(frame, android.R.id.icon);
                // not ready
                // imageView.setOnTouchListener(new OnSwipeUpListener(activity)
                // {
                // @Override
                // protected void onSwipingVertically(View view, MotionEvent ev)
                // {
                // super.onSwipingVertically(view, ev);
                // mViewPager.requestDisallowInterceptTouchEvent(true);
                // }
                //
                // @Override
                // protected void restore(View view) {
                // super.restore(view);
                // mViewPager.requestDisallowInterceptTouchEvent(false);
                // }
                //
                // @Override
                // protected void swipeAway(View view, float velocity, float
                // deltaY) {
                // super.swipeAway(view, velocity, deltaY);
                // mViewPager.requestDisallowInterceptTouchEvent(false);
                // }
                // });
                mPhotosService.loadBestImage(imageView, photo);

                frame.setTag(imageView);

                container.addView(frame);
                return frame;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View)object);
            }

            @Override
            public void setPrimaryItem(ViewGroup container, int position, Object object) {
                super.setPrimaryItem(container, position, object);
                if (!mPlayedEnterAnimation && (position == mPhotosService.getActivePhotoIndex())) {

                    mImageV = (ImageView)container.getChildAt(0).getTag();
                    if (mPhotosService.loadThumbnailIfExist(mImageV, mPhotos.get(position))) {
                        prepareEnterAnimation();
                    }
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
    public void onDestroy() {
        super.onDestroy();
    }

    private void prepareEnterAnimation() {
        if (mBitmapInfo != null) {
            mImageV.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mImageV.getViewTreeObserver().removeOnPreDrawListener(this);

                    int[] newPos = new int[2];
                    mImageV.getLocationOnScreen(newPos);

                    mLeftDelta = mBitmapInfo.mX - newPos[0];
                    mTopDelta = mBitmapInfo.mY - newPos[1];

                    mWidthScale = (float)mBitmapInfo.mWidth / mImageV.getWidth();
                    mHeightScale = (float)mBitmapInfo.mHeight / mImageV.getHeight();

                    playEnterAnimation();
                    return true;
                }
            });
        }
    }

    private void playEnterAnimation() {
        setPivotX(mImageV, 0);
        setPivotY(mImageV, 0);
        setScaleX(mImageV, mWidthScale);
        setScaleY(mImageV, mHeightScale);
        setTranslationX(mImageV, mLeftDelta);
        setTranslationY(mImageV, mTopDelta);

        animate(mImageV)
                .scaleX(1)
                .scaleY(1)
                .translationX(0)
                .translationY(0);

        ObjectAnimator animator = ObjectAnimator.ofInt(mBackground, "alpha", 0, 255);
        animator.setDuration(300L);
        animator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mViewPager.invalidateDrawable(mBackground);
            }
        });
        animator.start();
        mPlayedEnterAnimation = true;
    }

    @Override
    protected void onNewFolder(Context c, Intent i, Folder f) {
        throw new UnsupportedOperationException();
    }
}
