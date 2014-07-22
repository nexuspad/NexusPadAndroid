/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.common;

import android.content.Intent;
import android.content.res.Resources;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.PopupMenu;
import com.nexuspad.R;
import com.nexuspad.datamodel.NPFolder;
import com.nexuspad.dataservice.FolderService;
import com.nexuspad.common.activity.NewFolderActivity;
import com.nexuspad.common.adapters.FoldersEntriesListAdapter;
import com.nexuspad.common.adapters.ListFoldersAdapter;


public class OnFolderMenuClickListener implements OnClickListener {
    public static final String TAG = "OnFolderMenuClickListener";

    private static PopupMenu getPopupMenu(View view) {
        PopupMenu menu = (PopupMenu) view.getTag();
        if (menu == null) {
            menu = new PopupMenu(view.getContext(), view);
            menu.inflate(R.menu.folder);

            view.setTag(menu);
        }
        return menu;
    }

    private final FolderService mFolderService;
    private UndoBarController mController;
    private final ListView mListView;
    private final NPFolder mParentFolder;

    public OnFolderMenuClickListener(ListView listView, NPFolder parent, FolderService folderService, UndoBarController controller) {
        mListView = listView;
        mParentFolder = parent;
        mFolderService = folderService;
        mController = controller;
    }

    /**
     * You must call {@link #onFolderClick(com.nexuspad.datamodel.NPFolder, int, View)} if you are using
     * your own implementation.
     */
    @Override
    public void onClick(View v) {
	    FoldersEntriesListAdapter<?> felAdapter = ((FoldersEntriesListAdapter<?>) mListView.getAdapter());

        int position = mListView.getPositionForView(v);
        if (position != ListView.INVALID_POSITION) {
            ListFoldersAdapter adapter = felAdapter.getFoldersAdapter();
            NPFolder item = adapter.getItem(position);
            onFolderClick(item, position, v);
        }
    }

    public void onFolderClick(final NPFolder folder, final int position, View view) {
        PopupMenu popupMenu = getPopupMenu(view);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                return onFolderMenuClick(folder, position, menuItem.getItemId());
            }
        });
        popupMenu.show();
    }

    public boolean onFolderMenuClick(NPFolder folder, int position, int menuId) {
        switch (menuId) {
            case R.id.rename:
                renameFolder(folder);
                return true;
            case R.id.delete:
                deleteFolder(folder);
                return true;
        }
        return false;
    }

    private void renameFolder(NPFolder f) {
        NewFolderActivity.startWithParentFolder(mParentFolder, f, mListView.getContext());
    }

    private void deleteFolder(NPFolder folder) {
        final Resources resources = mListView.getResources();
        final String string = resources.getString(R.string.format_deleted_folder, folder.getFolderName());

        final Intent undoToken = new Intent(FolderService.ACTION_DELETE);
        undoToken.putExtra(FolderService.KEY_FOLDER, folder);

        mController.showUndoBar(false, string, undoToken);
    }

    public final ListView getListView() {
        return mListView;
    }

    public final FolderService getFolderService() {
        return mFolderService;
    }
}
