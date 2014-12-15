/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.doc.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.commonsware.cwac.richedit.RichEditText;
import com.nexuspad.common.Constants;
import com.nexuspad.common.annotation.FragmentName;
import com.ipaulpro.afilechooser.utils.FileUtils;
import com.nexuspad.R;
import com.nexuspad.common.annotation.ModuleInfo;
import com.nexuspad.app.App;
import com.nexuspad.common.fragment.EntryEditFragment;
import com.nexuspad.service.datamodel.NPDoc;
import com.nexuspad.service.datamodel.EntryTemplate;
import com.nexuspad.service.datamodel.NPFolder;
import com.nexuspad.service.datamodel.NPUpload;
import com.nexuspad.service.dataservice.ServiceConstants;

import java.io.File;
import java.util.List;

/**
 * @author Edmond
 */
@FragmentName(DocEditFragment.TAG)
@ModuleInfo(moduleId = ServiceConstants.DOC_MODULE, template = EntryTemplate.DOC)
public class DocEditFragment extends EntryEditFragment<NPDoc> {
    public static final String TAG = "DocUpdateFragment";

    protected static final int FILE_SELECT_REQUEST = 2;

    public static interface DocDetailCallback extends EntryDetailCallback<NPDoc> {
        void onUpdateEntry(NPDoc entry);
    }

    public static DocEditFragment of(NPFolder folder) {
        return of(null, folder);
    }

    public static DocEditFragment of(NPDoc doc, NPFolder f) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.KEY_ENTRY, doc);
        bundle.putParcelable(Constants.KEY_FOLDER, f);

        DocEditFragment fragment = new DocEditFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    private EditText mTitleV;
    private EditText mTagsV;
    private RichEditText mNoteV;
    private LinearLayout mAttachmentsFrameV;

    private DocDetailCallback mDocDetailCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mDocDetailCallback = App.getCallbackOrThrow(activity, DocDetailCallback.class);
    }

    @Override
    protected boolean shouldGetDetailEntry() {
        return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.doc_edit_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mFolderView = (TextView)view.findViewById(R.id.lbl_folder);
        mNoteV = (RichEditText)view.findViewById(R.id.journal_text);
        mTitleV = (EditText)view.findViewById(R.id.txt_title);
        mTagsV = (EditText)view.findViewById(R.id.txt_tags);
        mAttachmentsFrameV = (LinearLayout)view.findViewById(R.id.frame_attachment);

        mNoteV.enableActionModes(false);

	    View addAttachmentView = view.findViewById(R.id.add_attachment);
        addAttachmentView.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View v) {
		        final Intent intent = FileUtils.createGetContentIntent();
		        startActivityForResult(intent, FILE_SELECT_REQUEST);
	        }
        });

        installFolderSelectorListener(mFolderView);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    final NPDoc doc = createEditedEntry();

                    Uri uri = data.getData();
                    if (uri != null) {
                        addUriIfNeeded(uri, doc);
                    }

                    uri = data.getParcelableExtra(Intent.EXTRA_STREAM);
                    if (uri != null) {
                        addUriIfNeeded(uri, doc);
                    }

                    final List<Uri> list = data.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                    if (list != null) {
                        for (Uri theUri : list) {
                            addUriIfNeeded(theUri, doc);
                        }
                    }

                    updateUI();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void addUriIfNeeded(Uri uri, NPDoc doc) {
        for (NPUpload npUpload : doc.getAttachments()) {
            if (npUpload.getDownloadLink().equals(uri.getPath())) {
                return;
            }
        }

        final File file = FileUtils.getFile(getActivity(), uri);
        if (file != null) {
            final NPUpload npUpload = new NPUpload(getFolder());
            npUpload.setFileName(file.getName());
            npUpload.setDownloadLink(file.getPath());
            npUpload.setJustCreated(true);
            doc.addAttachment(npUpload);
        }
    }

    @Override
    protected void onFolderUpdated(NPFolder folder) {
        super.onFolderUpdated(folder);
        updateFolderView();
    }

    @Override
    protected void updateUI() {
        updateFolderView();

        final NPDoc doc = getEntry();
        if (doc != null) {
            mTitleV.setText(doc.getTitle());
            mTagsV.setText(doc.getTags());

            final String note = doc.getNote();
            if (note != null) {
                mNoteV.setText(Html.fromHtml(note));
            }

            mAttachmentsFrameV.removeAllViews();
            final List<NPUpload> attachments = doc.getAttachments();
            final LayoutInflater inflater = LayoutInflater.from(getActivity());
            for (final NPUpload attachment : attachments) {
                final View view = inflater.inflate(R.layout.list_item_with_icon, mAttachmentsFrameV, false);

                final TextView title = (TextView)view.findViewById(android.R.id.text1);
                final ImageView icon = (ImageView)view.findViewById(android.R.id.icon);
                final ImageButton menu = (ImageButton)view.findViewById(R.id.menu);

                title.setText(attachment.getFileName());
                icon.setImageResource(R.drawable.ic_file);

                menu.setImageResource(android.R.drawable.ic_delete);
                menu.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                menu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAttachmentsFrameV.removeView(view);
                    }
                });

                mAttachmentsFrameV.addView(view);
            }
        }
    }

    @Override
    public boolean isEditedEntryValid() {
	    return true;
    }

    @Override
    public NPDoc getEntryFromEditor() {
        NPDoc doc = createEditedEntry();
        setEntry(doc);
        return doc;
    }

    private NPDoc createEditedEntry() {
        final NPDoc entry = getEntry();
        NPDoc doc = entry == null ? new NPDoc(getFolder()) : new NPDoc(entry);
        doc.setTitle(mTitleV.getText().toString());
        doc.setNote(Html.toHtml(mNoteV.getText()));
        doc.setTags(mTagsV.getText().toString());
        return doc;
    }

    @Override
    protected void onUpdateEntry(NPDoc entry) {
        super.onUpdateEntry(entry);
        mDocDetailCallback.onUpdateEntry(entry);
    }
}
