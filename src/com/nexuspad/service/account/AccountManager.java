/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.account;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import com.nexuspad.service.datamodel.NPUser;
import com.nexuspad.service.datamodel.UserSetting;
import com.nexuspad.service.dataservice.AccountService;
import com.nexuspad.service.dataservice.ErrorCode;
import com.nexuspad.service.dataservice.NPException;
import com.nexuspad.service.datastore.db.AccountDao;
import com.nexuspad.service.datastore.db.DatabaseManager;
import com.nexuspad.service.util.Logs;

import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

public class AccountManager {
    public static final String PREF_FILE_NAME = "NexusPad_Account";
    public static final String TAG = "AccountManager";

    private static final String UUID_PREF_KEY = "uuid";

    private static NPUser sAccount;
    private static String sUniqueID;

    public static NPUser login() {
        return null;
    }

    public static NPUser currentAccount() throws NPException {
        try {
            if (AccountManager.sAccount == null) {
                AccountDao acctDao = new AccountDao(DatabaseManager.getDb());
                AccountManager.sAccount = acctDao.getAccountInfo();
                if (AccountManager.sAccount != null) {
                    AccountManager.sAccount.setTimeZone(AccountManager.getTimeZone());
                    AccountManager.sAccount.setCountryCode(AccountManager.getCountryCode());
                    AccountManager.sAccount.setLanguageCode(AccountManager.getLanguage());
                }
            }
        } catch (NPException npe) {
            throw new NPException(ErrorCode.NOT_LOGGED_IN, "There is no account information stored in local database...");
        }

        if (AccountManager.sAccount == null) {
            throw new NPException(ErrorCode.NOT_LOGGED_IN, "There is no account information stored in local database...");
        }

        return AccountManager.sAccount;
    }

    public static void storeAcctInfo(NPUser acct) throws NPException {
        AccountDao acctDao = new AccountDao(DatabaseManager.getDb());
        acctDao.updateAcctInfo(acct);
    }

    public static void setSettings(UserSetting settings) throws NPException {
        AccountManager.currentAccount().setSetting(settings);
    }

    public static String getSessionId() throws NPException {
        return AccountManager.currentAccount().getSessionId();
    }


    public static String autoSignIn(Context context) {
        Log.d("AccountManager", "Auto login user using the existing account information");
        try {
            AccountDao acctDao = new AccountDao(DatabaseManager.getDb());
            AccountManager.sAccount = acctDao.getAccountInfo();
            if ((AccountManager.sAccount != null) && !AccountManager.sAccount.getEmail().isEmpty() && !AccountManager.sAccount.getPassword().isEmpty()) {
                return AccountManager.autoSignIn(AccountManager.sAccount.getEmail(), AccountManager.sAccount.getPassword(), context);
            }
        } catch (NPException npe) {
            Log.e("AccountManager", npe.toString());
        }
        return null;
    }

    public static String autoSignIn(String userName, String password, Context context) {
        if (context == null) {
            throw new RuntimeException("Context is null in autoSignIn!");
        }

        AccountService accService = new AccountService(context);
        NPUser npUser;
        try {
            npUser = accService.login(userName, password, getUuid(context));
        } catch (NPException e) {
            return null;
        }

        if ((npUser != null) && !npUser.getSessionId().isEmpty()) {
            // The login was successful
            try {
                npUser.setPassword(password);
                AccountManager.storeAcctInfo(npUser);

            } catch (NPException npe) {
                Log.e("AccountManager", npe.toString());
            }
            AccountManager.sAccount = new NPUser(npUser);
            return npUser.getSessionId();

        } else {
            return null;
        }
    }

    public interface Callback {
        /**
         * Called when login is successful, the {@code NPUser} is guaranteed to
         * have a sessionId.
         *
         * @param user same as {@link com.nexuspad.service.account.AccountManager#currentAccount()}
         */
        void onLogin(NPUser user);

        /**
         * Called when login has failed.
         *
         * @param userName the attempted userName
         * @param password the attempted password
         */
        void onLoginFailed(String userName, String password);
    }

    /**
     * Call  asynchronously.
     *
     * @param userName userName
     * @param password password
     * @param c        can be null; it is also weakly-referenced, which means you can
     *                 safely use an anonymous inner class
     */
    public static void autoSignInAsync(String userName, String password, Context context, Callback c) {
        if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(password)) {
            new LoginTask(userName, password, context, c).execute((Void[]) null);
        } else {
            if (c != null) {
                c.onLoginFailed(userName, password);
            }
        }
    }

    private static class LoginTask extends AsyncTask<Void, Void, String> {
        private final String mUserName;
        private final String mPassword;
        private final Context mContext;

        /*
         * if the implementing class is (or references) an Activity, holding a
         * strong reference would cause a memory leak if the Activity is
         * destroyed before the task is finished
         */
        private final WeakReference<Callback> mCallback;

        private LoginTask(String userName, String password, Context context, Callback c) {
            mUserName = userName;
            mPassword = password;
            mContext = context;
            mCallback = new WeakReference<Callback>(c);
        }

        @Override
        protected String doInBackground(Void... params) {
            /*
             * TODO
        	 *
        	 * Callback interface needs to carry the Context over?
        	 *
        	 */
            return AccountManager.autoSignIn(mUserName, mPassword, mContext);
        }

        @Override
        protected void onPostExecute(String result) {
            Callback callback = mCallback.get();
            if (callback != null) {
                if (result != null) {
                    callback.onLogin(sAccount);
                } else {
                    callback.onLoginFailed(mUserName, mPassword);
                }
            } else {
                Logs.w(TAG, "cannot post callback because it is recycled");
            }
        }
    }

    public static void logout() {
        // Clear the account table
        try {
            Log.d("AccountManager", "Logging out user: " + currentAccount().toString());
            AccountDao acctDao = new AccountDao(DatabaseManager.getDb());
            acctDao.clearAcctInfo();
            AccountManager.sAccount = null;

        } catch (NPException npe) {
            Log.e("AccountManager: ", npe.toString());
        }
    }

    public static TimeZone getTimeZone() {
        return TimeZone.getDefault();
    }

    public static String getTimeZoneDisplayName() {
        return AccountManager.getTimeZone().getDisplayName(Locale.US); // This
        // is for
        // storing
        // in
        // database
    }

    public static String getCountryCode() {
        return Locale.getDefault().getCountry();
    }

    public static String getLanguage() {
        return Locale.getDefault().getLanguage();
    }

    /**
     * A unique id pairing with session Id (utoken) for authentication. Stackoverflow.
     *
     * @param context
     * @return
     */
    public synchronized static String getUuid(Context context) {
        if (sUniqueID == null) {
            SharedPreferences sharedPrefs = context.getSharedPreferences("NexusPad", Context.MODE_PRIVATE);

            sUniqueID = sharedPrefs.getString(UUID_PREF_KEY, null);
            if (sUniqueID == null) {
                sUniqueID = UUID.randomUUID().toString();
                Editor editor = sharedPrefs.edit();
                editor.putString(UUID_PREF_KEY, sUniqueID);
                editor.commit();
            }
        }

        //Logs.i("AccountManager", "Device UUID: " + sUniqueID);

        return sUniqueID;
    }
}
