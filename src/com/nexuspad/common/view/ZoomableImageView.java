package com.nexuspad.common.view;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import com.nexuspad.common.Logs;
import uk.co.senab.photoview.IPhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class ZoomableImageView extends ImageView implements IPhotoView {
    public static final String TAG = "ZoomableImageView";

    private final PhotoViewAttacher mAttacher;

    public void onScale(float scaleFactor, float focusX, float focusY) {
        mAttacher.onScale(scaleFactor, focusX, focusY);
    }

    public ImageView getImageView() {
        return mAttacher.getImageView();
    }

    public ZoomableImageView(Context context) {
        this(context, null);
    }

    public ZoomableImageView(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }

    public ZoomableImageView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);

        super.setScaleType(ImageView.ScaleType.MATRIX);
        mAttacher = new PhotoViewAttacher(this);
    }

    @Override
    public boolean canZoom() {
        return mAttacher.canZoom();
    }

    @Override
    public RectF getDisplayRect() {
        return mAttacher.getDisplayRect();
    }

    @Override
    public boolean setDisplayMatrix(Matrix finalMatrix) {
        return mAttacher.setDisplayMatrix(finalMatrix);
    }

    @Override
    public Matrix getDisplayMatrix() {
        return mAttacher.getDisplayMatrix();
    }

    @Override
    public float getMinScale() {
        return mAttacher.getMinScale();
    }

    @Override
    public float getMinimumScale() {
        return mAttacher.getMinimumScale();
    }

    @Override
    public float getMidScale() {
        return mAttacher.getMidScale();
    }

    @Override
    public float getMediumScale() {
        return mAttacher.getMediumScale();
    }

    @Override
    public float getMaxScale() {
        return mAttacher.getMaxScale();
    }

    @Override
    public float getMaximumScale() {
        return mAttacher.getMaximumScale();
    }

    @Override
    public float getScale() {
        return mAttacher.getScale();
    }

    @Override
    public ImageView.ScaleType getScaleType() {
        return ScaleType.MATRIX;
    }

    @Override
    public void setAllowParentInterceptOnEdge(boolean allow) {
        mAttacher.setAllowParentInterceptOnEdge(allow);
    }

    @Override
    public void setScale(float scale) {
        mAttacher.setScale(scale);
    }

    @Override
    public void setScale(float scale, boolean animate) {
        mAttacher.setScale(scale, animate);
    }

    @Override
    public void setScale(float scale, float focalX, float focalY, boolean animate) {
        mAttacher.setScale(scale, focalX, focalY, animate);
    }

    @Override
    public void setMinScale(float minScale) {
        mAttacher.setMinScale(minScale);
    }

    @Override
    public void setMinimumScale(float minimumScale) {
        mAttacher.setMinimumScale(minimumScale);
    }

    @Override
    public void setMidScale(float midScale) {
        mAttacher.setMidScale(midScale);
    }

    @Override
    public void setMediumScale(float mediumScale) {
        mAttacher.setMediumScale(mediumScale);
    }

    @Override
    public void setMaxScale(float maxScale) {
        mAttacher.setMaxScale(maxScale);
    }

    @Override
    public void setMaximumScale(float maximumScale) {
        mAttacher.setMaximumScale(maximumScale);
    }

    @Override
    // setImageBitmap calls through to this method
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        update();
    }

    public void update() {
        if (mAttacher != null) {
            try {
                mAttacher.update();
            } catch (IllegalStateException e) {
                // Picasso's callback fires even when the image view is not referenced
                Logs.e(TAG, e);
            }
        }
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        update();
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        update();
    }

    @Override
    public void setOnMatrixChangeListener(PhotoViewAttacher.OnMatrixChangedListener listener) {
        mAttacher.setOnMatrixChangeListener(listener);
    }

    @Override
    public void setOnLongClickListener(View.OnLongClickListener l) {
        mAttacher.setOnLongClickListener(l);
    }

    @Override
    public void setOnPhotoTapListener(PhotoViewAttacher.OnPhotoTapListener listener) {
        mAttacher.setOnPhotoTapListener(listener);
    }

    @Override
    public void setOnViewTapListener(PhotoViewAttacher.OnViewTapListener listener) {
        mAttacher.setOnViewTapListener(listener);
    }

    @Override
    public void setScaleType(ImageView.ScaleType scaleType) {
        // do nothing
    }

    @Override
    public void setZoomable(boolean zoomable) {
        mAttacher.setZoomable(zoomable);
    }

    @Override
    public void setPhotoViewRotation(float rotationDegree) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void onDetachedFromWindow() {
        mAttacher.cleanup();
        super.onDetachedFromWindow();
    }
}
