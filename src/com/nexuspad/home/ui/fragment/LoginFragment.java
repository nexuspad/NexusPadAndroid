/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.home.ui.fragment;

import static com.edmondapps.utils.android.view.ViewUtils.findView;
import static com.edmondapps.utils.android.view.ViewUtils.isAllTextNotEmpty;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.edmondapps.utils.android.view.LoadingViews;
import com.edmondapps.utils.java.Lazy;
import com.nexuspad.R;
import com.nexuspad.account.AccountManager;
import com.nexuspad.datamodel.NPUser;

/**
 * A {@code Fragment} that handles both login and signup.
 * 
 * @author Edmond
 * 
 */
@FragmentName(LoginFragment.TAG)
public class LoginFragment extends SherlockFragment {
    public static final String TAG = "LoginFragment";

    public interface Callback {
        void onLogin(NPUser user);
    }

    private Callback mCallback;
    private EditText mUserNameV;
    private EditText mPasswordV;
    private EditText mConfirmPwV;
    private EditText mFirstNameV;
    private EditText mLastNameV;
    private Button mLoginV;
    private TextView mSignUpV;
    private View mNewAccountV;

    private LoadingViews<Button, ProgressBar> mLoadingViews;
    private final Lazy<EditText[]> mLoginFields = new Lazy<EditText[]>() {
        @Override
        protected EditText[] onCreate() {
            return new EditText[] {mUserNameV, mPasswordV};
        }
    };
    private final Lazy<EditText[]> mSignUpFields = new Lazy<EditText[]>() {
        @Override
        protected EditText[] onCreate() {
            // order should correspond to the layout (error check should flow
            // from top to bottom)
            return new EditText[] {mUserNameV, mPasswordV, mFirstNameV, mLastNameV, mConfirmPwV};
        }
    };

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_sign_in, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUserNameV = findView(view, R.id.txt_user_name);
        mPasswordV = findView(view, R.id.txt_password);

        mFirstNameV = findView(view, R.id.txt_first_name);
        mLastNameV = findView(view, R.id.txt_last_name);

        mLoginV = findView(view, R.id.btn_login);

        mSignUpV = findView(view, R.id.lbl_sign_up);
        mNewAccountV = findView(view, R.id.frame_new_account);
        mConfirmPwV = findView(view, R.id.txt_confirm_pw);

        mLoadingViews = LoadingViews.of(mLoginV, (ProgressBar)findView(view, android.R.id.progress));
        initListeners();
    }

    private void initListeners() {
        mSignUpV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean wasSignUp = isSignUp();

                mUserNameV.setHint(wasSignUp ? R.string.username_or_email : R.string.email);
                mSignUpV.setText(wasSignUp ? R.string.lbl_sign_up : R.string.lbl_login);
                mNewAccountV.setVisibility(wasSignUp ? View.GONE : View.VISIBLE);
                mLoginV.setText(wasSignUp ? R.string.login : R.string.sign_up);
            }
        });

        mLoginV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isSignUp = isSignUp();
                EditText[] views = isSignUp ? mSignUpFields.get() : mLoginFields.get();

                if (!isAllTextNotEmpty(R.string.err_empty_field, views)) {
                    return;// error message is show on the EditText
                }

                mLoadingViews.startLoading();

                if (!isSignUp) {
                    // login
                    String userName = mUserNameV.getText().toString();
                    String password = mPasswordV.getText().toString();
                    AccountManager.autoSignInAsync(userName, password, new AccountManager.Callback() {
                        @Override
                        public void onLoginFailed(String userName, String password) {
                            mLoadingViews.doneLoading();
                            Toast.makeText(getActivity(), R.string.err_login_failed, Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onLogin(NPUser user) {
                            mLoadingViews.doneLoading();
                            mCallback.onLogin(user);
                        }
                    });
                }
            }
        });
    }

    private boolean isSignUp() {
        return mNewAccountV.getVisibility() == View.VISIBLE;
    }
}