/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.home.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.*;
import com.nexuspad.common.annotaion.FragmentName;
import com.nexuspad.common.view.LoadingViews;
import com.nexuspad.common.Lazy;
import com.nexuspad.R;
import com.nexuspad.account.AccountManager;
import com.nexuspad.app.App;
import com.nexuspad.datamodel.NPUser;

/**
 * A {@code Fragment} that handles both login and signup.
 *
 * @author Edmond
 */
@FragmentName(LoginFragment.TAG)
public class LoginFragment extends Fragment {
    public static final String TAG = "LoginFragment";

    public interface Callback {
        void onLogin(NPUser user);
    }

    private Callback mActivityCallback;
    private EditText mUserNameV;
    private EditText mPasswordV;
    private EditText mConfirmPwV;
    private EditText mFirstNameV;
    private EditText mLastNameV;
    private Button mLoginV;
    private TextView mSignUpV;

    private LoadingViews mLoadingViews;
    private final Lazy<EditText[]> mLoginFields = new Lazy<EditText[]>() {
        @Override
        protected EditText[] onCreate() {
            return new EditText[]{mUserNameV, mPasswordV};
        }
    };
    private final Lazy<EditText[]> mSignUpFields = new Lazy<EditText[]>() {
        @Override
        public EditText[] onCreate() {
            return new EditText[]{mFirstNameV, mLastNameV, mConfirmPwV};
        }
    };
    // order should correspond to the layout (error check should flow
    // from top to bottom)
    private final Lazy<EditText[]> mAllFields = new Lazy<EditText[]>() {
        @Override
        public EditText[] onCreate() {
            return new EditText[]{mUserNameV, mPasswordV, mFirstNameV, mLastNameV, mConfirmPwV};
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivityCallback = App.getCallbackOrThrow(activity, Callback.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sign_in_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUserNameV = (EditText)view.findViewById(R.id.txt_user_name);
        mPasswordV = (EditText)view.findViewById(R.id.txt_password);

        mFirstNameV = (EditText)view.findViewById(R.id.txt_first_name);
        mLastNameV = (EditText)view.findViewById(R.id.txt_last_name);

        mLoginV = (Button)view.findViewById(R.id.btn_login);

        mSignUpV = (TextView)view.findViewById(R.id.lbl_sign_up);
        mConfirmPwV = (EditText)view.findViewById(R.id.txt_confirm_pw);

        mLoadingViews = LoadingViews.of(mLoginV, view.findViewById(android.R.id.progress));
        initListeners();
    }

    private void initListeners() {
        mSignUpV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final boolean wasSignUp = isSignUp();

                mUserNameV.setHint(wasSignUp ? R.string.username_or_email : R.string.email);
                mSignUpV.setText(wasSignUp ? R.string.lbl_sign_up : R.string.lbl_login);
                mLoginV.setText(wasSignUp ? R.string.login : R.string.sign_up);

                prepareAnimation(wasSignUp);

                for (EditText e : mSignUpFields.get()) {
                    e.setVisibility(wasSignUp ? View.GONE : View.VISIBLE);
                }
            }

            private void translateView(View v, int delta) {
                v.setTranslationY(delta);
                v.animate().translationY(0);
            }

            private void fade(boolean in, View v) {
                v.setAlpha(in ? 0 : 1);
                v.animate().alpha(in ? 1 : 0);
            }

            private void prepareAnimation(final boolean wasSignUp) {
                final int[] oldPos = new int[2];
                mLoginV.getLocationOnScreen(oldPos);
                mLoginV.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        mLoginV.getViewTreeObserver().removeOnPreDrawListener(this);

                        int[] newPos = new int[2];
                        mLoginV.getLocationOnScreen(newPos);

                        for (EditText e : mSignUpFields.get()) {
                            fade(!wasSignUp, e);
                        }

                        int deltaY = oldPos[1] - newPos[1];
                        translateView(mLoginV, deltaY);
                        translateView(mSignUpV, deltaY);

                        return true;
                    }
                });
            }
        });

        mLoginV.setOnClickListener(new OnClickListener() {

            private AccountManager.Callback mLoginCallback = new AccountManager.Callback() {
                @Override
                public void onLoginFailed(String userName, String password) {
                    mLoadingViews.doneLoading();
                    Toast.makeText(getActivity(), R.string.err_login_failed, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onLogin(NPUser user) {
                    mLoadingViews.doneLoading();
                    mActivityCallback.onLogin(user);
                }
            };

            @Override
            public void onClick(View v) {
                boolean isSignUp = isSignUp();
                EditText[] views = isSignUp ? mAllFields.get() : mLoginFields.get();

                if (!isAllTextNotEmpty(R.string.err_empty_field, views)) {
                    return;// error message is show on the EditText
                }

                mLoadingViews.startLoading();

                if (!isSignUp) {
                    // login
                    String userName = mUserNameV.getText().toString();
                    String password = mPasswordV.getText().toString();
                    AccountManager.autoSignInAsync(userName, password, getActivity(), mLoginCallback);
                }
            }
        });
    }

	public static boolean isTextEmpty(int errorString, TextView e) {
		boolean isEmpty = TextUtils.isEmpty(e.getText().toString());
		if (isEmpty) {
			e.setError(e.getContext().getText(errorString));
			e.requestFocus();
		}
		return isEmpty;
	}

	public static boolean isAllTextNotEmpty(int errorString, TextView... views) {
		for (TextView v : views) {
			if (isTextEmpty(errorString, v)) {
				return false;
			}
		}
		return true;
	}

    private boolean isSignUp() {
        return mFirstNameV.getVisibility() == View.VISIBLE;
    }
}
