/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.common.listeners;

import android.content.Intent;
import android.content.res.Resources;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.PopupMenu;
import com.nexuspad.R;
import com.nexuspad.common.Constants;
import com.nexuspad.service.datamodel.NPEntry;
import com.nexuspad.service.dataservice.EntryService;
import com.nexuspad.common.utils.UndoBarController;

/**
 * Listener to handle entry item menu click.
 *
 * @param <T>
 */
public abstract class OnEntryMenuClickListener<T extends NPEntry> implements OnClickListener {
    public static final String TAG = OnEntryMenuClickListener.class.getName();

	protected View mView;
    protected EntryService mEntryService;
    protected UndoBarController mController;

    public OnEntryMenuClickListener(View view, EntryService entryService, UndoBarController controller) {
	    mView = view;
        mEntryService = entryService;
        mController = controller;
    }

	/**
	 * Must be overridden in individual fragments.
	 *
	 * @param v
	 */
    public abstract void onClick(View v);

	/**
	 * Pop up the menu.
	 * Usually called from onClick override.
	 *
	 * @param entry
	 * @param position
	 * @param view
	 */
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

	/**
	 * Handles each entry menu click.
	 *
	 * @param entry
	 * @param position
	 * @param menuId
	 * @return
	 */
    protected boolean onEntryMenuClick(T entry, int position, int menuId) {
        switch (menuId) {
            case R.id.delete:
                deleteEntry(entry);
                return true;
        }
        return false;
    }

	/**
	 * Handles deleting entry with undo bar.
	 *
	 * @param entry
	 */
    private void deleteEntry(T entry) {
        final Resources resources = mView.getResources();
	    if (resources != null) {
		    String string = resources.getString(R.string.format_deleted_entry, getEntryString(entry, resources), entry.getTitle());

		    final Intent undoToken = new Intent(EntryService.ACTION_DELETE);
		    undoToken.putExtra(Constants.KEY_ENTRY, entry);

		    mController.showUndoBar(false, string, undoToken);
	    }
    }

	public static PopupMenu getPopupMenu(View view) {
		PopupMenu menu = (PopupMenu) view.getTag();
		if (menu == null) {
			menu = new PopupMenu(view.getContext(), view);
			menu.inflate(R.menu.entry_list_popupmenu);

			view.setTag(menu);
		}
		return menu;
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

    public final EntryService getEntryService() {
        return mEntryService;
    }
}
