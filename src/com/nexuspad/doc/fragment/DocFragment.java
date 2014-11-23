/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.doc.fragment;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.*;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.nexuspad.common.Constants;
import com.nexuspad.common.annotation.FragmentName;
import com.nexuspad.common.utils.Lazy;
import com.nexuspad.R;
import com.nexuspad.service.datamodel.NPDoc;
import com.nexuspad.service.datamodel.NPFolder;
import com.nexuspad.service.datamodel.NPUpload;
import com.nexuspad.doc.activity.DocEditActivity;
import com.nexuspad.common.fragment.EntryFragment;

import java.util.List;

import static android.view.ViewGroup.LayoutParams;

/**
 * @author Edmond
 */
@FragmentName(DocFragment.TAG)
public class DocFragment extends EntryFragment<NPDoc> {
    public static final String TAG = "DocFragment";

    public static DocFragment of(NPDoc doc, NPFolder folder) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.KEY_ENTRY, doc);
        bundle.putParcelable(Constants.KEY_FOLDER, folder);

        DocFragment fragment = new DocFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    private Lazy<DownloadManager> mDownloadManager = new Lazy<DownloadManager>() {
        @Override
        protected DownloadManager onCreate() {
            return (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        }
    };

    private TextView mTitleV;
    private TextView mNoteV;

    private TextView mAttachmentsTitleV;
    private LinearLayout mAttachmentsFrameV;

    private TextView mTagsFrameV;
    private TextView mTagsV;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.doc_topmenu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit:
                NPDoc doc = getEntry();
                DocEditActivity.startWithDoc(getActivity(), getFolder(), doc);
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
//        mTitleV = findView(view, R.id.lbl_title);
        mNoteV = (TextView)view.findViewById(R.id.lbl_note);

        mAttachmentsTitleV = (TextView)view.findViewById(R.id.lbl_attachment);
        mAttachmentsFrameV = (LinearLayout)view.findViewById(R.id.frame_attachment);

        mTagsFrameV = (TextView)view.findViewById(R.id.lbl_tags_title);
        mTagsV = (TextView)view.findViewById(R.id.lbl_tags);

//        mTitleV.setTypeface(App.getRobotoLight());

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected boolean shouldGetDetailEntry() {
        return true;
    }

    @Override
    protected void updateUI() {
        NPDoc doc = getEntry();
        if (doc != null) {
//            mTitleV.setText(doc.getTitle());
            mTagsV.setText(doc.getTags());

            final String note = doc.getNote();
            if (!TextUtils.isEmpty(note)) {
                mNoteV.setText(Html.fromHtml(note));
            }

            final List<NPUpload> attachments = doc.getAttachments();
            final LayoutInflater inflater = LayoutInflater.from(getActivity());
            for (final NPUpload attachment : attachments) {
                final ViewGroup frame = (ViewGroup) inflater.inflate(R.layout.layout_selectable_frame, mAttachmentsFrameV, false);
                final View view = inflater.inflate(R.layout.list_item_with_icon, frame, false);

                final TextView title = (TextView)view.findViewById(android.R.id.text1);
                final ImageView icon = (ImageView)view.findViewById(android.R.id.icon);
                final View menu = view.findViewById(R.id.menu);

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
        final DownloadManager.Request request = new DownloadManager.Request(Uri.parse(attachment.getDownloadLink()));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        mDownloadManager.get().enqueue(request);
    }

    private void updateVisibilities(NPDoc doc) {
        final int tagsFlag = !TextUtils.isEmpty(doc.getTags()) ? View.VISIBLE : View.GONE;
        mTagsV.setVisibility(tagsFlag);
        mTagsFrameV.setVisibility(tagsFlag);

        final int noteFlag = !TextUtils.isEmpty(doc.getNote()) ? View.VISIBLE : View.GONE;
        mNoteV.setVisibility(noteFlag);

        final int attachmentsFlag = !doc.getAttachments().isEmpty() ? View.VISIBLE : View.GONE;
        mAttachmentsTitleV.setVisibility(attachmentsFlag);
        mAttachmentsFrameV.setVisibility(attachmentsFlag);
    }
}
