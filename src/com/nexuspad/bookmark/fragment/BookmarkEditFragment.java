/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.bookmark.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import com.nexuspad.R;
import com.nexuspad.common.annotation.FragmentName;
import com.nexuspad.common.annotation.ModuleInfo;
import com.nexuspad.common.fragment.EntryEditFragment;
import com.nexuspad.service.datamodel.EntryTemplate;
import com.nexuspad.service.datamodel.NPBookmark;
import com.nexuspad.service.dataservice.ServiceConstants;

/**
 * @author Edmond
 */
@FragmentName(BookmarkEditFragment.TAG)
@ModuleInfo(moduleId = ServiceConstants.BOOKMARK_MODULE, template = EntryTemplate.BOOKMARK)
public class BookmarkEditFragment extends EntryEditFragment<NPBookmark> {
    public static final String TAG = "BookmarkEditFragment";

    private EditText mWebAddressV;
    private EditText mNoteV;
    private EditText mTagsV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bookmark_edit_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
	    mFolderView = (TextView)view.findViewById(R.id.lbl_folder);
        mWebAddressV = (EditText)view.findViewById(R.id.txt_web_address);
        mNoteV = (EditText)view.findViewById(R.id.txt_note);
        mTagsV = (EditText)view.findViewById(R.id.txt_tags);

        installFolderSelectorListener(mFolderView);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void updateUI() {
        updateFolderView();

        NPBookmark bookmark = getEntry();
        if (bookmark != null) {
            mWebAddressV.setText(bookmark.getWebAddress());
            mNoteV.setText(bookmark.getNote());
            mTagsV.setText(bookmark.getTags());
        }
    }

    @Override
    public boolean isEditedEntryValid() {
	    boolean isEmpty = TextUtils.isEmpty(mWebAddressV.getText().toString());
	    if (isEmpty) {
		    mWebAddressV.setError(mWebAddressV.getContext().getText(R.string.err_empty_field));
		    mWebAddressV.requestFocus();
		    return false;
	    }

	    return true;
    }

    @Override
    public NPBookmark getEntryFromEditor() {
        final NPBookmark entry = getEntry();
        NPBookmark bookmark = entry == null ? new NPBookmark(getFolder()) : new NPBookmark(entry);
        bookmark.setWebAddress(mWebAddressV.getText().toString());
        bookmark.setNote(mNoteV.getText().toString());
        bookmark.setTags(mTagsV.getText().toString());

        setEntry(bookmark);
        return bookmark;
    }
}
