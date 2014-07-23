package com.nexuspad.calendar.ui.fragment;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.common.adapters.ListEntriesAdapter;
import com.nexuspad.common.adapters.ListViewHolder;
import com.nexuspad.common.annotaion.FragmentName;
import com.nexuspad.common.fragment.EntriesFragment;
import com.nexuspad.datamodel.EntryList;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.NPEvent;
import com.nexuspad.datamodel.NPFolder;
import com.nexuspad.dataservice.EntryListService;
import com.nexuspad.dataservice.NPException;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.util.DateUtil;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * User: edmond
 */
@FragmentName(EventsAgendaFragment.TAG)
@ModuleId(moduleId = ServiceConstants.CALENDAR_MODULE, template = EntryTemplate.EVENT)
public class EventsAgendaFragment extends EntriesFragment {
    public static final String TAG = "EventsAgendaFragment";
    public static final String KEY_START_DAY = "key_start_day";

    public static EventsAgendaFragment of(NPFolder folder) {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_FOLDER, folder);

        final EventsAgendaFragment fragment = new EventsAgendaFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public static EventsAgendaFragment of(NPFolder folder, long startDay) {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_FOLDER, folder);
        bundle.putLong(KEY_START_DAY, startDay);

        final EventsAgendaFragment fragment = new EventsAgendaFragment();
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

    @Override
    protected void getEntriesInFolder(EntryListService service, NPFolder folder, int page) throws NPException {
        final Date midPoint = mStartTime > 0 ? new Date(mStartTime) : new Date();
        final Date startDate = getStartDate(midPoint);
        final Date endDate = getEndDate(midPoint);
        service.getEntriesBetweenDates(folder, getTemplate(), startDate, endDate, page, PAGE_COUNT);
    }

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
        return inflater.inflate(R.layout.list_content_sticky, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void onListLoaded(EntryList list) {
		mEventList = list;

        final BaseAdapter adapter = getListAdapter();
        if (adapter == null) {
            setListAdapter(new EventsAgendaAdapter(mEventList));
        } else {
            adapter.notifyDataSetChanged();
        }

        scrollToStartTime(mEventList);

        super.onListLoaded(list);
    }

    private void scrollToStartTime(EntryList eventList) {
        if (mStartTime >= 0) {
            for (int i = 0, eventsSize = eventList.getEntries().size(); i < eventsSize; i++) {
                final NPEvent event = NPEvent.fromEntry(eventList.getEntries().get(i));
                if (event.getStartTime().getTime() >= mStartTime) {
//                    getListViewManager().smoothScrollToPosition(i);
                    break;
                }
            }
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
//        final EventsAgendaAdapter adapter = (EventsAgendaAdapter) getListAdapter();
//        EventActivity.startWith(getActivity(), adapter.getItem(position), getFolder());
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
    }

    private class EventsAgendaAdapter extends ListEntriesAdapter<NPEvent> implements StickyListHeadersAdapter {
        private final DateFormat mDayOfWeekFormat = new SimpleDateFormat("EEEE");
        private final DateFormat mMonthFormat = new SimpleDateFormat("MMMM yyyy");
        private final DateFormat mDateFormat;
        private final DateFormat mTimeFormat;

        private EventsAgendaAdapter(EntryList eventList) {
            this(getActivity(), eventList);
        }

        private EventsAgendaAdapter(Activity activity, EntryList eventList) {
            super(activity, eventList, getFolder(), getEntryListService(), getTemplate());
            mDateFormat = android.text.format.DateFormat.getMediumDateFormat(activity);
            mTimeFormat = android.text.format.DateFormat.getTimeFormat(activity);
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
        protected View getEmptyEntryView(LayoutInflater i, View c, ViewGroup p) {
            return getCaptionView(i, c, p, R.string.empty_events, R.drawable.event); //empty event
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
