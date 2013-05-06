/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;
import com.edmondapps.utils.android.Logs;
import com.edmondapps.utils.android.view.PopupMenu;
import com.edmondapps.utils.android.view.PopupMenu.OnMenuItemClickListener;
import com.nexuspad.R;
import com.nexuspad.account.AccountManager;
import com.nexuspad.datamodel.NPEntry;
import com.nexuspad.dataservice.EntryService;
import com.nexuspad.dataservice.NPException;

public class OnEntryMenuClickListener<T extends NPEntry> implements OnClickListener {
    public static final String TAG = "OnEntryMenuClickListener";

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

    private final ListView mListView;
    private final EntryService mEntryService;

    public OnEntryMenuClickListener(ListView listView, EntryService entryService) {
        mListView = listView;
        mEntryService = entryService;
    }

    @Override
    public void onClick(View v) {
        @SuppressWarnings("unchecked")
        FolderEntriesAdapter<? extends EntriesAdapter<T>> compoundAdapter = ((FolderEntriesAdapter<? extends EntriesAdapter<T>>)mListView.getAdapter());

        int pos = mListView.getPositionForView(v);
        if (pos != ListView.INVALID_POSITION) {
            EntriesAdapter<T> adapter = compoundAdapter.getEntriesAdapter();

            int position = compoundAdapter.getPositionForAdapter(pos);
            T item = adapter.getItem(position);
            onEntryClick(item, position, v);
        }
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

    protected boolean onEntryMenuClick(T entry, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                deleteEntry(entry);
                return true;
        }
        return false;
    }

    private void deleteEntry(T entry) {
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

    public final ListView getListView() {
        return mListView;
    }

    public final EntryService getEntryService() {
        return mEntryService;
    }
}
