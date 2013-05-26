/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.bookmark.ui.fragment;

import static com.edmondapps.utils.android.view.ViewUtils.findView;
import static com.edmondapps.utils.android.view.ViewUtils.isAllTextNotEmpty;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.edmondapps.utils.android.annotaion.FragmentName;
import com.nexuspad.R;
import com.nexuspad.datamodel.Bookmark;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.ui.fragment.NewEntryFragment;

/**
 * @author Edmond
 */
@FragmentName(NewBookmarkFragment.TAG)
public class NewBookmarkFragment extends NewEntryFragment<Bookmark> {
    public static final String TAG = "NewBookmarkFragment";

    public static NewBookmarkFragment of(Folder folder) {
        return of(null, folder);
    }

    public static NewBookmarkFragment of(Bookmark b, Folder f) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_ENTRY, b);
        bundle.putParcelable(KEY_FOLDER, f);

        NewBookmarkFragment fragment = new NewBookmarkFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    private EditText mWebAddressV;
    private EditText mNoteV;
    private EditText mTagsV;
    private Spinner mFoldersV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bookmark_new_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFoldersV = findView(view, R.id.spinner_folders);
        mWebAddressV = findView(view, R.id.txt_web_address);
        mNoteV = findView(view, R.id.txt_note);
        mTagsV = findView(view, R.id.txt_tags);

        prepSpinner();
        fillWithData();
    }

    private void prepSpinner() {
        ArrayAdapter<String> a = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item,
                makeFolderNameList());
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mFoldersV.setAdapter(a);
    }

    private List<String> makeFolderNameList() {
        List<String> out = new ArrayList<String>();
        Folder f = getFolder();

        if (f.getFolderId() != Folder.ROOT_FOLDER) {
            out.add(getString(R.string.root));
        }
        out.add(f.getFolderName());

        return out;
    }

    private void fillWithData() {
        Bookmark bookmark = getEntry();
        if (bookmark != null) {
            mWebAddressV.setText(bookmark.getWebAddress());
            mNoteV.setText(bookmark.getNote());
            mTagsV.setText(bookmark.getTags());
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
        bookmark.setNote(mNoteV.getText().toString());
        bookmark.setTags(mTagsV.getText().toString());
        return bookmark;
    }
}
