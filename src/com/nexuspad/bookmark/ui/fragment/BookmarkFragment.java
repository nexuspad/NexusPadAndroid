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
        Bookmark entry = getEntry();
        switch (item.getItemId()) {
            case R.id.edit:
                mCallback.onEdit(this, entry);
                return true;
            case R.id.delete:
                getEntryService().safeDeleteEntry(getActivity(), entry);
                mCallback.onDeleting(this, entry);
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

        updateUI();
        installListeners();
    }

    @Override
    public void setEntry(Bookmark e) {
        super.setEntry(e);
        updateUI();
    }

    private void updateUI() {
        Bookmark bookmark = getEntry();
        if (bookmark != null) {
            mNameV.setText(bookmark.getTitle());
            mWebAddressV.setText(bookmark.getWebAddress());
            mTagsV.setText(bookmark.getTags());
            mNoteV.setText(bookmark.getNote());
        }
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
        Uri uri = Uri.parse(getEntry().getWebAddress());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getActivity(), R.string.err_invalid_bookmark, Toast.LENGTH_LONG).show();
        }
    }
}
