/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui;

import android.content.Intent;
import android.content.res.Resources;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.PopupMenu;
import com.nexuspad.R;
import com.nexuspad.datamodel.NPEntry;
import com.nexuspad.dataservice.EntryService;

public class OnEntryMenuClickListener<T extends NPEntry> implements OnClickListener {
    public static final String TAG = "OnEntryMenuClickListener";

    private static PopupMenu getPopupMenu(View view) {
        PopupMenu menu = (PopupMenu) view.getTag();
        if (menu == null) {
            menu = new PopupMenu(view.getContext(), view);
            menu.inflate(R.menu.entry);

            view.setTag(menu);
        }
        return menu;
    }

    private final ListView mListView;
    private final EntryService mEntryService;
    private UndoBarController mController;

    public OnEntryMenuClickListener(ListView listView, EntryService entryService, UndoBarController controller) {
        mListView = listView;
        mEntryService = entryService;
        mController = controller;
    }

    @Override
    public void onClick(View v) {
        @SuppressWarnings("unchecked")
        FolderEntriesAdapter<? extends EntriesAdapter<T>> compoundAdapter = ((FolderEntriesAdapter<? extends EntriesAdapter<T>>) mListView.getAdapter());

        int pos = mListView.getPositionForView(v);
        if (pos != ListView.INVALID_POSITION) {
            EntriesAdapter<T> adapter = compoundAdapter.getEntriesAdapter();

            int position = compoundAdapter.getPositionForAdapter(pos);
            T item = adapter.getItem(position);
            onEntryClick(item, position, v);
        }
    }

    protected void onEntryClick(final T entry, final int position, View view) {
        PopupMenu popupMenu = getPopupMenu(view);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                return onEntryMenuClick(entry, position, menuItem.getItemId());
            }
        });
        popupMenu.show();
    }

    protected boolean onEntryMenuClick(T entry, int position, int menuId) {
        switch (menuId) {
            case R.id.delete:
                deleteEntry(entry);
                return true;
        }
        return false;
    }

    private void deleteEntry(T entry) {
        final Resources resources = mListView.getResources();
        final String string = resources.getString(
                R.string.format_deleted_entry, getEntryString(entry, resources), entry.getTitle());

        final Intent undoToken = new Intent(EntryService.ACTION_DELETE);
        undoToken.putExtra(EntryService.KEY_ENTRY, entry);

        mController.showUndoBar(false, string, undoToken);
    }

    private static String getEntryString(NPEntry entry, Resources resources) {
        final int id;
        switch (entry.getTemplate()) {
            case CONTACT:
                id = R.string.contact;
                break;
            case EVENT:
                id = R.string.event;
                break;
            case TASK:
                id = R.string.task;
                break;
            case BOOKMARK:
                id = R.string.bookmark;
                break;
            case NOTE:
                id = R.string.note;
                break;
            case DOC:
                id = R.string.doc;
                break;
            case UPLOAD:
                id = R.string.file;
                break;
            case PHOTO:
                id = R.string.photo;
                break;
            case ALBUM:
                id = R.string.album;
                break;
            case JOURNAL:
                id = R.string.journal;
                break;
            case STICKY:  // fall-through
            case NOT_ASSIGNED:  // fall-through
            default:
                id = R.string.entry;
                break;
        }
        return resources.getString(id);
    }

    public final ListView getListView() {
        return mListView;
    }

    public final EntryService getEntryService() {
        return mEntryService;
    }
}
