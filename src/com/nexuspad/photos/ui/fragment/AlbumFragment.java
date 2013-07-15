package com.nexuspad.photos.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.nexuspad.dataservice.NPService;
import com.nexuspad.photos.service.PhotosService;
import com.nexuspad.photos.ui.activity.PhotoActivity;
import com.nexuspad.photos.ui.activity.PhotosActivity;
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

    private GridView mGridView;
    private List<NPUpload> mPhotos;
    private PhotosAdapter mPhotosAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.photos_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mGridView = findView(view, R.id.grid_view);
        mGridView.setOnItemClickListener(this);

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void onEntryUpdated(Album entry) {
        super.onEntryUpdated(entry);
        updateUI();
    }

    @Override
    protected void onDetailEntryUpdated(Album entry) {
        super.onDetailEntryUpdated(entry);
        updateUI();
    }

    private void updateUI() {
        final Album album = getDetailEntry();
        if (album != null) {
            final List<NPUpload> attachments = album.getAttachments();
            if (attachments != null) {
                mPhotos = new ArrayList<NPUpload>(attachments);
                if (mPhotosAdapter == null) {
                    mPhotosAdapter = new PhotosAdapter();
                    mGridView.setAdapter(mPhotosAdapter);
                } else {
                    mPhotosAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PhotosService.getInstance(getActivity()).addPhotosFromAttachment(mPhotos);
        PhotoActivity.startWithFolder(getFolder(), getActivity());
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

            final String tnUrl = NPService.addAuthToken(getItem(position).getTnUrl());
            Picasso.with(activity)
                    .load(tnUrl)
                    .resizeDimen(R.dimen.photo_grid_width, R.dimen.photo_grid_height)
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_menu_slideshow)
                    .error(R.drawable.ic_launcher)
                    .into(view);

            return view;
        }
    }
}
