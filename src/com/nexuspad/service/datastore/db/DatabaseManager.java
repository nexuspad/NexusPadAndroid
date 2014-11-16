/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.datastore.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.nexuspad.service.dataservice.ErrorCode;
import com.nexuspad.service.dataservice.NPException;
import com.nexuspad.service.util.Logs;

/**
 * A utility class to get database instance
 *
 * @author ren
 */
// TODO check if db is somehow closed
public class DatabaseManager {
    private static SQLiteDatabase db;

    public static SQLiteDatabase getDb(Context ctx) {
        if (DatabaseManager.db == null) {
            Logs.d("DatabaseManager", "Initialize the database.");
            NPDatabaseHelper dbh = new NPDatabaseHelper(ctx);
            DatabaseManager.db = dbh.getWritableDatabase();
        }

        return DatabaseManager.db;
    }

    public static SQLiteDatabase getDb() throws NPException {
        if (DatabaseManager.db != null) {
            return DatabaseManager.db;
        }
        throw new NPException(ErrorCode.INTERNAL_ERROR, "The SQLite database hasn't be initialized.");
    }

    public static void setDb(SQLiteDatabase db) {
        DatabaseManager.db = db;
    }
}
