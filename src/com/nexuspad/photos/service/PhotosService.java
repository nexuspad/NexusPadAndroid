/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.photos.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.android.volley.toolbox.Volley;
import com.edmondapps.utils.android.Logs;
import com.edmondapps.utils.android.animation.ViewAnimations;
import com.nexuspad.R;
import com.nexuspad.app.BitmapLruCache;
import com.nexuspad.datamodel.Photo;
import com.nexuspad.dataservice.NPService;
import com.nexuspad.photos.ui.fragment.PhotoFragment.BitmapInfo;

/**
 * @author Edmond
 * 
 */
public final class PhotosService {
    public static final String TAG = "PhotosService";

    /**
     * An interface-like class for listening to changes of the photos, you may
     * choose to override any method.
     * 
     * @author Edmond
     */
    public static class OnPhotosChangedListener {
        /**
         * Called when new photos are added
         * 
         * @param newPhotos
         *            a view of the new photos
         */
        public void onPhotosAdded(List<? extends Photo> addedPhotos) {
        }

        /**
         * Called when changes happened to the list of photos, usually called
         * immediately after {@link #onPhotosAdded(List)}
         * 
         * @param newPhotos
         *            a view of the new set of photos
         */
        public void onPhotosChanged(List<Photo> newPhotos) {
        }
    }

    private static PhotosService mInstance;

    /**
     * Creates a {@link PhotosService} if necessary, only one
     * {@link PhotosService} is alive for the lifetime of an Android process.
     * 
     * @param c
     *            used to obtain the application context
     * @return the singleton of {@link PhotosService}
     * @see Context#getApplicationContext()
     */
    public static PhotosService getInstance(Context c) {
        if (mInstance == null) {
            mInstance = new PhotosService(c.getApplicationContext());
        }
        return mInstance;
    }

    private final int mMaxSide;
    private final ImageLoader mImageLoader;
    private final RequestQueue mRequestQueue;
    private final BitmapLruCache mImageCache;

    private int mActivePhotoIndex = -1;
    private List<Photo> mPhotos;
    private BitmapInfo mBitmapInfo;
    private OnPhotosChangedListener mOnPhotosChangedListener;
    private final int mThumbnailMaxSide;

    /**
     * @param c
     *            should be the application context
     *            ({@link Context#getApplicationContext()})
     */
    private PhotosService(Context c) {
        mImageCache = new BitmapLruCache();
        mRequestQueue = Volley.newRequestQueue(c);
        mImageLoader = new ImageLoader(mRequestQueue, mImageCache);

        WindowManager m = (WindowManager)c.getSystemService(Context.WINDOW_SERVICE);
        mMaxSide = initMaxSide(m.getDefaultDisplay());
        mThumbnailMaxSide = initThumbnailMaxSide(c.getResources());
    }

    private static int initMaxSide(Display d) {
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);

        int maxWidth = metrics.heightPixels;
        int maxHeight = metrics.widthPixels;

        if (maxWidth > maxHeight) {
            return maxWidth;
        } else {
            return maxHeight;
        }
    }

    private static int initThumbnailMaxSide(Resources res) {
        int maxWidth = res.getDimensionPixelSize(R.dimen.photo_grid_width);
        int maxHeight = res.getDimensionPixelSize(R.dimen.photo_grid_height);

        if (maxWidth > maxHeight) {
            return maxWidth;
        } else {
            return maxHeight;
        }
    }

    private static Bitmap getBlurredBitmap(Bitmap in, int width, int height) {
        if ( (width <= 0) || (height <= 0)) {
            Logs.e(TAG, "invalid width/height, returning original bitmap");
            return in;
        }

        float widthToHeightRatio = (float)in.getWidth() / (float)in.getHeight();

        int newWidth;
        int newHeight;
        if (width > height) {
            newHeight = height;
            newWidth = (int) (widthToHeightRatio * height);
        } else {
            newHeight = (int) (width / widthToHeightRatio);
            newWidth = width;
        }

        Bitmap bitmap = Bitmap.createScaledBitmap(in, newWidth, newHeight, false);

        return bitmap;
    }

    /**
     * Loads the thumbnail of the {@link Photo} if it is cached.
     * 
     * @return the thumbnail, or null
     */
    public Bitmap getThumbnailIfExist(Photo p) {
        final String thumbnailUrl = NPService.addAuthToken(p.getTnUrl());
        String cacheKey = ImageLoader.getCacheKey(thumbnailUrl, mThumbnailMaxSide, mThumbnailMaxSide);
        return mImageCache.getBitmap(cacheKey);
    }

    /**
     * Loads the thumbnail of the {@link Photo} if it is cached.
     * 
     * @return true if the thumbnail is found in the cached and loaded into the
     *         {@link ImageView}; false otherwise
     */
    public boolean loadThumbnailIfExist(ImageView v, Photo p) {
        Bitmap bitmap = getThumbnailIfExist(p);
        if (bitmap != null) {
            Bitmap blurredBitmap = getBlurredBitmap(bitmap, mMaxSide, mMaxSide);
            v.setImageBitmap(blurredBitmap);
            return true;
        }
        return false;
    }

    /**
     * Attempts to fade in the big photo is available is cache. If not, a
     * thumbnail (if exists) will be used while the big photo loads.
     * 
     * @param v
     *            the {@link ImageView} to load the photo into
     * @param p
     *            a {@link Photo} with the thumbnail url and big photo url
     */
    public void loadBestImage(final ImageView v, Photo p) {
        final String photoUrl = NPService.addAuthToken(p.getPhotoUrl());

        if (!mImageLoader.isCached(photoUrl, mMaxSide, mMaxSide)) {
            // no big photo yet, try thumbnail
            loadThumbnailIfExist(v, p);
        }

        // load the big photo either way
        mImageLoader.get(photoUrl, new ImageListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                v.setImageResource(R.drawable.ic_launcher);
            }

            @Override
            public void onResponse(ImageContainer response, boolean isImmediate) {
                Bitmap bitmap = response.getBitmap();
                if (bitmap != null) {
                    ViewAnimations.fadeInBitmap(v, bitmap, 300);
                } else {
                    v.setImageResource(android.R.drawable.ic_menu_slideshow);
                }
            }
        }, mMaxSide, mMaxSide);
    }

    /**
     * Stores a copy of the list of photos. The integrity of the active
     * {@link Photo} and {@link BitmapInfo} will also be checked.
     * 
     * @param photos
     *            the photos
     * @see #getPhotos()
     */
    public void addPhotos(List<? extends Photo> photos) {
        if (mPhotos == null) {
            mPhotos = new ArrayList<Photo>(photos);
        } else {
            for (Photo p : photos) {
                if (!mPhotos.contains(p)) {
                    mPhotos.add(p);
                }
            }
        }
        setActiveBitmapInfo(mBitmapInfo);
        // setActivePhoto(mActivePhotoIndex); (called by setActiveBitmapInfo())

        if (mOnPhotosChangedListener != null) {
            mOnPhotosChangedListener.onPhotosAdded(photos);
            mOnPhotosChangedListener.onPhotosChanged(getPhotos());
        }
    }

    /**
     * Stores an instance of {@link Photo} for future references.
     * <p>
     * The {@link Photo} instance must exist in the list of {@link Photo}.
     * 
     * @param p
     *            the {@link Photo} instance
     * @see #setActivePhoto(int)
     * @see #setPhotos(List)
     */
    public void setActivePhoto(Photo p) {
        int index = getPhotos().indexOf(p);
        if (index < 0) {
            throw new IllegalArgumentException("Photo does not exist in the list: " + p);
        }
        mActivePhotoIndex = index;
    }

    /**
     * Stores an instance of {@link Photo} for future references.
     * <p>
     * The {@link Photo} instance must exist in the list of {@link Photo}.
     * 
     * @param i
     *            the index of the photo in the list
     * @see #setActivePhoto(Photo)
     * @see #setPhotos(List)
     */
    public void setActivePhoto(int i) {
        Photo photo = getPhotos().get(i);
        if (photo == null) {
            throw new IllegalArgumentException("Cannot find any Photo with the index: " + i);
        } else {
            mActivePhotoIndex = i;
        }
    }

    /**
     * Stores a {@link BitmapInfo} for future references,
     * {@link #setActivePhoto(Photo)} will also be called, thus, the
     * {@link Photo} instance inside of the info must obey the contract of
     * {@link #setActivePhoto(Photo)}.
     * 
     * @param info
     */
    public void setActiveBitmapInfo(BitmapInfo info) {
        setActivePhoto(info.getPhoto());
        mBitmapInfo = info;
    }

    public void setOnPhotosChangedListener(OnPhotosChangedListener l) {
        mOnPhotosChangedListener = l;
    }

    /**
     * @return a view to the list of photos
     * @see #setPhotos(List)
     */
    public List<Photo> getPhotos() {
        if (mPhotos == null) {
            throw new IllegalStateException("no photos were added...EVER");
        }
        return Collections.unmodifiableList(mPhotos);
    }

    /**
     * @return null, or the {@link Photo} stored earlier with
     *         {@link #setActivePhoto}
     * @see #getActivePhotoIndex()
     */
    public Photo getActivePhoto() {
        if (mActivePhotoIndex < 0) {
            return null;
        }
        return getPhotos().get(mActivePhotoIndex);
    }

    /**
     * @return the index specified by {@link #setActivePhoto}, or a
     *         negative number if there is none
     * @see #getActivePhoto()
     */
    public int getActivePhotoIndex() {
        return mActivePhotoIndex;
    }

    /**
     * @return the {@link BitmapInfo} stored earlier with
     *         {@link #setActiveBitmapInfo(BitmapInfo)}
     */
    public BitmapInfo getActiveBitmapInfo() {
        return mBitmapInfo;
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    public BitmapLruCache getImageCache() {
        return mImageCache;
    }

    /**
     * @return the size used to request big pictures with {@link ImageLoader}
     */
    public int getMaxSide() {
        return mMaxSide;
    }

    /**
     * @return the size used to request thumbnails with {@link ImageLoader}
     */
    public int getThumbnailMaxSide() {
        return mThumbnailMaxSide;
    }
}
