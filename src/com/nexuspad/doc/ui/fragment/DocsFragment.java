/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.doc.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.*;
import android.widget.BaseAdapter;
import android.widget.ListView;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.edmondapps.utils.java.WrapperList;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.app.App;
import com.nexuspad.datamodel.Doc;
import com.nexuspad.datamodel.EntryList;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.dataservice.EntryListService;
import com.nexuspad.dataservice.EntryService;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.doc.ui.activity.NewDocActivity;
import com.nexuspad.ui.EntriesAdapter;
import com.nexuspad.ui.FolderEntriesAdapter;
import com.nexuspad.ui.FoldersAdapter;
import com.nexuspad.ui.OnEntryMenuClickListener;
import com.nexuspad.ui.fragment.EntriesFragment;

import java.util.List;

/**
 * @author Edmond
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
        mCallback = App.getCallbackOrThrow(activity, Callback.class);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.docs_frag, menu);

        setUpSearchView(menu.findItem(R.id.search));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_doc:
                NewDocActivity.startWithFolder(getActivity(), getFolder());
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

	    final DocsAdapter docsAdapter = new DocsAdapter(
			    getActivity(),
			    new WrapperList<Doc>(list.getEntries()));

	    docsAdapter.setOnMenuClickListener(new OnDocMenuClickListener(getListView(), getEntryService()));

	    FoldersDocsAdapter foldersDocsAdapter;

        if (hasNextPage()) {
            foldersDocsAdapter = new FoldersDocsAdapter(foldersAdapter, docsAdapter, getLoadMoreAdapter());
        } else {
            foldersDocsAdapter = new FoldersDocsAdapter(foldersAdapter, docsAdapter);
        }

        setListAdapter(foldersDocsAdapter);
        listView.setOnItemLongClickListener(foldersDocsAdapter);
    }

    @Override
    protected void onSearchLoaded(EntryList list) {
        getListAdapter().setShouldHideFolders(true);
        super.onSearchLoaded(list);
    }

    @Override
    protected FilterableAdapter getFilterableAdapter() {
        return getListAdapter().getEntriesAdapter();
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
        return (FoldersDocsAdapter) super.getListAdapter();
    }

    private class OnDocMenuClickListener extends OnEntryMenuClickListener<Doc> {
        public OnDocMenuClickListener(ListView listView, EntryService entryService) {
            super(listView, entryService, getUndoBarController());
        }

        @Override
        protected boolean onEntryMenuClick(Doc entry, int pos, int menuId) {
            switch (menuId) {
                case R.id.edit:
                    NewDocActivity.startWithDoc(getActivity(), getFolder(), entry);
                    return true;
                default:
                    return super.onEntryMenuClick(entry, pos, menuId);
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

    public class DocsAdapter extends EntriesAdapter<Doc> {
        public DocsAdapter(Activity a, List<Doc> entries) {
            super(a, entries, getFolder(), getEntryListService(), EntryTemplate.DOC);
        }

        @Override
        protected View getEntryView(Doc entry, int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_icon, parent, false);
            }
            ViewHolder holder = getHolder(convertView);

            holder.icon.setImageResource(R.drawable.ic_doc);
            holder.text1.setText(entry.getTitle());
            holder.menu.setOnClickListener(getOnMenuClickListener());

            return convertView;
        }

	    @Override
	    protected String getEntriesHeaderText() {
		    return getString(R.string.docs);
	    }

        @Override
        protected View getEmptyEntryView(LayoutInflater i, View c, ViewGroup p) {
            return getCaptionView(i, c, p, R.string.empty_docs, R.drawable.empty_folder);
        }

        @Override
        public void showRawEntries() {
            getListAdapter().setShouldHideFolders(false);
            super.showRawEntries();
        }
    }
}
