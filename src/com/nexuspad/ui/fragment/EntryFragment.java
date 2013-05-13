/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui.fragment;

import java.lang.ref.WeakReference;

import android.os.Bundle;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.edmondapps.utils.java.Lazy;
import com.nexuspad.R;
import com.nexuspad.datamodel.NPEntry;
import com.nexuspad.dataservice.EntryService;
import com.nexuspad.dataservice.EntryServiceCallback;
import com.nexuspad.dataservice.ServiceError;

/**
 * @author Edmond
 * 
 */
public abstract class EntryFragment<T extends NPEntry> extends SherlockFragment {
    public static final String KEY_ENTRY = "com.nexuspad.ui.fragment.EntryFragment.entry";

    private final Lazy<EntryService> mEntryService = new Lazy<EntryService>() {
        @Override
        protected EntryService onCreate() {
            return new EntryService(getActivity(), new EntryCallback(EntryFragment.this));
        }
    };

    private T mEntry;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            mEntry = arguments.getParcelable(KEY_ENTRY);
        }
    }

    public T getEntry() {
        return mEntry;
    }

    public void setEntry(T entry) {
        mEntry = entry;
    }

    public EntryService getEntryService() {
        return mEntryService.get();
    }

    protected void onEntryUpdated(T entry) {
    }

    protected void onEntryUpdateFailed(ServiceError error) {
        Toast.makeText(getActivity(), R.string.err_internal, Toast.LENGTH_LONG).show();
    }

    @SuppressWarnings("unchecked")
    private void onEntryUpdatedInternal(NPEntry e) {
        onEntryUpdated((T)e);
    }

    private static class EntryCallback implements EntryServiceCallback {
        private final WeakReference<EntryFragment<?>> mFragment;

        private EntryCallback(EntryFragment<?> f) {
            mFragment = new WeakReference<EntryFragment<?>>(f);
        }

        @Override
        public void successfulRetrieval(NPEntry entry) {
        }

        @Override
        public void successfulUpdate(NPEntry updatedEntry) {
            EntryFragment<?> fragment = mFragment.get();
            if ( (fragment != null) && fragment.isAdded()) {
                fragment.onEntryUpdatedInternal(updatedEntry);
            }
        }

        @Override
        public void failureCallback(ServiceError error) {
            EntryFragment<?> fragment = mFragment.get();
            if ( (fragment != null) && fragment.isAdded()) {
                fragment.onEntryUpdateFailed(error);
            }
        }
    }
}
