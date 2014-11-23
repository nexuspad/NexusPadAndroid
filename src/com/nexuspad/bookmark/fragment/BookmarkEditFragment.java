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
import com.nexuspad.common.Constants;
import com.nexuspad.common.annotation.FragmentName;
import com.nexuspad.R;
import com.nexuspad.common.annotation.ModuleId;
import com.nexuspad.service.datamodel.NPBookmark;
import com.nexuspad.service.datamodel.EntryTemplate;
import com.nexuspad.service.datamodel.NPFolder;
import com.nexuspad.service.dataservice.ServiceConstants;
import com.nexuspad.common.fragment.EntryEditFragment;

/**
 * @author Edmond
 */
@FragmentName(BookmarkEditFragment.TAG)
@ModuleId(moduleId = ServiceConstants.BOOKMARK_MODULE, template = EntryTemplate.BOOKMARK)
public class BookmarkEditFragment extends EntryEditFragment<NPBookmark> {
    public static final String TAG = "BookmarkEditFragment";

    public static BookmarkEditFragment of(NPFolder folder) {
        return BookmarkEditFragment.of(null, folder);
    }

    public static BookmarkEditFragment of(NPBookmark b, NPFolder f) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.KEY_ENTRY, b);
        bundle.putParcelable(Constants.KEY_FOLDER, f);

        BookmarkEditFragment fragment = new BookmarkEditFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    private TextView mFolderV;
    private EditText mWebAddressV;
    private EditText mNoteV;
    private EditText mTagsV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bookmark_edit_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mFolderV = (TextView)view.findViewById(R.id.lbl_folder);
        mWebAddressV = (EditText)view.findViewById(R.id.txt_web_address);
        mNoteV = (EditText)view.findViewById(R.id.journal_text);
        mTagsV = (EditText)view.findViewById(R.id.txt_tags);

        installFolderSelectorListener(mFolderV);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void onFolderUpdated(NPFolder folder) {
        super.onFolderUpdated(folder);
        updateFolderView();
    }

    private void updateFolderView() {
        mFolderV.setText(getFolder().getFolderName());
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
