/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.dataservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.google.common.base.Joiner;
import com.nexuspad.common.Constants;
import com.nexuspad.service.account.AccountManager;
import com.nexuspad.Manifest;
import com.nexuspad.R;
import com.nexuspad.service.datamodel.EntryFactory;
import com.nexuspad.service.datamodel.EntryTemplate;
import com.nexuspad.service.datamodel.NPEntry;
import com.nexuspad.service.datamodel.NPUpload;
import com.nexuspad.service.datastore.EntryStore;
import com.nexuspad.service.util.DateUtil;
import com.nexuspad.service.util.Logs;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Handles individual entry GET/UPDATE
 *
 * @author ren
 */
public class EntryService {

	public static final String TAG = "EntryService";

	public static final String ACTION_DELETE = "action_entry_delete";
	public static final String ACTION_NEW = "action_entry_new";
	public static final String ACTION_UPDATE = "action_entry_update";
	public static final String ACTION_GET = "action_entry_get";
	public static final String ACTION_ERROR = "action_entry_error";

	public static final String KEY_ERROR = "key_error";

	protected static EntryService mInstance;

	public static EntryService getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new EntryService(context);
		}
		return mInstance;
	}

	/**
	 * A {@link android.content.BroadcastReceiver} specified for listening to entry changes.
	 *
	 * @author Edmond
	 * @see EntryReceiver#getIntentFilter()
	 */
	public static abstract class EntryReceiver extends BroadcastReceiver {

		/**
		 * @return a new {@link android.content.IntentFilter} that works with
		 * {@link EntryReceiver}
		 * @see android.content.Context#registerReceiver(android.content.BroadcastReceiver, android.content.IntentFilter,
		 * String, android.os.Handler)
		 */
		public static IntentFilter getIntentFilter() {
			IntentFilter filter = new IntentFilter();
			filter.addAction(ACTION_DELETE);
			filter.addAction(ACTION_NEW);
			filter.addAction(ACTION_UPDATE);
			filter.addAction(ACTION_GET);
			filter.addAction(ACTION_ERROR);
			return filter;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			final NPEntry entry = intent.getParcelableExtra(Constants.KEY_ENTRY);

			if (ACTION_DELETE.equals(action)) {
				onDelete(context, intent, entry);

			} else if (ACTION_NEW.equals(action)) {
				onNew(context, intent, entry);

			} else if (ACTION_GET.equals(action)) {
				onGet(context, intent, entry);

			} else if (ACTION_UPDATE.equals(action)) {
				onUpdate(context, intent, entry);

			} else if (ACTION_ERROR.equals(action)) {
				onError(context, intent, intent.<ServiceError>getParcelableExtra(KEY_ERROR));

			} else {
				Logs.w(TAG, "unhandled action: " + action);
			}
		}

		/**
		 * Called when a new {@link com.nexuspad.service.datamodel.NPEntry} is created successfully on the
		 * server.
		 *
		 * @param entry the new {@code NPEntry}
		 */
		protected void onNew(Context context, Intent intent, NPEntry entry) {
		}

		/**
		 * Called when the {@code NPEntry} is deleted from the server.
		 *
		 * @param entry the deleted entry
		 * @see com.nexuspad.service.dataservice.EntryService#deleteEntry(com.nexuspad.service.datamodel.NPEntry)
		 */
		protected void onDelete(Context context, Intent intent, NPEntry entry) {
		}

		/**
		 * Called when a detail entry is retrieved.
		 *
		 * @param entry the detail entry
		 * @see com.nexuspad.service.dataservice.EntryService#getEntry(com.nexuspad.service.datamodel.NPEntry)
		 */
		protected void onGet(Context context, Intent intent, NPEntry entry) {
		}

		/**
		 * Called when a detail entry is updated.
		 *
		 * @param entry the updated entry
		 * @see com.nexuspad.service.dataservice.EntryService#updateEntry(com.nexuspad.service.datamodel.NPEntry)
		 */
		protected void onUpdate(Context context, Intent intent, NPEntry entry) {
		}

		protected void onError(Context context, Intent intent, ServiceError error) {
		}
	}

	protected final Context mContext;

	/*pkg*/ EntryService(Context context) {
		mContext = context.getApplicationContext();
	}

	/**
	 * This makes asynchronous call to retrieve an entry. The result is received
	 * in onSuccess function.
	 *
	 * @param entry
	 */
	public void getEntry(NPEntry entry) throws NPException {
		Map<String, String> params = new HashMap<String, String>();

		// The owner Id needs to be added to the URL for accessing shared data
		NPWebServiceUtil.addOwnerParam(params, entry.getAccessInfo());

		String entryUrl = NPWebServiceUtil.fullUrlWithAuthenticationTokens(entryBaseUri(entry.getTemplate()) + "/" + entry.getEntryId(), mContext);
		entryUrl = NPWebServiceUtil.appendParams(entryUrl, params);

		JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, entryUrl, null, new Response.Listener<JSONObject>() {
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

	/**
	 * Add an entry asynchronously.
	 *
	 * @param entry
	 * @throws NPException
	 */
	public void addEntry(NPEntry entry) throws NPException {
		final Date now = DateUtil.now();
		/*
		 * Update local store
		 */
		entry.setSynced(false);
		if (entry.getCreateTime() == null) {
			entry.setCreateTime(now);
		}
		entry.setLastModifiedTime(now);
		entry.setSyncId(EntryStore.generateSyncId());
		EntryStore.update(entry);

		/*
		 * Update the web service.
		 */
		Map<String, String> params = new HashMap<String, String>();
		params.put(ServiceConstants.OWNER_ID, String.valueOf(entry.getAccessInfo().getOwner().getUserId()));
		params.put(ServiceConstants.FOLDER_ID, String.valueOf(entry.getFolder().getFolderId()));

		NPWebServiceUtil.addOwnerParam(params, entry.getAccessInfo());

		String entryUrl = NPWebServiceUtil.fullUrlWithAuthenticationTokens(entryBaseUri(entry.getTemplate()), mContext);
		entryUrl = NPWebServiceUtil.appendParams(entryUrl, params);

		EntryUpdateRequest request = new EntryUpdateRequest(entry, Request.Method.POST, null, entryUrl, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					handleActionResponse(new JSONObject(response));
				} catch (JSONException e) {
					Logs.e(TAG, "Error parsing adding entry action response", e);
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
	 * Update an existing entry.
	 *
	 * @param entry
	 * @throws NPException
	 */
	public void updateEntry(NPEntry entry) throws NPException {
		Log.i("EntryService - [UPDATE]", entry.getEntryId());

		/*
		 * Update local store
		 */
		entry.setSynced(false);
		entry.setLastModifiedTime(DateUtil.now());
		EntryStore.update(entry);

		/*
		 * Update the web service.
		 */
		Map<String, String> params = new HashMap<String, String>();
		NPWebServiceUtil.addOwnerParam(params, entry.getAccessInfo());

		String entryUrl = NPWebServiceUtil.fullUrlWithAuthenticationTokens(entryBaseUri(entry.getTemplate()) + "/" + entry.getEntryId(), mContext);
		entryUrl = NPWebServiceUtil.appendParams(entryUrl, params);

		EntryUpdateRequest request = new EntryUpdateRequest(entry, Request.Method.POST, null, entryUrl, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					handleActionResponse(new JSONObject(response));
				} catch (JSONException e) {
					Logs.e(TAG, "Error parsing update entry action response: " + e);
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
	 * Handles actions like email/share/move, etc.
	 *
	 * @param entry
	 * @param actionDetail
	 * @throws NPException
	 */
	private void updateEntry(NPEntry entry, Map<String, String> actionDetail) throws NPException {
		Log.d("EntryService - [UPDATE]", entry.toMap().toString());

		Map<String, String> params = new HashMap<String, String>();
		NPWebServiceUtil.addOwnerParam(params, entry.getAccessInfo());

		String entryUrl = NPWebServiceUtil.fullUrlWithAuthenticationTokens(entryBaseUri(entry.getTemplate()) + "/" + entry.getEntryId(), mContext);
		entryUrl = NPWebServiceUtil.appendParams(entryUrl, params);

		EntryUpdateRequest request = new EntryUpdateRequest(entry, Request.Method.POST, actionDetail, entryUrl, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					handleActionResponse(new JSONObject(response));
				} catch (JSONException e) {
					Logs.e(TAG, "Error parsing update entry action response: " + e);
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
	 * Delete an entry.
	 *
	 * @param entry
	 * @throws NPException
	 */
	public void deleteEntry(NPEntry entry) throws NPException {
		Log.i("EntryService - [DELETE]", entry.getEntryId());

		/*
		 * Update local store
		 */
		entry.setSynced(false);
		entry.setStatus(ServiceConstants.ITEM_DELETED);
		entry.setLastModifiedTime(DateUtil.now());
		EntryStore.update(entry);

		/*
		 * Update the web service.
		 */
		Map<String, String> params = new HashMap<String, String>();
		NPWebServiceUtil.addOwnerParam(params, entry.getAccessInfo());

		String entryUrl = NPWebServiceUtil.fullUrlWithAuthenticationTokens(entryBaseUri(entry.getTemplate()) + "/" + entry.getEntryId(), mContext);
		entryUrl = NPWebServiceUtil.appendParams(entryUrl, params);

		JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, entryUrl, null, new Response.Listener<JSONObject>() {

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
	 * Fill in owner info. Deletes an entry. Handles NPException with Toast.
	 */
	public void safeDeleteEntry(Context c, NPEntry entry) {
		try {
			entry.setOwner(AccountManager.currentAccount());
			deleteEntry(entry);
		} catch (NPException e) {
			Logs.e(TAG, e);
			final String msg = c.getString(R.string.formatted_err_delete_failed, entry.getTitle());
			Toast.makeText(c, msg, Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Fill in owner info. Updates an entry if an entry ID exists, adds otherwise. Handles NPException with Toast.
	 */
	public void safePutEntry(Context c, NPEntry entry) {
		try {
			entry.setOwner(AccountManager.currentAccount());
			if (isNullOrEmpty(entry.getEntryId())) {
				addEntry(entry);
			} else {
				updateEntry(entry);
			}
		} catch (NPException e) {
			Logs.e(TAG, e);
			final String msg = c.getString(R.string.formatted_err_update_failed, entry.getTitle());
			Toast.makeText(c, msg, Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Delete an attachment from album or doc.
	 *
	 * @param attachment
	 * @throws NPException
	 */
	public void deleteAttachment(NPUpload attachment) throws NPException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("parent_entry_id", String.valueOf(attachment.getParentEntryId()));
		params.put("parent_entry_module", String.valueOf(attachment.getParentEntryModule()));
		params.put("parent_entry_folder", String.valueOf(attachment.getParentEntryFolder()));

		NPWebServiceUtil.addOwnerParam(params, attachment.getAccessInfo());

		String entryUrl = NPWebServiceUtil.fullUrlWithAuthenticationTokens(entryBaseUri(EntryTemplate.UPLOAD), mContext);
		entryUrl = NPWebServiceUtil.appendParams(entryUrl, params);

		JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, entryUrl, null, new Response.Listener<JSONObject>() {

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
	 * Email an entry
	 *
	 * @param entry
	 */
	public void emailEntry(NPEntry entry, List<String> emailTos, String subject, String message) {
		Map<String, String> actionDetail = new HashMap<String, String>();
		actionDetail.put("action", "email_entry");

		Joiner joiner = Joiner.on("; ");
		actionDetail.put("to_email", joiner.join(emailTos));

		if (subject != null)
			actionDetail.put("email_subject", subject);

		if (message != null) {
			actionDetail.put("email_message", message);
		}

		try {
			updateEntry(entry, actionDetail);
		} catch (NPException e) {
		}
	}


	/**
	 * Handles GET request result.
	 */
	protected void handleRetrievalResponse(JSONObject jsonObj) {
		ServiceResult result = new ServiceResult(jsonObj);

		if (result.isSuccessful()) {
			try {
				JSONObject messagePart = new JSONObject(jsonObj.getString(ServiceConstants.NP_RESPONSE_DATA));
				JSONObject entryPart = new JSONObject(messagePart.getString(ServiceConstants.ENTRY_PART));

				NPEntry entry = EntryFactory.jsonToEntry(entryPart);

				Logs.d(TAG, entry.toString());

				Intent intent = new Intent(ACTION_GET);
				intent.putExtra(Constants.KEY_ENTRY, entry);
				mContext.sendBroadcast(intent, Manifest.permission.LISTEN_ENTRY_CHANGES);

			} catch (JSONException e) {
				Logs.e(TAG, "Error parsing EntryService response", e);

			} catch (Exception e) {
				Logs.e(TAG, e);
			}

		} else {
			ErrorCode errorCode = ErrorCode.fromInt(result.getCode());
			ServiceError error = new ServiceError(errorCode, result.getMessage());
			sendErrorBroadcast(error);
		}
	}

	/**
	 * Handles POST/PUT/DELETE requests result.
	 *
	 * @param jsonObj
	 */
	protected void handleActionResponse(JSONObject jsonObj) {
		ServiceResult result = new ServiceResult(jsonObj);

		if (result.isSuccessful()) {
			if (result.isEntryActionResult()) {
				EntryActionResult actionResult = new EntryActionResult(result.getData());

				Logs.d(TAG, actionResult.getUpdatedEntry().toString());

				final String actionName = actionResult.getActionName();
				Intent intent = new Intent();
				intent.putExtra(Constants.KEY_ENTRY, actionResult.getUpdatedEntry());

				if (ServiceConstants.ACTION_ENTRY_DELETE.equals(actionName)) {
					intent.setAction(ACTION_DELETE);

					// Delete entry from the local data store
					EntryStore.delete(actionResult.getUpdatedEntry());

				} else if (ServiceConstants.ACTION_ENTRY_UPDATE.equals(actionName)) {
					intent.setAction(ACTION_UPDATE);

					// Update the entry record in data store
					NPEntry updatedEntry = actionResult.getUpdatedEntry();
					updatedEntry.setSynced(true);
					EntryStore.update(updatedEntry);

				} else if (ServiceConstants.ACTION_ENTRY_ADD.equals(actionName)) {
					intent.setAction(ACTION_NEW);

					// Update the entry record in data store
					NPEntry updatedEntry = actionResult.getUpdatedEntry();
					updatedEntry.setSynced(true);
					EntryStore.update(updatedEntry);

				} else {
					Logs.w(TAG, "unhandled action: " + actionResult);
				}

				mContext.sendBroadcast(intent, Manifest.permission.LISTEN_ENTRY_CHANGES);

			} else {
				// Really shouldn't happen.
			}

		} else {
			// TODO - handles service error
		}
	}

	protected void sendErrorBroadcast(ServiceError error) {
		final Intent intent = new Intent(ACTION_ERROR);
		intent.putExtra(KEY_ERROR, error);
		mContext.sendBroadcast(intent, Manifest.permission.LISTEN_ENTRY_CHANGES);
	}

	/**
	 * Volley error most likely are network related.
	 *
	 * @param error
	 */
	protected void handleVolleyError(VolleyError error) {
		Logs.e(TAG, error);
		if (error instanceof AuthFailureError) {
			// Let the frontend know the user should be logged out.
			sendErrorBroadcast(new ServiceError(ErrorCode.NOT_AUTHENTICATED, "The user should be logged out."));
		} else {
			sendErrorBroadcast(new ServiceError(ErrorCode.UNKNOWN_ERROR, "it's not AuthFailureError, that's all I know"));
		}

		// Switch to data store to deliver data
	}


	/**
	 * This makes a synchronous call to retrieve an entry.
	 * This is created for testing purpose and should not be used in UI code.
	 *
	 * @param entry
	 * @return
	 * @throws NPException
	 */
	public NPEntry getEntrySynchronously(NPEntry entry) throws NPException {
		String entryUrl = NPWebServiceUtil.fullUrlWithAuthenticationTokens(entryBaseUri(entry.getTemplate()) + "/" + entry.getEntryId(), mContext);

		RequestFuture<JSONObject> future = RequestFuture.newFuture();
		JsonObjectRequest request = new JsonObjectRequest(entryUrl, null, future, future);
		NPWebServiceUtil.getRequestQueue(mContext).add(request);

		try {
			JSONObject response = future.get(); // this will block
			JSONObject dataPart = response.getJSONObject(ServiceConstants.NP_RESPONSE_DATA);

			return EntryFactory.jsonToEntry(dataPart.getJSONObject(ServiceConstants.ENTRY_PART));

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static String entryBaseUri(EntryTemplate entryTemplate) {
		switch (entryTemplate) {
			case CONTACT:
				return "/contact";

			case EVENT:
				return "/event";

			case TASK:
				return "/task";

			case JOURNAL:
				return "/journal";

			case DOC:
			case NOTE:
				return "/doc";

			case PHOTO:
				return "/photo";

			case ALBUM:
				return "/album";

			case BOOKMARK:
				return "/bookmark";

			case UPLOAD:
				return "/upload";

			default:
				break;
		}

		return "";
	}


	/**
	 * Handles Entry update/delete request.
	 *
	 * @author ren
	 */
	public static class EntryUpdateRequest extends StringRequest {

		private NPEntry entry;
		private Map<String, String> params;

		/**
		 *
		 * @param params the params must not have nulls in the values
		 */
		public EntryUpdateRequest(NPEntry e, int method, Map<String, String> params, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
			super(method, url, listener, errorListener);
			entry = e;
			this.params = params;
		}

		@Override
		protected Map<String, String> getParams() throws AuthFailureError {
			Map<String, String> postParams = new HashMap<String, String>();

			if (params != null) {
				postParams.putAll(params);
			}

			postParams.putAll(entry.toMap());

			Log.i("POST PARAMS.....", postParams.toString());

			return postParams;
		}
	}
}
