/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.dataservice.caching;

import android.util.Log;
import com.nexuspad.service.datastore.db.CacheDao;
import com.nexuspad.service.datastore.db.DatabaseManager;
import com.nexuspad.service.dataservice.NPException;

public class DataCacheUtil {

    public static void clearExpired() {
        Log.d("DataCacheUtil", "Delete all expired cache records.");
        try {
            CacheDao cacheDao = new CacheDao(DatabaseManager.getDb());
            cacheDao.delete(false);

        } catch (NPException npe) {
            Log.e("EntryCacheUtil", "NPException:" + npe.toString());
        }
    }

    public static void clearAll() {
        Log.d("DataCacheUtil", "Delete all cache records.");
        try {
            CacheDao cacheDao = new CacheDao(DatabaseManager.getDb());
            cacheDao.delete(true);
        } catch (NPException npe) {
            Log.e("EntryCacheUtil", "NPException:" + npe.toString());
        }
    }
}
