/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.doc.ui.fragment;

import static com.edmondapps.utils.android.view.ViewUtils.findView;
import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.nexuspad.R;
import com.nexuspad.datamodel.Doc;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.doc.ui.activity.NewDocActivity;
import com.nexuspad.ui.activity.NewEntryActivity.Mode;
import com.nexuspad.ui.fragment.EntryFragment;

/**
 * @author Edmond
 * 
 */
@FragmentName(DocFragment.TAG)
public class DocFragment extends EntryFragment<Doc> {
    public static final String TAG = "DocFragment";

    public static DocFragment of(Doc doc, Folder folder) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_ENTRY, doc);
        bundle.putParcelable(KEY_FOLDER, folder);

        DocFragment fragment = new DocFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    private TextView mTitleV;
    private TextView mTagsV;
    private TextView mNoteV;

    private TextView mTagsFrameV;
    private TextView mNoteFrameV;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.doc_frag, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit:
                Doc doc = getDetailEntryIfExist();
                NewDocActivity.startWithDoc(doc, getFolder(), Mode.EDIT, getActivity());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.doc_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTitleV = findView(view, R.id.lbl_title);
        mTagsV = findView(view, R.id.lbl_tags);
        mNoteV = findView(view, R.id.lbl_note);

        mTagsFrameV = findView(view, R.id.lbl_tags_frame);
        mNoteFrameV = findView(view, R.id.lbl_note_frame);

        // magic to make the links inside the <a></a> tags clickable
        mNoteV.setMovementMethod(LinkMovementMethod.getInstance());

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

    private void updateUI() {
        Doc doc = getDetailEntryIfExist();
        if (doc != null) {
            mTitleV.setText(doc.getTitle());
            mTagsV.setText(doc.getTags());

            String note = doc.getNote();
            if (!TextUtils.isEmpty(note)) {
                mNoteV.setText(Html.fromHtml(note));
            }

            updateVisibilities(doc);
        }
    }

    private void updateVisibilities(Doc doc) {
        int tagsFlag = !TextUtils.isEmpty(doc.getTags()) ? View.VISIBLE : View.GONE;
        mTagsV.setVisibility(tagsFlag);
        mTagsFrameV.setVisibility(tagsFlag);

        int noteFlag = !TextUtils.isEmpty(doc.getNote()) ? View.VISIBLE : View.GONE;
        mNoteV.setVisibility(noteFlag);
        mNoteFrameV.setVisibility(noteFlag);
    }
}
