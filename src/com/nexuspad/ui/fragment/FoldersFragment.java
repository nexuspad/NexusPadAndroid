/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.edmondapps.utils.android.Logs;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.edmondapps.utils.android.ui.CompoundAdapter;
import com.edmondapps.utils.android.ui.SingleAdapter;
import com.edmondapps.utils.android.view.ViewUtils;
import com.google.common.collect.Iterables;
import com.nexuspad.Manifest;
import com.nexuspad.R;
import com.nexuspad.account.AccountManager;
import com.nexuspad.app.App;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.dataservice.FolderService;
import com.nexuspad.dataservice.FolderService.FolderReceiver;
import com.nexuspad.dataservice.NPException;
import com.nexuspad.ui.FoldersAdapter;
import com.nexuspad.ui.OnFolderMenuClickListener;
import com.nexuspad.ui.activity.NewFolderActivity;

import java.util.List;

/**
 * You must pass in a moduleId with {@link FoldersFragment#KEY_PARENT_FOLDER} as an argument or use
 * the static factory method {@link #of(Folder)}.
 *
 * @author Edmond
 */
@FragmentName(FoldersFragment.TAG)
public class FoldersFragment extends ListFragment {
    public static final String TAG = "FoldersFragment";
    public static final String KEY_PARENT_FOLDER = "com.nexuspad.ui.fragment.FoldersFragment.parent_folder";

    /**
     * @param folder the parent folder of the folders list
     */
    public static FoldersFragment of(Folder folder) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_PARENT_FOLDER, folder);

        FoldersFragment fragment = new FoldersFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    public interface Callback {
        void onFolderClicked(FoldersFragment f, Folder folder);

        void onSubFolderClicked(FoldersFragment f, Folder folder);

        /**
         * Called when the "up" folder is clicked, the activity should navigate to the parent of the current parent folder
         *
         * @param f
         */
        void onUpFolderClicked(FoldersFragment f);
    }

    private final FolderReceiver mFolderReceiver = new FolderReceiver() {
        @Override
        protected void onGotAll(Context c, Intent i, List<Folder> folders) {
            mSubFolders = folders;
            onSubFoldersLoaded(folders);
        }

        @Override
        protected void onDelete(Context c, Intent i, Folder folder) {
            if (Iterables.removeIf(mSubFolders, folder.filterById())) {
                notifyDataSetChanged();
            } else {
                Logs.w(TAG, "folder deleted from the server, but no matching ID found in the list. " + folder);
            }
        }

        @Override
        protected void onNew(Context c, Intent i, Folder f) {
            if (!Iterables.tryFind(mSubFolders, f.filterById()).isPresent()) {
                mSubFolders.add(f);
                notifyDataSetChanged();
            } else {
                Logs.w(TAG, "folder created on the server, but ID already exists in the list, updating instead: " + f);
                onUpdate(c, i, f);
            }
        }

        @Override
        protected void onUpdate(Context c, Intent i, Folder folder) {
            final int index = Iterables.indexOf(mSubFolders, folder.filterById());
            if (index >= 0) {
                mSubFolders.remove(index);
                mSubFolders.add(index, folder);
                notifyDataSetChanged();
            } else {
                Logs.w(TAG, "cannot find the updated entry in the list; folder: " + folder);
            }
        }

        private void notifyDataSetChanged() {
            getListAdapter().notifyDataSetChanged();
        }
    };

    private Callback mCallback;
    private FolderService mFolderService;
    private Folder mParentFolder;
    private List<Folder> mSubFolders;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = App.getCallback(activity, Callback.class);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.folders_frag, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_folder:
                NewFolderActivity.startWithParentFolder(mParentFolder, getActivity());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null) {
            mParentFolder = arguments.getParcelable(KEY_PARENT_FOLDER);
        }

        mFolderService = new FolderService(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().registerReceiver(
                mFolderReceiver,
                FolderReceiver.getIntentFilter(),
                Manifest.permission.LISTEN_FOLDER_CHANGES,
                null);

        try {
            mParentFolder.setOwner(AccountManager.currentAccount());
            mFolderService.getSubFolders(mParentFolder);

        } catch (NPException e) {
            Logs.e(TAG, e);
            Toast.makeText(getActivity(), R.string.err_network, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mFolderReceiver);
    }

    /**
     * Called when the sub-folders are retrieved.
     *
     * @param folders same as {@link #getSubFolders()}
     */
    protected void onSubFoldersLoaded(List<Folder> folders) {
        final UpFolderAdapter upFolderAdapter = new UpFolderAdapter(getActivity(), mParentFolder);
        final FoldersAdapter foldersAdapter = onCreateFoldersAdapter(folders);
        final CompoundAdapter adapter = new CompoundAdapter(upFolderAdapter, foldersAdapter);

        super.setListAdapter(adapter);
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    final int realPos = getListAdapter().getPositionForAdapter(position);
                    return getFoldersAdapter().onItemLongClick(parent, view, realPos, id);
                } else {
                    return false;
                }
            }
        });
    }

    /**
     * Creates an adapter with the given list of {@code Folder}s.
     *
     * @param folders the folders to be displayed
     * @return an instance of {@link FoldersAdapter}
     */
    protected FoldersAdapter onCreateFoldersAdapter(List<Folder> folders) {
        final FoldersAdapter adapter = new NamedFoldersAdapter(getActivity(), folders, mParentFolder);
        ListView listView = getListView();
        adapter.setOnMenuClickListener(new OnFolderMenuClickListener(listView, mParentFolder, mFolderService) {
            @Override
            public void onClick(View v) {
                final int pos = getListView().getPositionForView(v);
                final int realPos = getListAdapter().getPositionForAdapter(pos);
                onFolderClick(adapter.getItem(realPos), realPos, v);
            }
        });
        adapter.setOnSubFolderClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final int pos = getListView().getPositionForView(v);
                final int realPos = getListAdapter().getPositionForAdapter(pos);
                final Folder folder = adapter.getItem(realPos);
                mCallback.onSubFolderClicked(FoldersFragment.this, folder);
            }
        });
        return adapter;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if (position == 0) {
            if (mParentFolder.getFolderId() == Folder.ROOT_FOLDER) {
                mCallback.onFolderClicked(this, mParentFolder);
            } else {
                mCallback.onUpFolderClicked(this);
            }
        } else {
            final int realPos = getListAdapter().getPositionForAdapter(position);
            mCallback.onFolderClicked(this, getFoldersAdapter().getItem(realPos));
        }
    }

    /**
     * This {@code Fragment} guarantees the use of {@link FoldersAdapter}.
     * <p/>
     * Override {@link #onCreateFoldersAdapter(List)} instead.
     *
     * @throws UnsupportedOperationException every time this method is invoked
     */
    @Override
    @Deprecated
    public void setListAdapter(ListAdapter adapter) {
        throw new UnsupportedOperationException("NO");
    }

    @Override
    public CompoundAdapter getListAdapter() {
        return (CompoundAdapter) super.getListAdapter();
    }

    public FoldersAdapter getFoldersAdapter() {
        return (FoldersAdapter) getListAdapter().getAdapter(1);
    }

    protected FolderService getFolderService() {
        return mFolderService;
    }

    protected List<Folder> getSubFolders() {
        return mSubFolders;
    }

    private static class UpFolderAdapter extends SingleAdapter<View> {

        public UpFolderAdapter(Context context, Folder folder) {
            super(createView(context, folder));
        }

        private static View createView(Context context, Folder folder) {
            final boolean isRoot = folder.getFolderId() == Folder.ROOT_FOLDER;
            final View view = LayoutInflater.from(context).inflate(R.layout.list_item_icon_2, null);

            final TextView title = ViewUtils.findView(view, android.R.id.text1);
            final ImageView icon = ViewUtils.findView(view, android.R.id.icon);
            final View icon2 = ViewUtils.findView(view, android.R.id.icon2);
            final View menu = ViewUtils.findView(view, R.id.menu);

            title.setText(isRoot ? folder.getFolderName() : context.getText(R.string.up));
            icon.setImageResource(R.drawable.ic_np_folder);
            menu.setVisibility(View.GONE);
            menu.setFocusable(false);
            icon2.setVisibility(View.INVISIBLE);
            icon2.setFocusable(false);

            return view;
        }
    }

    private static class NamedFoldersAdapter extends FoldersAdapter {

        private final Folder mParent;

        public NamedFoldersAdapter(Activity a, List<? extends Folder> folders, Folder parent) {
            super(a, folders, true);
            mParent = parent;
        }

        @Override
        protected CharSequence getHeaderText(int position, View convertView, ViewGroup parent) {
            return parent.getResources().getString(R.string.formatted_sub_folders, mParent.getFolderName());
        }
    }
}
