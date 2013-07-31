/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.bookmark.ui.fragment;

import static com.edmondapps.utils.android.view.ViewUtils.findView;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.nexuspad.R;
import com.nexuspad.app.App;
import com.nexuspad.datamodel.Bookmark;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.ui.fragment.EntryFragment;

/**
 * @author Edmond
 * 
 */
@FragmentName(BookmarkFragment.TAG)
public class BookmarkFragment extends EntryFragment<Bookmark> {
    public static final String TAG = "BookmarkFragment";

    public static BookmarkFragment of(Bookmark bookmark, Folder f) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_ENTRY, bookmark);
        bundle.putParcelable(KEY_FOLDER, f);

        BookmarkFragment fragment = new BookmarkFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    public interface Callback extends EntryFragment.Callback<Bookmark> {
        void onEdit(BookmarkFragment f, Bookmark b);
    }

    private Callback mCallback;

    private TextView mNameV;
    private TextView mWebAddressV;
    private TextView mTagsV;
    private TextView mNoteV;

    private TextView mTagsFrameV;
    private TextView mNoteFrameV;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof Callback) {
            mCallback = (Callback)activity;
        } else {
            throw new IllegalStateException(activity + " must implement Callback.");
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.bookmark_frag, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Bookmark entry = getDetailEntryIfExist();
        switch (item.getItemId()) {
            case R.id.edit:
                mCallback.onEdit(this, entry);
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
        super.onViewCreated(view, savedInstanceState);
        mNameV = findView(view, R.id.lbl_title);
        mWebAddressV = findView(view, R.id.lbl_web_address);
        mTagsV = findView(view, R.id.lbl_tags);
        mNoteV = findView(view, R.id.lbl_note);

        mTagsFrameV = findView(view, R.id.lbl_tags_frame);
        mNoteFrameV = findView(view, R.id.lbl_note_frame);

        mNameV.setTypeface(App.getRobotoLight());

        updateUI();
        installListeners();
    }

    @Override
    protected void onEntryUpdated(Bookmark entry) {
        super.onEntryUpdated(entry);
        updateUI();
    }

    @Override
    protected void onDetailEntryUpdated(Bookmark entry) {
        super.onDetailEntryUpdated(entry);
        updateUI();
    }

    private void updateUI() {
        Bookmark bookmark = getDetailEntryIfExist();
        if (bookmark != null) {
            mNameV.setText(bookmark.getTitle());
            mWebAddressV.setText(bookmark.getWebAddress());
            mTagsV.setText(bookmark.getTags());
            mNoteV.setText(bookmark.getNote());

            updateVisibilities(bookmark);
        }
    }

    private void updateVisibilities(Bookmark bookmark) {
        int noteFlag = !TextUtils.isEmpty(bookmark.getNote()) ? View.VISIBLE : View.GONE;
        mNoteV.setVisibility(noteFlag);
        mNoteFrameV.setVisibility(noteFlag);

        int tagsFlag = !TextUtils.isEmpty(bookmark.getTags()) ? View.VISIBLE : View.GONE;
        mTagsV.setVisibility(tagsFlag);
        mTagsFrameV.setVisibility(tagsFlag);
    }

    private void installListeners() {
        mWebAddressV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                launchBrowser();
            }
        });
    }

    private void launchBrowser() {
        Uri uri = Uri.parse(getDetailEntryIfExist().getWebAddress());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getActivity(), R.string.err_invalid_bookmark, Toast.LENGTH_LONG).show();
        }
    }
}
