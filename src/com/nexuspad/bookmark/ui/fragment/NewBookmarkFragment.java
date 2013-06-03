/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.bookmark.ui.fragment;

import static com.edmondapps.utils.android.view.ViewUtils.findView;
import static com.edmondapps.utils.android.view.ViewUtils.isAllTextNotEmpty;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.edmondapps.utils.android.annotaion.FragmentName;
import com.nexuspad.R;
import com.nexuspad.datamodel.Bookmark;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.ui.activity.FoldersActivity;
import com.nexuspad.ui.fragment.NewEntryFragment;

/**
 * @author Edmond
 */
@FragmentName(NewBookmarkFragment.TAG)
public class NewBookmarkFragment extends NewEntryFragment<Bookmark> {
    public static final String TAG = "NewBookmarkFragment";

    private static final int REQ_FOLDER = 1;

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
        super.onViewCreated(view, savedInstanceState);
        mFolderV = findView(view, R.id.lbl_folder);
        mWebAddressV = findView(view, R.id.txt_web_address);
        mNoteV = findView(view, R.id.txt_note);
        mTagsV = findView(view, R.id.txt_tags);

        intstallListeners();
        updateFolderView();
        updateBookmarkViews();
    }

    private void intstallListeners() {
        mFolderV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Folder folder = Folder.rootFolderOf(ServiceConstants.BOOKMARK_MODULE);
                Intent intent = FoldersActivity.ofParentFolder(getActivity(), folder);
                startActivityForResult(intent, REQ_FOLDER);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQ_FOLDER:
                setFolder(data.<Folder> getParcelableExtra(FoldersActivity.KEY_FOLDER));
                break;
            default:
                throw new AssertionError("unknown requestCode: " + requestCode);
        }
    }

    @Override
    protected void onEntryUpdated(Bookmark entry) {
        super.onEntryUpdated(entry);
        updateBookmarkViews();
    }

    @Override
    protected void onFolderUpdated(Folder folder) {
        super.onFolderUpdated(folder);
        updateFolderView();
    }

    private void updateFolderView() {
        mFolderV.setText(getFolder().getFolderName());
    }

    private void updateBookmarkViews() {
        mFolderV.setText(getFolder().getFolderName());

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
