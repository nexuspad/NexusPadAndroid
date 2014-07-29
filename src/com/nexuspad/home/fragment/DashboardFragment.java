/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.home.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import com.nexuspad.common.annotation.FragmentName;
import com.nexuspad.R;
import com.nexuspad.about.activity.AboutActivity;
import com.nexuspad.account.AccountManager;
import com.nexuspad.app.App;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.home.activity.LoginActivity;
import com.nexuspad.common.adapters.IconListAdapter;

import static com.nexuspad.dataservice.ServiceConstants.*;

/**
 * @author Edmond
 */
@FragmentName(DashboardFragment.TAG)
public class DashboardFragment extends Fragment {
	public static final String TAG = "DashboardFragment";

	/*
	 * All 3 arrays must be consistent when re-ordering them
	 */
	private static final int[] sModules = {
			0,
			CONTACT_MODULE,
			CALENDAR_MODULE,
			JOURNAL_MODULE,
			DOC_MODULE,
			PHOTO_MODULE,
			BOOKMARK_MODULE
	};

	private static final int[] sDrawables = {
			R.drawable.avatar,
			R.drawable.contact,
			R.drawable.event,
			R.drawable.journal,
			R.drawable.doc,
			R.drawable.photo,
			R.drawable.bookmark
	};

	private static final int[] sStrings = {
			R.string.file,
			R.string.contacts,
			R.string.events,
			R.string.journal,
			R.string.docs,
			R.string.photos,
			R.string.bookmarks
	};

	public interface Callback {
		/**
		 * Called when an module is clicked.
		 *
		 * @param f          caller of this method
		 * @param moduleType one of the {@code *_MODULE} constants defined in
		 *                   {@link ServiceConstants}
		 */
		void onModuleClicked(DashboardFragment f, int moduleType);
	}

	private Callback mCallback;
	private ListAdapter mListAdapter;
	private ListView mListView;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mCallback = App.getCallbackOrThrow(activity, Callback.class);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.dashboard_frag, container, false);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.dashboard, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.logout:
				AccountManager.logout();
				FragmentActivity activity = getActivity();
				startActivity(new Intent(activity, LoginActivity.class));
				activity.finish();
				return true;

			case R.id.about:
				startActivity(new Intent(getActivity(), AboutActivity.class));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mListView = (ListView)view.findViewById(R.id.main_list_view);

		mListAdapter = new IconListAdapter(getActivity(), sDrawables, sStrings);
		mListView.setAdapter(mListAdapter);

		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onListItemClick(mListView, view, position, id);
			}
		});
	}

	public void onListItemClick(ListView l, View v, int position, long id) {
		mCallback.onModuleClicked(this, sModules[position]);
	}
}
