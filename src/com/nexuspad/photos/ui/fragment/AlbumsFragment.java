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
import com.edmondapps.utils.java.WrapperList;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.Album;
import com.nexuspad.datamodel.EntryList;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.photos.ui.activity.AlbumActivity;
import com.nexuspad.photos.ui.activity.PhotosActivity;
import com.nexuspad.ui.EntriesAdapter;
import com.nexuspad.ui.activity.FoldersActivity;
import com.nexuspad.ui.fragment.EntriesFragment;
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

        setQuickReturnListener(getListViewManager(), null);
        setOnFolderSelectedClickListener(REQ_FOLDER);

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

    private class AlbumsAdapter extends EntriesAdapter<Album> {

        public AlbumsAdapter() {
            super(getActivity(), mAlbums, getFolder(), getEntryListService(), getTemplate());
        }

        @Override
        protected View getEntryView(Album entry, int position, View convertView, ViewGroup parent) {
            final AlbumsFragment.ViewHolder holder;
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_album, parent, false);

                holder = new AlbumsFragment.ViewHolder();
                holder.thumbnail = findView(convertView, android.R.id.icon);
                holder.title = findView(convertView, android.R.id.text1);

                convertView.setTag(holder);
            } else {
                holder = (AlbumsFragment.ViewHolder) convertView.getTag();
            }

            final Album album = getItem(position);
            holder.title.setText(album.getTitle());
            final String tnUrl = album.getTnUrl();
            if (!TextUtils.isEmpty(tnUrl)) {

                Picasso.with(getActivity())
                        .load(tnUrl)
                        .resizeDimen(R.dimen.photo_grid_width, R.dimen.photo_grid_height)
                        .centerCrop()
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.ic_launcher)
                        .into(holder.thumbnail);
            } else {
                holder.thumbnail.setImageResource(R.drawable.placeholder);
            }

            return convertView;
        }

        @Override
        protected View getEmptyEntryView(LayoutInflater i, View c, ViewGroup p) {
            return getCaptionView(i, c, p, R.string.empty_photos, R.drawable.empty_folder);
        }

        @Override
        protected String getEntriesHeaderText() {
            return null;
        }
    }
}
