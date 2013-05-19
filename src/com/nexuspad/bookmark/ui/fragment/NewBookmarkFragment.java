/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.bookmark.ui.fragment;

import static com.edmondapps.utils.android.view.ViewUtils.findView;
import static com.edmondapps.utils.android.view.ViewUtils.isAllTextNotEmpty;

import java.util.Collections;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;

import com.edmondapps.utils.android.annotaion.FragmentName;
import com.nexuspad.R;
import com.nexuspad.datamodel.Bookmark;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.ui.FoldersAdapter;
import com.nexuspad.ui.fragment.NewEntryFragment;

/**
 * @author Edmond
 * 
 */
@FragmentName(NewBookmarkFragment.TAG)
public class NewBookmarkFragment extends NewEntryFragment<Bookmark> {
    public static final String TAG = "NewBookmarkFragment";

    public static NewBookmarkFragment of(Bookmark bookmark) {
        return newInstance(bookmark, bookmark.getFolder());
    }

    public static NewBookmarkFragment of(Folder folder) {
        return newInstance(null, folder);
    }

    private static NewBookmarkFragment newInstance(Bookmark b, Folder f) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_ENTRY, b);
        bundle.putParcelable(KEY_FOLDER, f);

        NewBookmarkFragment fragment = new NewBookmarkFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    public interface Callback extends NewEntryFragment.Callback<Bookmark> {
    }

    private EditText mWebAddressV;
    private Spinner mFoldersV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bookmark_new_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mWebAddressV = findView(view, R.id.txt_web_address);
        mFoldersV = findView(view, R.id.spinner_folders);

        prepSpinner();
        fillWithData();
    }

    private void prepSpinner() {
        FoldersAdapter adapter = new FoldersAdapter(getActivity(), Collections.singletonList(getFolder()));
        adapter.setHasHeader(false);
        mFoldersV.setAdapter(adapter);
    }

    private void fillWithData() {
        Bookmark bookmark = getEntry();
        if (bookmark != null) {
            mWebAddressV.setText(bookmark.getWebAddress());
        }
    }

    @Override
    public boolean isEditedEntryValid() {
        return isAllTextNotEmpty(R.string.err_empty_field, mWebAddressV);
    }

    @Override
    public Bookmark getEditedEntry() {
        Bookmark bookmark = getEntry();
        if (bookmark == null) {
            bookmark = new Bookmark(getFolder());
            setEntry(bookmark);
        }
        bookmark.setWebAddress(mWebAddressV.getText().toString());
        return bookmark;
    }
}
