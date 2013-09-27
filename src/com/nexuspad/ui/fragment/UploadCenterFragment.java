package com.nexuspad.ui.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.edmondapps.utils.android.ui.CompoundAdapter;
import com.edmondapps.utils.android.view.PopupMenu;
import com.nexuspad.R;
import com.nexuspad.app.Request;
import com.nexuspad.app.service.UploadService;
import com.nexuspad.dataservice.FileUploadService;
import com.nexuspad.datamodel.EntryTemplate;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.edmondapps.utils.android.view.ViewUtils.findView;

/**
 * Author: edmond
 */
@FragmentName(UploadCenterFragment.TAG)
public class UploadCenterFragment extends ListFragment {
    public static final String TAG = "UploadCenterFragment";

    private final UploadService.OnNewRequestListener mOnNewRequestListener = new UploadService.OnNewRequestListener() {
        @Override
        public void onNewRequest(Request request) {
            addRequestToList(request);
            updateUI();
        }
    };
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (UploadService.UploadBinder) service;

            mBinder.addRequests(mPendingRequests);
            mPendingRequests.clear();

            addAllRequestsToList(mBinder.peekRequests());

            mBinder.addCallback(mOnNewRequestListener);
            updateUI();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBinder = null;
        }
    };
    private final List<Request> mAlbumRequests = new ArrayList<Request>();
    private final List<Request> mPhotoRequests = new ArrayList<Request>();
    private final Set<Request> mPendingRequests = new HashSet<Request>();
    private UploadAdapter mAdapter;
    private UploadService.UploadBinder mBinder;
    private boolean mViewCreated;

    private void addAllRequestsToList(Iterable<Request> requests) {
        for (Request request : requests) {
            addRequestToList(request);
        }
    }

    private void addRequestToList(Request request) {
        switch (request.getTarget()) {
            case FOLDER:
                mPhotoRequests.add(request);
                break;
            case ENTRY:
                if (EntryTemplate.ALBUM.equals(request.getNPEntry().getTemplate())) {
                    mAlbumRequests.add(request);
                }
                break;
            default:
                throw new AssertionError("unexpected request target: " + request.getTarget());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new UploadAdapter(getActivity(), mAlbumRequests, mPhotoRequests);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_content, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewCreated = true;
        setListAdapter(mAdapter);
        updateUI();
    }

    private void updateUI() {
        if (mViewCreated) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final FragmentActivity activity = getActivity();
        final Intent service = new Intent(activity, UploadService.class);
        activity.startService(service);
        activity.bindService(service, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mBinder != null) {
            mBinder.removeCallback(mOnNewRequestListener);
        }
        getActivity().unbindService(mConnection);
    }

    public void addRequest(Request request) {
        if (mBinder == null) {
            mPendingRequests.add(request);
        } else {
            mBinder.addRequest(request);
        }
    }

    private static class ViewHolder {
        private ImageView icon;
        private TextView title;
        private TextView subTitle;
        private ProgressBar progressbar;
        private ImageButton menu;
        private boolean cancelled;
        private FileUploadService.Callback callback = new FileUploadService.Callback() {
            @Override
            public boolean onProgress(long progress, long total) {
                progressbar.setMax((int) total);
                progressbar.setProgress((int) progress);
                int percent = (int) ((double) progress / total * 100);
                subTitle.setText(subTitle.getResources().getString(R.string.formatted_progress, percent, progress, total));
                return !cancelled;
            }

            @Override
            public void onDone(boolean success) {
                progressbar.setVisibility(View.GONE);
                final String rawString = subTitle.getResources()
                        .getString(success ? R.string.upload_completed : R.string.upload_failed);
                final String color = success ? "green" : "red";
                subTitle.setText(Html.fromHtml("<b><font color='" + color + "'>" + rawString + "</font></b>"));
            }
        };
    }

    private static class UploadAdapter extends CompoundAdapter {
        public UploadAdapter(Context c, List<Request> albumRequests, List<Request> photoRequests) {
            super(new RequestAdapter(c, albumRequests, R.string.albums),
                    new RequestAdapter(c, photoRequests, R.string.photos));
        }
    }

    private static class RequestAdapter extends BaseAdapter {
        private static final int VIEW_TYPE_HEADER = 0;
        private static final int VIEW_TYPE_REQUEST = 1;

        private final Context mContext;
        private final List<Request> mRequests;
        private final int mHeaderRes;

        private RequestAdapter(Context context, List<Request> requests, int headerRes) {
            mContext = context;
            mRequests = requests;
            mHeaderRes = headerRes;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) return VIEW_TYPE_HEADER;
            return VIEW_TYPE_REQUEST;
        }

        @Override
        public boolean isEmpty() {
            return mRequests.isEmpty();
        }

        @Override
        public int getCount() {
            return mRequests.size() + 1;
        }

        @Override
        public Request getItem(int position) {
            return mRequests.get(position - 1);
        }

        @Override
        public long getItemId(int position) {
            switch (getItemViewType(position)) {
                case VIEW_TYPE_HEADER:
                    return -1;
                case VIEW_TYPE_REQUEST:
                    return getItem(position).getTimeStamp();
                default:
                    throw new AssertionError("unexpected view type at pos: " + position);
            }
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            switch (getItemViewType(position)) {
                case VIEW_TYPE_HEADER:
                    return getHeaderView(convertView, parent);
                case VIEW_TYPE_REQUEST:
                    return getPhotoRequestView(position, convertView, parent);
                default:
                    throw new AssertionError("unexpected view type at pos: " + position);
            }
        }

        private View getHeaderView(View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.list_header, parent, false);

                holder = new ViewHolder();
                holder.title = findView(convertView, android.R.id.text1);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.title.setText(mHeaderRes);
            return convertView;
        }

        private View getPhotoRequestView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_upload_photo, parent, false);

                holder = new ViewHolder();
                holder.icon = findView(convertView, android.R.id.icon);
                holder.title = findView(convertView, android.R.id.text1);
                holder.subTitle = findView(convertView, android.R.id.text2);
                holder.progressbar = findView(convertView, android.R.id.progress);
                holder.menu = findView(convertView, R.id.menu);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final Request request = getItem(position);
            request.setCallback(holder.callback);

            Picasso.with(mContext)
                    .load(request.getUri())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.ic_launcher)
                    .resizeDimen(R.dimen.photo_grid_width, R.dimen.photo_grid_height)
                    .centerCrop()
                    .into(holder.icon);

            holder.title.setText(request.getFile(mContext).getName());
            holder.menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final PopupMenu popupMenu = PopupMenu.newInstance(mContext, v);
                    popupMenu.inflate(R.menu.photos_upload);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(int menuId) {
                            switch (menuId) {
                                case R.id.cancel:
                                    holder.cancelled = true;
                                    return true;
                                default:
                                    throw new AssertionError("unexpected menuId: " + menuId);
                            }
                        }
                    });
                    popupMenu.show();
                }
            });

            return convertView;
        }
    }
}