/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.actionbarsherlock.view.MenuItem;
import com.edmondapps.utils.android.view.PopupMenu;
import com.edmondapps.utils.android.view.PopupMenu.OnMenuItemClickListener;
import com.nexuspad.R;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.datamodel.NPEntry;
import com.nexuspad.dataservice.EntryService;
import com.nexuspad.dataservice.FolderService;

public abstract class OnEntryMenuClickListener<T extends NPEntry> implements OnClickListener {
    public static PopupMenu getFolderPopupMenu(View view) {
        return getPopupMenu(view, R.menu.folder);
    }

    public static PopupMenu getEntryPopupMenu(View view) {
        return getPopupMenu(view, R.menu.entry);
    }

    private static PopupMenu getPopupMenu(View view, int menuId) {
        PopupMenu menu = (PopupMenu)view.getTag();
        if (menu == null) {
            menu = new PopupMenu(view.getContext(), view);
            menu.inflate(menuId);

            view.setTag(menu);
        }
        return menu;
    }

    private final FolderService mFolderService;
    private final ListView mListView;
    private final EntryService mEntryService;

    public OnEntryMenuClickListener(ListView listView, EntryService entryService, FolderService folderService) {
        mListView = listView;
        mEntryService = entryService;
        mFolderService = folderService;
    }

    protected abstract boolean onFolderMenuClick(Folder folder, MenuItem item);

    protected abstract boolean onEntryMenuClick(T entry, MenuItem item);

    protected void onFolderClick(final Folder folder, int position, View view) {
        PopupMenu popupMenu = getFolderPopupMenu(view);
        popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onFolderMenuClick(folder, item);
            }
        });
        popupMenu.show();
    }

    protected void onEntryClick(final T entry, int position, View view) {
        PopupMenu popupMenu = getEntryPopupMenu(view);
        popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onEntryMenuClick(entry, item);
            }
        });
        popupMenu.show();
    }

    @Override
    public void onClick(View v) {
        @SuppressWarnings("unchecked")
        EntriesAdapter<T> adapter = ((EntriesAdapter<T>)mListView.getAdapter());

        int pos = mListView.getPositionForView(v);
        if (pos != ListView.INVALID_POSITION) {
            switch (adapter.getItemViewType(pos)) {
                case EntriesAdapter.TYPE_FOLDER:
                    onFolderClick(adapter.getFolder(pos), pos, v);
                    break;
                case EntriesAdapter.TYPE_ENTRY:
                    onEntryClick(adapter.getItem(pos), pos, v);
                    break;
            }
        }
    }

    public final ListView getListView() {
        return mListView;
    }

    public final FolderService getFolderService() {
        return mFolderService;
    }

    public final EntryService getEntryService() {
        return mEntryService;
    }
}
