/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.edmondapps.utils.android.Logs;
import com.edmondapps.utils.android.view.PopupMenu;
import com.edmondapps.utils.android.view.PopupMenu.OnMenuItemClickListener;
import com.nexuspad.R;
import com.nexuspad.account.AccountManager;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.dataservice.FolderService;
import com.nexuspad.dataservice.NPException;

public class OnFolderMenuClickListener implements OnClickListener {
    public static final String TAG = "OnFolderMenuClickListener";

    private static PopupMenu getPopupMenu(View view) {
        PopupMenu menu = (PopupMenu)view.getTag();
        if (menu == null) {
            menu = PopupMenu.newInstance(view.getContext(), view);
            menu.inflate(R.menu.folder);

            view.setTag(menu);
        }
        return menu;
    }

    private final FolderService mFolderService;
    private final ListView mListView;

    public OnFolderMenuClickListener(ListView listView, FolderService folderService) {
        mListView = listView;
        mFolderService = folderService;
    }

    @Override
    public void onClick(View v) {
        FolderEntriesAdapter<?> compoundAdapter = ((FolderEntriesAdapter<?>)mListView.getAdapter());

        int pos = mListView.getPositionForView(v);
        if (pos != ListView.INVALID_POSITION) {
            FoldersAdapter adapter = compoundAdapter.getFoldersAdapter();
            int position = compoundAdapter.getPositionForAdapter(pos);

            Folder item = adapter.getItem(position);
            onFolderClick(item, position, v);
        }
    }

    protected void onFolderClick(final Folder folder, int position, View view) {
        PopupMenu popupMenu = getPopupMenu(view);
        popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int menuId) {
                return onFolderMenuClick(folder, menuId);
            }
        });
        popupMenu.show();
    }

    protected boolean onFolderMenuClick(Folder folder, int menuId) {
        switch (menuId) {
            case R.id.delete:
                deleteFolder(folder);
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

    public final ListView getListView() {
        return mListView;
    }

    public final FolderService getFolderService() {
        return mFolderService;
    }
}
