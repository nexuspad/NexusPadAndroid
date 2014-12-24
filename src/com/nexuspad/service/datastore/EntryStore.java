/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.datastore;

import android.os.AsyncTask;
import android.os.Build;
import com.nexuspad.service.datamodel.EntryList;
import com.nexuspad.service.datamodel.EntryTemplate;
import com.nexuspad.service.datamodel.NPEntry;
import com.nexuspad.service.datamodel.NPFolder;
import com.nexuspad.service.dataservice.NPException;
import com.nexuspad.service.datastore.db.DatabaseManager;
import com.nexuspad.service.datastore.db.EntryDao;
import com.nexuspad.service.util.Logs;

import java.util.List;
import java.util.Random;

/**
 * Handles entry store select and update.
 *
 * @author ren
 */
public class EntryStore extends DataStore {

    /**
     * Get entries.
     *
     * @param folder
     * @param type
     * @param page
     * @param count
     * @return
     * @throws com.nexuspad.service.dataservice.NPException
     */
    public static List<NPEntry> getEntriesInFolder(NPFolder folder, EntryTemplate type, int page, int count) throws NPException {
        EntryDao entryDao = new EntryDao(DatabaseManager.getDb());
        return entryDao.selectEntries(folder.getModuleId(), folder.getFolderId(), type,
                folder.getAccessInfo().getOwner().getUserId(), page, count);
    }


    /**
     * Search entries.
     *
     * @param query
     * @param folder
     * @param type
     * @param page
     * @param count
     * @throws com.nexuspad.service.dataservice.NPException
     */
    public static void searchEntriesInFolder(String query, NPFolder folder, EntryTemplate type, int page, int count) throws NPException {
    }

    /**
     * Get entries between dates.
     *
     * @param folder
     * @param type
     * @param startDate
     * @param endDate
     * @param page
     * @param count
     * @throws com.nexuspad.service.dataservice.NPException
     */
    public static void getEntriesBetweenDates(NPFolder folder, EntryTemplate type, long startDate, long endDate, int page, int count) throws NPException {
    }


    public static List<NPEntry> getUnsyncedEntries(int ownerId) throws NPException {
        EntryDao entryDao = new EntryDao(DatabaseManager.getDb());
        return entryDao.selectUnsyncedEntries(ownerId);
    }

    /**
     * Update an entry.
     *
     * @param entry
     */
    public static void update(NPEntry entry) {
        //Logs.i("EntryStore", "Update the entry in data store: " + entry.toString());

        try {
            EntryDao entryDao = new EntryDao(DatabaseManager.getDb());
            entryDao.update(entry);

        } catch (NPException e) {
            Logs.e("EntryStore", "Entry not updated: " + entry.toString(), e);
        }
    }

    /**
     * Delete an entry.
     *
     * @param entry
     */
    public static void delete(NPEntry entry) {
        Logs.i("EntryStore", "Delete the entry in data store: " + entry.toString());

        try {
            EntryDao entryDao = new EntryDao(DatabaseManager.getDb());
            entryDao.delete(entry);

        } catch (NPException e) {
            Logs.e("EntryStore", "Entry not deleted: " + entry.toString(), e);
        }
    }

    /**
     * Generate a random id for entry.
     *
     * @return
     */
    public static String generateSyncId() {
        Random r = new Random();
        int n = r.nextInt(99999 - 90000) + 90000;

        return "_" + n;
    }

    /**
     * Update a list of entries in non blocking fashion.
     *
     * @param entryList
     * @return
     */
    public static AsyncTask<?, ?, ?> updateEntryListAsync(EntryList entryList) {
        AsyncTask<Void, Long, Boolean> bgTask = new EntryStoreUpdateTask(entryList);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            bgTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
        else
            bgTask.execute((Void[]) null);

        return bgTask;
    }

    /**
     * Update a list of entries.
     *
     * @param entryList
     */
    public static void updateEntryList(EntryList entryList) {
        try {
            EntryDao entryDao = new EntryDao(DatabaseManager.getDb());
            entryDao.updateEntries(entryList);
        } catch (NPException e) {
            e.printStackTrace();
        }
    }

    /**
     * Async task for updating entry list.
     *
     * @author ren
     */
    private static class EntryStoreUpdateTask extends AsyncTask<Void, Long, Boolean> {
        private final EntryList entryList;

        private EntryStoreUpdateTask(EntryList entryList) {
            this.entryList = entryList;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            updateEntryList(entryList);
            return null;
        }

    }
}
