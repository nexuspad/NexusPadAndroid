/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.home.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;

import com.actionbarsherlock.app.SherlockFragment;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.nexuspad.account.AccountManager;
import com.nexuspad.app.App;
import com.nexuspad.datamodel.NPUser;
import com.nexuspad.util.Logs;
import com.nexuspad.dataservice.NPException;

/**
 * @author Edmond
 * 
 */
@FragmentName(MainFragment.TAG)
public class MainFragment extends SherlockFragment {
    public static final String TAG = "MainFragment";

    public interface Callback {
        void onLogin(MainFragment f, NPUser user);

        void onLoginFailed(MainFragment f, String userName, String password);

        void onNoUserStored(MainFragment f);
    }

    private Callback mCallback;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = App.getCallback(activity, Callback.class);
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        try {
            NPUser currentUser = AccountManager.currentAccount();
            Logs.d(TAG, "Current user stored in SQLite: " + currentUser);

            if (!TextUtils.isEmpty(currentUser.getSessionId())) {
                mCallback.onLogin(null, currentUser);
            } else {
                String email = currentUser.getEmail();
                String password = currentUser.getPassword();
                AccountManager.autoSignInAsync(email, password, getActivity(), new AccountManager.Callback() {
                    @Override
                    public void onLoginFailed(String userName, String password) {
                        mCallback.onLoginFailed(MainFragment.this, userName, password);
                    }

                    @Override
                    public void onLogin(NPUser user) {
                        mCallback.onLogin(MainFragment.this, user);
                    }
                });
            }
        } catch (NPException npe) {
            mCallback.onNoUserStored(this);
        }
    }
}
