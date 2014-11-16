/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.datastore.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.nexuspad.service.util.Logs;

public class NPDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "npdb";
    private static final int SCHEMA = 4;

    public NPDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA);
        Logs.d("NPDatabaseHelper", "SQLiteOpenHelper created.");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Logs.d("NPDatabaseHelper", "Update the npdb schema...");

        try {
            db.beginTransaction();
            db.execSQL("DROP TABLE IF EXISTS account");
            db.execSQL("DROP TABLE IF EXISTS data_cache");
            db.execSQL("DROP TABLE IF EXISTS entries");
            db.execSQL("DROP TABLE IF EXISTS folders");
            db.setTransactionSuccessful();

        } catch (Exception e) {
            Logs.e("NPDatabaseHelper", "Error upgrading table schemas.", e);

        } finally {
            db.endTransaction();
        }

        createTables(db);
    }

    private static void createTables(SQLiteDatabase db) {
        try {
            Logs.d("NPDatabaseHelper", "Start creating tables for NexusPad.");
            db.beginTransaction();

            // Create account table
            String createAcctTableSql =
                    "CREATE TABLE IF NOT EXISTS account (user_id INTEGER, session_id TEXT, email TEXT, user_name TEXT, password TEXT, first_name TEXT) ";
            db.execSQL(createAcctTableSql);

            // Create caching table
            String createCachingTableSql =
                    "CREATE TABLE IF NOT EXISTS data_cache (cache_key TEXT, cache_data BLOB, expiry_time INTEGER) ";
            db.execSQL(createCachingTableSql);

            // Create entries table
            String createEntriesTableSql =
                    "CREATE TABLE IF NOT EXISTS entries (" +
                            "module_id		INTEGER, " +
                            "entry_id		TEXT, " +
                            "folder_id		INTEGER, " +
                            "template_id	INTEGER, " +
                            "owner_id		INTEGER, " +
                            "status			INTEGER, " +
                            "content		BLOB, " +
                            "keyword_filter	TEXT, " +
                            "time_filter	INTEGER, " +
                            "create_time	INTEGER, " +
                            "last_modified	INTEGER, " +
                            "synced			INTEGER) ";

            db.execSQL(createEntriesTableSql);

            // Create folders table
            String createFoldersTableSql =
                    "CREATE TABLE IF NOT EXISTS folders (" +
                            "module_id		INTEGER, " +
                            "folder_id		INTEGER, " +
                            "folder_name	TEXT, " +
                            "color_label	TEXT, " +
                            "parent_id		INTEGER, " +
                            "owner_id		INTEGER, " +
                            "status			INTEGER, " +
                            "last_modified	INTEGER, " +
                            "synced			INTEGER) ";

            db.execSQL(createFoldersTableSql);

            db.setTransactionSuccessful();

            Logs.d("NPDatabaseHelper", "Finish creating tables.");

        } catch (Exception e) {
            Logs.e("NPDatabaseHelper", "Error creating tables.", e);

        } finally {
            db.endTransaction();
        }
    }
}
