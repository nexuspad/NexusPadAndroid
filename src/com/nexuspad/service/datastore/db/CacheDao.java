/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.datastore.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

public class CacheDao {

    private SQLiteDatabase db;

    public CacheDao(SQLiteDatabase db) {
        this.db = db;
    }

    public JSONObject getCachedRow(String cacheKey, long expiryTs) {
        JSONObject entryListJson = null;
        Log.d("", "SELECT cache_data FROM data_cache WHERE cache_key ='" + cacheKey + "' AND expiry_time > " + expiryTs);

        Cursor result = this.db.rawQuery("SELECT cache_data FROM data_cache WHERE cache_key ='" + cacheKey + "' AND expiry_time > " + expiryTs, null);

        if (result.getCount() == 0) {
            result.close();
            return null;
        }

        while (result.moveToNext()) {
            try {
                entryListJson = new JSONObject(result.getString(0));
            } catch (JSONException e) {
                Log.e("CacheDao", "Stored cache entry is not a JSON formatted string. Cache key: " + cacheKey);
            }
        }

        result.close();

        return entryListJson;
    }

    public void saveCachedRow(String cacheKey, String jsonInString, long expiryTime) {
        try {
            this.db.beginTransaction();

            this.delete(cacheKey);

            ContentValues cv = new ContentValues();
            cv.put("cache_key", cacheKey);
            cv.put("cache_data", jsonInString);
            cv.put("expiry_time", expiryTime);
            this.db.insertOrThrow("data_cache", null, cv);
            db.setTransactionSuccessful();
        } finally {
            this.db.endTransaction();
        }
    }

    public void delete(String cacheKey) {
        if (cacheKey.substring(cacheKey.length() - 1).endsWith("%")) {
            Log.d("", "DELETE FROM data_cache WHERE cache_key LIKE '" + cacheKey + "'");
            this.db.execSQL("DELETE FROM data_cache WHERE cache_key LIKE '" + cacheKey + "'");
        } else {
            this.db.execSQL("DELETE FROM data_cache WHERE cache_key = '" + cacheKey + "'");
        }
    }

    public void delete(boolean deleteAll) {
        if (deleteAll) {
            this.db.execSQL("DELETE * FROM data_cache");
        } else {
            // Delete expired only
            this.db.execSQL("DELETE * FROM data_cache WHERE expiry_time < strftime('%s','now') ");
        }
    }
}
