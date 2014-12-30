/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.dataservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.google.common.base.Strings;
import com.nexuspad.Manifest;
import com.nexuspad.service.account.AccountManager;
import com.nexuspad.service.datamodel.NPUser;
import com.nexuspad.service.datamodel.UserSetting;
import com.nexuspad.service.util.Logs;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

/**
 * Handles account related services.
 *
 * @author ren
 */
public class AccountService {

    public static final String TAG = "AccountService";

    protected final Context mContext;

    public static final String ACCOUNT_ERROR = "settings_retrieval_error";

    public static final String KEY_ACCOUNT_INFO = "key_account_info";
    public static final String KEY_ERROR = "key_error";

    public static final String ACTION_UPDATE = "action_account_update";
    public static final String ACTION_GET = "action_account_get";
    public static final String ACTION_ERROR = "action_account_error";


    private static AccountService mInstance;

    public static AccountService getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new AccountService(context);
        }
        return mInstance;
    }

    public AccountService(Context context) {
        mContext = context;
    }

    public static abstract class AccountInfoReceiver extends BroadcastReceiver {
        public static IntentFilter getIntentFilter() {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_UPDATE);
            filter.addAction(ACTION_GET);
            filter.addAction(ACTION_ERROR);
            return filter;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            UserSetting settings = intent.getParcelableExtra(KEY_ACCOUNT_INFO);

            if (ACTION_GET.equals(action)) {
                onGot(context, intent, settings);

            } else if (ACTION_UPDATE.equals(action)) {
                onUpdate(context, intent, settings);

            } else if (ACTION_ERROR.equals(action)) {
                onError(context, intent, intent.<ServiceError>getParcelableExtra(KEY_ERROR));

            } else {
                Logs.w(TAG, "unhandled action: " + action);
            }
        }

        /**
         * Called when account information is received.
         *
         * @param context
         * @param intent
         * @param settings
         */
        protected void onGot(Context context, Intent intent, UserSetting settings) {
        }

        /**
         * Called when account is updated.
         *
         * @param context
         * @param intent
         * @param entry
         */
        protected void onUpdate(Context context, Intent intent, UserSetting entry) {
        }

        /**
         * Called when there is error retrieving account information.
         *
         * @param context
         * @param intent
         * @param error
         */
        protected void onError(Context context, Intent intent, ServiceError error) {
        }
    }

    /**
     * Login a user with username/email and password
     *
     * @param userName
     * @param password
     * @return
     * @throws NPException
     */
    public NPUser login(String userName, String password, String uuid) throws NPException {
        Map<String, String> postParams = new HashMap<String, String>();
        postParams.put(ServiceConstants.ACCT_LOGIN, userName);
        postParams.put(ServiceConstants.ACCT_PASSWORD, password);
        postParams.put(ServiceConstants.NP_UUID, uuid);

        RequestFuture<String> future = RequestFuture.newFuture();

        AccountRequest request = new AccountRequest(null, postParams, accountUrl("login"), future, future);

        NPWebServiceUtil.getRequestQueue(mContext).add(request);

        ServiceResult serviceResult = null;

        try {
            String responseString = future.get(); // this will block

            JSONObject response = new JSONObject(responseString);
            serviceResult = new ServiceResult(response);

        } catch (InterruptedException e) {
            throw new RuntimeException("Login failed.", e);

        } catch (ExecutionException e) {
            throw new RuntimeException("Login failed.", e);

        } catch (JSONException e) {
            throw new RuntimeException("Login failed.", e);
        }

        NPUser acct = new NPUser();

        if (serviceResult != null && serviceResult.isSuccessful()) {
            try {
                JSONObject acctResult = serviceResult.getData();

                acct.setUserId(acctResult.getInt(ServiceConstants.ACCT_USER_ID));
                acct.setUserName(acctResult.getString(ServiceConstants.ACCT_USER_NAME));
                acct.setEmail(acctResult.getString(ServiceConstants.ACCT_EMAIL));
                acct.setPadHost(acctResult.getString(ServiceConstants.ACCT_PAD_HOST));
                acct.setSessionId(acctResult.getString(ServiceConstants.ACCT_SESSION_ID));

                if (acctResult.has(ServiceConstants.FIRST_NAME)) {
                    acct.setFirstName(acctResult.getString(ServiceConstants.FIRST_NAME));
                }

                if (acctResult.has(ServiceConstants.LAST_NAME)) {
                    acct.setLastName(acctResult.getString(ServiceConstants.LAST_NAME));
                }

                if (acctResult.has(ServiceConstants.TIMEZONE)) {
                    acct.setTimeZone(TimeZone.getTimeZone(acctResult.getString(ServiceConstants.TIMEZONE)));
                }

            } catch (JSONException e) {

            }
        }

        return acct;
    }

    /**
     * Create an account
     *
     * @param acct
     * @return
     * @throws NPException
     */
    public NPUser createAccount(NPUser acct) throws NPException {
        Map<String, String> postParams = new HashMap<String, String>();

        postParams.put(ServiceConstants.ACCT_EMAIL, acct.getEmail());
        postParams.put(ServiceConstants.ACCT_PASSWORD, acct.getPassword());
        postParams.put(ServiceConstants.FIRST_NAME, acct.getFirstName());
        postParams.put(ServiceConstants.LAST_NAME, acct.getLastName());
        postParams.put(ServiceConstants.TIMEZONE, acct.getTimeZone().getDisplayName());

        if (!Strings.isNullOrEmpty(acct.getCountryCode())) {
            postParams.put(ServiceConstants.COUNTRY_CODE, acct.getCountryCode());
        }

        if (!Strings.isNullOrEmpty(acct.getLanguageCode())) {
            postParams.put(ServiceConstants.LANGUAGE_CODE, acct.getLanguageCode());
        }

        if (!Strings.isNullOrEmpty(acct.getUuid())) {
            postParams.put(ServiceConstants.NP_UUID, acct.getUuid());
        }

        RequestFuture<String> future = RequestFuture.newFuture();

        AccountRequest request = new AccountRequest(null, postParams, accountUrl("register"), future, future);

        NPWebServiceUtil.getRequestQueue(mContext).add(request);

        ServiceResult serviceResult = null;

        try {
            String responseString = future.get(); // this will block

            JSONObject response = new JSONObject(responseString);
            serviceResult = new ServiceResult(response);

        } catch (InterruptedException e) {
            Log.e(TAG, e.toString());
            throw new NPException(ErrorCode.INTERNAL_ERROR, "Unable to register.");

        } catch (ExecutionException e) {
            Log.e(TAG, e.toString());
            throw new NPException(ErrorCode.INTERNAL_ERROR, "Unable to register.");

        } catch (JSONException e) {
            Log.e(TAG, e.toString());
            throw new NPException(ErrorCode.INTERNAL_ERROR, "Unable to register.");
        }

        if (serviceResult != null && serviceResult.isSuccessful()) {
            try {
                JSONObject acctResult = serviceResult.getData();

                acct.setUserId(acctResult.getInt(ServiceConstants.ACCT_USER_ID));
                acct.setUserName(acctResult.getString(ServiceConstants.ACCT_USER_NAME));
                acct.setEmail(acctResult.getString(ServiceConstants.ACCT_EMAIL));
                acct.setPadHost(acctResult.getString(ServiceConstants.ACCT_PAD_HOST));
                acct.setSessionId(acctResult.getString(ServiceConstants.ACCT_SESSION_ID));

                if (acctResult.has(ServiceConstants.FIRST_NAME)) {
                    acct.setFirstName(acctResult.getString(ServiceConstants.FIRST_NAME));
                }

                if (acctResult.has(ServiceConstants.LAST_NAME)) {
                    acct.setFirstName(acctResult.getString(ServiceConstants.LAST_NAME));
                }

                if (acctResult.has(ServiceConstants.TIMEZONE)) {
                    acct.setTimeZone(TimeZone.getTimeZone(acctResult.getString(ServiceConstants.TIMEZONE)));
                }

            } catch (JSONException e) {

            }
        } else {
            throw new NPException(ErrorCode.fromInt(serviceResult.getCode()), serviceResult.getMessage());
        }

        return acct;
    }


    /**
     * Get account settings.
     *
     * @throws NPException
     */
    public void getSettingsAndUsage() throws NPException {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, settingsUrl("preferences"), null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                handleSettingsResponse(response);
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


    public void handleSettingsResponse(JSONObject jsonObj) {
        ServiceResult result = new ServiceResult(jsonObj);

        if (result.isSuccessful()) {
            JSONObject settingsResult = result.getData();
            UserSetting settings = new UserSetting();

            try {
                settings.setSpaceAllocationFormatted(settingsResult.getString(ServiceConstants.ACCT_SPACE_ALLOCATION_FORMATTED));
                settings.setSpaceUsageFormatted(settingsResult.getString(ServiceConstants.ACCT_SPACE_USAGE_FORMATTED));

            } catch (JSONException e) {
                Logs.e(TAG, "Error parsing AccountService response", e);
            }

            try {
                AccountManager.setSettings(settings);

                NPUser currentUser = AccountManager.currentAccount();
                currentUser.setFirstName(settingsResult.getString(ServiceConstants.FIRST_NAME));
                currentUser.setLastName(settingsResult.getString(ServiceConstants.LAST_NAME));

                if (settingsResult.has(ServiceConstants.MIDDLE_NAME)) {
                    currentUser.setMiddleName(settingsResult.getString(ServiceConstants.MIDDLE_NAME));
                }

                currentUser.setProfileImageUrl(settingsResult.getString(ServiceConstants.ACCT_PROFILE_IMAGE_URL));

            } catch (NPException e) {
                Logs.e(TAG, "Error set settings", e);
            } catch (JSONException e) {
                Logs.e(TAG, "Error parsing AccountService response", e);
            }

            Intent intent = new Intent(ACTION_GET);
            intent.putExtra(KEY_ACCOUNT_INFO, settings);

            mContext.sendBroadcast(intent, Manifest.permission.RECEIVE_ACCOUNT_INFO);

        } else {
            if (result.getCode() == ErrorCode.INVALID_USER_TOKEN.getIntValue()) {
                // Frontend needs to log user out.
            }
        }
    }

    protected void sendErrorBroadcast(ServiceError error) {
        final Intent intent = new Intent(ACCOUNT_ERROR);
        intent.putExtra(KEY_ERROR, error);
        mContext.sendBroadcast(intent, Manifest.permission.LISTEN_ENTRY_CHANGES);
    }


    /**
     * Volley error most likely are network related.
     *
     * @param error
     */
    protected void handleVolleyError(VolleyError error) {
        Log.e("EntryListService", error.toString());
        if (error instanceof AuthFailureError) {
            // Let the frontend know the user should be logged out.
            sendErrorBroadcast(new ServiceError(ErrorCode.NOT_AUTHENTICATED, "The user should be logged out."));
        }

        // Notify user that internet connection is required.
    }


    private String accountUrl(String activity) {
        String url = "/user/" + activity;

        if (activity.equals("settings")) {
            try {
                url = NPWebServiceUtil.fullUrlWithAuthenticationTokens(url, mContext);
            } catch (NPException e) {
            }

        } else {
            url = NPWebServiceUtil.fullUrl(url);
        }

        return url;
    }

    private String settingsUrl(String activity) {
        String url = "/user/account/" + activity;

        try {
            url = NPWebServiceUtil.fullUrlWithAuthenticationTokens(url, mContext);
        } catch (NPException e) {
        }

        return url;
    }

    /**
     * Handles account post activities.
     *
     * @author ren
     */
    public static class AccountRequest extends StringRequest {

        public static final String TAG = "AccountRequest";

        private NPUser user;
        private Map<String, String> params;

        public AccountRequest(NPUser user, Map<String, String> params, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
            super(Method.POST, url, listener, errorListener);

            Log.i(TAG, url);

            this.user = user;
            this.params = params;
        }

        public AccountRequest(int method, Map<String, String> params, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
            super(method, url, listener, errorListener);
            this.params = params;
        }

        @Override
        protected Map<String, String> getParams() throws AuthFailureError {
            Map<String, String> postParams = new HashMap<String, String>();

            if (params != null) {
                postParams.putAll(params);
            }

            if (user != null) {
                // Data passed from user object.
            }

            return postParams;
        }
    }
}
