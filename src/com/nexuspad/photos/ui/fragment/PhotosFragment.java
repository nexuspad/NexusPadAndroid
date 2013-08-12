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
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.edmondapps.utils.android.view.RunnableAnimatorListener;
import com.edmondapps.utils.java.WrapperList;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.*;
import com.nexuspad.dataservice.NPService;
import com.nexuspad.photos.ui.activity.PhotoActivity;
import com.nexuspad.photos.ui.activity.PhotosActivity;
import com.nexuspad.ui.DirectionalScrollListener;
import com.nexuspad.ui.OnListEndListener;
import com.nexuspad.ui.activity.FoldersActivity;
import com.nexuspad.ui.fragment.EntriesFragment;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.squareup.picasso.Picasso;

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

    private View mQuickReturnView;
    private TextView mFolderView;
    // Parcelable
    private ArrayList<Photo> mPhotos;

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
            Photo photo = (Photo) entry;
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
                    activity.finish();
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
        mQuickReturnView = findView(view, R.id.sticky);
        mFolderView = findView(view, R.id.lbl_folder);

        mGridView.setOnItemClickListener(this);
        mFolderView.setText(getFolder().getFolderName());
        mFolderView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FragmentActivity activity = getActivity();
                final Intent intent = FoldersActivity.ofParentFolder(activity, Folder.rootFolderOf(PHOTO_MODULE));
                startActivityForResult(intent, REQ_FOLDER);
                activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        super.onViewCreated(view, savedInstanceState);
    }

    private void stableNotifyAdapter(BaseAdapter adapter) {
        final int prevPos = mGridView.getFirstVisiblePosition();
        adapter.notifyDataSetChanged();
        mGridView.setSelection(prevPos);
    }

    @Override
    protected void onListLoaded(EntryList list) {
        super.onListLoaded(list);

        mPhotos = new ArrayList<Photo>(new WrapperList<Photo>(list.getEntries()));

        BaseAdapter adapter = (BaseAdapter) mGridView.getAdapter();
        if (adapter != null) {
            stableNotifyAdapter(adapter);
        } else {
            mGridView.setOnScrollListener(new DirectionalScrollListener(0, new OnListEndListener() {
                @Override
                protected void onListEnd(int page) {
                    queryEntriesAync(getCurrentPage() + 1);
                }
            }) {
                @Override
                public void onScrollDirectionChanged(final boolean showing) {
                    final int height = showing ? 0 : mQuickReturnView.getHeight();
                    ViewPropertyAnimator.animate(mQuickReturnView)
                            .translationY(height)
                            .setDuration(200L)
                            .setListener(new RunnableAnimatorListener(true).withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    mFolderView.setClickable(showing);
                                    mFolderView.setFocusable(showing);
                                }
                            }));
                }
            });
            mGridView.setAdapter(new PhotosAdapter());
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PhotosAdapter adapter = (PhotosAdapter) mGridView.getAdapter();
        Photo photo = adapter.getItem(position);

        PhotoActivity.startWithFolder(getFolder(), photo, mPhotos, getActivity());
    }

    private class PhotosAdapter extends BaseAdapter {

        private Picasso mPicasso = Picasso.with(getActivity());

        @Override
        public int getCount() {
            return mPhotos.size();
        }

        @Override
        public Photo getItem(int position) {
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
                LayoutInflater inflater = activity.getLayoutInflater();
                view = (ImageView) inflater.inflate(R.layout.layout_photo_grid, parent, false);
            } else {
                view = (ImageView) convertView;
            }

            final String url = NPService.addAuthToken(getItem(position).getTnUrl());
            mPicasso.load(url)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.ic_launcher)
                    .resizeDimen(R.dimen.photo_grid_width, R.dimen.photo_grid_height)
                    .centerCrop()
                    .into(view);

            return view;
        }
    }
}
