package com.nexuspad.photos.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import com.edmondapps.utils.android.Logs;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.edmondapps.utils.android.view.ViewUtils;
import com.edmondapps.utils.java.Lazy;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.Album;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.datamodel.NPUpload;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.dataservice.UploadService;
import com.nexuspad.photos.ui.activity.PhotosSelectActivity;
import com.nexuspad.ui.activity.NewEntryActivity;
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
        return NewAlbumFragment.of(null, folder);
    }

    public static NewAlbumFragment of(Album album, Folder folder) {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_ENTRY, album);
        bundle.putParcelable(KEY_FOLDER, folder);

        final NewAlbumFragment fragment = new NewAlbumFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    // 1 is used by REQ_FOLDER (NewEntryFragment)
    private static final int REQ_PICK_IMAGES = 2;

    private final Lazy<UploadService> mUploadService = new Lazy<UploadService>() {
        @Override
        protected UploadService onCreate() {
            return new UploadService(getActivity());
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
                return R.layout.album_new_frag;
            case EDIT:
                return R.layout.album_edit_frag;
            default:
                throw new AssertionError("unexpected mode: " + getMode());
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);

        installFolderSelectorListener(mFolderV);

        if (NewEntryActivity.Mode.EDIT.equals(getMode())) {
            final int size = mUris.size();
            mNumPhotosV.setText(getResources().getQuantityString(R.plurals.numberOfPhotos, size, size));

            mAdapter = new PhotosAdapter(mUris, getActivity());
            ViewUtils.<GridView>findView(view, R.id.grid_view).setAdapter(mAdapter);

            findView(view, R.id.pick_img).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Intent intent = PhotosSelectActivity.of(getActivity());
                    startActivityForResult(intent, REQ_PICK_IMAGES);
                }
            });
        }

        updateUI();
    }

    private void findViews(View parent) {
        mFolderV = findView(parent, R.id.lbl_folder);
        mTitleV = findView(parent, R.id.txt_album_name);
        mNumPhotosV = findView(parent, R.id.lbl_num_photos);
    }

    @Override
    protected void onFolderUpdated(Folder folder) {
        super.onFolderUpdated(folder);
        updateUI();
    }

    private void updateUI() {
        mFolderV.setText(getFolder().getFolderName());
        final Album album = getDetailEntryIfExist();
        if (album != null) {
            mTitleV.setText(album.getTitle());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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
                throw new AssertionError("unknown requestCode: " + requestCode);
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

    @Override
    protected void onAddEntry(Album entry) {
        super.onAddEntry(entry);
        final List<NPUpload> attachments = entry.getAttachments();
        if (!attachments.isEmpty()) {
            Logs.e(TAG, "attachments are not uploaded (entry not created yet): " + attachments);
        }
    }

    @Override
    protected void onUpdateEntry(Album entry) {
        super.onUpdateEntry(entry);
        // upload the photos right the way because we already know the entry ID
        mUploadService.get().uploadAllAttachments(entry);
    }

    private void addPathsToAlbum(Album album) {
        final Folder folder = getFolder();
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
                holder.imageView = ViewUtils.findView(convertView, android.R.id.icon);

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
