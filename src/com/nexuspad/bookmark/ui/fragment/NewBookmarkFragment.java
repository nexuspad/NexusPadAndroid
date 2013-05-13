/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.bookmark.ui.fragment;

import static com.edmondapps.utils.android.view.ViewUtils.findView;
import static com.edmondapps.utils.android.view.ViewUtils.isAllTextNotEmpty;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.edmondapps.utils.android.annotaion.FragmentName;
import com.nexuspad.R;
import com.nexuspad.datamodel.Bookmark;
import com.nexuspad.ui.fragment.NewEntryFragment;

/**
 * @author Edmond
 * 
 */
@FragmentName(NewBookmarkFragment.TAG)
public class NewBookmarkFragment extends NewEntryFragment<Bookmark> {
    public static final String TAG = "NewBookmarkFragment";

    public static NewBookmarkFragment of(Bookmark bookmark) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_ENTRY, bookmark);

        NewBookmarkFragment fragment = new NewBookmarkFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    public interface Callback extends NewEntryFragment.Callback<Bookmark> {
    }

    private EditText mNameV;
    private EditText mWebAddressV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_new_bookmark, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mNameV = findView(view, R.id.txt_name);
        mWebAddressV = findView(view, R.id.txt_web_address);

        fillWithData();
    }

    private void fillWithData() {
        Bookmark bookmark = getEntry();
        if (bookmark != null) {
            mNameV.setText(bookmark.getTitle());
            mWebAddressV.setText(bookmark.getWebAddress());
        }
    }

    @Override
    public boolean isEditedEntryValid() {
        return (getEntry() != null) &&
                isAllTextNotEmpty(R.string.err_empty_field, mNameV, mWebAddressV);
    }

    @Override
    public Bookmark getEditedEntry() {
        Bookmark bookmark = getEntry();
        if (bookmark != null) {
            bookmark.setTitle(mNameV.getText().toString());
            bookmark.setWebAddress(mWebAddressV.getText().toString());
        }
        return bookmark;
    }
}
