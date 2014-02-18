/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.journal.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.edmondapps.utils.android.view.ViewUtils;
import com.edmondapps.utils.java.WrapperList;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.*;
import com.nexuspad.dataservice.EntryListService;
import com.nexuspad.dataservice.NPException;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.ui.fragment.EntriesFragment;
import com.nexuspad.util.DateUtil;
import com.nexuspad.util.Logs;

import java.util.Date;
import java.util.List;

/**
 * @author Edmond
 */
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
    }

    private ViewPager mViewPager;

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
    protected void getEntriesInFolder(EntryListService service, Folder folder, int page) throws NPException {
        final long now = System.currentTimeMillis();
        final Date startDate = DateUtil.getFirstDateOfTheMonth(now);
        final Date endDate = DateUtil.getEndDateOfTheMonth(now);
        service.getEntriesBetweenDates(folder, getTemplate(), startDate, endDate, page, getEntriesCountPerPage());
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
        final JournalsAdapter adapter = (JournalsAdapter) mViewPager.getAdapter();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        } else {
            mViewPager.setAdapter(newJournalsAdapter(list));
        }
    }

    private JournalsAdapter newJournalsAdapter(EntryList list) {
        final List<Journal> entries = new WrapperList<Journal>(list.getEntries());
        return new JournalsAdapter(entries, getFolder(), getChildFragmentManager());
    }

    private static class JournalsAdapter extends FragmentPagerAdapter {
        private final List<? extends Journal> mList;
        private final Folder mFolder;

        public JournalsAdapter(List<? extends Journal> list, Folder folder, FragmentManager fm) {
            super(fm);
            mList = list;
            mFolder = folder;
        }

        private Journal getJournal(int pos) {
            return mList.get(pos);
        }

        @Override
        public Fragment getItem(int pos) {
            return JournalFragment.of(getJournal(pos), mFolder);
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }
}
