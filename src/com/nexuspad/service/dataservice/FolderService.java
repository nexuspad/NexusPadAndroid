/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.dataservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.util.SparseArray;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.nexuspad.Manifest;
import com.nexuspad.service.datamodel.NPFolder;
import com.nexuspad.service.datamodel.NPModule;
import com.nexuspad.service.datastore.FolderStore;
import com.nexuspad.service.datastore.db.DatabaseManager;
import com.nexuspad.service.dataservice.caching.FolderCacheUtil;
import com.nexuspad.service.util.DateUtil;
import com.nexuspad.service.util.Logs;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles folder retrieval, update and other actions in all modules.
 *
 * @author ren
 */
public class FolderService {
	public static final String TAG = "FolderService";

	public static final String KEY_FOLDER = "key_folder";
	public static final String KEY_FOLDERS = "key_folders";
	public static final String KEY_ERROR = "extra_error";

	public static final String ACTION_NEW = "action_folder_new";
	public static final String ACTION_DELETE = "action_folder_del";
	public static final String ACTION_UPDATE = "action_folder_update";
	public static final String ACTION_GET_ALL = "action_folder_get_all";
	public static final String ACTION_ERROR = "action_folder_get_all";

	private static FolderService mInstance;

	private SparseArray<NPFolder> mAllFolders;
	private NPFolder mParentFolder;

	private final Context mContext;


	public static FolderService getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new FolderService(context);
		}

		mInstance.mAllFolders = new SparseArray<NPFolder>();

		return mInstance;
	}

	/**
	 * Initialization.
	 */
	private FolderService(Context context) {
		mContext = context.getApplicationContext();
		DatabaseManager.getDb(context);
	}


	/**
	 * A {@link android.content.BroadcastReceiver} specified for listening to {@link com.nexuspad.service.datamodel.NPFolder}
	 * changes.
	 *
	 * @author Edmond
	 * @see FolderReceiver#getIntentFilter()
	 */
	public static abstract class FolderReceiver extends BroadcastReceiver {


		/**
		 * @return an {@link android.content.IntentFilter} that works with
		 * {@link FolderReceiver}
		 * @see android.content.Context#registerReceiver(android.content.BroadcastReceiver, android.content.IntentFilter,
		 * String, android.os.Handler)
		 */
		public static IntentFilter getIntentFilter() {
			IntentFilter filter = new IntentFilter();
			filter.addAction(ACTION_NEW);
			filter.addAction(ACTION_DELETE);
			filter.addAction(ACTION_UPDATE);
			filter.addAction(ACTION_GET_ALL);
			filter.addAction(ACTION_ERROR);
			return filter;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			final NPFolder folder = intent.getParcelableExtra(KEY_FOLDER);

			if (ACTION_NEW.equals(action)) {
				onNew(context, intent, folder);

			} else if (ACTION_GET_ALL.equals(action)) {
				onGotAll(context, intent, intent.<NPFolder>getParcelableArrayListExtra(KEY_FOLDERS));

			} else if (ACTION_DELETE.equals(action)) {
				onDelete(context, intent, folder);

			} else if (ACTION_UPDATE.equals(action)) {
				onUpdate(context, intent, folder);

			} else if (ACTION_ERROR.equals(action)) {
				onError(context, intent, intent.<ServiceError>getParcelableExtra(KEY_ERROR));

			} else {
				Logs.w(TAG, "unhandled action: " + action);
			}
		}

		/**
		 * Called when a new {@link com.nexuspad.service.datamodel.NPFolder} is created successfully on the
		 * server.
		 *
		 * @param f the new {@code Folder}
		 */
		protected void onNew(Context c, Intent i, NPFolder f) {
		}

		/**
		 * Called when the requested sub-folders are loaded
		 *
		 * @see com.nexuspad.service.dataservice.FolderService#getSubFolders(com.nexuspad.service.datamodel.NPFolder)
		 */
		protected void onGotAll(Context c, Intent i, List<NPFolder> folders) {
		}

		/**
		 * Called when the folder is deleted from the server
		 *
		 * @see com.nexuspad.service.dataservice.FolderService#deleteFolder(com.nexuspad.service.datamodel.NPFolder)
		 */
		protected void onDelete(Context c, Intent i, NPFolder folder) {
		}

		/**
		 * Called when a {@code Folder} is updated.
		 *
		 * @param folder the updated folder
		 * @see com.nexuspad.service.dataservice.FolderService#updateFolder(com.nexuspad.service.datamodel.NPFolder)
		 */
		protected void onUpdate(Context c, Intent i, NPFolder folder) {
		}

		/**
		 * Called when an error has occurred.
		 *
		 * @param error the service error
		 */
		protected void onError(Context context, Intent intent, ServiceError error) {
		}
	}


	/**
	 * Get all folders. This is called when the folder view is initially opened.
	 *
	 * @throws NPException
	 */
	public void getSubFolders(NPFolder parentFolder) throws NPException {

		this.mParentFolder = parentFolder; // This must be set because it's used

		if (mAllFolders != null && mAllFolders.size() > 0) {
			Log.d(TAG, "Use the existing folders...");
			Intent intent = new Intent(ACTION_GET_ALL);
			if (parentFolder.getFolderId() == ServiceConstants.ROOT_FOLDER) {
				intent.putParcelableArrayListExtra(KEY_FOLDERS,
						filterByParent(mAllFolders, parentFolder.getFolderId()));

			} else {
				Map<String, NPFolder> folders = findSubFolders(mAllFolders, parentFolder);
				intent.putParcelableArrayListExtra(KEY_FOLDERS,
						filterByParent(mAllFolders, parentFolder.getFolderId()));
			}
			mContext.sendBroadcast(intent, Manifest.permission.LISTEN_FOLDER_CHANGES);

		} else {
			String folderUrl = folderListUri(parentFolder);

			Map<String, String> params = new HashMap<String, String>();

			// The owner Id needs to be added to the URL for accessing shared data
			NPWebServiceUtil.addOwnerParam(params, parentFolder.getAccessInfo());

			folderUrl = NPWebServiceUtil.fullUrlWithAuthenticationTokens(folderUrl, mContext);
			folderUrl = NPWebServiceUtil.appendParams(folderUrl, params);

			JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, folderUrl, null, new Response.Listener<JSONObject>() {
				@Override
				public void onResponse(JSONObject response) {
					handleRetrievalResponse(response);
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

	}

	/**
	 * Returns a folder object using the stored information in FolderService.
	 * This method could return null.
	 *
	 * @param moduleId
	 * @param folderId
	 * @return
	 */
	public NPFolder getFolder(int moduleId, int folderId) {
		if (mParentFolder.getModuleId() != moduleId) {
			return null;
		}
		if (mAllFolders != null && mAllFolders.get(folderId) != null) {
			return mAllFolders.get(folderId);
		}
		return null;
	}

	/**
	 * Update the folder.
	 *
	 * @param folder
	 */
	public void updateFolder(NPFolder folder) {
		mAllFolders = null;

        /*
         * Update local data store
    	 */
		folder.setSynced(false);
		folder.setLastModified(DateUtil.now());
		FolderStore.update(folder);

		// Add a sync Id for new folder
		if (folder.getFolderId() <= 0) {
			folder.setSyncId(FolderStore.generateSyncId());
		}

    	/*
    	 * Update web service
    	 */
		Map<String, String> params = new HashMap<String, String>();
		params.put(ServiceConstants.OWNER_ID, String.valueOf(folder.getAccessInfo().getOwner().getUserId()));

		NPWebServiceUtil.addOwnerParam(params, folder.getAccessInfo());

		String folderUrl = folderUri(folder);
		try {
			folderUrl = NPWebServiceUtil.fullUrlWithAuthenticationTokens(folderUrl, mContext);
			folderUrl = NPWebServiceUtil.appendParams(folderUrl, params);

		} catch (NPException e) {
			sendErrorBroadcast(e.getServiceError());
		}

		FolderUpdateRequest request = new FolderUpdateRequest(folder, Request.Method.POST, null, folderUrl, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					handleActionResponse(new JSONObject(response));
				} catch (JSONException e) {
					Log.e("FolderService", "Error parsing updating folder action response: " + e);
					sendErrorBroadcast(new ServiceError(ErrorCode.INTERNAL_ERROR, "Error parsing action response."));
				}
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
	 * Delete the folder.
	 *
	 * @param folder
	 */
	public void deleteFolder(NPFolder folder) {
		mAllFolders = null;

    	/*
    	 * Update local data store
    	 */
		folder.setSynced(false);
		folder.setLastModified(DateUtil.now());
		folder.setStatus(ServiceConstants.ITEM_DELETED);
		FolderStore.update(folder);

    	/*
    	 * Update web service
    	 */
		String folderUrl = folderUri(folder);

		Map<String, String> params = new HashMap<String, String>();

		// The owner Id needs to be added to the URL for accessing shared data
		NPWebServiceUtil.addOwnerParam(params, folder.getAccessInfo());

		try {
			folderUrl = NPWebServiceUtil.fullUrlWithAuthenticationTokens(folderUrl, mContext);
		} catch (NPException e) {
			sendErrorBroadcast(e.getServiceError());
		}

		folderUrl = NPWebServiceUtil.appendParams(folderUrl, params);

		JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, folderUrl, null, new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject response) {
				handleActionResponse(response);
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
	 * Share folder to another user.
	 *
	 * @param folder
	 */
	public void shareFolder(NPFolder folder) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(ServiceConstants.OWNER_ID, String.valueOf(folder.getAccessInfo().getOwner().getUserId()));

		NPWebServiceUtil.addOwnerParam(params, folder.getAccessInfo());

		String folderUrl = "";
		try {
			folderUrl = NPWebServiceUtil.fullUrlWithAuthenticationTokens(folderUrl, mContext);
			folderUrl = NPWebServiceUtil.appendParams(folderUrl, params);

		} catch (NPException e) {
			sendErrorBroadcast(e.getServiceError());
		}

		FolderUpdateRequest request = new FolderUpdateRequest(folder, Request.Method.POST, null, folderUrl, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					handleActionResponse(new JSONObject(response));
				} catch (JSONException e) {
					Log.e("FolderService", "Error parsing updating folder action response: " + e);
					sendErrorBroadcast(new ServiceError(ErrorCode.INTERNAL_ERROR, "Error parsing action response."));
				}
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


	public void handleRetrievalResponse(JSONObject jsonObj) {
		ServiceResult result = new ServiceResult(jsonObj);
		if (result.isSuccessful()) {

			try {
				mAllFolders = convertJsonToFolders(jsonObj);

				Logs.d("FolderService", "Retrieved all folders:");
				Logs.d("FolderService", mAllFolders.toString());

				NPFolder rootFolder = NPFolder.rootFolderOf(mParentFolder.getModuleId(), mContext);
				rootFolder.setAccessInfo(mParentFolder.getAccessInfo());

				FolderCacheUtil.cacheFolders(rootFolder, jsonObj);

				Intent intent = new Intent(ACTION_GET_ALL);
				if (mParentFolder.getFolderId() == ServiceConstants.ROOT_FOLDER) {
					intent.putParcelableArrayListExtra(KEY_FOLDERS,
							filterByParent(mAllFolders, mParentFolder.getFolderId()));

				} else {
					Map<String, NPFolder> folders = findSubFolders(mAllFolders, mParentFolder);
					intent.putParcelableArrayListExtra(KEY_FOLDERS,
							filterByParent(mAllFolders, mParentFolder.getFolderId()));
				}
				mContext.sendBroadcast(intent, Manifest.permission.LISTEN_FOLDER_CHANGES);

			} catch (Exception e) {
				throw new RuntimeException("Error parsing FolderService response: ", e);
			}

		} else {
			// TODO - handles service error
		}
	}

	public void handleActionResponse(JSONObject jsonObj) {
		ServiceResult result = new ServiceResult(jsonObj);

		if (result.isSuccessful()) {

			if (result.isFolderActionResult()) {

				FolderActionResult actionResult = new FolderActionResult(result.getData());
				Logs.d("FolderService", actionResult.toString());

				System.out.println(actionResult);
				System.out.println(actionResult.getUpdatedFolder());

				final String actionName = actionResult.getActionName();
				final Intent intent = new Intent();
				intent.putExtra(KEY_FOLDER, actionResult.getUpdatedFolder());

				if (ServiceConstants.ACTION_FOLDER_DELETE.equals(actionName)) {
					Logs.i(TAG, "action: delete");
					intent.setAction(ACTION_DELETE);

					// Delete folder from local data store
					NPFolder f = actionResult.getUpdatedFolder();
					FolderStore.delete(f);

				} else if (ServiceConstants.ACTION_FOLDER_UPDATE.equals(actionName)) {
					Logs.i(TAG, "action: update");
					intent.setAction(ACTION_UPDATE);

					// Update folder in local data store
					NPFolder f = actionResult.getUpdatedFolder();
					f.setSynced(true);
					FolderStore.update(f);

				} else if (ServiceConstants.ACTION_FOLDER_ADD.equals(actionName)) {
					Logs.i(TAG, "action: add");
					intent.setAction(ACTION_NEW);

					// Update folder in local data store
					NPFolder f = actionResult.getUpdatedFolder();
					f.setSynced(true);
					FolderStore.update(f);

				} else {
					Logs.w(TAG, "unhandled action: " + actionResult);
				}

				mContext.sendBroadcast(intent, Manifest.permission.LISTEN_FOLDER_CHANGES);

			}

		} else {
			// TODO - handles service error
		}
	}

	/**
	 * Volley error most likely are network related.
	 *
	 * @param error
	 */
	private void handleVolleyError(VolleyError error) {
		Log.e("FolderService", error.toString());
		if (error instanceof AuthFailureError) {
			// Let the frontend know the user should be logged out.
			sendErrorBroadcast(new ServiceError(ErrorCode.NOT_AUTHENTICATED, "The user should be logged out."));
		}

		// Switch to data store to deliver data
	}


	/**
	 * Convert json object to folder objects.
	 *
	 * @param jsonObj
	 * @return
	 * @throws Exception
	 */
	private static SparseArray<NPFolder> convertJsonToFolders(JSONObject jsonObj) throws JSONException {

		SparseArray<NPFolder> allFolders = new SparseArray<NPFolder>();

		JSONObject dataPart = jsonObj.getJSONObject(ServiceConstants.NP_RESPONSE_DATA);

		JSONArray foldersArray = dataPart.getJSONArray(ServiceConstants.FOLDERS);

		int folderCount = foldersArray.length();

		Map<String, List<String>> parentChildrenMapping = new HashMap<String, List<String>>();

		for (int i = 0; i < folderCount; i++) {

			JSONObject folderJsonObj = foldersArray.getJSONObject(i);

			NPFolder folder = new NPFolder(folderJsonObj);

			allFolders.put(folder.getFolderId(), folder);

			String parentIdStr = String.valueOf(folder.getParentId());
			String folderIdStr = String.valueOf(folder.getFolderId());

			if (!parentChildrenMapping.containsKey(parentIdStr)) {
				List<String> children = new ArrayList<String>();
				parentChildrenMapping.put(parentIdStr, children);
			}

			parentChildrenMapping.get(parentIdStr).add(folderIdStr);
		}

		// User the parent children mapping to populate the subfolders
		for (String parentIdStr : parentChildrenMapping.keySet()) {
			List<String> children = parentChildrenMapping.get(parentIdStr);
			if (!parentIdStr.equals("0")) {
				NPFolder folder = allFolders.get(Integer.parseInt(parentIdStr));

				if (folder != null) {
					for (String childFolderId : children) {
						NPFolder childFolder = allFolders.get(Integer.parseInt(childFolderId));
						folder.addSubFolder(childFolder);
					}
				}
			}
		}

		return allFolders;
	}

	private static ArrayList<NPFolder> filterByParent(SparseArray<NPFolder> all, int parentId) {
		ArrayList<NPFolder> list = new ArrayList<NPFolder>();

		int folderId = 0;
		for(int i = 0; i < all.size(); i++) {
			folderId = all.keyAt(i);
			// get the object by the key.
			NPFolder f = all.get(folderId);
			if (f.getParentId() == parentId) {
				list.add(f);
			}
		}
		return list;
	}

	private static Map<String, NPFolder> findSubFolders(SparseArray<NPFolder> allFolders, NPFolder parentFolder) {
		Map<String, NPFolder> subFolders = new HashMap<String, NPFolder>();

		int folderId = 0;
		for(int i = 0; i < allFolders.size(); i++) {
			folderId = allFolders.keyAt(i);

			if (folderId == parentFolder.getFolderId()) {
				NPFolder f = allFolders.get(folderId);

				if (f.getSubFolders() != null) {
					for (NPFolder aSubFolder : f.getSubFolders()) {
						subFolders.put(String.valueOf(aSubFolder.getFolderId()), aSubFolder);
					}
				}
				break;
			}
		}

		return subFolders;
	}


	private void sendErrorBroadcast(ServiceError error) {
		final Intent intent = new Intent(ACTION_ERROR);
		intent.putExtra(KEY_ERROR, error);
		mContext.sendBroadcast(intent, Manifest.permission.LISTEN_FOLDER_CHANGES);
	}

	public static String folderListUri(NPFolder folder) {
		String url = "";

		switch (folder.getModuleId()) {
			case NPModule.CONTACT:
				url = "/contact/folder";
				break;

			case NPModule.CALENDAR:
				url = "/calendar/calendar";
				break;

			case NPModule.DOC:
				url = "/doc/folder";
				break;

			case NPModule.PHOTO:
				url = "/photo/folder";
				break;

			case NPModule.BOOKMARK:
				url = "/bookmark/folder";
				break;

			default:
				break;
		}

		/**
		 * For now we make the same API call to get all folders event for subfolders.
		 * The problem is convertJsonToFolders does not parse properly on API call with folder_id parameter and
		 * result in an empty subfolder list.
		 *
		 * TODO - this needs to be optimized to avoid getting all folders in every call.
		 * What we can do is to make "mAllFolders" a FolderService class attribute.
		 */
		url = url + "?AllFolders";

		return url;
	}


	public static String folderUri(NPFolder folder) {
		String url = "";

		switch (folder.getModuleId()) {
			case NPModule.CONTACT:
				url = "/contact/folder";
				break;

			case NPModule.CALENDAR:
				url = "/calendar/calendar";
				break;

			case NPModule.DOC:
				url = "/doc/folder";
				break;

			case NPModule.PHOTO:
				url = "/photo/folder";
				break;

			case NPModule.BOOKMARK:
				url = "/bookmark/folder";
				break;

			default:
				break;
		}

		url = url + "?folder_id=" + folder.getFolderId();
		return url;
	}


	/**
	 * Handles Folder update request.
	 *
	 * @author ren
	 */
	public static class FolderUpdateRequest extends StringRequest {

		private NPFolder folder;
		private Map<String, String> params;

		public FolderUpdateRequest(NPFolder f, int method, Map<String, String> params, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
			super(method, url, listener, errorListener);
			folder = f;
			this.params = params;
		}

		@Override
		protected Map<String, String> getParams() throws AuthFailureError {
			Map<String, String> postParams = new HashMap<String, String>();

			if (params != null) {
				postParams.putAll(params);
			}

			postParams.putAll(folder.toMap());

			return postParams;
		}
	}
}
