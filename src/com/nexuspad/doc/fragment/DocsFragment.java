/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.doc.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.*;
import android.widget.ListView;
import com.nexuspad.R;
import com.nexuspad.app.App;
import com.nexuspad.common.Constants;
import com.nexuspad.common.adapters.EntriesAdapter;
import com.nexuspad.common.adapters.FoldersAndEntriesAdapter;
import com.nexuspad.common.adapters.ListFoldersAdapter;
import com.nexuspad.common.adapters.ListViewHolder;
import com.nexuspad.common.annotation.FragmentName;
import com.nexuspad.common.annotation.ModuleInfo;
import com.nexuspad.common.fragment.FoldersAndEntriesFragment;
import com.nexuspad.common.listeners.OnEntryMenuClickListener;
import com.nexuspad.service.datamodel.EntryList;
import com.nexuspad.service.datamodel.EntryTemplate;
import com.nexuspad.service.datamodel.NPDoc;
import com.nexuspad.service.datamodel.NPFolder;
import com.nexuspad.service.dataservice.ServiceConstants;
import com.nexuspad.doc.activity.DocEditActivity;

/**
 * @author Edmond
 */
@FragmentName(DocsFragment.TAG)
@ModuleInfo(moduleId = ServiceConstants.DOC_MODULE, template = EntryTemplate.DOC)
public class DocsFragment extends FoldersAndEntriesFragment {
	public static final String TAG = "DocsFragment";

	private Callback mCallback;

	public static DocsFragment of(NPFolder parent) {
		Bundle bundle = new Bundle();
		bundle.putParcelable(Constants.KEY_FOLDER, parent);

		DocsFragment fragment = new DocsFragment();
		fragment.setArguments(bundle);

		return fragment;
	}

	public interface Callback extends ActivityCallback {
		void onFolderClick(DocsFragment f, NPFolder folder);
		void onDocClick(DocsFragment f, NPDoc doc);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mListView.setOnScrollListener(mLoadMoreScrollListener);

		if (mEntryList == null) {
			queryEntriesAsync();
		} else {
			onListLoaded(mEntryList);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mCallback = App.getCallbackOrThrow(activity, Callback.class);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.docs_topmanu, menu);

		setUpSearchView(menu.findItem(R.id.search));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.new_doc:
				DocEditActivity.startWithFolder(getActivity(), getFolder());
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onSearchLoaded(EntryList list) {
		((FoldersAndEntriesAdapter)getAdapter()).setShouldHideFolders(true);
		super.onSearchLoaded(list);
	}

	@Override
	protected void reDisplayListEntries() {
		dismissProgressIndicator();

		// Need to reset the scroll listener.
		mLoadMoreScrollListener.reset();

		if (mCurrentSearchKeyword == null) {
			((FoldersAndEntriesAdapter)getAdapter()).setShouldHideFolders(false);
		} else {
			((FoldersAndEntriesAdapter)getAdapter()).setShouldHideFolders(true);
		}

		((FoldersAndEntriesAdapter)getAdapter()).setDisplayFoldersAndEntries(mEntryList);
	}

	@Override
	protected void onListLoaded(EntryList newListToDisplay) {
		Log.i(TAG, "Receiving entry list.");

		super.onListLoaded(newListToDisplay);

		FoldersAndEntriesAdapter a = (FoldersAndEntriesAdapter)getAdapter();

		if (a == null) {
			final DocsAdapter docsAdapter = new DocsAdapter(getActivity(), newListToDisplay);

			docsAdapter.setOnMenuClickListener(new OnEntryMenuClickListener<NPDoc>(mListView, getEntryService(), getUndoBarController()) {
				@Override
				public void onClick(View v) {
					@SuppressWarnings("unchecked")
					FoldersAndEntriesAdapter<? extends EntriesAdapter<NPDoc>> felAdapter = ((FoldersAndEntriesAdapter<? extends EntriesAdapter<NPDoc>>) mListView.getAdapter());

					int position = mListView.getPositionForView(v);
					if (position != ListView.INVALID_POSITION && felAdapter.isPositionEntries(position)) {
						NPDoc item = (NPDoc)felAdapter.getItem(position);
						onEntryClick(item, position, v);
					}
				}

				@Override
				protected boolean onEntryMenuClick(NPDoc entry, int pos, int menuId) {
					switch (menuId) {
						case R.id.edit:
							DocEditActivity.startWithDoc(getActivity(), getFolder(), entry);
							return true;
						default:
							return super.onEntryMenuClick(entry, pos, menuId);
					}
				}
			});

			ListFoldersAdapter foldersAdapter = newFoldersAdapter();

			FoldersAndEntriesAdapter combinedAdapter = new FoldersAndEntriesAdapter(foldersAdapter, docsAdapter);
			setAdapter(combinedAdapter);
			mListView.setAdapter(combinedAdapter);
			mListView.setOnItemLongClickListener(combinedAdapter);

		} else {
			a.getEntriesAdapter().setDisplayEntryList(newListToDisplay);
		}

		clearVisualIndicator();
	}

	/**
	 * SwipeRefresh handler.
	 */
	@Override
	public void onRefresh() {
		if (mListFrame instanceof SwipeRefreshLayout) {
			((SwipeRefreshLayout)mListFrame).setRefreshing(true);
			queryEntriesAsync();
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		FoldersAndEntriesAdapter adapter = ((FoldersAndEntriesAdapter)getAdapter());

		if (adapter.isPositionFolder(position)) {
			mCallback.onFolderClick(this, adapter.getFoldersAdapter().getItem(position));

		} else if (adapter.isPositionEntries(position)) {
			mCallback.onDocClick(this, (NPDoc)adapter.getEntriesAdapter().getItem(position - adapter.getFoldersAdapter().getCount()));
		}
	}


	public class DocsAdapter extends EntriesAdapter<NPDoc> {
		public DocsAdapter(Activity a, EntryList entryList) {
			super(a, entryList);
		}

		@Override
		protected View getEntryView(NPDoc entry, int position, View convertView, ViewGroup parent) {
			if (convertView == null || convertView.findViewById(android.R.id.icon) == null) {
				convertView = getLayoutInflater().inflate(R.layout.list_item_with_icon, parent, false);
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
	}
}
