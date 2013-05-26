/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui.fragment;

import static com.edmondapps.utils.android.view.ViewUtils.findView;
import static com.edmondapps.utils.android.view.ViewUtils.isAllTextNotEmpty;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.nexuspad.R;
import com.nexuspad.account.AccountManager;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.dataservice.NPException;

/**
 * @author Edmond
 * 
 */
@FragmentName(NewFolderFragment.TAG)
public class NewFolderFragment extends SherlockDialogFragment {
    public static final String TAG = "NewFolderFragment";

    private static final String KEY_FOLDER = "key_folder";

    public static NewFolderFragment of(Folder parent) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_FOLDER, parent);

        NewFolderFragment fragment = new NewFolderFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    private EditText mFolderNameV;
    private Folder mParentFolder;
    private int mModuleId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null) {
            mParentFolder = arguments.getParcelable(KEY_FOLDER);
        }

        if (mParentFolder == null) {
            throw new IllegalStateException("You must pass in a parent Folder with KEY_FOLDER.");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.folder_new_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFolderNameV = findView(view, R.id.txt_folder_name);
    }

    public boolean isEditedFolderValid() {
        return isAllTextNotEmpty(R.string.err_empty_field, mFolderNameV);
    }

    public Folder getEditedFolder() {
        Folder folder = new Folder(mModuleId);
        folder.setParent(mParentFolder);
        folder.setFolderName(mFolderNameV.getText().toString());
        try {
            folder.setOwner(AccountManager.currentAccount());
        } catch (NPException e) {
            throw new AssertionError("I thought I'm logged in.");
        }
        return folder;
    }

    public Folder getParentFolder() {
        return mParentFolder;
    }
}
