/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.datastore.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.nexuspad.service.datamodel.NPUser;

public class AccountDao {

    private final SQLiteDatabase db;

    public AccountDao(SQLiteDatabase db) {
        this.db = db;
    }

    public NPUser getAccountInfo() {
        NPUser acct = new NPUser();

        Log.d("AccountDao", "SELECT user_id, session_id, user_name, email, password, first_name FROM account LIMIT 1");

        Cursor result = db.rawQuery("SELECT user_id, session_id, user_name, email, password, first_name FROM account LIMIT 1", null);

        if (result.getCount() == 0) {
            result.close();
            return null;
        }

        while (result.moveToNext()) {
            acct.setUserId(result.getInt(0));
            acct.setSessionId(result.getString(1));
            acct.setUserName(result.getString(2));
            acct.setEmail(result.getString(3));
            acct.setPassword(result.getString(4));
            acct.setFirstName(result.getString(5));
        }

        result.close();

        return acct;
    }

    public void updateAcctInfo(NPUser acct) {
        try {
            db.beginTransaction();
            db.execSQL("DELETE FROM account WHERE user_id = " + acct.getUserId());

            ContentValues cv = new ContentValues();
            cv.put("user_id", acct.getUserId());
            cv.put("session_id", acct.getSessionId());
            cv.put("email", acct.getEmail());
            cv.put("password", acct.getPassword());
            cv.put("user_name", acct.getUserName());
            cv.put("first_name", acct.getFirstName());

            db.insertOrThrow("account", null, cv);

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }
    }

    public void clearAcctInfo() {
        try {
            db.beginTransaction();
            db.execSQL("DELETE FROM account");
            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }
    }
}
