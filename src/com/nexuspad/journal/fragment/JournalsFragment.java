/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.journal.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.nexuspad.R;
import com.nexuspad.service.account.AccountManager;
import com.nexuspad.app.App;
import com.nexuspad.common.annotation.FragmentName;
import com.nexuspad.common.annotation.ModuleId;
import com.nexuspad.common.fragment.EntriesFragment;
import com.nexuspad.service.datamodel.*;
import com.nexuspad.service.dataservice.EntryService;
import com.nexuspad.service.dataservice.JournalService;
import com.nexuspad.service.dataservice.NPException;
import com.nexuspad.service.dataservice.ServiceConstants;
import com.nexuspad.service.util.DateUtil;

import java.util.*;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Ren
 */
@FragmentName(JournalsFragment.TAG)
@ModuleId(moduleId = ServiceConstants.JOURNAL_MODULE, template = EntryTemplate.JOURNAL)
public class JournalsFragment extends EntriesFragment {
	public static final String TAG = "JournalsFragment";

	private ViewPager mViewPager;
	private JournalsAdapter mPagerAdapter;

	private int scrollToPage;

	public static final int LEFT_PAGE = 0;
	public static final int RIGHT_PAGE = 2;
	public static final int CENTER_PAGE = 1;

	private JournalsCallback mJournalsCallback;

	private Date mStartDate;
	private Date mEndDate;

	private TreeMap<String, NPJournal> mJournals = new TreeMap<String, NPJournal>();

	private JournalService mJournalService;

	private enum PagingDirection {
		LEFT, RIGHT;
	}

	public interface JournalsCallback {
		void onListLoaded(EntriesFragment f, EntryList list);
		void onJournalSelected(JournalsFragment f, String dateString);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mJournalsCallback = App.getCallbackOrThrow(activity, JournalsCallback.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.journals_frag, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		mViewPager = (ViewPager)view.findViewById(R.id.view_pager);
		super.onViewCreated(view, savedInstanceState);

		queryEntriesAsync();
	}

	@Override
	public void onPause() {
		super.onPause();
		saveJournalsInFragments();
	}

	/**
	 * Save the journals in the fragments, get the edited text before saving.
	 */
	private void saveJournalsInFragments() {
		final EntryService entryService = getEntryService();
		final FragmentActivity activity = getActivity();

		if (mPagerAdapter != null) {
			List<JournalEditFragment> frags = mPagerAdapter.getJournalEditFragments();
			for (JournalEditFragment frag : frags) {
				if (frag.journalEdited()) {
					NPJournal updatedJournal = frag.getUpdatedJournal();
					if (!isNullOrEmpty(updatedJournal.getNote())) {
						Log.i(TAG, "Save journal for: " + updatedJournal.getJournalYmd());
						entryService.safePutEntry(activity, updatedJournal);
					}
				}
			}
		}
	}

	public void setDisplayDate(long date) {
		Log.i(TAG, "setDisplayDate is called...........");
		Date selectedDate = new Date(date);
		mStartDate = DateUtil.addDaysTo(selectedDate, -1);
		mEndDate = DateUtil.addDaysTo(selectedDate, 1);
		queryEntriesAsync();
	}

	/**
	 * Request the first 3 journals
	 */
	@Override
	protected void queryEntriesAsync() {
		if (mStartDate == null) {
			Date today = DateUtil.now();
			mStartDate = DateUtil.addDaysTo(today, -1);
			mEndDate = DateUtil.addDaysTo(today, 1);
		}

		try {
			mFolder.setOwner(AccountManager.currentAccount());
			getEntryListService().getEntriesBetweenDates(mFolder, EntryTemplate.JOURNAL, DateUtil.convertToYYYYMMDD(mStartDate),
					DateUtil.convertToYYYYMMDD(mEndDate), 1);
		} catch (NPException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onListLoaded(EntryList list) {
		Log.i(TAG, "Receiving entry list.");

		dismissProgressIndicator();

		mJournals.clear();

		List<NPJournal> journals = new ArrayList<NPJournal>();
		for (NPEntry e : (List<? extends NPEntry>)list.getEntries()) {
			journals.add(NPJournal.fromEntry(e));
		}

		for (Date day = mStartDate; day.compareTo(mEndDate) <= 0; day = DateUtil.addDaysTo(day, 1)) {
			final Date theDay = day;

			NPJournal journal = Iterables.tryFind(journals, new Predicate<NPJournal>() {
				@Override
				public boolean apply(NPJournal journal) {
					final Date time = journal.getCreateTime();
					return DateUtil.isSameDay(time, theDay);
				}
			}).orNull();  // we won't call createJournalForDate() unless we have to; JAVA 8 LAMBDA PLS

			if (journal == null) {
				journal = createJournalForDate(theDay);
			}
			mJournals.put(journal.getJournalYmd(), journal);
		}

		saveJournalsInFragments();

		/*
		 * Create adapter with initial journals.
		 */
		mPagerAdapter = new JournalsAdapter();

		for (String ymd : mJournals.keySet()) {
			NPJournal j = mJournals.get(ymd);
			JournalEditFragment jef = JournalEditFragment.of(j);
			mPagerAdapter.addPage(jef, PagingDirection.RIGHT);
		}

		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setCurrentItem(CENTER_PAGE);

		/*
		 * Handles changing page.
		 */
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageScrollStateChanged(int state) {

				if (state == ViewPager.SCROLL_STATE_IDLE) {
					String retrieveJournalYmd = null;

					saveJournalsInFragments();

					if (scrollToPage == LEFT_PAGE) {
						mStartDate = DateUtil.addDaysTo(mStartDate, -1);
						mEndDate = DateUtil.addDaysTo(mEndDate, -1);
						retrieveJournalYmd = DateUtil.convertToYYYYMMDD(mStartDate);

					} else if (scrollToPage == RIGHT_PAGE) {
						mStartDate = DateUtil.addDaysTo(mStartDate, 1);
						mEndDate = DateUtil.addDaysTo(mEndDate, 1);
						retrieveJournalYmd = DateUtil.convertToYYYYMMDD(mEndDate);
					}

					mPagerAdapter = new JournalsAdapter();

					NPJournal j = getJournal(DateUtil.convertToYYYYMMDD(mStartDate));
					JournalEditFragment newFrag = JournalEditFragment.of(j);
					mPagerAdapter.addPage(newFrag, PagingDirection.RIGHT);

					j = getJournal(DateUtil.convertToYYYYMMDD(DateUtil.addDaysTo(mStartDate, 1)));
					newFrag = JournalEditFragment.of(j);
					mPagerAdapter.addPage(newFrag, PagingDirection.RIGHT);

					j = getJournal(DateUtil.convertToYYYYMMDD(mEndDate));
					newFrag = JournalEditFragment.of(j);
					mPagerAdapter.addPage(newFrag, PagingDirection.RIGHT);

					mViewPager.setAdapter(null);
					mViewPager.setAdapter(mPagerAdapter);
					mViewPager.setCurrentItem(CENTER_PAGE, false);

					try {
						getJournalService().getJournal(retrieveJournalYmd);
					} catch (NPException e) {
						Log.e(TAG, "Error getting journal: " + e.toString());
					}

					// Update the date in action bar based on the current pager item.
					JournalEditFragment currentFrag = mPagerAdapter.getJournalEditFragment(mViewPager.getCurrentItem());
					mJournalsCallback.onJournalSelected(JournalsFragment.this, currentFrag.getJournalDateYmd());
				}
			}

			@Override
			public void onPageSelected(int position) {
				scrollToPage = position;
			}
		});

		// Update the date in action bar based on the current pager item.
		JournalEditFragment currentFrag = mPagerAdapter.getJournalEditFragment(mViewPager.getCurrentItem());
		mJournalsCallback.onJournalSelected(JournalsFragment.this, currentFrag.getJournalDateYmd());
	}

	/**
	 * On receiving journal to be added to the view pager.
	 *
	 * @param entry
	 */
	@Override
	protected void onGetEntry(NPEntry entry) {
		Log.i(TAG, "Receive journal");

		NPJournal j = NPJournal.fromEntry(entry);
		mJournals.put(j.getJournalYmd(), j);
	}

	/**
	 * Get entry from mJournals or create new NPJournal.
	 *
	 * @param ymd
	 * @return
	 */
	private NPJournal getJournal(String ymd) {
		NPJournal j = mJournals.get(ymd);
		if (j == null) {
			j = new NPJournal();
			j.setJournalYmd(ymd);
		}

		return j;
	}

	private NPJournal createJournalForDate(Date date) {
		final NPJournal emptyJournal = new NPJournal(NPFolder.rootFolderOf(NPModule.JOURNAL));
		emptyJournal.setCreateTime(date);
		return emptyJournal;
	}

	private JournalService getJournalService() {
		if (mJournalService == null) {
			mJournalService = new JournalService(getActivity());
		}
		return mJournalService;
	}


	/**
	 * JournalsAdapter.
	 */
	private class JournalsAdapter extends FragmentStatePagerAdapter {
		private TreeMap<String, JournalEditFragment> journalEditFrags = new TreeMap<String, JournalEditFragment>();

		private JournalsAdapter() {
			super(getChildFragmentManager());
		}

		public JournalEditFragment getJournalEditFragment(int position) {
			NavigableSet<String> orderedYmds = journalEditFrags.navigableKeySet();
			int i = 0;
			for (String ymd : orderedYmds) {
				if (i == position) {
					JournalEditFragment frag = journalEditFrags.get(ymd);
					return frag;
				}
				i++;
			}

			return null;
		}

		public boolean beginningOfPager(int position) {
			if (position == 0) {
				return true;
			}
			return false;
		}

		public boolean endOfPager(int position) {
			if (position == 2) {
				return true;
			}
			return false;
		}

		public void addPage(JournalEditFragment fragment, PagingDirection direction) {
			Log.i(TAG, "Add journal fragment for " + fragment.getJournalDateYmd());
			journalEditFrags.put(fragment.getJournalDateYmd(), fragment);

			if (direction == PagingDirection.RIGHT) {
				Date dropDate = DateUtil.addDaysTo(DateUtil.parseFromYYYYMMDD(fragment.getJournalDateYmd()), -3);
				journalEditFrags.remove(DateUtil.convertToYYYYMMDD(dropDate));
			}
		}

		@Override
		public Fragment getItem(int position) {
			Log.i(TAG, "getItem called...." + String.valueOf(position));
			return getJournalEditFragment(position);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			Log.i(TAG, "destroyItem called...." + String.valueOf(position));
			super.destroyItem(container, position, object);
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}

		public List<JournalEditFragment> getJournalEditFragments() {
			return new ArrayList<JournalEditFragment>(journalEditFrags.values());
		}
	}
}
