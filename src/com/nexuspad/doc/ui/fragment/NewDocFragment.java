/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.doc.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import com.commonsware.cwac.richedit.RichEditText;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.Doc;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.ui.activity.FoldersActivity;
import com.nexuspad.ui.fragment.NewEntryFragment;

import static com.edmondapps.utils.android.view.ViewUtils.findView;
import static com.edmondapps.utils.android.view.ViewUtils.isAllTextNotEmpty;

/**
 * @author Edmond
 */
@FragmentName(NewDocFragment.TAG)
@ModuleId(moduleId = ServiceConstants.DOC_MODULE)
public class NewDocFragment extends NewEntryFragment<Doc> {
    public static final String TAG = "NewDocFragment";

    public static NewDocFragment of(Folder folder) {
        return of(null, folder);
    }

    public static NewDocFragment of(Doc doc, Folder f) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_ENTRY, doc);
        bundle.putParcelable(KEY_FOLDER, f);

        NewDocFragment fragment = new NewDocFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    private TextView mFolderV;
    private EditText mTitleV;
    private EditText mTagsV;
    private RichEditText mNoteV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.doc_new_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFolderV = findView(view, R.id.lbl_folder);
        mNoteV = findView(view, R.id.txt_note);
        mTitleV = findView(view, R.id.txt_title);
        mTagsV = findView(view, R.id.txt_tags);

        mNoteV.enableActionModes(false);

        installFolderSelectorListener(mFolderV);
        updateUI();
    }

    @Override
    protected void onEntryUpdated(Doc entry) {
        super.onEntryUpdated(entry);
        updateUI();
    }

    @Override
    protected void onDetailEntryUpdated(Doc entry) {
        super.onDetailEntryUpdated(entry);
        updateUI();
    }

    @Override
    protected void onFolderUpdated(Folder folder) {
        super.onFolderUpdated(folder);
        updateFolderView();
    }

    private void updateFolderView() {
        mFolderV.setText(getFolder().getFolderName());
    }

    private void updateUI() {
        updateFolderView();

        Doc doc = getDetailEntryIfExist();
        if (doc != null) {
            mTitleV.setText(doc.getTitle());
            mTagsV.setText(doc.getTags());

            String note = doc.getNote();
            if (note != null) {
                mNoteV.setText(Html.fromHtml(note));
            }
        }
    }

    @Override
    public boolean isEditedEntryValid() {
        return isAllTextNotEmpty(R.string.err_empty_field, mTitleV);
    }

    @Override
    public Doc getEditedEntry() {
        final Doc entry = getDetailEntryIfExist();
        Doc doc = entry == null ? new Doc(getFolder()) : new Doc(entry);
        doc.setTitle(mTitleV.getText().toString());
        doc.setNote(Html.toHtml(mNoteV.getText()));
        doc.setTags(mTagsV.getText().toString());

        setDetailEntry(doc);
        return doc;
    }
}
