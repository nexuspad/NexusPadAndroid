/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.bookmark.ui.fragment;

import android.content.Context;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;
import com.edmondapps.utils.android.Logs;
import com.nexuspad.R;
import com.nexuspad.account.AccountManager;
import com.nexuspad.datamodel.Bookmark;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.datamodel.NPEntry;
import com.nexuspad.dataservice.EntryService;
import com.nexuspad.dataservice.FolderService;
import com.nexuspad.dataservice.NPException;
import com.nexuspad.ui.OnEntryMenuClickListener;

/**
 * @author Edmond
 * 
 */
public class OnBookmarkMenuClickListener extends OnEntryMenuClickListener<Bookmark> {
    public static final String TAG = "OnBookmarkMenuClickListener";

    public OnBookmarkMenuClickListener(ListView listView, EntryService entryService, FolderService folderService) {
        super(listView, entryService, folderService);
    }

    @Override
    protected boolean onFolderMenuClick(Folder folder, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                deleteFolder(folder);
                return true;
        }
        return false;
    }

    @Override
    protected boolean onEntryMenuClick(Bookmark entry, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                deleteEntry(entry);
                return true;
        }
        return false;
    }

    private void deleteFolder(Folder folder) {
        FolderService service = getFolderService();
        try {
            folder.setOwner(AccountManager.currentAccount());
            service.deleteFolder(folder);
        } catch (NPException e) {
            Logs.e(TAG, e);
            Context context = getListView().getContext();
            String msg = context.getString(R.string.formatted_err_delete_failed, folder.getDisplayName());
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        }
    }

    private void deleteEntry(NPEntry entry) {
        EntryService service = getEntryService();
        try {
            entry.setOwner(AccountManager.currentAccount());
            service.deleteEntry(entry);
        } catch (NPException e) {
            Logs.e(TAG, e);
            Context context = getListView().getContext();
            String msg = context.getString(R.string.formatted_err_delete_failed, entry.getTitle());
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        }
    }
}
