package com.nexuspad.calendar.fragment;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import com.nexuspad.R;
import com.nexuspad.calendar.activity.EventActivity;
import com.nexuspad.calendar.activity.EventEditActivity;
import com.nexuspad.common.adapters.EntriesAdapter;
import com.nexuspad.common.adapters.ListViewHolder;
import com.nexuspad.common.annotation.FragmentName;
import com.nexuspad.common.annotation.ModuleId;
import com.nexuspad.common.fragment.EntriesFragment;
import com.nexuspad.common.listeners.OnEntryMenuClickListener;
import com.nexuspad.datamodel.*;
import com.nexuspad.dataservice.EntryListService;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.util.DateUtil;
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
@ModuleId(moduleId = ServiceConstants.CALENDAR_MODULE, template = EntryTemplate.EVENT)
public class EventsListFragment extends EntriesFragment {

	public static final String TAG = "EventsListFragment";
	public static final String KEY_START_DAY = "key_start_day";

	private StickyListHeadersListView mStickyHeaderEventListView;

	public static EventsListFragment of(NPFolder folder) {
		final Bundle bundle = new Bundle();
		bundle.putParcelable(KEY_FOLDER, folder);

		final EventsListFragment fragment = new EventsListFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	public static EventsListFragment of(NPFolder folder, long startDay) {
		final Bundle bundle = new Bundle();
		bundle.putParcelable(KEY_FOLDER, folder);
		bundle.putLong(KEY_START_DAY, startDay);

		final EventsListFragment fragment = new EventsListFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	private EntryList mEventList;
	private long mStartTime = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Bundle arguments = getArguments();
		mStartTime = mStartTime >= 0 ? mStartTime : arguments.getLong(KEY_START_DAY, -1);
	}

//    @Override
//    protected void getEntriesInFolder(EntryListService service, NPFolder folder, int page) throws NPException {
//        final Date midPoint = mStartTime > 0 ? new Date(mStartTime) : new Date();
//        final Date startDate = getStartDate(midPoint);
//        final Date endDate = getEndDate(midPoint);
//        service.getEntriesBetweenDates(folder, getTemplate(), startDate, endDate, page);
//    }

	private static Date getEndDate(Date midPoint) {
		return DateUtil.addDaysTo(midPoint, 60);
	}

	private static Date getStartDate(Date midPoint) {
		return DateUtil.addDaysTo(midPoint, -60);
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

		mStickyHeaderEventListView.setFastScrollEnabled(false);     // not ready for the first release

		// set the folder selector view bar
		mStickyHeaderEventListView.setOnScrollListener(newDirectionalScrollListener(null));

		// set the listener for folder selector
		initFolderSelector(ACTIVITY_REQ_CODE_FOLDER_SELECTOR);

		mStickyHeaderEventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (getAdapter() != null) {
				}
			}
		});

		//mStickyHeaderContactListView.setItemsCanFocus(true);
		mStickyHeaderEventListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	}


	@Override
	protected void onListLoaded(EntryList entryList) {
		Log.i(TAG, "Receive event list: " + String.valueOf(entryList.getEntries().size()));

		mEventList = entryList;

		EventsListAdapter adapter = (EventsListAdapter)getAdapter();

		if (adapter == null) {
			adapter = new EventsListAdapter(getActivity(), entryList, getFolder(), getEntryListService());

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

			mStickyHeaderEventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					if (getAdapter() != null) {
						final NPEvent event = ((EventsListAdapter)getAdapter()).getItem(position);
						EventActivity.startWith(getActivity(), event, getFolder());
					}
				}
			});

			mStickyHeaderEventListView.setAdapter(adapter);
			mStickyHeaderEventListView.setOnItemLongClickListener(adapter);
			setAdapter(adapter);

		} else {
			adapter.notifyDataSetChanged();
		}

		dismissProgressIndicator();

		scrollToStartTime(mEventList);
	}

	private void scrollToStartTime(EntryList eventList) {
		if (mStartTime >= 0) {
			for (NPEntry entry : (List<? extends NPEntry>)eventList.getEntries()) {
				NPEvent event = NPEvent.fromEntry(entry);

				int i = 0;
				if (event.getStartTime().getTime() >= mStartTime) {
					mListView.smoothScrollToPosition(i);
					break;
				}

				i++;
			}
		}
	}


	public void setStartTime(long startTime) {
		final Date newStartTime = new Date(startTime);
		final Date oldStartTime = new Date(mStartTime);

		final Date startDate = getStartDate(oldStartTime);
		final Date endDate = getEndDate(oldStartTime);

		mStartTime = startTime;

		if (newStartTime.after(startDate) && newStartTime.before(endDate)) {
			scrollToStartTime(mEventList);
		} else {
			queryEntriesAsync();
		}
	}


	private static class EventViewHolder {
		private ViewGroup mDateFrame;
		private TextView mDayOfWeekV;
		private TextView mDateV;
		private TextView mTitleV;
		private TextView mTimeV;
		private ImageButton mMenu;
	}

	private class EventsListAdapter extends EntriesAdapter<NPEvent> implements StickyListHeadersAdapter {
		private final DateFormat mDayOfWeekFormat = new SimpleDateFormat("EEEE");
		private final DateFormat mMonthFormat = new SimpleDateFormat("MMMM yyyy");
		private final DateFormat mDateFormat;
		private final DateFormat mTimeFormat;

		private EventsListAdapter(Activity a, EntryList entryList, NPFolder folder, EntryListService service) {
			super(a, entryList, folder, service, EntryTemplate.EVENT);
			mDateFormat = android.text.format.DateFormat.getMediumDateFormat(a);
			mTimeFormat = android.text.format.DateFormat.getTimeFormat(a);
		}

		@Override
		protected View getEntryView(NPEvent event, int position, View convertView, ViewGroup parent) {
			final EventViewHolder viewHolder;
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.list_item_event, parent, false);

				viewHolder = new EventViewHolder();
				viewHolder.mDayOfWeekV = (TextView)convertView.findViewById(R.id.lbl_day_of_week);
				viewHolder.mDateV = (TextView)convertView.findViewById(R.id.lbl_date);
				viewHolder.mTitleV = (TextView)convertView.findViewById(R.id.lbl_title);
				viewHolder.mTimeV = (TextView)convertView.findViewById(R.id.lbl_time);
				viewHolder.mDateFrame = (ViewGroup)convertView.findViewById(R.id.date_frame);
				viewHolder.mMenu = (ImageButton)convertView.findViewById(R.id.menu);

				convertView.setTag(viewHolder);
			} else {
				viewHolder = (EventViewHolder) convertView.getTag();
			}

			final Date startTime = event.getStartTime();
			final Date endTime = event.getEndTime();
			final int color = Color.parseColor(event.getColorLabel());

			final String timeStr;
			if (event.isAllDayEvent()) {
				timeStr = getString(R.string.all_day);
			} else if (startTime != null && endTime != null) {
				timeStr = DateUtils.formatDateRange(getActivity(), startTime.getTime(), endTime.getTime(), 0);
			} else if (startTime != null) {
				timeStr = mTimeFormat.format(startTime);
			} else {
				timeStr = "";
			}

			viewHolder.mTimeV.setText(timeStr);
			viewHolder.mDayOfWeekV.setText(mDayOfWeekFormat.format(startTime));
			viewHolder.mDateV.setText(mDateFormat.format(startTime));
			viewHolder.mTitleV.setText(event.getTitle());
			viewHolder.mDateFrame.setBackgroundColor(color);

			viewHolder.mMenu.setOnClickListener(getOnMenuClickListener());

			return convertView;
		}

		@Override
		public View getHeaderView(int position, View convertView, ViewGroup parent) {
			if (isEmpty()) return new View(getLayoutInflater().getContext()); // empty view (no header)
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
	}
}
