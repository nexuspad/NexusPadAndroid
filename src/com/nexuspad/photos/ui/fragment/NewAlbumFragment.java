package com.nexuspad.photos.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.edmondapps.utils.android.view.ViewUtils;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.Album;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.datamodel.NPUpload;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.photos.ui.activity.PhotosSelectActivity;
import com.nexuspad.ui.fragment.NewEntryFragment;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.edmondapps.utils.android.view.ViewUtils.findView;

@FragmentName(NewAlbumFragment.TAG)
@ModuleId(moduleId = ServiceConstants.PHOTO_MODULE, template = EntryTemplate.ALBUM)
public class NewAlbumFragment extends NewEntryFragment<Album> {
    public static final String TAG = "NewAlbumFragment";
    private static final String KEY_PATHS = "key_paths";

    public static NewAlbumFragment of(Folder folder) {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_FOLDER, folder);

        final NewAlbumFragment fragment = new NewAlbumFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    private static final int REQ_PICK_IMAGES = 2;

    private final ArrayList<String> mPaths = new ArrayList<String>();
    private PhotosAdapter mAdapter;

    private TextView mNumPhotosV;
    private TextView mTitleV;
    private TextView mFolderV;

    @Override
    public void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);

        b.putStringArrayList(KEY_PATHS, mPaths);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            final List<String> list = savedInstanceState.getStringArrayList(KEY_PATHS);
            addIfAbsent(list);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.album_new_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);

        installFolderSelectorListener(mFolderV);

        final int size = mPaths.size();
        mNumPhotosV.setText(getResources().getQuantityString(R.plurals.numberOfPhotos, size, size));

        mAdapter = new PhotosAdapter(mPaths, getActivity());
        ViewUtils.<GridView>findView(view, R.id.grid_view).setAdapter(mAdapter);

        findView(view, R.id.pick_img).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = PhotosSelectActivity.of(getActivity());
                startActivityForResult(intent, REQ_PICK_IMAGES);
            }
        });
    }

    private void findViews(View parent) {
        mFolderV = findView(parent, R.id.lbl_folder);
        mTitleV = findView(parent, R.id.txt_album_name);
        mNumPhotosV = findView(parent, R.id.lbl_num_photos);
    }

    @Override
    protected void onFolderUpdated(Folder folder) {
        super.onFolderUpdated(folder);
        mFolderV.setText(folder.getFolderName());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_PICK_IMAGES:
                if (resultCode == Activity.RESULT_OK) {
                    final List<String> paths = data.getStringArrayListExtra(PhotosSelectActivity.KEY_FILES_PATHS);
                    addIfAbsent(paths);
                    mAdapter.notifyDataSetChanged();

                    final int size = mPaths.size();
                    mNumPhotosV.setText(getResources().getQuantityString(R.plurals.numberOfPhotos, size, size));
                }
                break;
            default:
                throw new AssertionError("unknown requestCode: " + requestCode);
        }
    }

    private void addIfAbsent(Iterable<String> paths) {
        for (String path : paths) {
            if (!mPaths.contains(path)) {
                mPaths.add(path);
            }
        }
    }

    @Override
    public boolean isEditedEntryValid() {
        return !ViewUtils.isTextEmpty(R.string.err_empty_field, mTitleV);
    }

    @Override
    public Album getEditedEntry() {
        final Album entry = getDetailEntryIfExist();
        final Album album = entry == null ? new Album(getFolder()) : new Album(entry);

        album.setTitle(mTitleV.getText().toString());
        addPathsToAlbum(album);

        setDetailEntry(album);
        return album;
    }

    private void addPathsToAlbum(Album album) {
        final Folder folder = getFolder();
        for (String path : mPaths) {
            final NPUpload npUpload = new NPUpload(folder);
            npUpload.setFileName(path);
            album.addAttachment(npUpload);
        }
    }

    private static class PhotosAdapter extends BaseAdapter {

        private final LayoutInflater mInflater;
        private final Picasso mPicasso;

        private static class ViewHolder {
            public ImageView imageView;
        }

        private final List<? extends String> mFilePaths;

        private PhotosAdapter(List<? extends String> files, Context context) {
            mFilePaths = files;
            mInflater = LayoutInflater.from(context);
            mPicasso = Picasso.with(context);
        }

        @Override
        public int getCount() {
            return mFilePaths.size();
        }

        @Override
        public String getItem(int position) {
            return mFilePaths.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.layout_photo_grid, parent, false);

                holder = new ViewHolder();
                holder.imageView = ViewUtils.findView(convertView, android.R.id.icon);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            mPicasso.load(new File(getItem(position)))
                    .resizeDimen(R.dimen.photo_grid_width, R.dimen.photo_grid_height)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.ic_launcher)
                    .into(holder.imageView);

            return convertView;
        }
    }
}
