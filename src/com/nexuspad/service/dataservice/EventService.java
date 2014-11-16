package com.nexuspad.service.dataservice;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.nexuspad.service.datamodel.NPEvent;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Handle event services.
 *
 * @author ren
 */
public class EventService extends EntryService {

    public EventService(Context context) {
        super(context);
    }

    /**
     * Get the event detail with the recur id.
     *
     * @param event
     */
    public void getEvent(NPEvent event) throws NPException {

        Map<String, String> params = new HashMap<String, String>();
        params.put(ServiceConstants.EVENT_RECUR_ID, event.getRecurId());
        NPWebServiceUtil.addOwnerParam(params, event.getAccessInfo());

        String entryUrl = "/event/" + event.getEntryId();

        entryUrl = NPWebServiceUtil.fullUrlWithAuthenticationTokens(entryUrl, mContext);
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
     * Delete event with recur id.
     *
     * @param event
     * @throws com.nexuspad.service.dataservice.NPException
     */
    public void deleteEvent(NPEvent event) throws NPException {

        Map<String, String> params = new HashMap<String, String>();
        params.put(ServiceConstants.EVENT_RECUR_ID, event.getRecurId());
        NPWebServiceUtil.addOwnerParam(params, event.getAccessInfo());

        String entryUrl = "/event/" + event.getEntryId();
        entryUrl = NPWebServiceUtil.fullUrlWithAuthenticationTokens(entryUrl, mContext);
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
}
