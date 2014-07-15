/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.photos.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.edmondapps.utils.java.WrapperList;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.*;
import com.nexuspad.photos.ui.PhotosAdapter;
import com.nexuspad.photos.ui.activity.PhotoActivity;
import com.nexuspad.photos.ui.activity.PhotosActivity;
import com.nexuspad.ui.OnListEndListener;
import com.nexuspad.ui.activity.FoldersActivity;
import com.nexuspad.ui.fragment.EntriesFragment;

import java.util.ArrayList;

import static com.edmondapps.utils.android.view.ViewUtils.findView;
import static com.nexuspad.dataservice.ServiceConstants.PHOTO_MODULE;

/**
 * @author Edmond
 */
@FragmentName(PhotosFragment.TAG)
@ModuleId(moduleId = PHOTO_MODULE, template = EntryTemplate.PHOTO)
public class PhotosFragment extends EntriesFragment implements OnItemClickListener {
    public static final String TAG = "PhotosFragment";

    private static final int REQ_FOLDER = 1;

    private GridView mGridView;

    // Parcelable
    private ArrayList<Photo> mPhotos = new ArrayList<Photo>();

    public static PhotosFragment of(Folder f) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_FOLDER, f);

        PhotosFragment fragment = new PhotosFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void onNewEntry(NPEntry entry) {
        if (entry instanceof Photo) {  // or album, which will be handled at AlbumsFragment
            final Photo photo = (Photo) entry;
            mPhotos.add(photo);
        }
        super.onNewEntry(entry);
    }

    @Override
    protected void onDeleteEntry(NPEntry entry) {
        if (entry instanceof Photo) {
            final Photo photo = (Photo) entry;
            mPhotos.remove(photo);
        }
        super.onDeleteEntry(entry);
    }

    @Override
    protected void onEntryListUpdated() {
        super.onEntryListUpdated();
        BaseAdapter adapter = (BaseAdapter) mGridView.getAdapter();
        stableNotifyAdapter(adapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_FOLDER:
                if (resultCode == Activity.RESULT_OK) {
                    final FragmentActivity activity = getActivity();
                    final Folder folder = data.getParcelableExtra(FoldersActivity.KEY_FOLDER);
                    PhotosActivity.startWithFolder(folder, activity);
                    activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
                break;
            default:
                throw new AssertionError("unknown requestCode: " + requestCode);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.photos_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mGridView = findView(view, R.id.grid_view);

        setQuickReturnListener(mGridView, new OnListEndListener() {
            @Override
            protected void onListEnd(int page) {
                queryEntriesAsync(getCurrentPage() + 1);
            }
        });
        mGridView.setOnItemClickListener(this);
        mGridView.setAdapter(newPhotosAdapter());

        super.onViewCreated(view, savedInstanceState);

        setOnFolderSelectedClickListener(REQ_FOLDER);
    }

    private PhotosAdapter newPhotosAdapter() {
        return new PhotosAdapter(getActivity(), mPhotos, getFolder(), getEntryListService(), getTemplate());
    }

    private void stableNotifyAdapter(BaseAdapter adapter) {
        final int prevPos = mGridView.getFirstVisiblePosition();
        adapter.notifyDataSetChanged();
        mGridView.setSelection(prevPos);
    }

    @Override
    protected void onListLoaded(EntryList list) {
        super.onListLoaded(list);

        mPhotos.clear();
        mPhotos.addAll(new WrapperList<Photo>(list.getEntries()));

        final BaseAdapter adapter = (BaseAdapter) mGridView.getAdapter();
        stableNotifyAdapter(adapter);

        fadeInListFrame();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PhotosAdapter adapter = (PhotosAdapter) mGridView.getAdapter();
        Photo photo = adapter.getItem(position);

        PhotoActivity.startWithFolder(getFolder(), photo, mPhotos, getActivity());
    }


}
