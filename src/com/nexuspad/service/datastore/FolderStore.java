/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.datastore;

import android.os.AsyncTask;
import android.os.Build;
import com.nexuspad.service.datamodel.NPFolder;
import com.nexuspad.service.dataservice.NPException;
import com.nexuspad.service.datastore.db.DatabaseManager;
import com.nexuspad.service.datastore.db.FolderDao;
import com.nexuspad.service.util.Logs;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Handles folder store select and update.
 *
 * @author ren
 */
public class FolderStore extends DataStore {

    /**
     * Get all folders.
     *
     * @param moduleId
     * @param ownerId
     * @return
     */
    public static List<NPFolder> getAllFolders(int moduleId, int ownerId) {
        List<NPFolder> folders = new ArrayList<NPFolder>();

        try {
            FolderDao folderDao = new FolderDao(DatabaseManager.getDb());
            folders = folderDao.selectFolders(moduleId, ownerId, -1);

        } catch (NPException e) {
            Logs.e("FolderStore", "Error retrieving folders.", e);
        }

        return folders;
    }


    /**
     * Get sub folders.
     *
     * @param moduleId
     * @param parentId
     * @param ownerId
     * @return
     */
    public static List<NPFolder> getSubFolders(int moduleId, int parentId, int ownerId) {
        List<NPFolder> folders = new ArrayList<NPFolder>();

        try {
            FolderDao folderDao = new FolderDao(DatabaseManager.getDb());
            folders = folderDao.selectFolders(moduleId, ownerId, parentId);

        } catch (NPException e) {
            Logs.e("FolderStore", "Error retrieving folders.", e);
        }

        return folders;
    }

    /**
     * Get the folders that need to be synced upstream.
     *
     * @param ownerId
     * @return
     */
    public static List<NPFolder> getUnsyncedFolders(int ownerId) {
        List<NPFolder> folders = new ArrayList<NPFolder>();

        try {
            FolderDao folderDao = new FolderDao(DatabaseManager.getDb());
            folders = folderDao.selectUnsyncedFolders(ownerId);

        } catch (NPException e) {
            Logs.e("FolderStore", "Error retrieving unsynced folders.", e);
        }

        return folders;
    }

    /**
     * Update a folder.
     *
     * @param folder
     */
    public static void update(NPFolder folder) {
        Logs.i("FolderStore", "Update the folder in data store: " + folder.toString());

        try {
            FolderDao folderDao = new FolderDao(DatabaseManager.getDb());
            folderDao.update(folder);

        } catch (NPException e) {
            Logs.e("FolderStore", "Error updating folder.", e);
        }
    }


    /**
     * Delete a folder.
     *
     * @param folder
     */
    public static void delete(NPFolder folder) {
        Logs.i("FolderStore", "Delete the folder in data store: " + folder.toString());

        try {
            FolderDao folderDao = new FolderDao(DatabaseManager.getDb());
            folderDao.delete(folder);

        } catch (NPException e) {
            Logs.e("FolderStore", "Error deleting folder.", e);
        }
    }

    /**
     * Generate a random number for folder sync id.
     * 9000 - 9999
     *
     * @return
     */
    public static int generateSyncId() {
        Random r = new Random();
        return r.nextInt(9999 - 9000) + 9000;
    }

    /**
     * Update a list of folders non blocking fashion.
     *
     * @param folders
     * @return
     */
    public static AsyncTask<?, ?, ?> updateFoldersAsync(List<NPFolder> folders) {
        AsyncTask<Void, Long, Boolean> bgTask = new FolderStoreUpdateTask(folders);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            bgTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
        else
            bgTask.execute((Void[]) null);

        return bgTask;
    }

    public static void updateFolders(List<NPFolder> folders) {
        try {
            FolderDao folderDao = new FolderDao(DatabaseManager.getDb());
            folderDao.updateFolders(folders);
        } catch (NPException e) {
            e.printStackTrace();
        }
    }

    private static class FolderStoreUpdateTask extends AsyncTask<Void, Long, Boolean> {
        private final List<NPFolder> folders;

        private FolderStoreUpdateTask(List<NPFolder> folders) {
            this.folders = folders;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            updateFolders(folders);
            return null;
        }

    }
}
