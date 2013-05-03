/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.home.ui.fragment;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;

import com.actionbarsherlock.app.SherlockFragment;
import com.edmondapps.utils.android.Logs;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.nexuspad.account.AccountManager;
import com.nexuspad.datamodel.NPUser;
import com.nexuspad.dataservice.NPException;

/**
 * @author Edmond
 * 
 */
@FragmentName(MainFragment.TAG)
public class MainFragment extends SherlockFragment {
    public static final String TAG = "MainFragment";

    public interface Callback {
        void onUserLoggedIn(MainFragment f, NPUser user);

        void onNoUserStored(MainFragment f);
    }

    private Callback mCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof Callback) {
            mCallback = (Callback)activity;
        } else {
            throw new IllegalStateException(activity + " must implement Callback.");
        }
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        try {
            NPUser currentUser = AccountManager.currentAccount();
            Logs.d(TAG, "Current user stored in SQLite: " + currentUser);

            if (!TextUtils.isEmpty(currentUser.getSessionId())) {
                mCallback.onUserLoggedIn(null, currentUser);
            } else {
                String email = currentUser.getEmail();
                String password = currentUser.getPassword();
                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                    new LoginTask(email, password).execute((Void[])null);
                }
            }

        } catch (NPException npe) {
            mCallback.onNoUserStored(this);
        }
    }

    private static class LoginTask extends AsyncTask<Void, Void, String> {
        private final String mEmail;
        private final String mPassword;

        private LoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected String doInBackground(Void... params) {
            return AccountManager.autoSignIn(mEmail, mPassword);
        }
    }
}
