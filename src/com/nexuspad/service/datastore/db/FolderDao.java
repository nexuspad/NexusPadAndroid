/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.datastore.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.nexuspad.service.datamodel.NPFolder;
import com.nexuspad.service.dataservice.ErrorCode;
import com.nexuspad.service.dataservice.NPException;
import com.nexuspad.service.util.Logs;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Database access for folders table.
 *
 * @author ren
 */
public class FolderDao {
    private SQLiteDatabase db;

    public FolderDao(SQLiteDatabase db) {
        this.db = db;
    }

    /**
     * Select folders.
     *
     * @param moduleId
     * @param ownerId
     * @return
     */
    public List<NPFolder> selectFolders(int moduleId, int ownerId, int parentId) {
        List<NPFolder> folders = new ArrayList<NPFolder>();

        String sql = null;
        ;

        if (parentId < 0) {        // Select all folders
            sql = "SELECT module_id, folder_id, folder_name, parent_id, color_label, status, last_modified, synced FROM folders WHERE " +
                    " module_id = " + moduleId +
                    " AND owner_id = " + ownerId +
                    " ORDER BY folder_name ";
        } else {
            sql = "SELECT module_id, folder_id, folder_name, parent_id, color_label, status, last_modified, synced FROM folders WHERE " +
                    " module_id = " + moduleId +
                    " folder_id = " + parentId +
                    " AND owner_id = " + ownerId +
                    " ORDER BY folder_name ";
        }

        Cursor result = this.db.rawQuery(sql, null);

        while (result.moveToNext()) {
            folders.add(resultToFolder(result));
        }

        result.close();
        return folders;
    }

    /**
     * Select unsynced folders.
     *
     * @param ownerId
     * @return
     */
    public List<NPFolder> selectUnsyncedFolders(int ownerId) {
        List<NPFolder> folders = new ArrayList<NPFolder>();

        String sql = "SELECT module_id, folder_id, folder_name, parent_id, color_label, status, last_modified, synced FROM folders WHERE " +
                " owner_id = " + ownerId + " AND synced = 0 ";

        Cursor result = this.db.rawQuery(sql, null);

        if (result.getCount() == 0) {
            result.close();
            return folders;
        }

        while (result.moveToNext()) {
            folders.add(resultToFolder(result));
        }

        result.close();
        return folders;
    }


    /**
     * Select folder.
     *
     * @param folder
     * @return
     */
    public NPFolder select(NPFolder folder) {
        String sql = "SELECT module_id, folder_id, folder_name, parent_id, color_label, status, last_modified, synced FROM folders WHERE " +
                " module_id = " + folder.getModuleId() +
                " AND owner_id = " + folder.getAccessInfo().getOwner().getUserId() +
                " AND folder_id = " + folder.getFolderId();

        Cursor result = this.db.rawQuery(sql, null);

        if (result.getCount() == 0) {
            result.close();
            return null;
        }

        while (result.moveToNext()) {
            folder = resultToFolder(result);
            break;
        }

        result.close();

        return folder;
    }

    /**
     * Create Folder object from result.
     * <p/>
     * module_id, folder_id, folder_name, parent_id, color_label, status, last_modified, synced
     *
     * @param result
     * @return
     */
    private NPFolder resultToFolder(Cursor result) {
        NPFolder f = new NPFolder();
        f.setModuleId(result.getInt(0));
        f.setFolderId(result.getInt(1));
        f.setFolderName(result.getString(2));
        f.setParentId(result.getInt(3));
        f.setColorLabel(result.getString(4));
        f.setStatus(result.getInt(5));

        if (result.getLong(6) != 0) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(result.getLong(6));
            f.setLastModified(cal.getTime());
        }

        int synced = result.getInt(7);
        if (synced > 0) {
            f.setSynced(true);
        } else {
            f.setSynced(false);
        }

        return f;
    }

    /**
     * Insert an folder.
     *
     * @param folder
     * @throws com.nexuspad.service.dataservice.NPException
     */
    public void insert(NPFolder folder) throws NPException {
        try {
            this.db.beginTransaction();
            ContentValues cv = createContentValues(folder);
            db.insertOrThrow("folders", null, cv);
            db.setTransactionSuccessful();

        } finally {
            this.db.endTransaction();
        }
    }


    /**
     * Update folder.
     *
     * @param folder
     * @throws com.nexuspad.service.dataservice.NPException
     */
    public void update(NPFolder folder) throws NPException {
        if (folder.getSyncId() == 0 && folder.getFolderId() <= 0) {
            throw new NPException(ErrorCode.INTERNAL_ERROR, "Folder has neither valid folder Id nor sync Id.");
        }

        try {
            db.beginTransaction();
            ContentValues cv = createContentValues(folder);

            int updatedRows = 0;

            // The sync Id takes precedence when updating the folder record.
            if (folder.getSyncId() != 0) {
                String whereStmt = "module_id = " + folder.getModuleId() + " AND folder_id = " + folder.getSyncId() +
                        " AND owner_id = " + folder.getAccessInfo().getOwner().getUserId();
                updatedRows = db.update("folders", cv, whereStmt, null);

            } else if (folder.getFolderId() > 0) {
                String whereStmt = "module_id = " + folder.getModuleId() + " AND folder_id = " + folder.getFolderId() +
                        " AND owner_id = " + folder.getAccessInfo().getOwner().getUserId();
                updatedRows = db.update("folders", cv, whereStmt, null);
            }

            // No local update, go ahead insert the folder.
            if (updatedRows == 0) {
                db.insertOrThrow("folders", null, cv);
            }

            db.setTransactionSuccessful();

        } finally {
            this.db.endTransaction();
        }
    }


	/**
	 * update folders.
	 *
	 * @param folders
	 */
    public void updateFolders(List<NPFolder> folders) {
        for (NPFolder folder : folders) {

            folder.setSynced(true);

            ContentValues cv = null;
            try {
                cv = createContentValues(folder);
            } catch (NPException e) {
                Logs.e("FolderDao", "FolderStore not updated: " + folder, e);
            }

            try {
                db.beginTransaction();

                Logs.d("FolderDao", "Update folder: " + folder);

                String whereStmt = "module_id = " + folder.getModuleId() + " AND folder_id = " + folder.getFolderId() +
                        " AND owner_id = " + folder.getAccessInfo().getOwner().getUserId();

                int affected = db.update("folders", cv, whereStmt, null);

                if (affected == 0) {
                    Logs.d("FolderDao", "	Insert folder: " + folder);
                    db.insertOrThrow("folders", null, cv);
                }

                db.setTransactionSuccessful();

            } finally {
                this.db.endTransaction();
            }

        }
    }

    /**
     * Create Content values.
     *
     * @param folder
     * @return
     * @throws com.nexuspad.service.dataservice.NPException
     */
    private ContentValues createContentValues(NPFolder folder) throws NPException {
        ContentValues cv = new ContentValues();

        cv.put("module_id", folder.getModuleId());

        if (folder.getFolderId() > 0) {
            cv.put("folder_id", folder.getFolderId());

        } else if (folder.getSyncId() > 0) {
            cv.put("folder_id", folder.getSyncId());

        } else {
            throw new NPException(ErrorCode.MISSING_PARAM, "Update folder store missing folder Id.");
        }

        cv.put("folder_name", folder.getFolderName());
        cv.put("color_label", folder.getColorLabel());
        cv.put("parent_id", folder.getParentId());
        cv.put("owner_id", folder.getAccessInfo().getOwner().getUserId());
        cv.put("status", folder.getStatus());

        if (folder.getLastModified() != null) {
            cv.put("last_modified", folder.getLastModified().getTime());
        } else {
            cv.put("last_modified", 0);
        }

        if (folder.isSynced()) {
            cv.put("synced", 1);
        } else {
            cv.put("synced", 0);
        }

        return cv;
    }


    /**
     * Delete folder.
     *
     * @param folder
     */
    public void delete(NPFolder folder) {
        String sql = "DELETE FROM folders WHERE " +
                " module_id = " + folder.getModuleId() + " AND folder_id = " + folder.getFolderId() + " AND owner_id = " + folder.getAccessInfo().getOwner().getUserId();

        try {
            db.beginTransaction();
            db.execSQL(sql);

            sql = "DELETE FROM entries WHERE " +
                    " module_id = " + folder.getModuleId() + " AND folder_id = " + folder.getFolderId() + " AND owner_id = " + folder.getAccessInfo().getOwner().getUserId();
            db.execSQL(sql);

            db.setTransactionSuccessful();

        } finally {
            this.db.endTransaction();
        }
    }

}
