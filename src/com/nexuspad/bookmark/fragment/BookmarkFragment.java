/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.bookmark.fragment;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import com.nexuspad.R;
import com.nexuspad.app.App;
import com.nexuspad.common.fragment.EntryFragment;
import com.nexuspad.datamodel.NPBookmark;
import com.nexuspad.datamodel.NPFolder;

/**
 * @author Edmond
 */
public class BookmarkFragment extends EntryFragment<NPBookmark> {
    public static final String TAG = "BookmarkFragment";

    public static BookmarkFragment of(NPBookmark bookmark, NPFolder f) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_ENTRY, bookmark);
        bundle.putParcelable(KEY_FOLDER, f);

        BookmarkFragment fragment = new BookmarkFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    public interface BookmarkDetailCallback extends EntryDetailCallback<NPBookmark> {
        void onEdit(BookmarkFragment f, NPBookmark b);
    }

    private BookmarkDetailCallback mBookmarkDetailCallback;

    private TextView mNameV;
    private TextView mWebAddressV;
    private TextView mTagsV;
    private TextView mNoteV;

    private TextView mTagsFrameV;
    private TextView mNoteFrameV;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mBookmarkDetailCallback = App.getCallbackOrThrow(activity, BookmarkDetailCallback.class);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.bookmark_frag, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        NPBookmark entry = getEntry();
        switch (item.getItemId()) {
            case R.id.edit:
                mBookmarkDetailCallback.onEdit(this, entry);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bookmark_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mNameV = (TextView)view.findViewById(R.id.lbl_title);
        mWebAddressV = (TextView)view.findViewById(R.id.lbl_web_address);
        mTagsV = (TextView)view.findViewById(R.id.lbl_tags);
        mNoteV = (TextView)view.findViewById(R.id.lbl_note);

        mTagsFrameV = (TextView)view.findViewById(R.id.lbl_tags_title);
        mNoteFrameV = (TextView)view.findViewById(R.id.lbl_note_frame);

        mNameV.setTypeface(App.getRobotoLight());

        super.onViewCreated(view, savedInstanceState);

        mWebAddressV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                launchBrowser();
            }
        });
    }

    @Override
    protected void updateUI() {
        NPBookmark bookmark = getEntry();
        if (bookmark != null) {
            mNameV.setText(bookmark.getTitle());
            mWebAddressV.setText(bookmark.getWebAddress());
            mTagsV.setText(bookmark.getTags());
            mNoteV.setText(bookmark.getNote());

            updateVisibilities(bookmark);
        }
    }

    private void updateVisibilities(NPBookmark bookmark) {
        int noteFlag = !TextUtils.isEmpty(bookmark.getNote()) ? View.VISIBLE : View.GONE;
        mNoteV.setVisibility(noteFlag);
        mNoteFrameV.setVisibility(noteFlag);

        int tagsFlag = !TextUtils.isEmpty(bookmark.getTags()) ? View.VISIBLE : View.GONE;
        mTagsV.setVisibility(tagsFlag);
        mTagsFrameV.setVisibility(tagsFlag);
    }

    private void launchBrowser() {
        final String uriString = App.addSchemaIfRequired(getEntry().getWebAddress());
        Uri uri = Uri.parse(uriString);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getActivity(), R.string.err_invalid_bookmark, Toast.LENGTH_LONG).show();
        }
    }
}
