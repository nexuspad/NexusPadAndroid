/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.doc.ui.fragment;

import android.widget.ListView;

import com.edmondapps.utils.android.annotaion.FragmentName;
import com.edmondapps.utils.java.WrapperList;
import com.nexuspad.R;
import com.nexuspad.datamodel.Doc;
import com.nexuspad.datamodel.EntryList;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.dataservice.EntryService;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.doc.ui.DocsAdapter;
import com.nexuspad.ui.FolderEntriesAdapter;
import com.nexuspad.ui.FoldersAdapter;
import com.nexuspad.ui.OnEntryMenuClickListener;
import com.nexuspad.ui.fragment.EntriesFragment;

/**
 * @author Edmond
 * 
 */
@FragmentName(DocsFragment.TAG)
public class DocsFragment extends EntriesFragment {
    public static final String TAG = "DocsFragment";

    @Override
    protected void onListLoaded(EntryList list) {
        super.onListLoaded(list);

        ListView listView = getListView();

        FoldersAdapter foldersAdapter = newFoldersAdapter(list, getActivity(), listView);
        DocsAdapter docsAdapter = newDocsAdapter(list);

        FoldersDocsAdapter foldersDocsAdapter = new FoldersDocsAdapter(foldersAdapter, docsAdapter);
        setListAdapter(foldersDocsAdapter);

        listView.setOnItemLongClickListener(foldersDocsAdapter);
    }

    private DocsAdapter newDocsAdapter(EntryList list) {
        DocsAdapter adapter = new DocsAdapter(getActivity(), new WrapperList<Doc>(list.getEntries()));
        adapter.setOnMenuClickListener(new OnDocMenuClickListener(getListView(), getEntryService()));
        return adapter;
    }

    @Override
    protected int getModule() {
        return ServiceConstants.DOC_MODULE;
    }

    @Override
    protected EntryTemplate getTemplate() {
        return EntryTemplate.DOC;
    }

    private class OnDocMenuClickListener extends OnEntryMenuClickListener<Doc> {
        public OnDocMenuClickListener(ListView listView, EntryService entryService) {
            super(listView, entryService);
        }

        @Override
        protected boolean onEntryMenuClick(Doc entry, int menuId) {
            switch (menuId) {
                case R.id.edit:
                    // TODO
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
    }
}
