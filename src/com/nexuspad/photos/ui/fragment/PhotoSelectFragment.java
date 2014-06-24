package com.nexuspad.photos.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.edmondapps.utils.android.view.ViewUtils;
import com.nexuspad.R;
import com.nexuspad.app.App;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import static com.edmondapps.utils.android.view.ViewUtils.findView;

@FragmentName(PhotoSelectFragment.TAG)
public class PhotoSelectFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener {
    public static final String TAG = "PhotoSelectFragment";

    public interface Callback {
        void onCancel(PhotoSelectFragment f);

        void onOK(PhotoSelectFragment f, ArrayList<String> paths);
    }

    private static final int LOADER_ID = 1;

    private final SparseArray<String> mFilePaths = new SparseArray<String>();

    private Callback mCallback;
    private GridView mGridView;
    private PhotosCursorAdapter mAdapter;
    private View mOkButton;
    private View mCancelButton;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mCallback = App.getCallbackOrThrow(activity, Callback.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.photos_select_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);

        mAdapter = new PhotosCursorAdapter(getActivity());
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(this);

        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onOkClicked();
            }
        });
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onCancel(PhotoSelectFragment.this);
            }
        });

        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    private void findViews(View view) {
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
        mCallback.onOK(PhotoSelectFragment.this, list);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        final String[] projection = {BaseColumns._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Thumbnails.DATA};
        return new CursorLoader(
                getActivity(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final String original = mFilePaths.get(position); // original/false by default
        final String newPath = original == null ? mAdapter.getFilePath(position) : null; // flips the original state
        mFilePaths.put(position, newPath);

        final ViewHolder holder = (ViewHolder) view.getTag();
        holder.checkBox.setChecked(newPath != null);
    }

    private static class ViewHolder {
        public ImageView imageView;
        public CheckBox checkBox;
    }

    /**
     * A {@link CursorAdapter} that loads the thumbnails into an {@code ImageView}. <br>
     * The {@code Cursor} must contain the {@link MediaStore.Images.Thumbnails#DATA} column.
     */
    private class PhotosCursorAdapter extends CursorAdapter {

        private final LayoutInflater mInflater;
        private final Picasso mPicasso;

        public PhotosCursorAdapter(Context context) {
            super(context, null, 0);
            mInflater = LayoutInflater.from(context);
            mPicasso = Picasso.with(context);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            final View view = mInflater.inflate(R.layout.layout_selectable_photo_grid, parent, false);

            final ViewHolder holder = new ViewHolder();
            holder.imageView = ViewUtils.findView(view, android.R.id.icon);
            holder.checkBox = ViewUtils.findView(view, android.R.id.checkbox);

            view.setTag(holder);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final ViewHolder holder = (ViewHolder) view.getTag();
            final String path = getTnFilePath(cursor);
            mPicasso.load(new File(path))
                    .resizeDimen(R.dimen.photo_grid_width, R.dimen.photo_grid_height)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.ic_launcher)
                    .into(holder.imageView);

            holder.checkBox.setChecked(mFilePaths.get(cursor.getPosition()) != null);
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

        /**
         * @return the file path from the {@code Cursor}, which must contain the {@link MediaStore.Images.Media#DATA} column
         */
        public String getFilePath(int position) {
            return getFilePath(getItem(position));
        }

        private String getFilePath(Cursor cursor) {
            return cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        }

        private String getTnFilePath(Cursor cursor) {
            return cursor.getString(cursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA));
        }
    }
}
