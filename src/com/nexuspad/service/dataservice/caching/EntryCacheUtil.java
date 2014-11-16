/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.dataservice.caching;

import android.util.Log;
import com.nexuspad.service.datamodel.EntryList;
import com.nexuspad.service.datamodel.EntryTemplate;
import com.nexuspad.service.datamodel.NPFolder;
import com.nexuspad.service.datastore.db.CacheDao;
import com.nexuspad.service.datastore.db.DatabaseManager;
import com.nexuspad.service.dataservice.NPException;
import com.nexuspad.service.dataservice.ServiceConstants;
import com.nexuspad.service.util.DateUtil;
import org.json.JSONObject;

public class EntryCacheUtil extends DataCacheUtil {

    /**
     * Get the cached entry list, as JSONObject.
     *
     * @param cacheKey
     * @return
     */
    public static JSONObject getCachedEntryList(String cacheKey) {
        Log.d("EntryCacheUtil", "Retrieve cached the entry list using key: " + cacheKey);

        try {
            CacheDao cacheDao = new CacheDao(DatabaseManager.getDb());
            return cacheDao.getCachedRow(cacheKey, DateUtil.getCurrentTimestamp());

        } catch (NPException npe) {
            Log.e("EntryCacheUtil", "NPException:" + npe.toString());
        }

        return null;
    }

    /**
     * Store the entry list JSONObject to cache.
     *
     * @param cacheKey
     * @param entryListJson
     */
    public static void cacheEntryList(NPFolder folder, String cacheKey, JSONObject entryListJson) {
        Log.d("EntryCacheUtil", "Cache the entry list using the key: " + cacheKey);

        try {
            CacheDao cacheDao = new CacheDao(DatabaseManager.getDb());
            cacheDao.saveCachedRow(cacheKey, entryListJson.toString(), cacheExpiryTime(folder));

        } catch (NPException npe) {
            Log.e("EntryCacheUtil", "NPException:" + npe.toString());
        }
    }

    /**
     * Get the cache key for entry list.
     *
     * @param ownerId
     * @param moduleId
     * @param folderId
     * @param templateId
     * @param pageId
     * @return
     */
    public static String listCacheKey(int ownerId, int moduleId, int folderId, EntryTemplate templateId,
                                      String startYmd, String endYmd, int pageId) {
        StringBuffer buf = new StringBuffer();
        buf.append(ownerId).append("|").
                append(moduleId).append("|").
                append(folderId).append("|").
                append(templateId.getIntValue()).append("|");

        if (!startYmd.isEmpty()) {
            buf.append(startYmd).append("|");
        }

        if (!endYmd.isEmpty()) {
            buf.append(endYmd).append("|");
        }

        if (pageId == 0) {
            buf.append("%"); // This is for using in deleting all cache records.
        } else {
            buf.append(pageId);
        }

        return buf.toString();
    }

    /**
     * Get cache key for entry list.
     *
     * @param entryList
     * @return
     */
    public static String listCacheKey(EntryList entryList) {
        StringBuffer buf = new StringBuffer();
        buf.append(entryList.getAccessInfo().getOwner().getUserId()).append("|").
                append(entryList.getFolder().getModuleId()).append("|").
                append(entryList.getFolder().getFolderId()).append("|").
                append(entryList.getEntryTemplate().getIntValue()).append("|");

        if (entryList.getStartYmd() != null && !entryList.getStartYmd().isEmpty()) {
            buf.append(entryList.getStartYmd()).append("|");
        }
        if (entryList.getEndYmd() != null && !entryList.getEndYmd().isEmpty()) {
            buf.append(entryList.getEndYmd()).append("|");
        }

        buf.append(entryList.getPageId());

        return buf.toString();
    }

    private static long cacheExpiryTime(NPFolder folder) {
        if (folder.getModuleId() == ServiceConstants.CONTACT_MODULE && folder.getFolderId() == ServiceConstants.ROOT_FOLDER) {
            // may be a longer cacher here.
        }
        return DateUtil.getCurrentTimestamp() + 120; // 2 minutes for now
    }

    /**
     * This is to be used to delete all pages of folder listing.
     *
     * @param ownerId
     * @param moduleId
     * @param folderId
     * @param templateId
     */
    public static void deleteListCache(int ownerId, int moduleId, int folderId, EntryTemplate templateId) {
        String cacheKey = listCacheKey(ownerId, moduleId, folderId, templateId, "", "", 0);
        Log.d("EntryCacheUtil", "Delete entry cache with the key: " + cacheKey);
        try {
            CacheDao cacheDao = new CacheDao(DatabaseManager.getDb());
            cacheDao.delete(cacheKey);

        } catch (NPException npe) {
            Log.e("EntryCacheUtil", "NPException:" + npe.toString());
        }
    }
}
