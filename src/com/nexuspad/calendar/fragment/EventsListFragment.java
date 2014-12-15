package com.nexuspad.calendar.fragment;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import com.google.common.base.Strings;
import com.nexuspad.R;
import com.nexuspad.calendar.activity.EventActivity;
import com.nexuspad.calendar.activity.EventEditActivity;
import com.nexuspad.common.Constants;
import com.nexuspad.common.adapters.EntriesAdapter;
import com.nexuspad.common.adapters.ListViewHolder;
import com.nexuspad.common.annotation.FragmentName;
import com.nexuspad.common.annotation.ModuleInfo;
import com.nexuspad.common.fragment.EntriesFragment;
import com.nexuspad.common.listeners.OnEntryMenuClickListener;
import com.nexuspad.common.listeners.OnEventListEndListener;
import com.nexuspad.service.account.AccountManager;
import com.nexuspad.service.datamodel.*;
import com.nexuspad.service.dataservice.EntryListService;
import com.nexuspad.service.dataservice.NPException;
import com.nexuspad.service.dataservice.ServiceConstants;
import com.nexuspad.service.util.DateUtil;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * User: ren
 */
@FragmentName(EventsListFragment.TAG)
@ModuleInfo(moduleId = ServiceConstants.CALENDAR_MODULE, template = EntryTemplate.EVENT)
public class EventsListFragment extends EntriesFragment {

	public static final String TAG = "EventsListFragment";

	public static final String KEY_START_YMD = "key_start_ymd";
	public static final String KEY_END_YMD = "key_end_ymd";

	private StickyListHeadersListView mStickyHeaderEventListView;

	private EntryList mEventList;

	private NPDateRange mDateRange;

	protected OnEventListEndListener mLoadMoreScrollListener = new OnEventListEndListener() {
		/**
		 * List more events going backward.
		 * @param dateRange
		 */
		@Override
		protected void onListTop(NPDateRange dateRange) {
			queryEntriesAsync();
		}

		/**
		 * List more events going forward.
		 *
		 * @param dateRange
		 */
		@Override
		protected void onListBottom(NPDateRange dateRange) {
			mDateRange = new NPDateRange(dateRange);
			queryEntriesAsync();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Bundle arguments = getArguments();
		mDateRange = new NPDateRange((String)arguments.get(KEY_START_YMD), (String)arguments.get(KEY_END_YMD));
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		setHasOptionsMenu(true);
	}

	@Override
	public void setUpSearchView(MenuItem searchItem) { // public for the activity to install it
		super.setUpSearchView(searchItem);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.events_list_frag, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final View theView = view.findViewById(R.id.list_view);

		mStickyHeaderEventListView = (StickyListHeadersListView)theView;

		// Set the scroll listener for loading more
		Log.i(TAG, "The initial date range is: " + mDateRange);

		mLoadMoreScrollListener.setCurrentDateRange(mDateRange);
		mStickyHeaderEventListView.setOnScrollListener(newDirectionalScrollListener(mLoadMoreScrollListener));

		// Set the listener for folder selector
		initFolderSelector(ACTIVITY_REQ_CODE_FOLDER_SELECTOR);

		// Set the listener for each item
		mStickyHeaderEventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (getAdapter() != null) {
					final NPEvent event = ((EventsListAdapter)getAdapter()).getItem(position);
					EventActivity.startWith(getActivity(), event, getFolder());
				}
			}
		});

		mStickyHeaderEventListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		queryEntriesAsync();

		mLoadMoreScrollListener.setLoadingDisabled(false);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.events_topmenu, menu);
		setUpSearchView(menu.findItem(R.id.search));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.new_event:
				EventEditActivity.startWithFolder(getActivity(), mFolder);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}


	@Override
	protected void onListLoaded(EntryList newListToDisplay) {
		Log.i(TAG, "Receive event list: " + String.valueOf(newListToDisplay.getEntries().size()));

		super.onListLoaded(newListToDisplay);

		mEventList = newListToDisplay;

		EventsListAdapter adapter = (EventsListAdapter)getAdapter();

		if (adapter == null) {
			adapter = new EventsListAdapter(getActivity(), newListToDisplay, getFolder(), getEntryListService());

			adapter.setOnMenuClickListener(new OnEntryMenuClickListener<NPEvent>(mStickyHeaderEventListView, getEntryService(), getUndoBarController()) {
				@Override
				public void onClick(View v) {
					final int i = mStickyHeaderEventListView.getPositionForView(v);
					final NPEvent event = (NPEvent) getAdapter().getItem(i);
					onEntryClick(event, i, v);
				}

				@Override
				protected boolean onEntryMenuClick(NPEvent event, int pos, int menuId) {
					switch (menuId) {
						case R.id.edit:
							EventEditActivity.startWithEvent(getActivity(), getFolder(), event);
							return true;
						default:
							return super.onEntryMenuClick(event, pos, menuId);
					}
				}
			});

			mStickyHeaderEventListView.setAdapter(adapter);
			mStickyHeaderEventListView.setOnItemLongClickListener(adapter);
			setAdapter(adapter);

			scrollToToday(mEventList);

		} else {
			adapter.setDisplayEntryList(newListToDisplay);
		}

		dismissProgressIndicator();

		mLoadMoreScrollListener.reset();

		if (!newListToDisplay.isEntryUpdated()) {
			mLoadMoreScrollListener.setLoadingDisabled(true);
		}
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case ACTIVITY_REQ_CODE_FOLDER_SELECTOR:
				if (resultCode == Activity.RESULT_OK) {
					mFolder = data.getParcelableExtra(Constants.KEY_FOLDER);
					queryEntriesAsync();

					// Refresh Fragment list content after selecting the folder from folder navigator.
					// Since Activity remains the same, we need to update the title in Action bar.
					final ActionBar actionBar = getActivity().getActionBar();
					actionBar.setTitle(mFolder.getFolderName());
				}
				break;

			default:
				throw new AssertionError("unknown requestCode: " + requestCode);
		}
	}

	private void scrollToToday(EntryList eventList) {
		Date today = Calendar.getInstance().getTime();

		int i = 0;
		for (NPEntry entry : (List<? extends NPEntry>)eventList.getEntries()) {
			NPEvent event = NPEvent.fromEntry(entry);
			if (event.getStartTime().after(today)) {
				mStickyHeaderEventListView.smoothScrollToPosition(i);
				break;
			}
			i++;
		}
	}

	public void setStartEndTime(String startYmd, String endYmd) {
		mDateRange = new NPDateRange(startYmd, endYmd);
	}

	@Override
	protected void queryEntriesAsync() {
		try {
			mFolder.setOwner(AccountManager.currentAccount());
			getEntryListService().getEntriesBetweenDates(mFolder, getTemplate(), mDateRange.getStartYmd(), mDateRange.getEndYmd(), 0);

		} catch (NPException e) {
			Log.e(TAG, e.toString());
			handleServiceError(e.getServiceError());
		}
	}

	/**
	 * Extend mEndYmd by 30 days and load more events
	 */
	protected void loadMoreFutureEvents() {
		mDateRange.setStartYmd(mDateRange.getEndYmd());
		mDateRange.setEndYmd(DateUtil.convertToYYYYMMDD(DateUtil.addDaysTo(DateUtil.parseFromYYYYMMDD(mDateRange.getEndYmd()), 30)));

		try {
			getEntryListService().getEntriesBetweenDates(mFolder, getTemplate(), mDateRange.getStartYmd(), mDateRange.getEndYmd(), 0);

		} catch (NPException e) {
			Log.e(TAG, e.toString());
			handleServiceError(e.getServiceError());
		}
	}

	/**
	 * Extend the mStartYmd back by 30 days and load events
	 */
	protected void loadMorePreviousEvents() {
		mDateRange.setEndYmd( mDateRange.getStartYmd());
		mDateRange.setStartYmd(DateUtil.convertToYYYYMMDD(DateUtil.addDaysTo(DateUtil.parseFromYYYYMMDD(mDateRange.getStartYmd()), -30)));

		try {
			getEntryListService().getEntriesBetweenDates(mFolder, getTemplate(), mDateRange.getStartYmd(), mDateRange.getEndYmd(), 0);

		} catch (NPException e) {
			Log.e(TAG, e.toString());
			handleServiceError(e.getServiceError());
		}
	}

	/**
	 * View holder for event item.
	 */
	private static class EventViewHolder {
		private ViewGroup mColorLabelFrame;
		private TextView mTitleV;
		private TextView mTimeV;
		private ImageButton mMenu;
	}


	/**
	 * Event list view adapter.
	 */
	private class EventsListAdapter extends EntriesAdapter<NPEvent> implements StickyListHeadersAdapter {
		private final DateFormat mDayOfWeekFormat = new SimpleDateFormat("EEEE");
		private final DateFormat mMonthFormat = new SimpleDateFormat("MMMM yyyy");
		private final DateFormat mDateFormat;
		private final DateFormat mTimeFormat;

		private EventsListAdapter(Activity a, EntryList entryList, NPFolder folder, EntryListService service) {
			super(a, entryList);
			mDateFormat = android.text.format.DateFormat.getMediumDateFormat(a);
			mTimeFormat = android.text.format.DateFormat.getTimeFormat(a);
		}

		@Override
		protected View getEntryView(NPEvent event, int position, View convertView, ViewGroup parent) {
			final EventViewHolder viewHolder;

			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.list_item_event, parent, false);

				viewHolder = new EventViewHolder();
				viewHolder.mTitleV = (TextView)convertView.findViewById(R.id.lbl_title);
				viewHolder.mTimeV = (TextView)convertView.findViewById(R.id.lbl_time);
				viewHolder.mColorLabelFrame = (ViewGroup)convertView.findViewById(R.id.color_label_frame);

				viewHolder.mMenu = (ImageButton)convertView.findViewById(R.id.menu);

				convertView.setTag(viewHolder);

			} else {
				viewHolder = (EventViewHolder) convertView.getTag();
			}

			final Date startTime = event.getStartTime();
			final Date endTime = event.getEndTime();

			String timeStr = "";

			if (event.isAllDayEvent()) {
				timeStr = getString(R.string.all_day);

			} else if (startTime != null && endTime != null) {
				timeStr = DateUtils.formatDateRange(getActivity(), startTime.getTime(), endTime.getTime(), 0);

			} else if (startTime != null) {
				timeStr = mTimeFormat.format(startTime);

			} else {
				timeStr = "";
			}

			timeStr = mDayOfWeekFormat.format(startTime) + ", " + timeStr;

			viewHolder.mTimeV.setText(timeStr);
			viewHolder.mTitleV.setText(event.getTitle());

			if (!Strings.isNullOrEmpty(event.getColorLabel())) {
				final int color = Color.parseColor(event.getColorLabel());
				viewHolder.mColorLabelFrame.setBackgroundColor(color);
			}

			viewHolder.mMenu.setOnClickListener(getOnMenuClickListener());

			return convertView;
		}

		@Override
		public View getHeaderView(int position, View convertView, ViewGroup parent) {
			if (isEmpty())
				return new View(getLayoutInflater().getContext());

			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.list_header, parent, false);
			}
			final ListViewHolder holder = getHolder(convertView);
			final NPEvent item = getItem(position);

			holder.getText1().setText(mMonthFormat.format(item.getStartTime()));

			return convertView;
		}

		@Override
		protected String getEntriesHeaderText() {
			return null;
		}

		@Override
		public long getHeaderId(int position) {
			final NPEvent item = getItem(position);
			final Date time = item.getStartTime();
			final Calendar calendar = Calendar.getInstance();
			calendar.setTime(time);
			return calendar.get(Calendar.MONTH);
		}

		@Override
		public int getCount() {
			return mDisplayEntryList.getEntries().size();
		}

		@Override
		public NPEvent getItem(int position) {
			if (mDisplayEntryList.isEmpty()) {
				return null;
			}
			return (NPEvent)mDisplayEntryList.getEntries().get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

	}
}
