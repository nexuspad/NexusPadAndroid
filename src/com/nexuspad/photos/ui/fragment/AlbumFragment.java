package com.nexuspad.photos.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.nexuspad.R;
import com.nexuspad.datamodel.Album;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.datamodel.NPUpload;
import com.nexuspad.datamodel.Photo;
import com.nexuspad.photos.ui.activity.NewAlbumActivity;
import com.nexuspad.photos.ui.activity.PhotoActivity;
import com.nexuspad.ui.fragment.EntryFragment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static com.edmondapps.utils.android.view.ViewUtils.findView;

/**
 * Author: Edmond
 */

@FragmentName(AlbumFragment.TAG)
public class AlbumFragment extends EntryFragment<Album> implements AdapterView.OnItemClickListener {
    public static final String TAG = "AlbumFragment";

    public static AlbumFragment of(Album album, Folder folder) {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_ENTRY, album);
        bundle.putParcelable(KEY_FOLDER, folder);

        final AlbumFragment fragment = new AlbumFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private final PhotosAdapter mPhotosAdapter = new PhotosAdapter();
    private List<NPUpload> mPhotos = new ArrayList<NPUpload>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.album_frag, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit:
                onEdit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected boolean shouldGetDetailEntry() {
        return true;
    }

    private void onEdit() {
        final Intent intent = NewAlbumActivity.of(getActivity(), getFolder(), getEntry());
        startActivity(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.album_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        final GridView gridView = findView(view, R.id.grid_view);
        gridView.setOnItemClickListener(this);
        gridView.setAdapter(mPhotosAdapter);
        gridView.setEmptyView(findView(view, android.R.id.empty));

        findView(view, R.id.edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onEdit();
            }
        });

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void updateUI() {
        final Album album = getEntry();
        if (album != null) {
            final List<NPUpload> attachments = album.getAttachments();
            if (attachments != null) {
                mPhotos.addAll(attachments);
                mPhotosAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Photo photo = new Photo(mPhotosAdapter.getItem(position));
        final ArrayList<Photo> photos = new ArrayList<Photo>(mPhotos.size());
        for (NPUpload npUpload : mPhotos) {
            photos.add(new Photo(npUpload));
        }
        PhotoActivity.startWithFolder(getFolder(), photo, photos, getActivity());
    }

    private class PhotosAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mPhotos.size();
        }

        @Override
        public NPUpload getItem(int position) {
            return mPhotos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Activity activity = getActivity();
            final ImageView view;

            if (convertView == null) {
                final LayoutInflater inflater = activity.getLayoutInflater();
                view = (ImageView) inflater.inflate(R.layout.layout_photo_grid, parent, false);
            } else {
                view = (ImageView) convertView;
            }

            final String tnUrl = getItem(position).getTnUrl();
            Picasso.with(activity)
                    .load(tnUrl)
                    .resizeDimen(R.dimen.photo_grid_width, R.dimen.photo_grid_height)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.ic_launcher)
                    .into(view);

            return view;
        }
    }
}
