/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.home.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import com.nexuspad.common.annotation.FragmentName;
import com.nexuspad.service.account.AccountManager;
import com.nexuspad.app.App;
import com.nexuspad.service.datamodel.NPUser;
import com.nexuspad.service.dataservice.NPException;
import com.nexuspad.service.util.Logs;

/**
 * @author Edmond
 */
@FragmentName(MainFragment.TAG)
public class MainFragment extends Fragment {
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
        mCallback = App.getCallbackOrThrow(activity, Callback.class);
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
