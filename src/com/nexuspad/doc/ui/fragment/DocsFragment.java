/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.doc.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
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
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.doc.ui.activity.NewDocActivity;
import com.nexuspad.ui.adapters.FoldersEntriesListAdapter;
import com.nexuspad.ui.adapters.ListEntriesAdapter;
import com.nexuspad.ui.adapters.ListFoldersAdapter;
import com.nexuspad.ui.adapters.ListViewHolder;
import com.nexuspad.ui.fragment.EntriesFragment;
import com.nexuspad.ui.listeners.OnEntryMenuClickListener;

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
	    Log.i(TAG, "Receiving entry list.");

	    super.onListLoaded(list);

	    FoldersEntriesListAdapter a = getListAdapter();

	    if (a != null) {
		    if (!hasNextPage()) {
			    a.removeLoadMoreAdapter();
		    }
		    a.notifyDataSetChanged();
		    return;
	    }

	    ListView listView = getListView();

	    final DocsAdapter docsAdapter = new DocsAdapter(
			    getActivity(),
			    new WrapperList<Doc>(list.getEntries()));

	    docsAdapter.setOnMenuClickListener(new OnEntryMenuClickListener<Doc>(mListView, getEntryService(), getUndoBarController()) {
		    @Override
		    public void onClick(View v) {
			    @SuppressWarnings("unchecked")
			    FoldersEntriesListAdapter<? extends ListEntriesAdapter<Doc>> felAdapter = ((FoldersEntriesListAdapter<? extends ListEntriesAdapter<Doc>>) mListView.getAdapter());

			    int position = mListView.getPositionForView(v);
			    if (position != ListView.INVALID_POSITION && felAdapter.isPositionEntries(position)) {
				    Doc item = (Doc)felAdapter.getItem(position);
				    onEntryClick(item, position, v);
			    }
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
	    });

	    ListFoldersAdapter foldersAdapter = newFoldersAdapter();

	    if (hasNextPage()) {
		    mListAdapter = new FoldersEntriesListAdapter(foldersAdapter, docsAdapter, getLoadMoreAdapter());
	    } else {
		    mListAdapter = new FoldersEntriesListAdapter(foldersAdapter, docsAdapter);
	    }

	    listView.setOnItemLongClickListener(mListAdapter);
    }

    @Override
    protected void onSearchLoaded(EntryList list) {
        getListAdapter().setShouldHideFolders(true);
        super.onSearchLoaded(list);
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
	    super.onListItemClick(l, v, position, id);

	    FoldersEntriesListAdapter adapter = getListAdapter();

	    if (adapter.isPositionFolder(position)) {
		    mCallback.onFolderClick(this, adapter.getFoldersAdapter().getItem(position));

	    } else if (adapter.isPositionEntries(position)) {
		    mCallback.onDocClick(this, (Doc)adapter.getEntriesAdapter().getItem(position - adapter.getFoldersAdapter().getCount()));
	    }
    }

	@Override
	public FoldersEntriesListAdapter getListAdapter() {
		return (FoldersEntriesListAdapter) super.getListAdapter();
	}

    public class DocsAdapter extends ListEntriesAdapter<Doc> {
        public DocsAdapter(Activity a, List<Doc> entries) {
            super(a, entries, getFolder(), getEntryListService(), EntryTemplate.DOC);
        }

        @Override
        protected View getEntryView(Doc entry, int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_icon, parent, false);
            }
            ListViewHolder holder = getHolder(convertView);

            holder.getIcon().setImageResource(R.drawable.ic_doc);
            holder.getText1().setText(entry.getTitle());
            holder.getMenu().setOnClickListener(getOnMenuClickListener());

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
