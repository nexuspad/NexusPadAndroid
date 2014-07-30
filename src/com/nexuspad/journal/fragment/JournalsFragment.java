/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.journal.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.nexuspad.R;
import com.nexuspad.account.AccountManager;
import com.nexuspad.app.App;
import com.nexuspad.common.annotation.FragmentName;
import com.nexuspad.common.annotation.ModuleId;
import com.nexuspad.common.fragment.EntriesFragment;
import com.nexuspad.datamodel.*;
import com.nexuspad.dataservice.EntryService;
import com.nexuspad.dataservice.JournalService;
import com.nexuspad.dataservice.NPException;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.util.DateUtil;

import java.util.*;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Edmond
 */
@FragmentName(JournalsFragment.TAG)
@ModuleId(moduleId = ServiceConstants.JOURNAL_MODULE, template = EntryTemplate.JOURNAL)
public class JournalsFragment extends EntriesFragment {
	public static final String TAG = "JournalsFragment";

	public interface JournalsCallback {
		void onListLoaded(EntriesFragment f, EntryList list);
		void onJournalSelected(JournalsFragment f, NPJournal journal);
	}

	private ViewPager mViewPager;
	private JournalsAdapter mPagerAdapter;

	private JournalsCallback mJournalsCallback;
	private Date mStartDate;
	private Date mEndDate;

	private Map<String, NPJournal> mJournals = new HashMap<String, NPJournal>();

	private JournalService mJournalService;

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
	}

	@Override
	public void onPause() {
		super.onPause();

		updateJournalsFromUI();
		final EntryService entryService = getEntryService();
		final FragmentActivity activity = getActivity();
		for (NPJournal journal : mJournals.values()) {
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
			final SparseArray<JournalEditFragment> fragments = mPagerAdapter.getAliveFragments();
			for (int i = 0, length = fragments.size(); i < length; ++i) {
				final int position = fragments.keyAt(i);
				final JournalEditFragment fragment = fragments.valueAt(i);
				mPagerAdapter.updateJournalFromUI(position, fragment);
			}
		}
	}

	@Override
	protected void queryEntriesAsync() {
		final long now = System.currentTimeMillis();
		mStartDate = DateUtil.getFirstDateOfTheMonth(now);
		mEndDate = DateUtil.getEndDateOfTheMonth(now);
		try {
			mFolder.setOwner(AccountManager.currentAccount());
			getEntryListService().getEntriesBetweenDates(mFolder, EntryTemplate.JOURNAL, mStartDate, mEndDate, 1);
		} catch (NPException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onListLoaded(EntryList list) {
		Log.i(TAG, "Receiving entry list.");

		fadeInListFrame();

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

		if (mPagerAdapter != null) {
			mPagerAdapter.notifyDataSetChanged();

		} else {
			mPagerAdapter = new JournalsAdapter();
			mViewPager.setAdapter(mPagerAdapter);

			/*
			 * Handles changing page.
			 */
			mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
				@Override
				public void onPageSelected(int position) {
					Log.i("TRACK POSITION: ", String.valueOf(position));

					// Request the next journal, shift the start/end dates
					if (mPagerAdapter.beginningOfPager(position)) {
						mStartDate = DateUtil.addDaysTo(mStartDate, -1);
						mEndDate = DateUtil.addDaysTo(mStartDate, 2);

						try {
							getJournalService().getJournal(DateUtil.convertToYYYYMMDD(mStartDate));
						} catch (NPException e) {
						}

					} else if (mPagerAdapter.endOfPager(position)) {
						mStartDate = DateUtil.addDaysTo(mStartDate, 1);
						mEndDate = DateUtil.addDaysTo(mEndDate, 1);

						try {
							getJournalService().getJournal(DateUtil.convertToYYYYMMDD(mEndDate));
						} catch (NPException e) {
						}
					}

					mJournalsCallback.onJournalSelected(JournalsFragment.this, mPagerAdapter.getJournal(position));
				}
			});

			final int entriesSize = mJournals.size();
			if (entriesSize > 0) {
				int initialIndex = 0;
				for (int i = 0; i < entriesSize; i++) {
					final NPJournal journal = mJournals.get(i);
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
	private class JournalsAdapter extends FragmentPagerAdapter {
		private SparseArray<JournalEditFragment> mAliveFragments = new SparseArray<JournalEditFragment>();

		private JournalsAdapter() {
			super(getChildFragmentManager());
		}

		private NPJournal getJournal(int pos) {
			return mJournals.get(pos);
		}

		public boolean beginningOfPager(int position) {
			if (position == 0) {
				return true;
			}
			return false;
		}

		public boolean endOfPager(int position) {
			if (position == mAliveFragments.size() - 1) {
				return true;
			}
			return false;
		}

		@Override
		public Fragment getItem(int pos) {
			return JournalEditFragment.of(getJournal(pos), NPFolder.rootFolderOf(NPModule.JOURNAL));
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			final JournalEditFragment fragment = (JournalEditFragment) super.instantiateItem(container, position);
			mAliveFragments.put(position, fragment);
			return fragment;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			super.destroyItem(container, position, object);
			updateJournalFromUI(position, (JournalEditFragment) object);
			mAliveFragments.remove(position);
		}

		public void updateJournalFromUI(int position, JournalEditFragment fragment) {
			final NPJournal journal = fragment.getEditedEntry();
			mJournals.put(journal.getJournalYmd(), journal);
		}

		@Override
		public int getCount() {
			return mJournals.size();
		}

		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}

		public SparseArray<JournalEditFragment> getAliveFragments() {
			return mAliveFragments;
		}

	}
}
