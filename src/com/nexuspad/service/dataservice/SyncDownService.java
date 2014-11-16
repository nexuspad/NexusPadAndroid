/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.dataservice;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.nexuspad.service.account.AccountManager;
import com.nexuspad.service.datamodel.AccessEntitlement;
import com.nexuspad.service.datamodel.EntryList;
import com.nexuspad.service.datamodel.NPEntry;
import com.nexuspad.service.datamodel.NPFolder;
import com.nexuspad.service.datastore.EntryStore;
import com.nexuspad.service.datastore.FolderStore;
import com.nexuspad.service.util.DateUtil;
import com.nexuspad.service.util.Logs;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.concurrent.ExecutionException;

/**
 * Handles downloading updated items on the server side and update the local data store.
 *
 * @author ren
 */
public class SyncDownService implements Runnable {

	private final String LAST_SYNC_TIME_PREFKEY = "LastSyncTime";

	private final Context mContext;
	private AccessEntitlement accessInfo;

	public SyncDownService(Context context) {
		mContext = context;

		accessInfo = new AccessEntitlement();
		try {
			accessInfo.setOwner(AccountManager.currentAccount());
		} catch (NPException e) {
			Logs.e("SyncDownService", "Cannot get current account.");
		}
	}

	@Override
	public void run() {
		try {
			syncUpdatedItems();
		} catch (NPException e) {
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
		}
	}

	/**
	 * Download the items updated on the server side and update local data store.
	 *
	 * @throws NPException
	 * @throws InterruptedException
	 * @throws java.util.concurrent.ExecutionException
	 */
	private void syncUpdatedItems() throws NPException, InterruptedException, ExecutionException {
		String whatsNewUrl = NPWebServiceUtil.fullUrlWithAuthenticationTokens("/whatsnew", mContext);

		long lastSyncTime = getLastSyncTime();
		if (lastSyncTime != 0) {
			whatsNewUrl = NPWebServiceUtil.appendParam(whatsNewUrl, "last_sync_time", String.valueOf(lastSyncTime));
		}

		RequestFuture<String> future = RequestFuture.newFuture();
		StringRequest request = new StringRequest(Request.Method.GET, whatsNewUrl, future, future);
		NPWebServiceUtil.getRequestQueue(mContext).add(request);

		String responseString = future.get();

		try {
			JSONObject response = new JSONObject(responseString);
			ServiceResult result = new ServiceResult(response);

			if (result.isSuccessful()) {
				JSONObject dataForModules = result.getData();

				@SuppressWarnings("unchecked")
				Iterator<String> it = dataForModules.keys();

				while (it.hasNext()) {
					String moduleKey = it.next();

					Logs.i("SyncDownService", "---- Process data for module: " + moduleKey + "----");
					JSONObject moduleData = dataForModules.getJSONObject(moduleKey);

					EntryList theList = new EntryList();
					theList.setFolder(new NPFolder(NPFolder.ROOT_FOLDER));
					theList.initWithJSONObject(moduleData);

					Log.d("EntryListService", theList.toString());

					for (Object o : theList.getEntries()) {
						NPEntry entry = (NPEntry)o;

						entry.setAccessInfo(accessInfo);

						if (entry.getStatus() == ServiceConstants.ITEM_DELETED) {
							EntryStore.delete(entry);
						} else {
							entry.setSynced(true);
							EntryStore.update(entry);
						}
					}

					for (NPFolder folder : theList.getFolder().getSubFolders()) {
						folder.setAccessInfo(accessInfo);

						if (folder.getStatus() == ServiceConstants.ITEM_DELETED) {
							FolderStore.delete(folder);
						} else {
							folder.setSynced(true);
							FolderStore.update(folder);
						}
					}
				}

				//setLastSyncTime();
			}

		} catch (JSONException e) {
			Logs.e("SyncDownService", "Error parsing updated entries. ", e);
		}

	}

	/**
	 * Get the last sync time from preference.
	 *
	 * @return
	 */
	private long getLastSyncTime() {
		SharedPreferences sharedPrefs = mContext.getSharedPreferences("NexusPad", Context.MODE_PRIVATE);
		return sharedPrefs.getLong(LAST_SYNC_TIME_PREFKEY, 0);
	}

	/**
	 * Set the last sync time in preference.
	 */
	private void setLastSyncTime() {
		Logs.i("SyncDownService", "Update the last sync time to now.");

		SharedPreferences sharedPrefs = mContext.getSharedPreferences("NexusPad", Context.MODE_PRIVATE);
		Editor editor = sharedPrefs.edit();
		editor.putLong(LAST_SYNC_TIME_PREFKEY, DateUtil.now().getTime() - 60000);    // Now minus 1 minute
		editor.commit();
	}
}
