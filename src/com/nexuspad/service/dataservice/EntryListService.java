/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.dataservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;
import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.common.base.Strings;
import com.nexuspad.Manifest;
import com.nexuspad.service.datamodel.EntryList;
import com.nexuspad.service.datamodel.EntryTemplate;
import com.nexuspad.service.datamodel.NPFolder;
import com.nexuspad.service.datamodel.NPModule;
import com.nexuspad.service.datastore.db.DatabaseManager;
import com.nexuspad.service.util.Logs;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Handles retrieving entry list.
 * <p/>
 * The service gets the data from two sources: web service and data store.
 * <p/>
 * If the web service is not available, the data store is used as secondary source, essentially the offline support.
 * <p/>
 * At the end of each web service call, the service would also update the data store with fresh (or not) data.
 *
 * @author ren
 */
public class EntryListService {
	public static final String TAG = "EntryListService";

	/**
	 * contains a string key for retrieving an EntryList with {@link #getEntryListFromKey(String)}.
	 * Parceling exceeds the capacity of Android's RPC limit.
	 */
	public static final String KEY_ENTRY_LIST_STRING_KEY = "key_entry_list_string_key";
	/**
	 * Contains the {@link com.nexuspad.service.datamodel.EntryTemplate} (as Serializable)
	 */
	public static final String KEY_ENTRY_TEMPLATE = "key_entry_template";
	public static final String KEY_ERROR = "key_error";

	public static final String ACTION_GET_FOLDER_LISTING = "action_get_folder_listing";
	public static final String ACTION_SEARCH = "action_entry_list_search";
	public static final String ACTION_ERROR = "action_entry_list_error";

	private static EntryListService mInstance;

	public static EntryListService getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new EntryListService(context);
		}
		return mInstance;
	}

	/**
	 * A {@link android.content.BroadcastReceiver} specified for listening to {@link com.nexuspad.service.datamodel.EntryList}
	 * retrievals.
	 *
	 * @author Edmond
	 * @see EntryListReceiver#getIntentFilter()
	 */
	public static abstract class EntryListReceiver extends BroadcastReceiver {

		/**
		 * @return an {@link android.content.IntentFilter} that works with
		 * {@link EntryListReceiver}
		 * @see android.content.Context#registerReceiver(android.content.BroadcastReceiver, android.content.IntentFilter,
		 * String, android.os.Handler)
		 */
		public static IntentFilter getIntentFilter() {
			IntentFilter filter = new IntentFilter();
			filter.addAction(ACTION_GET_FOLDER_LISTING);
			filter.addAction(ACTION_SEARCH);
			filter.addAction(ACTION_ERROR);
			return filter;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			final String listKey = intent.getStringExtra(KEY_ENTRY_LIST_STRING_KEY);
			final EntryTemplate entryTemplate = (EntryTemplate) intent.getSerializableExtra(KEY_ENTRY_TEMPLATE);

			if (ACTION_GET_FOLDER_LISTING.equals(action)) {
				onReceiveFolderListing(context, intent, entryTemplate, listKey);

			} else if (ACTION_SEARCH.equals(action)) {
				onReceiveSearchResult(context, intent, entryTemplate, listKey);

			} else if (ACTION_ERROR.equals(action)) {
				onError(context, intent, intent.<ServiceError>getParcelableExtra(KEY_ERROR));

			} else {
				Logs.w(TAG, "unhandled action: " + action);
			}
		}

		/**
		 * Called when the requested entry list is retrieved, use {@link com.nexuspad.service.dataservice.EntryListService#getEntryListFromKey(String)} to get the list
		 */
		protected void onReceiveFolderListing(Context c, Intent i, EntryTemplate entryTemplate, String key) {
		}

		protected void onReceiveSearchResult(Context c, Intent i, EntryTemplate entryTemplate, String key) {
		}

		/**
		 * Called when an error has occurred.
		 *
		 * @param error the service error
		 */
		protected void onError(Context context, Intent intent, ServiceError error) {
		}
	}

	private final HashMap<String, EntryList> mEntryListMap = new HashMap<String, EntryList>(2);
	private final Context mContext;
	private NPFolder currentFolder;

	private EntryListService(Context context) {
		mContext = context.getApplicationContext();
		DatabaseManager.getDb(context);
	}

	/**
	 * The list is removed internally (you can only retrieve the list once
	 *
	 * @param key given by {@link #KEY_ENTRY_LIST_STRING_KEY}; cannot be anything else
	 * @return the {@code EntryList}
	 */
	public EntryList getEntryListFromKey(String key) {
		if (!mEntryListMap.containsKey(key)) throw new IllegalArgumentException();
		final EntryList list = mEntryListMap.get(key);
		mEntryListMap.remove(key);
		return list;
	}

	/**
	 * Get entries in folder.
	 *
	 * @param folder
	 * @param type   - the type of entry.
	 * @param page
	 * @param count
	 * @return
	 */
	public void getEntriesInFolder(NPFolder folder, EntryTemplate type, int page, int count) throws NPException {
		searchEntriesInFolder(null, folder, type, page, count);
	}

	public void searchEntriesInFolder(String query, NPFolder folder, EntryTemplate type, int page, int count) throws NPException {
		currentFolder = folder;

		Map<String, String> params = new HashMap<String, String>();

		if (folder.getModuleId() == NPModule.CONTACT && folder.getFolderId() == NPFolder.ROOT_FOLDER) {
			params.put(ServiceConstants.PARAM_FOLDER_ID, "all");
		} else {
			params.put(ServiceConstants.PARAM_FOLDER_ID, String.valueOf(folder.getFolderId()));
		}

		params.put(ServiceConstants.PARAM_PAGE, String.valueOf(page));
		params.put(ServiceConstants.PARAM_COUNT, String.valueOf(count));

		if (!TextUtils.isEmpty(query)) {
			params.put(ServiceConstants.PARAM_KEYWORD, query);
		}

		// The owner Id needs to be added to the URL for accessing shared data
		NPWebServiceUtil.addOwnerParam(params, folder.getAccessInfo());

		String entryListUrl = NPWebServiceUtil.fullUrlWithAuthenticationTokens(entryListBaseUri(type), mContext);
		entryListUrl = NPWebServiceUtil.appendParams(entryListUrl, params);

		Logs.i(TAG, "Request URL: " + entryListUrl);

		JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, entryListUrl, null, new Response.Listener<JSONObject>() {

			@Override
			public void onResponse(JSONObject response) {
				handleServiceResponse(response);
			}

		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				handleVolleyError(error);
			}
		}
		);

		NPWebServiceUtil.getRequestQueue(mContext).add(request);
	}

	/**
	 * Get entries between dates.
	 * <p/>
	 * Where to use this:
	 * 1. Getting events in Calendar module
	 * 2. Getting journals in Journal module
	 *
	 * @throws NPException
	 */
	public void getEntriesBetweenDates(NPFolder folder, EntryTemplate type, String startYmd, String endYmd, int page) throws NPException {
		currentFolder = folder;

		Map<String, String> params = new HashMap<String, String>();
		params.put(ServiceConstants.PARAM_FOLDER_ID, String.valueOf(folder.getFolderId()));
		params.put(ServiceConstants.PARAM_TYPE, String.valueOf(type.getIntValue()));
		params.put(ServiceConstants.PARAM_START_DATE, startYmd);
		params.put(ServiceConstants.PARAM_END_DATE, endYmd);
		params.put(ServiceConstants.PARAM_PAGE, String.valueOf(page));

		NPWebServiceUtil.addOwnerParam(params, folder.getAccessInfo());

		String entryListUrl = NPWebServiceUtil.fullUrlWithAuthenticationTokens(entryListBaseUri(type), mContext);
		entryListUrl = NPWebServiceUtil.appendParams(entryListUrl, params);

		Log.i(TAG, entryListUrl);

		JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, entryListUrl, null, new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject response) {
				handleServiceResponse(response);
			}

		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				handleVolleyError(error);
			}
		}
		);

		NPWebServiceUtil.getRequestQueue(mContext).add(request);
	}

	public NPFolder getCurrentFolder() {
		return currentFolder;
	}

	/**
	 * Handles the JSON service response.
	 *
	 * @param jsonObj
	 */
	private void handleServiceResponse(JSONObject jsonObj) {
		ServiceResult result = new ServiceResult(jsonObj);

		if (result.isSuccessful()) {
			try {
				final EntryList theList = new EntryList();
				theList.setFolder(currentFolder);
				theList.setAccessInfo(currentFolder.getAccessInfo());
				theList.initWithJSONObject(result.getData());

				//EntryStore.updateEntryList(theList);			// For junit testing
				//EntryStore.updateEntryListAsync(theList);

				//FolderStore.updateFolders(theList.getFolder().getSubFolders());

				sendEntryListBroadcast(theList);

			} catch (Exception e) {
				Logs.e(TAG, e);
				ServiceError error = new ServiceError(ErrorCode.INTERNAL_ERROR, e.getMessage());
				sendErrorBroadcast(error);
			}

		} else {
			// TODO - application error based on returned code
			ErrorCode errorCode = ErrorCode.fromInt(result.getCode());
			ServiceError error = new ServiceError(errorCode, result.getMessage());
			sendErrorBroadcast(error);
		}
	}

	/**
	 * Volley error most likely are network related.
	 *
	 * @param error
	 */
	private void handleVolleyError(VolleyError error) {
		Logs.e(TAG, error);

		final ServiceError serviceError;
		if (error instanceof AuthFailureError) {
			// Let the frontend know the user should be logged out.
			serviceError = new ServiceError(ErrorCode.NOT_AUTHENTICATED, "The user should be logged out.");

		} else if (error instanceof NetworkError) {
			serviceError = new ServiceError(ErrorCode.CONNECTION_TIMEOUT, error.toString());
		} else {
			serviceError = new ServiceError(ErrorCode.UNKNOWN_ERROR, error.toString());
		}
		sendErrorBroadcast(serviceError);

		// Switch to data store to deliver data
	}


	private void sendEntryListBroadcast(EntryList list) {
		final String action = Strings.isNullOrEmpty(list.getKeyword()) ? ACTION_GET_FOLDER_LISTING : ACTION_SEARCH;
		final Intent intent = new Intent(action);
		final String randomKey = String.valueOf(System.currentTimeMillis() + new Random().nextLong());
		mEntryListMap.put(randomKey, list);
		intent.putExtra(KEY_ENTRY_LIST_STRING_KEY, randomKey);
		intent.putExtra(KEY_ENTRY_TEMPLATE, list.getEntryTemplate());
		mContext.sendBroadcast(intent, Manifest.permission.RECEIVE_ENTRY_LIST);
	}

	private void sendErrorBroadcast(ServiceError error) {
		final Intent intent = new Intent(ACTION_ERROR);
		intent.putExtra(KEY_ERROR, error);
		mContext.sendBroadcast(intent, Manifest.permission.RECEIVE_ENTRY_LIST);
	}

	/**
	 * Get the base list URL.
	 *
	 * @param entryTemplate
	 * @return
	 */
	private String entryListBaseUri(EntryTemplate entryTemplate) {
		switch (entryTemplate) {
			case CONTACT:
				return "/contacts";

			case EVENT:
				return "/events";

			case TASK:
				return "/tasks";

			case JOURNAL:
				return "/journals";

			case DOC:
			case NOTE:
				return "/docs";

			case PHOTO:
				return "/photos";

			case ALBUM:
				return "/albums";

			case BOOKMARK:
				return "/bookmarks";

			default:
				break;
		}

		return "";
	}
}
