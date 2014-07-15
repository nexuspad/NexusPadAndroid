package com.nexuspad.photos.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.*;
import android.widget.AdapterView;
import android.widget.GridView;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.nexuspad.R;
import com.nexuspad.datamodel.*;
import com.nexuspad.dataservice.EntryListService;
import com.nexuspad.photos.ui.PhotosAdapter;
import com.nexuspad.photos.ui.activity.NewAlbumActivity;
import com.nexuspad.photos.ui.activity.PhotoActivity;
import com.nexuspad.ui.fragment.EntryFragment;

import java.util.ArrayList;
import java.util.List;

import static com.edmondapps.utils.android.view.ViewUtils.findView;

/**
 * Author: Edmond
 */

@FragmentName(AlbumFragment.TAG)
public class AlbumFragment extends EntryFragment<Album> implements AdapterView.OnItemClickListener {
    public static final String TAG = "AlbumFragment";
    private GridView mGridView;

    public static AlbumFragment of(Album album, Folder folder) {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_ENTRY, album);
        bundle.putParcelable(KEY_FOLDER, folder);

        final AlbumFragment fragment = new AlbumFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private PhotosAdapter mPhotosAdapter;
    private ArrayList<Photo> mPhotos = new ArrayList<Photo>();  // ArrayList for Parcelling

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
        mGridView = findView(view, R.id.grid_view);
        mGridView.setOnItemClickListener(this);

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void updateUI() {
        final Album album = getEntry();
        if (album != null) {
            final List<NPUpload> attachments = album.getAttachments();
            if (attachments != null) {
                for (NPUpload npUpload : attachments) {
                    mPhotos.add(new Photo(npUpload));
                }

                if (mPhotosAdapter == null) {
                    mPhotosAdapter = newPhotosAdapter();
                    mGridView.setAdapter(mPhotosAdapter);
                } else {
                    mPhotosAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Photo photo = mPhotosAdapter.getItem(position);
        PhotoActivity.startWithFolder(getFolder(), photo, mPhotos, getActivity());
    }

    private PhotosAdapter newPhotosAdapter() {
        final FragmentActivity a = getActivity();
        return new PhotosAdapter(a, mPhotos, getFolder(), EntryListService.getInstance(a), EntryTemplate.PHOTO);
    }
}
