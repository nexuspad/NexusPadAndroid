/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.datastore.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.google.common.base.Strings;
import com.nexuspad.service.datamodel.EntryFactory;
import com.nexuspad.service.datamodel.EntryList;
import com.nexuspad.service.datamodel.EntryTemplate;
import com.nexuspad.service.datamodel.NPEntry;
import com.nexuspad.service.dataservice.ErrorCode;
import com.nexuspad.service.dataservice.NPException;
import com.nexuspad.service.util.Logs;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.*;

/**
 * Database access for entries table.
 *
 * @author ren
 */
public class EntryDao {

	private SQLiteDatabase db;

	public EntryDao(SQLiteDatabase db) {
		this.db = db;
	}

	/**
	 * Select entries.
	 *
	 * @param moduleId
	 * @param folderId
	 * @param templateId
	 * @param ownerId
	 * @return
	 */
	public List<NPEntry> selectEntries(int moduleId, int folderId, EntryTemplate templateId, int ownerId, int pageId, int countPerPage) {
		List<NPEntry> entries = new ArrayList<NPEntry>();

		String sql = "SELECT module_id, entry_id, content, last_modified, status, synced FROM entries WHERE " +
				" module_id = " + moduleId +
				" AND folder_id = " + folderId +
				" AND template_id = " + templateId.getIntValue() +
				" AND owner_id = " + ownerId +
				" ORDER BY last_modified DESC ";

		if (pageId != 0) {
			int offset = (pageId - 1) * countPerPage;
			sql = "SELECT module_id, entry_id, content, last_modified, status, synced FROM entries WHERE " +
					" module_id = " + moduleId + " AND entry_id IN " +
					" (SELECT entry_id FROM entries WHERE " +
					" module_id = " + moduleId +
					" AND folder_id = " + folderId +
					" AND template_id = " + templateId.getIntValue() +
					" AND owner_id = " + ownerId +
					" ORDER BY last_modified DESC) " +
					" LIMIT " + countPerPage + " OFFSET " + offset;
		}

		Logs.d("EntryDao", sql);

		Cursor result = this.db.rawQuery(sql, null);

		while (result.moveToNext()) {
			NPEntry e = resultToEntry(result);
			entries.add(e);
		}

		result.close();

		return entries;
	}


	/**
	 * Get all unsynced entries.
	 *
	 * @param ownerId
	 * @return
	 */
	public List<NPEntry> selectUnsyncedEntries(int ownerId) {
		List<NPEntry> entries = new ArrayList<NPEntry>();

		String sql = "SELECT module_id, entry_id, content, last_modified, status, synced FROM entries WHERE " +
				" owner_id = " + ownerId + " AND synced = 0";

		Logs.d("EntryDao", sql);

		Cursor result = this.db.rawQuery(sql, null);

		while (result.moveToNext()) {
			NPEntry e = resultToEntry(result);

			if (e != null) {
				entries.add(e);
			}
		}

		result.close();
		return entries;
	}


	/**
	 * Select entry.
	 *
	 * @param entry
	 * @return
	 */
	public NPEntry select(NPEntry entry) {
		String sql = "SELECT module_id, entry_id, content, last_modified, status, synced FROM entries WHERE " +
				" module_id = " + entry.getModuleId() +
				" AND owner_id = " + entry.getAccessInfo().getOwner().getUserId() +
				" AND entry_id = '" + entry.getEntryId() + "'" +
				" ORDER BY last_modified DESC ";

		Cursor result = this.db.rawQuery(sql, null);

		if (result.getCount() == 0) {
			result.close();
			return null;
		}

		while (result.moveToNext()) {
			entry = resultToEntry(result);
			break;
		}

		result.close();

		return entry;
	}


	/**
	 * Get NPEntry object based on the result.
	 *
	 * @param result
	 * @return
	 */
	private NPEntry resultToEntry(Cursor result) {
		int moduleId = result.getInt(0);
		String entryId = result.getString(1);

		String jsonStr = result.getString(2);
		try {
			JSONObject jsonObj = new JSONObject(jsonStr);
			@SuppressWarnings("unchecked")
			Iterator<String> keys = jsonObj.keys();

			/*
			 * Convert the content string to JSON object or array.
			 */
			while (keys.hasNext()) {
				String key = (String) keys.next();

				JSONTokener tokener = new JSONTokener((String) jsonObj.get(key));

				if (tokener.more()) {
					Object obj = tokener.nextValue();

					if (obj instanceof JSONObject) {
						jsonObj.put(key, new JSONObject(jsonObj.getString(key)));

					} else if (obj instanceof JSONArray) {
						jsonObj.put(key, new JSONArray(jsonObj.getString(key)));
					}
				}
			}

			NPEntry entry = EntryFactory.jsonToEntry(moduleId, jsonObj);
			entry.setEntryId(entryId);

			long lastModified = result.getLong(3);
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(lastModified);
			entry.setLastModifiedTime(cal.getTime());

			entry.setStatus(result.getInt(4));

			if (result.getInt(5) == 0) {
				entry.setSynced(false);
			} else {
				entry.setSynced(true);
			}

			return entry;

		} catch (JSONException e) {
			Logs.e("EntryDao", "Error parsing json string: " + jsonStr, e);
		}

		return null;
	}

	/**
	 * Insert an entry.
	 *
	 * @param entry
	 * @throws com.nexuspad.service.dataservice.NPException
	 */
	public void insert(NPEntry entry) throws NPException {
		try {
			this.db.beginTransaction();

			ContentValues cv = createContentValues(entry);
			db.insertOrThrow("entries", null, cv);
			db.setTransactionSuccessful();

		} finally {
			this.db.endTransaction();
		}
	}


	/**
	 * Update entry.
	 *
	 * @param entry
	 * @throws com.nexuspad.service.dataservice.NPException
	 */
	public void update(NPEntry entry) throws NPException {
		if (Strings.isNullOrEmpty(entry.getEntryId()) && Strings.isNullOrEmpty(entry.getSyncId())) {
			throw new NPException(ErrorCode.INTERNAL_ERROR, "Entry has neither valid entry Id nor sync Id.");
		}

		try {
			db.beginTransaction();

			ContentValues cv = createContentValues(entry);

			int updatedRows = 0;

			// The sync Id takes precedence when updating the folder record.
			if (!Strings.isNullOrEmpty(entry.getSyncId())) {

				String whereStmt = "module_id = " + entry.getFolder().getModuleId() + " AND entry_id = '" + entry.getSyncId() + "'" +
						" AND owner_id = " + entry.getAccessInfo().getOwner().getUserId();

				updatedRows = db.update("entries", cv, whereStmt, null);

			} else if (!Strings.isNullOrEmpty(entry.getEntryId())) {

				String whereStmt = "module_id = " + entry.getFolder().getModuleId() + " AND entry_id = '" + entry.getEntryId() + "'" +
						" AND owner_id = " + entry.getAccessInfo().getOwner().getUserId();

				updatedRows = db.update("entries", cv, whereStmt, null);
			}

			if (updatedRows == 0) {
				db.insertOrThrow("entries", null, cv);
			}

			db.setTransactionSuccessful();

		} finally {
			this.db.endTransaction();
		}
	}


	/**
	 * Update entries.
	 *
	 * @param entryList
	 */
	public void updateEntries(EntryList entryList) {
		for (Object o : entryList.getEntries()) {
			NPEntry entry = (NPEntry)o;

			entry.setSynced(true);

			ContentValues cv = null;
			try {
				cv = createContentValues(entry);
			} catch (NPException e) {
				Logs.e("EntryDao", "EntryStore not updated: " + entry, e);
			}

			try {
				db.beginTransaction();

				Logs.d("EntryDao", "Update entry: " + entry.getEntryId());

				String whereStmt = "module_id = " + entry.getModuleId() + " AND entry_id = '" + entry.getEntryId() + "' AND owner_id = " + entry.getAccessInfo().getOwner().getUserId();

				int affected = db.update("entries", cv, whereStmt, null);

				if (affected == 0) {
					Logs.d("EntryDao", "	Insert entry: " + entry.getEntryId());
					db.insertOrThrow("entries", null, cv);
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
	 * @param entry
	 * @return
	 * @throws com.nexuspad.service.dataservice.NPException
	 */
	private ContentValues createContentValues(NPEntry entry) throws NPException {
		ContentValues cv = new ContentValues();

		cv.put("module_id", entry.getModuleId());

		if (!Strings.isNullOrEmpty(entry.getEntryId())) {
			cv.put("entry_id", entry.getEntryId());

		} else if (!Strings.isNullOrEmpty(entry.getSyncId())) {
			cv.put("entry_id", entry.getSyncId());

		} else {
			throw new NPException(ErrorCode.MISSING_PARAM, "Update entry store missing entry Id.");
		}

		cv.put("folder_id", entry.getFolder().getFolderId());
		cv.put("template_id", entry.getTemplate().getIntValue());
		cv.put("owner_id", entry.getAccessInfo().getOwner().getUserId());
		cv.put("status", entry.getStatus());

		Map<String, String> data = entry.toMap();
		JSONObject jsonObj = new JSONObject(data);
		cv.put("content", jsonObj.toString());

		cv.put("keyword_filter", entry.getKeywordFilter());
		cv.put("time_filter", entry.getTimeFilter().getTime());

		cv.put("create_time", entry.getCreateTime().getTime());
		cv.put("last_modified", entry.getLastModifiedTime().getTime());

		if (entry.isSynced()) {
			cv.put("synced", 1);
		} else {
			cv.put("synced", 0);
		}

		return cv;
	}


	/**
	 * Delete entry.
	 *
	 * @param entry
	 */
	public void delete(NPEntry entry) {
		String sql = "DELETE FROM entries WHERE " +
				" module_id = " + entry.getModuleId() + " AND entry_id = '" + entry.getEntryId() + "' AND owner_id = " + entry.getAccessInfo().getOwner().getUserId();

		try {
			db.beginTransaction();
			db.execSQL(sql);
			db.setTransactionSuccessful();

		} finally {
			this.db.endTransaction();
		}
	}
}
