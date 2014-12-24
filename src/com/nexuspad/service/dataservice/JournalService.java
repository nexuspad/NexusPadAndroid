/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.dataservice;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.nexuspad.Manifest;
import com.nexuspad.R;
import com.nexuspad.common.Constants;
import com.nexuspad.service.account.AccountManager;
import com.nexuspad.service.datamodel.*;
import com.nexuspad.service.datastore.EntryStore;
import com.nexuspad.service.util.DateUtil;
import com.nexuspad.service.util.Logs;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles the Journal requests.
 *
 * @author ren
 */
public class JournalService extends EntryService {

	public static JournalService getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new JournalService(context);
		}
		return (JournalService)mInstance;
	}

	private JournalService(Context context) {
		super(context);
	}

	/**
     * Get the journal detail for a particular date.
     *
     * @param forYmd
     * @throws NPException
     */
    public void getJournal(String forYmd) throws NPException {

        String entryUrl = "/journal/" + forYmd;

        entryUrl = NPWebServiceUtil.fullUrlWithAuthenticationTokens(entryUrl, mContext);

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

	public void saveJournal(Context c, NPJournal journal) {
		try {
			Log.i("JournalService - [UPDATE]", journal.getJournalYmd());

			Logs.i(TAG, journal.getNote());

			/*
			 * local store
			 */
			journal.setSynced(false);
			journal.setLastModifiedTime(DateUtil.now());
			EntryStore.update(journal);

			Map<String, String> params = new HashMap<String, String>();
			NPWebServiceUtil.addOwnerParam(params, journal.getAccessInfo());

			String entryUrl = "/journal/" + journal.getJournalYmd();
			String journalUpdateUrl = NPWebServiceUtil.fullUrlWithAuthenticationTokens(entryUrl, mContext);

			journalUpdateUrl = NPWebServiceUtil.appendParams(journalUpdateUrl, params);

			EntryUpdateRequest request = new EntryUpdateRequest(journal, Request.Method.POST, null, journalUpdateUrl, new Response.Listener<String>() {
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

		} catch (NPException e) {
			Logs.e(TAG, e);
			final String msg = c.getString(R.string.formatted_err_update_failed, journal.getJournalYmd());
			Toast.makeText(c, msg, Toast.LENGTH_LONG).show();
		}
	}


	@Override
	protected void handleRetrievalResponse(JSONObject jsonObj) {
		ServiceResult result = new ServiceResult(jsonObj);

		if (result.isSuccessful()) {
			try {
				JSONObject messagePart = new JSONObject(jsonObj.getString(ServiceConstants.NP_RESPONSE_DATA));
				JSONObject entryPart = new JSONObject(messagePart.getString(ServiceConstants.ENTRY_PART));

				NPEntry entry = EntryFactory.jsonToEntry(entryPart);

				NPJournal j = NPJournal.fromEntry(entry);

				NPUser owner = AccountManager.currentAccount();
				j.setAccessInfo(new AccessEntitlement(owner, owner));

				Logs.d(TAG, j.toString());

				Intent intent = new Intent(ACTION_GET);
				intent.putExtra(Constants.KEY_ENTRY, j);
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
}
