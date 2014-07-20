/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.journal.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.DateUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.edmondapps.utils.android.view.ViewUtils;
import com.edmondapps.utils.java.WrapperList;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.app.App;
import com.nexuspad.datamodel.EntryList;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.datamodel.Journal;
import com.nexuspad.dataservice.EntryListService;
import com.nexuspad.dataservice.EntryService;
import com.nexuspad.dataservice.NPException;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.ui.fragment.EntriesFragment;
import com.nexuspad.ui.view.EndlessViewPager;
import com.nexuspad.util.DateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Edmond
 */
@FragmentName(JournalsFragment.TAG)
@ModuleId(moduleId = ServiceConstants.JOURNAL_MODULE, template = EntryTemplate.JOURNAL)
public class JournalsFragment extends EntriesFragment {
    public static final String TAG = "com.nexuspad.journal.ui.fragment.JournalsFragment";

    public static JournalsFragment of(Folder folder) {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_FOLDER, folder);

        final JournalsFragment fragment = new JournalsFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public interface Callback extends EntriesFragment.Callback {
        void onJournalSelected(JournalsFragment f, Journal journal);
    }

    private EndlessViewPager mViewPager;
    private Callback mCallback;
    private Date mStartDate;
    private Date mEndDate;

    private List<Journal> mJournals = new ArrayList<Journal>();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = App.getCallbackOrThrow(activity, Callback.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.journals_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mViewPager = ViewUtils.findView(view, R.id.view_pager);

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();

        updateJournalsFromUI();
        final EntryService entryService = getEntryService();
        final FragmentActivity activity = getActivity();
        for (Journal journal : mJournals) {
            if (!isNullOrEmpty(journal.getNote())) {
                entryService.safePutEntry(activity, journal);
            }
        }
    }

    public void setDisplayDate(long date) {
        final Date target = new Date(date);
        if (mStartDate.compareTo(target) <= 0 && target.compareTo(mEndDate) <= 0) {
            final int daysDiff = DateUtil.daysBetween(mStartDate, target);
            if (mViewPager != null) {
                mViewPager.setCurrentItem(daysDiff, false);  // mStartDate is at 0, daysDiff is the index
            }
        }
    }

    private void updateJournalsFromUI() {
        if (mViewPager != null) {
            final EndlessViewPager.EndlessAdapter adapter = mViewPager.getAdapter();
            if (adapter != null) {
                final JournalsAdapter journalsAdapter = (JournalsAdapter) adapter.getRealAdapter();
                final SparseArray<NewJournalFragment> fragments = journalsAdapter.getAliveFragments();
                for (int i = 0, length = fragments.size(); i < length; ++i) {
                    final int position = fragments.keyAt(i);
                    final NewJournalFragment fragment = fragments.valueAt(i);
                    journalsAdapter.updateJournalFromUI(position, fragment);
                }
            }
        }
    }

    @Override
    protected void getEntriesInFolder(EntryListService service, Folder folder, int page) throws NPException {
        final long now = System.currentTimeMillis();
        mStartDate = DateUtil.getFirstDateOfTheMonth(now);
        mEndDate = DateUtil.getEndDateOfTheMonth(now);
        service.getEntriesBetweenDates(folder, getTemplate(), mStartDate, mEndDate, page, PAGE_COUNT);
    }

    @Override
    protected void onListLoaded(EntryList list) {
        super.onListLoaded(list);
        handleNewList(list);
        fadeInListFrame();
    }

    @Override
    protected void onEntryListUpdated() {
        super.onEntryListUpdated();
        handleNewList(getEntryList());
    }

    private void handleNewList(EntryList list) {
        final PagerAdapter adapter = mViewPager.getAdapter();
        updateJournalList(list);

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        } else {
            final JournalsAdapter journalsAdapter = new JournalsAdapter();
            mViewPager.setAdapter(journalsAdapter);
            mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    position = mViewPager.getAdapter().getRealPosition(position);
                    onJournalSelected(journalsAdapter.getJournal(position));
                }
            });

            final int entriesSize = mJournals.size();
            if (entriesSize > 0) {
                int initialIndex = 0;
                for (int i = 0; i < entriesSize; i++) {
                    final Journal journal = mJournals.get(i);
                    final long journalTime = journal.getCreateTime().getTime();
                    if (DateUtils.isToday(journalTime)) {
                        initialIndex = i;
                        break;
                    }
                }
                mViewPager.setCurrentItem(initialIndex);  // onJournalSelected will get called (from the above OnPageChangeListener)
            }
        }
    }

    private void onJournalSelected(Journal journal) {
        mCallback.onJournalSelected(JournalsFragment.this, journal);
    }

    private void updateJournalList(EntryList list) {
        mJournals.clear();

        final WrapperList<Journal> journals = new WrapperList<Journal>(list.getEntries());
        for (Date day = mStartDate; day.compareTo(mEndDate) <= 0; day = DateUtil.addDaysTo(day, 1)) {
            final Date theDay = day;

            Journal journal = Iterables.tryFind(journals, new Predicate<Journal>() {
                @Override
                public boolean apply(Journal journal) {
                    final Date time = journal.getCreateTime();
                    return DateUtil.isSameDay(time, theDay);
                }
            }).orNull();  // we won't call createJournalForDate() unless we have to; JAVA 8 LAMBDA PLS

            if (journal == null) {
                journal = createJournalForDate(theDay);
            }
            mJournals.add(journal);
        }
    }

    private Journal createJournalForDate(Date date) {
        final Journal emptyJournal = new Journal(getFolder());
        emptyJournal.setCreateTime(date);
        return emptyJournal;
    }

    private class JournalsAdapter extends FragmentPagerAdapter {
        private SparseArray<NewJournalFragment> mAliveFragments = new SparseArray<NewJournalFragment>();

        private JournalsAdapter() {
            super(getChildFragmentManager());
        }

        private Journal getJournal(int pos) {
            return mJournals.get(pos);
        }

        @Override
        public Fragment getItem(int pos) {
            return NewJournalFragment.of(getJournal(pos), getFolder());
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final NewJournalFragment fragment = (NewJournalFragment) super.instantiateItem(container, position);
            mAliveFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
            updateJournalFromUI(position, (NewJournalFragment) object);
            mAliveFragments.remove(position);
        }

        public void updateJournalFromUI(int position, NewJournalFragment fragment) {
            final Journal journal = fragment.getEditedEntry();
            mJournals.remove(position);
            mJournals.add(position, journal);
        }

        @Override
        public int getCount() {
            return mJournals.size();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        public SparseArray<NewJournalFragment> getAliveFragments() {
            return mAliveFragments;
        }

    }
}
