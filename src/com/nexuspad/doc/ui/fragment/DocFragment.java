/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.doc.ui.fragment;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.edmondapps.utils.android.Logs;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.edmondapps.utils.android.view.ViewUtils;
import com.nexuspad.R;
import com.nexuspad.app.App;
import com.nexuspad.datamodel.Doc;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.datamodel.NPUpload;
import com.nexuspad.doc.ui.activity.NewDocActivity;
import com.nexuspad.ui.activity.NewEntryActivity.Mode;
import com.nexuspad.ui.fragment.EntryFragment;

import java.util.List;

import static android.view.ViewGroup.LayoutParams;
import static com.edmondapps.utils.android.view.ViewUtils.findView;

/**
 * @author Edmond
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

    private LinearLayout mAttachmentsFrameV;
    private TextView mTagsFrameV;

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
        mAttachmentsFrameV = findView(view, R.id.attachments_frame);

        mTitleV.setTypeface(App.getRobotoLight());

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

            final String note = doc.getNote();
            if (!TextUtils.isEmpty(note)) {
                mNoteV.setText(Html.fromHtml(note));
            }

            final List<NPUpload> attachments = doc.getAttachments();
            final LayoutInflater inflater = LayoutInflater.from(getActivity());
            for (final NPUpload attachment : attachments) {
                final ViewGroup frame = (ViewGroup) inflater.inflate(R.layout.layout_selectable_frame, mAttachmentsFrameV, false);
                final View view = inflater.inflate(R.layout.list_item_icon, frame, false);

                final TextView title = ViewUtils.findView(view, android.R.id.text1);
                final ImageView icon = ViewUtils.findView(view, android.R.id.icon);
                final View menu = ViewUtils.findView(view, R.id.menu);

                title.setText(attachment.getFileName());
                icon.setImageResource(R.drawable.ic_file);
                menu.setVisibility(View.GONE);

                frame.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        downloadAttachment(attachment);
                    }
                });

                frame.addView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                mAttachmentsFrameV.addView(frame);
            }


            updateVisibilities(doc);
        }
    }

    private void downloadAttachment(NPUpload attachment) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(attachment.getDownloadLink()));
        startActivity(intent);
    }

    private void updateVisibilities(Doc doc) {
        final int tagsFlag = !TextUtils.isEmpty(doc.getTags()) ? View.VISIBLE : View.GONE;
        mTagsV.setVisibility(tagsFlag);
        mTagsFrameV.setVisibility(tagsFlag);

        final int noteFlag = !TextUtils.isEmpty(doc.getNote()) ? View.VISIBLE : View.GONE;
        mNoteV.setVisibility(noteFlag);

        final int attachmentsFlag = !doc.getAttachments().isEmpty() ? View.VISIBLE : View.GONE;
        mAttachmentsFrameV.setVisibility(attachmentsFlag);
    }
}
