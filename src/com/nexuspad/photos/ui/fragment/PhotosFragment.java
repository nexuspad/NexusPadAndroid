/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.photos.ui.fragment;

import static com.edmondapps.utils.android.view.ViewUtils.findView;
import static com.nexuspad.dataservice.ServiceConstants.PHOTO_MODULE;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.edmondapps.utils.android.Logs;
import com.edmondapps.utils.android.animation.ViewAnimations;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.edmondapps.utils.android.service.FileUploadService;
import com.edmondapps.utils.java.WrapperList;
import com.ipaulpro.afilechooser.FileChooserActivity;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.EntryList;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.datamodel.Photo;
import com.nexuspad.dataservice.NPService;
import com.nexuspad.dataservice.UploadService;
import com.nexuspad.photos.service.PhotosService;
import com.nexuspad.photos.ui.activity.PhotoActivity;
import com.nexuspad.photos.ui.activity.PhotosUploadActivity;
import com.nexuspad.photos.ui.fragment.PhotoFragment.BitmapInfo;
import com.nexuspad.ui.OnListEndListener;
import com.nexuspad.ui.fragment.EntriesFragment;

/**
 * @author Edmond
 * 
 */
@FragmentName(PhotosFragment.TAG)
@ModuleId(moduleId = PHOTO_MODULE, template = EntryTemplate.PHOTO)
public class PhotosFragment extends EntriesFragment implements OnItemClickListener {
    public static final String TAG = "PhotosFragment";

    private static final int REQ_CHOOSE_FILE = 1;

    private GridView mGridView;

    private PhotosService mPhotosService;

    public static PhotosFragment of(Folder f) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_FOLDER, f);

        PhotosFragment fragment = new PhotosFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPhotosService = PhotosService.getInstance(getActivity());
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.photos_frag, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_photos:
                // temporary
                Intent intent = new Intent(getActivity(), FileChooserActivity.class);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, REQ_CHOOSE_FILE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CHOOSE_FILE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = data.getData();
                    uploadFile(uri);
                }
                break;
            default:
                throw new AssertionError("unknown requestCode: " + requestCode);
        }
    }

    // temporary
    @Deprecated
    private void uploadFile(Uri uri) {
        PhotosUploadActivity.startWith(uri, getFolder(), getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.photos_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mGridView = findView(view, R.id.grid_view);
        mGridView.setOnItemClickListener(this);
        mGridView.setOnScrollListener(new OnListEndListener() {
            @Override
            protected void onListEnd(int page) {
                queryEntriesAync(getCurrentPage() + 1);
            }
        });

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onListLoaded(EntryList list) {
        super.onListLoaded(list);

        List<Photo> photos = new WrapperList<Photo>(list.getEntries());
        mPhotosService.addPhotos(photos);

        BaseAdapter adapter = (BaseAdapter)mGridView.getAdapter();
        if (adapter != null) {
            final int prevPos = mGridView.getFirstVisiblePosition();
            adapter.notifyDataSetChanged();
            mGridView.setSelection(prevPos);
            return;
        }

        mGridView.setAdapter(new PhotosAdapter(photos));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PhotosAdapter adapter = (PhotosAdapter)mGridView.getAdapter();
        Photo photo = adapter.getItem(position);

        mPhotosService.setActiveBitmapInfo(new BitmapInfo(photo, getViewLocationOnScreen(view), view.getWidth(), view.getHeight()));
        PhotoActivity.startWithFolder(getFolder(), getActivity());
    }

    private static int[] getViewLocationOnScreen(View v) {
        int[] p = new int[2];
        v.getLocationOnScreen(p);
        return p;
    }

    @Override
    protected void onNewFolder(Context c, Intent i, Folder f) {
        throw new UnsupportedOperationException();
    }

    private class PhotosAdapter extends BaseAdapter {

        private final List<? extends Photo> mList;

        private PhotosAdapter(List<? extends Photo> list) {
            mList = list;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Photo getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Activity activity = getActivity();
            final ImageView view;

            if (convertView == null) {
                LayoutInflater inflater = activity.getLayoutInflater();
                view = (ImageView)inflater.inflate(R.layout.layout_photo_grid, parent, false);
            } else {
                view = (ImageView)convertView;
            }

            ImageContainer container = (ImageContainer)view.getTag();
            if (container != null) {
                container.cancelRequest();
            }

            int maxSide = mPhotosService.getThumbnailMaxSide();

            String url = NPService.addAuthToken(getItem(position).getTnUrl());
            container = mPhotosService.getImageLoader().get(url, new ImageListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    view.setImageResource(R.drawable.ic_launcher);
                }

                @Override
                public void onResponse(ImageContainer response, boolean isImmediate) {
                    Bitmap bitmap = response.getBitmap();
                    if (bitmap != null) {
                        if (!isImmediate) {
                            ViewAnimations.fadeInBitmap(view, bitmap, 300);
                        } else {
                            view.setImageBitmap(bitmap);
                        }
                    } else {
                        view.setImageResource(android.R.drawable.ic_menu_slideshow);
                    }
                }
            }, maxSide, maxSide);
            view.setTag(container);

            return view;
        }
    }
}
