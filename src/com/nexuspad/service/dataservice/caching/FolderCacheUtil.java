package com.nexuspad.service.dataservice.caching;

import android.util.Log;
import com.nexuspad.service.datamodel.NPFolder;
import com.nexuspad.service.datastore.db.CacheDao;
import com.nexuspad.service.datastore.db.DatabaseManager;
import com.nexuspad.service.dataservice.NPException;
import com.nexuspad.service.util.DateUtil;
import org.json.JSONObject;

public class FolderCacheUtil extends DataCacheUtil {

    /**
     * Get cached folder list.
     *
     * @param parentFolder
     * @return
     */
    public static JSONObject getCachedFolder(NPFolder parentFolder) {
        String cacheKey = folderCacheKey(parentFolder);
        Log.d("FolderCacheUtil", "Retrieve cached the entry list using key: " + cacheKey);

        try {
            CacheDao cacheDao = new CacheDao(DatabaseManager.getDb());
            return cacheDao.getCachedRow(cacheKey, DateUtil.getCurrentTimestamp());

        } catch (NPException npe) {
            Log.e("FolderCacheUtil", "NPException:" + npe.toString());
        }

        return null;
    }

    public static void cacheFolders(NPFolder parentFolder, JSONObject folderListJson) {
        String cacheKey = folderCacheKey(parentFolder);
        Log.d("FolderCacheUtil", "Cache the folders using the key: " + cacheKey);

        try {
            CacheDao cacheDao = new CacheDao(DatabaseManager.getDb());
            cacheDao.saveCachedRow(cacheKey, folderListJson.toString(), cacheExpiryTime());

        } catch (NPException npe) {
            Log.e("FolderCacheUtil", "NPException:" + npe.toString());
        }
    }

    public static String folderCacheKey(int moduleId, int ownerId) {
        StringBuffer buf = new StringBuffer();
        buf.append(moduleId).append("|").append(ownerId);
        return buf.toString();
    }

    public static String folderCacheKey(NPFolder folder) {
        StringBuffer buf = new StringBuffer();
        buf.append(folder.getModuleId()).append("|").
                append(folder.getAccessInfo().getOwner().getUserId());
        return buf.toString();
    }

    private static long cacheExpiryTime() {
        return DateUtil.getCurrentTimestamp() + 120; // 2 minutes for now
    }

    public static void deleteFolderCache(int moduleId, int ownerId) {
        String cacheKey = folderCacheKey(moduleId, ownerId);
        Log.d("EntryCacheUtil", "Delete the folder cache with key: " + cacheKey);
        try {
            CacheDao cacheDao = new CacheDao(DatabaseManager.getDb());
            cacheDao.delete(cacheKey);

        } catch (NPException npe) {
            Log.e("EntryCacheUtil", "NPException:" + npe.toString());
        }
    }
}
