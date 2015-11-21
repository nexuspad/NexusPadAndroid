/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.dataservice;

import android.content.Context;
import android.util.Log;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.nexuspad.service.account.AccountManager;
import com.nexuspad.service.datamodel.AccessEntitlement;
import com.nexuspad.service.util.Logs;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class NPWebServiceUtil {

    private static String BASE_URL = "https://api-lab.nexuspad.com";

    private static RequestQueue volleyQueue;

    public static RequestQueue getRequestQueue(Context context) {
        if (volleyQueue == null) {
            volleyQueue = Volley.newRequestQueue(context);
        }

        return volleyQueue;
    }

    public static void setIsProduction(boolean isProduction) {
        if (isProduction) {
            BASE_URL = "https://davinci.nexuspad.com/api";
	        //BASE_URL = "https://nexuspad.com/api";
        }
    }

    /**
     * Get the timeout value based on the web service call.
     * <p/>
     * This is based on the connectivity state.
     * NO connection: 0
     * WiFi connection: 2
     * 3G connection: 4
     *
     * @return
     */
    public static int getTimeoutInSeconds() {
        return 3;
    }

    /**
     * We might need to add owner id in the request. This is decided by
     * comparing the owner and viewer in AccessEntitlement.
     *
     * @param params
     * @param accessInfo
     */
    protected static void addOwnerParam(Map<String, String> params, AccessEntitlement accessInfo) {
        if (accessInfo.getOwner() != accessInfo.getViewer()) {
            params.put(ServiceConstants.OWNER_ID, String.valueOf(accessInfo.getOwner().getUserId()));
        }
    }

    /**
     * Create a complete URL with UTOKEN parameter.
     *
     * @param url
     * @return
     * @throws NPException
     */
    public static String fullUrlWithAuthenticationTokens(String url, Context context) throws NPException {
        try {
            String sessionId = AccountManager.getSessionId();
            if ((sessionId == null) || (sessionId.length() == 0)) {
                Log.d("NPWebService", "There is no valid taken stored. Try auto signin...");
                AccountManager.autoSignIn(context);
            }
        } catch (NPException e) {
            throw new NPException(ErrorCode.INVALID_USER_TOKEN, "Cannot get valid user authentication token.");
        }

        if (url.startsWith("http")) {
            url = appendParam(url, ServiceConstants.NP_UTOKEN, AccountManager.getSessionId());
        } else {
            url = appendParam(BASE_URL + url, ServiceConstants.NP_UTOKEN, AccountManager.getSessionId());
        }

        Logs.i("===>", appendParam(url, ServiceConstants.NP_UUID, AccountManager.getUuid(context)));

        return appendParam(url, ServiceConstants.NP_UUID, AccountManager.getUuid(context));
    }


    public static String fullUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }


    public static String appendParam(String url, String name, String value) {
        if (!url.contains("?")) {
            url = url + "?";
        } else {
            url = url + "&";
        }
        try {
            if (value == null) {
                url = url + name;
            } else {
                url = url + name + "=" + URLEncoder.encode(value, "UTF-8");
            }
        } catch (UnsupportedEncodingException ignored) { //impossible
        }
        return url;
    }

    public static String appendParams(String url, Map<String, String> params) {
        if (!url.contains("?")) {
            url = url + "?";
        } else {
            url = url + "&";
        }

        try {
            for (String key : params.keySet()) {
                url = url + key + "=" + URLEncoder.encode(params.get(key), "UTF-8") + "&";
            }
        } catch (UnsupportedEncodingException ignored) {
        }

        if ((url.length() > 0) && (url.charAt(url.length() - 1) == '&')) {
            url = url.substring(0, url.length() - 1);
            return url;
        } else {
            return url;
        }
    }
}
