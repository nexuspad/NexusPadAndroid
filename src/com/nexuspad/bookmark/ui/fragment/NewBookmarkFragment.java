/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.bookmark.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.Bookmark;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.ui.fragment.NewEntryFragment;

import static com.edmondapps.utils.android.view.ViewUtils.findView;
import static com.edmondapps.utils.android.view.ViewUtils.isAllTextNotEmpty;

/**
 * @author Edmond
 */
@FragmentName(NewBookmarkFragment.TAG)
@ModuleId(moduleId = ServiceConstants.BOOKMARK_MODULE, template = EntryTemplate.BOOKMARK)
public class NewBookmarkFragment extends NewEntryFragment<Bookmark> {
    public static final String TAG = "NewBookmarkFragment";

    public static NewBookmarkFragment of(Folder folder) {
        return NewBookmarkFragment.of(null, folder);
    }

    public static NewBookmarkFragment of(Bookmark b, Folder f) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_ENTRY, b);
        bundle.putParcelable(KEY_FOLDER, f);

        NewBookmarkFragment fragment = new NewBookmarkFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    private TextView mFolderV;
    private EditText mWebAddressV;
    private EditText mNoteV;
    private EditText mTagsV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bookmark_new_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mFolderV = findView(view, R.id.lbl_folder);
        mWebAddressV = findView(view, R.id.txt_web_address);
        mNoteV = findView(view, R.id.txt_note);
        mTagsV = findView(view, R.id.txt_tags);

        installFolderSelectorListener(mFolderV);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void onFolderUpdated(Folder folder) {
        super.onFolderUpdated(folder);
        updateFolderView();
    }

    private void updateFolderView() {
        mFolderV.setText(getFolder().getFolderName());
    }

    @Override
    protected void updateUI() {
        updateFolderView();

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
        final Bookmark entry = getEntry();
        Bookmark bookmark = entry == null ? new Bookmark(getFolder()) : new Bookmark(entry);
        bookmark.setWebAddress(mWebAddressV.getText().toString());
        bookmark.setNote(mNoteV.getText().toString());
        bookmark.setTags(mTagsV.getText().toString());

        setEntry(bookmark);
        return bookmark;
    }
}
