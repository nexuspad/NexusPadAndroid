/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.doc.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.edmondapps.utils.java.WrapperList;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.Doc;
import com.nexuspad.datamodel.EntryList;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.dataservice.EntryService;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.doc.ui.DocsAdapter;
import com.nexuspad.doc.ui.activity.NewDocActivity;
import com.nexuspad.ui.FolderEntriesAdapter;
import com.nexuspad.ui.FoldersAdapter;
import com.nexuspad.ui.OnEntryMenuClickListener;
import com.nexuspad.ui.activity.NewEntryActivity.Mode;
import com.nexuspad.ui.fragment.EntriesFragment;

/**
 * @author Edmond
 * 
 */
@FragmentName(DocsFragment.TAG)
@ModuleId(moduleId = ServiceConstants.DOC_MODULE, template = EntryTemplate.DOC)
public class DocsFragment extends EntriesFragment {
    public static final String TAG = "DocsFragment";

    public static DocsFragment of(Folder parent) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_FOLDER, parent);

        DocsFragment fragment = new DocsFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    public interface Callback extends EntriesFragment.Callback {
        void onFolderClick(DocsFragment f, Folder folder);

        void onDocClick(DocsFragment f, Doc doc);
    }

    private Callback mCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof Callback) {
            mCallback = (Callback)activity;
        } else {
            throw new IllegalStateException(activity + " must implement Callback.");
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.docs_frag, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_doc:
                NewDocActivity.startWithFolder(getFolder(), Mode.NEW, getActivity());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onListLoaded(EntryList list) {
        super.onListLoaded(list);

        FoldersDocsAdapter a = getListAdapter();
        if (a != null) {
            a.notifyDataSetChanged();
            if (!hasNextPage()) {
                a.removeAdapter(getLoadMoreAdapter());
            }
            return;
        }

        ListView listView = getListView();

        FoldersAdapter foldersAdapter = newFoldersAdapter();
        DocsAdapter docsAdapter = newDocsAdapter(list);

        FoldersDocsAdapter foldersDocsAdapter;

        if (hasNextPage()) {
            foldersDocsAdapter = new FoldersDocsAdapter(foldersAdapter, docsAdapter, getLoadMoreAdapter());
        } else {
            foldersDocsAdapter = new FoldersDocsAdapter(foldersAdapter, docsAdapter);
        }

        setListAdapter(foldersDocsAdapter);

        listView.setOnItemLongClickListener(foldersDocsAdapter);
    }

    private DocsAdapter newDocsAdapter(EntryList list) {
        DocsAdapter adapter = new DocsAdapter(getActivity(), new WrapperList<Doc>(list.getEntries()));
        adapter.setOnMenuClickListener(new OnDocMenuClickListener(getListView(), getEntryService()));
        return adapter;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        FoldersDocsAdapter adapter = getListAdapter();
        int realPos = adapter.getPositionForAdapter(position);

        if (adapter.isPositionFolder(position)) {
            Folder folder = adapter.getFoldersAdapter().getItem(realPos);
            mCallback.onFolderClick(this, folder);

        } else if (adapter.isPositionEntries(position)) {
            Doc doc = adapter.getEntriesAdapter().getItem(realPos);
            mCallback.onDocClick(this, doc);
        }
    }

    @Override
    public FoldersDocsAdapter getListAdapter() {
        return (FoldersDocsAdapter)super.getListAdapter();
    }

    private class OnDocMenuClickListener extends OnEntryMenuClickListener<Doc> {
        public OnDocMenuClickListener(ListView listView, EntryService entryService) {
            super(listView, entryService);
        }

        @Override
        protected boolean onEntryMenuClick(Doc entry, int menuId) {
            switch (menuId) {
                case R.id.edit:
                    NewDocActivity.startWithDoc(entry, getFolder(), Mode.EDIT, getActivity());
                    return true;
                default:
                    return super.onEntryMenuClick(entry, menuId);
            }
        }
    }

    private static class FoldersDocsAdapter extends FolderEntriesAdapter<DocsAdapter> {
        public FoldersDocsAdapter(FoldersAdapter folderAdapter, DocsAdapter entriesAdapter) {
            super(folderAdapter, entriesAdapter);
        }

        public FoldersDocsAdapter(FoldersAdapter folderAdapter, DocsAdapter entriesAdapter, BaseAdapter a) {
            super(folderAdapter, entriesAdapter, a);
        }
    }
}
