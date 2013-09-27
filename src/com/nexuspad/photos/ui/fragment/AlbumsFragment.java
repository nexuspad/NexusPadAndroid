/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.photos.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.edmondapps.utils.android.view.RunnableAnimatorListener;
import com.edmondapps.utils.java.WrapperList;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.Album;
import com.nexuspad.datamodel.EntryList;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.dataservice.NPException;
import com.nexuspad.dataservice.NPWebServiceUtil;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.photos.ui.activity.AlbumActivity;
import com.nexuspad.photos.ui.activity.PhotosActivity;
import com.nexuspad.ui.DirectionalScrollListener;
import com.nexuspad.ui.activity.FoldersActivity;
import com.nexuspad.ui.fragment.EntriesFragment;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.squareup.picasso.Picasso;

import java.util.List;

import static com.edmondapps.utils.android.view.ViewUtils.findView;

/**
 * @author Edmond
 */
@FragmentName(AlbumsFragment.TAG)
@ModuleId(moduleId = ServiceConstants.PHOTO_MODULE, template = EntryTemplate.ALBUM)
public class AlbumsFragment extends EntriesFragment {
    public static final String TAG = "AlbumsFragment";

    public static AlbumsFragment of(Folder folder) {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_FOLDER, folder);

        final AlbumsFragment fragment = new AlbumsFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private static final int REQ_FOLDER = 1;

    private List<Album> mAlbums;

    private View mQuickReturnView;
    private TextView mFolderView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.albums_frag, container, false);
    }

    @Override
    protected void onListLoaded(EntryList list) {
        super.onListLoaded(list);
        mAlbums = new WrapperList<Album>(list.getEntries());
        updateUI();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mQuickReturnView = findView(view, R.id.sticky);
        mFolderView = findView(view, R.id.lbl_folder);
        mFolderView.setText(getFolder().getFolderName());
        mFolderView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FragmentActivity activity = getActivity();
                final Folder folder = Folder.rootFolderOf(ServiceConstants.PHOTO_MODULE, activity);
                startActivityForResult(FoldersActivity.ofParentFolder(activity, folder), REQ_FOLDER);
                activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        getListView().setOnScrollListener(new DirectionalScrollListener(0) {
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
        updateUI();
    }

    private void updateUI() {
        if (mAlbums != null) {
            final BaseAdapter adapter = getListAdapter();
            if (adapter == null) {
                setListAdapter(new AlbumsAdapter());
            } else {
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_FOLDER:
                if (resultCode == Activity.RESULT_OK) {
                    final FragmentActivity activity = getActivity();
                    final Folder folder = data.getParcelableExtra(FoldersActivity.KEY_FOLDER);
                    PhotosActivity.startWithFolderAndIndex(folder, activity, 1);
                    activity.finish();
                    activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
                break;
            default:
                throw new AssertionError("unexpected requestCode: " + requestCode);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        final Album album = ((AlbumsAdapter) getListAdapter()).getItem(position);
        AlbumActivity.startWith(album, getFolder(), getActivity());
    }

    private static class ViewHolder {
        ImageView thumbnail;
        TextView title;
    }

    private class AlbumsAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mAlbums.size();
        }

        @Override
        public Album getItem(int position) {
            return mAlbums.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_album, parent, false);

                holder = new ViewHolder();
                holder.thumbnail = findView(convertView, android.R.id.icon);
                holder.title = findView(convertView, android.R.id.text1);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final Album album = getItem(position);
            holder.title.setText(album.getTitle());
            final String tnUrl = album.getTnUrl();
            if (!TextUtils.isEmpty(tnUrl)) {

                try {
                    final String url = NPWebServiceUtil.fullUrlWithAuthenticationTokens(tnUrl, getActivity());

                    Picasso.with(getActivity())
                            .load(url)
                            .resizeDimen(R.dimen.photo_grid_width, R.dimen.photo_grid_height)
                            .centerCrop()
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.ic_launcher)
                            .into(holder.thumbnail);

                } catch (NPException e) {
                    // TODO handle error
                }

            } else {
                holder.thumbnail.setImageResource(R.drawable.placeholder);
            }

            return convertView;
        }
    }

}
