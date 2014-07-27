package com.nexuspad.photo.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.common.activity.UpdateEntryActivity;
import com.nexuspad.common.activity.UploadCenterActivity;
import com.nexuspad.common.annotaion.FragmentName;
import com.nexuspad.common.fragment.UpdateEntryFragment;
import com.nexuspad.common.utils.Lazy;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.NPAlbum;
import com.nexuspad.datamodel.NPFolder;
import com.nexuspad.datamodel.NPUpload;
import com.nexuspad.dataservice.EntryUploadService;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.photo.activity.PhotosSelectActivity;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@FragmentName(NewAlbumFragment.TAG)
@ModuleId(moduleId = ServiceConstants.PHOTO_MODULE, template = EntryTemplate.ALBUM)
public class NewAlbumFragment extends UpdateEntryFragment<NPAlbum> {
    public static final String TAG = "NewAlbumFragment";
    private static final String KEY_PATHS = "key_paths";

    public static NewAlbumFragment of(NPFolder folder) {
        return NewAlbumFragment.of(null, folder);
    }

    public static NewAlbumFragment of(NPAlbum album, NPFolder folder) {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_ENTRY, album);
        bundle.putParcelable(KEY_FOLDER, folder);

        final NewAlbumFragment fragment = new NewAlbumFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    private static final int REQ_PICK_IMAGES = REQ_SUBCLASSES + 1;

    private final Lazy<EntryUploadService> mUploadService = new Lazy<EntryUploadService>() {
        @Override
        protected EntryUploadService onCreate() {
            return new EntryUploadService(getActivity());
        }
    };
    private final ArrayList<Uri> mUris = new ArrayList<Uri>();
    private PhotosAdapter mAdapter;

    private TextView mNumPhotosV;
    private TextView mTitleV;
    private TextView mFolderV;

    @Override
    public void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);

        b.putParcelableArrayList(KEY_PATHS, mUris);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            final List<Uri> list = savedInstanceState.getParcelableArrayList(KEY_PATHS);
            addIfAbsent(list);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(getLayoutId(), container, false);
    }

    private int getLayoutId() {
        switch (getMode()) {
            case NEW:
                return R.layout.album_edit_frag;
            case EDIT:
                return R.layout.album_edit_frag;
            default:
                throw new AssertionError("unexpected mode: " + getMode());
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        findViews(view);

        installFolderSelectorListener(mFolderV);

        if (UpdateEntryActivity.Mode.EDIT.equals(getMode())) {
            final int size = mUris.size();
            mNumPhotosV.setText(getResources().getQuantityString(R.plurals.numberOfPhotos, size, size));

            mAdapter = new PhotosAdapter(mUris, getActivity());

	        GridView photosGridView = (GridView)view.findViewById(R.id.grid_view);
            photosGridView.setAdapter(mAdapter);

	        View imagePicker = view.findViewById(R.id.pick_img);
            imagePicker.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View v) {
		            final Intent intent = PhotosSelectActivity.of(getActivity());
		            startActivityForResult(intent, REQ_PICK_IMAGES);
	            }
            });
        }

        super.onViewCreated(view, savedInstanceState);
    }

    private void findViews(View parent) {
        mFolderV = (TextView)parent.findViewById(R.id.lbl_folder);
        mTitleV = (TextView)parent.findViewById(R.id.txt_album_name);
        mNumPhotosV = (TextView)parent.findViewById(R.id.lbl_num_photos);
    }

    @Override
    protected void updateUI() {
        mFolderV.setText(getFolder().getFolderName());
        final NPAlbum album = getEntry();
        if (album != null) {
            mTitleV.setText(album.getTitle());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQ_PICK_IMAGES:
                if (resultCode == Activity.RESULT_OK) {
                    final List<Uri> paths = data.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                    addIfAbsent(paths);
                    mAdapter.notifyDataSetChanged();

                    final int size = mUris.size();
                    mNumPhotosV.setText(getResources().getQuantityString(R.plurals.numberOfPhotos, size, size));
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void addIfAbsent(Iterable<Uri> uris) {
        for (Uri uri : uris) {
            if (!mUris.contains(uri)) {
                mUris.add(uri);
            }
        }
    }

    @Override
    public boolean isEditedEntryValid() {
	    return true;
    }

    @Override
    public NPAlbum getEditedEntry() {
        final NPAlbum entry = getEntry();
        final NPAlbum album = entry == null ? new NPAlbum(getFolder()) : new NPAlbum(entry);

        album.setTitle(mTitleV.getText().toString());
        addPathsToAlbum(album);

        setEntry(album);
        return album;
    }

    @Override
    protected void onAddEntry(NPAlbum entry) {
        super.onAddEntry(entry);
        final List<NPUpload> attachments = entry.getAttachments();
        if (!attachments.isEmpty()) {
            Log.e(TAG, "attachments are not uploaded (entry not created yet): " + attachments);
        }
    }

    @Override
    protected void onUpdateEntry(NPAlbum entry) {
        super.onUpdateEntry(entry);
        UploadCenterActivity.startWith(mUris, entry, getActivity());
    }

    private void addPathsToAlbum(NPAlbum album) {
        final NPFolder folder = getFolder();
        for (Uri uri : mUris) {
            final NPUpload npUpload = new NPUpload(folder);
            npUpload.setFileName(uri.getPath());
            album.addAttachment(npUpload);
        }
    }

    private static class PhotosAdapter extends BaseAdapter {

        private final LayoutInflater mInflater;
        private final Picasso mPicasso;

        private static class ViewHolder {
            public ImageView imageView;
        }

        private final List<? extends Uri> mUris;

        private PhotosAdapter(List<? extends Uri> uris, Context context) {
            mUris = uris;
            mInflater = LayoutInflater.from(context);
            mPicasso = Picasso.with(context);
        }

        @Override
        public int getCount() {
            return mUris.size();
        }

        @Override
        public Uri getItem(int position) {
            return mUris.get(position);
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
                holder.imageView = (ImageView)convertView.findViewById(android.R.id.icon);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            mPicasso.load(new File(getItem(position).getPath()))
                    .resizeDimen(R.dimen.photo_grid_width, R.dimen.photo_grid_height)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.ic_launcher)
                    .into(holder.imageView);

            return convertView;
        }
    }
}
