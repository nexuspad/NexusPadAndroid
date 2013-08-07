package com.nexuspad.photos.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import com.actionbarsherlock.app.SherlockFragment;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.edmondapps.utils.android.view.RunnableAnimatorListener;
import com.nexuspad.R;
import com.nexuspad.ui.DirectionalScrollListener;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.edmondapps.utils.android.view.ViewUtils.findView;

@FragmentName(PhotoSelectFragment.TAG)
public class PhotoSelectFragment extends SherlockFragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemSelectedListener {
    public static final String TAG = "PhotoSelectFragment";

    public interface Callback {
        void onCancel(PhotoSelectFragment f);

        void onOk(PhotoSelectFragment f, ArrayList<String> paths);
    }

    private static final int LOADER_ID = 1;

    private final SparseArray<String> mFilePaths = new SparseArray<String>();

    private Callback mCallback;
    private GridView mGridView;
    private PhotosAdapter mAdapter;
    private View mQuickReturnView;
    private View mOkButton;
    private View mCancelButton;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof Callback) {
            mCallback = (Callback) activity;
        } else {
            throw new IllegalStateException(activity + " must implement Callback.");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.photos_select_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);

        mQuickReturnView.setVisibility(View.GONE);

        mAdapter = new PhotosAdapter(getActivity());
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemSelectedListener(this);
        mGridView.setOnScrollListener(new DirectionalScrollListener(0) {
            @Override
            public void onScrollDirectionChanged(final boolean showing) {
                final int height = showing ? 0 : mQuickReturnView.getHeight();
                ViewPropertyAnimator.animate(mQuickReturnView)
                        .translationY(height)
                        .setDuration(200L)
                        .setListener(new RunnableAnimatorListener(true).withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                mOkButton.setClickable(showing);
                                mOkButton.setFocusable(showing);
                                mCancelButton.setClickable(showing);
                                mCancelButton.setFocusable(showing);
                            }
                        }));
            }
        });

        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View _) {
                onOkClicked();
            }
        });
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View _) {
                mCallback.onCancel(PhotoSelectFragment.this);
            }
        });

        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    private void findViews(View view) {
        mQuickReturnView = findView(view, R.id.sticky);
        mGridView = findView(view, R.id.grid_view);
        mOkButton = findView(view, R.id.btn_ok);
        mCancelButton = findView(view, R.id.btn_cancel);
    }

    private void onOkClicked() {
        final int size = mFilePaths.size();
        final ArrayList<String> list = new ArrayList<String>(size);
        for (int i = 0; i < size; ++i) {
            final String filePath = mFilePaths.valueAt(i);
            if (filePath != null) {
                list.add(filePath);
            }
        }
        mCallback.onOk(PhotoSelectFragment.this, list);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(
                getActivity(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{BaseColumns._ID, MediaStore.Images.Media.DATA},
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.changeCursor(null);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        final String original = mFilePaths.get(position); // original/false by default
        final String newValue = original == null ? mAdapter.getFilePath(position) : null; // flips the original state
        mFilePaths.put(position, newValue);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private static class PhotosAdapter extends CursorAdapter {
        public static class ViewHolder {
            ImageView imageView;
        }

        private final LayoutInflater mInflater;
        private final Picasso mPicasso;

        private PhotosAdapter(Context context) {
            super(context, null, 0);
            mInflater = LayoutInflater.from(context);
            mPicasso = Picasso.with(context);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            final View view = mInflater.inflate(R.layout.layout_selectable_photo_grid, parent, false);

            final ViewHolder holder = new ViewHolder();
            holder.imageView = findView(view, android.R.id.icon);

            view.setTag(holder);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final ViewHolder holder = (ViewHolder) view.getTag();
            final String path = getFilePath(cursor);
            mPicasso.load(path)
                    .resizeDimen(R.dimen.photo_grid_width, R.dimen.photo_grid_height)
                    .centerCrop()
                    .into(holder.imageView);
        }

        @Override
        public Cursor getItem(int position) {
            final Cursor cursor = getCursor();
            if (cursor != null) {
                cursor.moveToPosition(position);
                return cursor;
            }
            return null;
        }

        public String getFilePath(int position) {
            return getFilePath(getItem(position));
        }

        private String getFilePath(Cursor cursor) {
            return cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        }
    }
}
