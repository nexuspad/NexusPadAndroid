package com.nexuspad.calendar.ui.fragment;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.edmondapps.utils.java.WrapperList;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.calendar.ui.activity.EventActivity;
import com.nexuspad.datamodel.EntryList;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Event;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.dataservice.EntryListService;
import com.nexuspad.dataservice.NPException;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.ui.EntriesAdapter;
import com.nexuspad.ui.fragment.EntriesFragment;
import com.nexuspad.util.DateUtil;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.edmondapps.utils.android.view.ViewUtils.findView;

/**
 * User: edmond
 */
@FragmentName(EventsAgendaFragment.TAG)
@ModuleId(moduleId = ServiceConstants.CALENDAR_MODULE, template = EntryTemplate.EVENT)
public class EventsAgendaFragment extends EntriesFragment {
    public static final String TAG = "EventsAgendaFragment";
    public static final String KEY_START_DAY = "key_start_day";

    public static EventsAgendaFragment of(Folder folder) {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_FOLDER, folder);

        final EventsAgendaFragment fragment = new EventsAgendaFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public static EventsAgendaFragment of(Folder folder, long startDay) {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_FOLDER, folder);
        bundle.putLong(KEY_START_DAY, startDay);

        final EventsAgendaFragment fragment = new EventsAgendaFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private List<Event> mEvents = new ArrayList<Event>();
    private long mStartTime = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle arguments = getArguments();
        mStartTime = mStartTime >= 0 ? mStartTime : arguments.getLong(KEY_START_DAY, -1);
    }

    @Override
    protected void getEntriesInFolder(EntryListService service, Folder folder, int page) throws NPException {
        final Date midPoint = mStartTime > 0 ? new Date(mStartTime) : new Date();
        final Date startDate = DateUtil.addDaysTo(midPoint, -60);
        final Date endDate = DateUtil.addDaysTo(midPoint, 60);
        service.getEntriesBetweenDates(folder, getTemplate(), startDate, endDate, page, getEntriesCountPerPage());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_content_sticky, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setListAdapter(new EventsAgendaAdapter(mEvents));
    }

    @Override
    protected void onListLoaded(EntryList list) {
        final List<Event> events = new WrapperList<Event>(list.getEntries());
        mEvents.clear();
        mEvents.addAll(events);

        getListAdapter().notifyDataSetChanged();
        scrollToStartTime(events);

        super.onListLoaded(list);
    }

    private void scrollToStartTime(List<Event> events) {
        if (mStartTime >= 0) {
            for (int i = 0, eventsSize = events.size(); i < eventsSize; i++) {
                final Event event = events.get(i);
                if (event.getStartTime().getTime() >= mStartTime) {
                    getListViewManager().smoothScrollToPosition(i);
                    break;
                }
            }
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        final EventsAgendaAdapter adapter = (EventsAgendaAdapter) getListAdapter();
        EventActivity.startWith(getActivity(), adapter.getItem(position), getFolder());
    }

    public void setStartTime(long startTime) {
        mStartTime = startTime;
        scrollToStartTime(mEvents);
    }

    private static class ViewHolder {
        private ViewGroup mDateFrame;
        private TextView mDayOfWeekV;
        private TextView mDateV;
        private TextView mTitleV;
        private TextView mTimeV;
    }

    private class EventsAgendaAdapter extends EntriesAdapter<Event> implements StickyListHeadersAdapter {
        private final DateFormat mDayOfWeekFormat = new SimpleDateFormat("EEEE");
        private final DateFormat mMonthFormat = new SimpleDateFormat("MMMM yyyy");
        private final DateFormat mDateFormat;
        private final DateFormat mTimeFormat;

        private EventsAgendaAdapter(List<? extends Event> list) {
            this(getActivity(), list);
        }

        private EventsAgendaAdapter(Activity activity, List<? extends Event> list) {
            super(activity, list);
            mDateFormat = android.text.format.DateFormat.getMediumDateFormat(activity);
            mTimeFormat = android.text.format.DateFormat.getTimeFormat(activity);
        }

        @Override
        protected View getEntryView(Event event, int position, View convertView, ViewGroup parent) {
            final EventsAgendaFragment.ViewHolder viewHolder;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_event, parent, false);

                viewHolder = new EventsAgendaFragment.ViewHolder();
                viewHolder.mDayOfWeekV = findView(convertView, R.id.lbl_day_of_week);
                viewHolder.mDateV = findView(convertView, R.id.lbl_date);
                viewHolder.mTitleV = findView(convertView, R.id.lbl_title);
                viewHolder.mTimeV = findView(convertView, R.id.lbl_time);
                viewHolder.mDateFrame = findView(convertView, R.id.date_frame);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (EventsAgendaFragment.ViewHolder) convertView.getTag();
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
            if(isEmpty()) return new View(getLayoutInflater().getContext()); // empty view (no header)
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_header, parent, false);
            }
            final ViewHolder holder = getHolder(convertView);
            final Event item = getItem(position);

            holder.text1.setText(mMonthFormat.format(item.getStartTime()));

            return convertView;
        }

        @Override
        protected View getEmptyEntryView(LayoutInflater i, View c, ViewGroup p) {
            return getCaptionView(i, c, p, R.string.empty_events, R.drawable.event); //empty event
        }

        @Override
        protected int getEntryStringId() {
            return 0;
        }

        @Override
        public long getHeaderId(int position) {
            final Event item = getItem(position);
            final Date time = item.getStartTime();
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(time);
            return calendar.get(Calendar.MONTH);
        }
    }
}
