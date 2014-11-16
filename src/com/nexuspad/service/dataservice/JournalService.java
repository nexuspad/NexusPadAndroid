/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.dataservice;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONObject;

/**
 * Handles the Journal requests.
 *
 * @author ren
 */
public class JournalService extends EntryService {

    public JournalService(Context context) {
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
}
