/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.journal.ui.fragment;

import java.util.Date;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.edmondapps.utils.android.R;
import com.edmondapps.utils.android.view.ViewUtils;
import com.edmondapps.utils.java.WrapperList;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.EntryList;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.datamodel.Journal;
import com.nexuspad.dataservice.EntryListService;
import com.nexuspad.dataservice.NPException;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.ui.fragment.EntriesFragment;

/**
 * @author Edmond
 * 
 */
@ModuleId(moduleId = ServiceConstants.JOURNAL_MODULE, template = EntryTemplate.JOURNAL)
public class JournalsFragment extends EntriesFragment {

    private ViewPager mViewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.ed__layout_view_pager, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mViewPager = ViewUtils.findView(view, R.id.view_pager);

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void getEntriesInFolder(EntryListService service, Folder folder, int page) throws NPException {
        @SuppressWarnings("deprecation")
        Date start = new Date(2013 - 1900, 0, 1);
        Date end = new Date();

        service.getEntriesBetweenDates(folder, getTemplate(), start, end, page, PAGE_COUNT);
    }

    @Override
    protected void onListLoaded(EntryList list) {
        super.onListLoaded(list);

        mViewPager.setAdapter(newJournalsAdapter(list));
    }

    private JournalsAdapter newJournalsAdapter(EntryList list) {
        List<Journal> entries = new WrapperList<Journal>(list.getEntries());
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
    }
}
