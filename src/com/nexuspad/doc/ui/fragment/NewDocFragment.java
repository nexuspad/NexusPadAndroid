/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.doc.ui.fragment;

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
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.edmondapps.utils.android.view.ViewUtils;
import com.ipaulpro.afilechooser.utils.FileUtils;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.app.App;
import com.nexuspad.datamodel.Doc;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.datamodel.NPUpload;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.ui.activity.UploadCenterActivity;
import com.nexuspad.ui.fragment.EntryFragment;
import com.nexuspad.ui.fragment.NewEntryFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.edmondapps.utils.android.view.ViewUtils.findView;
import static com.edmondapps.utils.android.view.ViewUtils.isAllTextNotEmpty;

/**
 * @author Edmond
 */
@FragmentName(NewDocFragment.TAG)
@ModuleId(moduleId = ServiceConstants.DOC_MODULE, template = EntryTemplate.DOC)
public class NewDocFragment extends NewEntryFragment<Doc> {
    public static final String TAG = "NewDocFragment";

    protected static final int REQ_PICK_FILE = 2;

    public static interface Callback extends NewEntryFragment.Callback<Doc> {
        void onUpdateEntry(Doc entry);
    }

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
    private LinearLayout mAttachmentsFrameV;

    private Callback mCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = App.getCallbackOrThrow(activity, Callback.class);
    }

    @Override
    protected boolean shouldGetDetailEntry() {
        return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.doc_new_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mFolderV = findView(view, R.id.lbl_folder);
        mNoteV = findView(view, R.id.txt_note);
        mTitleV = findView(view, R.id.txt_title);
        mTagsV = findView(view, R.id.txt_tags);
        mAttachmentsFrameV = findView(view, R.id.frame_attachment);

        mNoteV.enableActionModes(false);
        findView(view, R.id.add_attachment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = FileUtils.createGetContentIntent();
                startActivityForResult(intent, REQ_PICK_FILE);
            }
        });

        installFolderSelectorListener(mFolderV);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQ_PICK_FILE:
                if (resultCode == Activity.RESULT_OK) {
                    final Doc doc = createEditedEntry();

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

    private void addUriIfNeeded(Uri uri, Doc doc) {
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

        final Doc doc = getEntry();
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
                final View view = inflater.inflate(R.layout.list_item_icon, mAttachmentsFrameV, false);

                final TextView title = ViewUtils.findView(view, android.R.id.text1);
                final ImageView icon = ViewUtils.findView(view, android.R.id.icon);
                final ImageButton menu = ViewUtils.findView(view, R.id.menu);

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
        return isAllTextNotEmpty(R.string.err_empty_field, mTitleV);
    }

    @Override
    public Doc getEditedEntry() {
        Doc doc = createEditedEntry();
        setEntry(doc);
        return doc;
    }

    private Doc createEditedEntry() {
        final Doc entry = getEntry();
        Doc doc = entry == null ? new Doc(getFolder()) : new Doc(entry);
        doc.setTitle(mTitleV.getText().toString());
        doc.setNote(Html.toHtml(mNoteV.getText()));
        doc.setTags(mTagsV.getText().toString());
        return doc;
    }

    @Override
    protected void onUpdateEntry(Doc entry) {
        super.onUpdateEntry(entry);
        mCallback.onUpdateEntry(entry);
    }
}
