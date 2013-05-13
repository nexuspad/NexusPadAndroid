/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.doc.ui.fragment;

import com.nexuspad.datamodel.EntryList;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.ui.fragment.EntriesFragment;

/**
 * @author Edmond
 * 
 */
public class DocsFragment extends EntriesFragment {
    @Override
    protected void onListLoaded(EntryList list) {
        super.onListLoaded(list);

        // TODO
    }

    @Override
    protected int getModule() {
        return ServiceConstants.DOC_MODULE;
    }

    @Override
    protected EntryTemplate getTemplate() {
        return EntryTemplate.DOC;
    }
}
