/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.home.ui.fragment;

import static com.edmondapps.utils.android.view.ViewUtils.findView;
import static com.edmondapps.utils.android.view.ViewUtils.isAllTextNotEmpty;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockFragment;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.nexuspad.R;

/**
 * @author Edmond
 * 
 */
@FragmentName(LoginFragment.TAG)
public class LoginFragment extends SherlockFragment {
    public static final String TAG = "LoginFragment";

    private EditText mUserNameV;
    private EditText mPasswordV;
    private EditText mFirstNameV;
    private EditText mLastNameV;
    private Button mLoginV;

    private View mSignUpV;

    private View mNewAccountV;

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

        initListeners();
    }

    private void initListeners() {
        mSignUpV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean wasSignUp = isSignUp();

                mNewAccountV.setVisibility(wasSignUp ? View.GONE : View.VISIBLE);
                mLoginV.setText(wasSignUp ? R.string.login : R.string.sign_up);
            }
        });

        mLoginV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isSignUp = isSignUp();
                EditText[] views = isSignUp ?
                        new EditText[] {mUserNameV, mPasswordV, mFirstNameV, mLastNameV} :
                        new EditText[] {mUserNameV, mPasswordV};

                if (!isAllTextNotEmpty(R.string.err_empty_field, views)) {
                    return;// error message is show on the EditText
                }

                // TODO login
            }
        });
    }

    private boolean isSignUp() {
        return mNewAccountV.getVisibility() == View.VISIBLE;
    }
}
