/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.about.fragment;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.nexuspad.Manifest;
import com.nexuspad.R;
import com.nexuspad.account.AccountManager;
import com.nexuspad.app.App;
import com.nexuspad.common.utils.Lazy;
import com.nexuspad.common.annotation.FragmentName;
import com.nexuspad.datamodel.NPUser;
import com.nexuspad.datamodel.UserSetting;
import com.nexuspad.dataservice.AccountService;
import com.nexuspad.dataservice.AccountService.AccountInfoReceiver;
import com.nexuspad.dataservice.ErrorCode;
import com.nexuspad.dataservice.NPException;
import com.nexuspad.dataservice.ServiceError;
import com.nexuspad.home.activity.LoginActivity;
import com.squareup.picasso.Picasso;

/**
 * @author Edmond
 */
@FragmentName(AboutFragment.TAG)
public class AboutFragment extends Fragment {
    public static final String TAG = "AboutFragment";

    private static final String SUPPORT_EMAIL = "support@nexuspad.com";
    private static final String TERMS_OF_USE_URL = "http://nexuspad.com/page/termsofuse";

    private TextView mAccountEmailV;
    private TextView mAccountUsageV;
    private ImageView mProfileImageV;
    private TextView mUsernameV;
    private TextView mNameV;
    private NPUser mUser;

	private final Lazy<AccountService> mAccountService = new Lazy<AccountService>() {
		@Override
		protected AccountService onCreate() {
			return AccountService.getInstance(getActivity());
		}
	};

	private final AccountInfoReceiver mAccountInfoReceiver = new AccountService.AccountInfoReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			super.onReceive(context, intent);
		}

		@Override
		protected void onGot(Context context, Intent intent, UserSetting settings) {
			try {
				mUser = AccountManager.currentAccount();
			} catch (NPException e) {
			}
			updateUI();
		}

		@Override
		protected void onUpdate(Context context, Intent intent, UserSetting entry) {
			updateUI();
		}

		@Override
		protected void onError(Context context, Intent intent, ServiceError error) {
			handleServiceError(error);
		}
	};

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.about_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAccountEmailV = (TextView)view.findViewById(R.id.lbl_ac_email);
        mAccountUsageV = (TextView)view.findViewById(R.id.lbl_ac_usage);
        mProfileImageV = (ImageView)view.findViewById(android.R.id.icon);
        mNameV = (TextView)view.findViewById(R.id.lbl_ac_name);
        mUsernameV = (TextView)view.findViewById(R.id.lbl_ac_username);

        installListeners(view);
    }

	@Override
	public void onResume() {
		super.onResume();
		final FragmentActivity activity = getActivity();

		activity.registerReceiver(mAccountInfoReceiver,
				AccountService.AccountInfoReceiver.getIntentFilter(),
				Manifest.permission.RECEIVE_ACCOUNT_INFO,
				null);

		loadUser();
		updateUI();
	}

	@Override
	public void onPause() {
		super.onPause();
		final FragmentActivity activity = getActivity();

		activity.unregisterReceiver(mAccountInfoReceiver);
	}

    private void loadUser() {
        try {
            mUser = AccountManager.currentAccount();
	        mAccountService.get().getSettingsAndUsage();

        } catch (NPException e) {
            // I thought I am logged in
            throw new AssertionError(e);
        }
    }

    private void installListeners(View parent) {
	    TextView emailSupport = (TextView)parent.findViewById(R.id.lbl_email_support);
        emailSupport.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmailTo(SUPPORT_EMAIL);
            }
        });

	    TextView termOfUse = (TextView)parent.findViewById(R.id.lbl_terms_of_use);
        termOfUse.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(TERMS_OF_USE_URL));
                try {
                    getActivity().startActivity(intent);
                } catch (ActivityNotFoundException a) {
                    // very rare, but anyway
                    Toast.makeText(getActivity(), R.string.err_no_browser, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendEmailTo(String email) {
        App.sendEmail(email, getActivity());
    }

    private void updateUI() {
        NPUser user = mUser;

        updateEmail(user);
        updateName(user);
        updateProfileImage(user);
        updateUsage(user);
    }

    private void updateEmail(NPUser user) {
        mAccountEmailV.setText(user.getEmail());
        mUsernameV.setText(user.getUserName());
    }

    private void updateName(NPUser user) {
        String firstName = user.getFirstName();
        String lastName = user.getLastName();

        if ((firstName != null) && (lastName != null)) {
            mNameV.setText(getString(R.string.format_ac_name, firstName, lastName));
            mNameV.setEnabled(true);
        } else {
            mNameV.setText(R.string.no_name);
            mNameV.setEnabled(false);
        }
    }

    private void updateProfileImage(NPUser user) {
        String imageUrl = user.getProfileImageUrl();
        if (imageUrl != null) {
            Picasso.with(getActivity()).load(imageUrl).into(mProfileImageV);
        }
    }

    private void updateUsage(NPUser user) {
        UserSetting setting = user.getSetting();
        if (setting != null) {
            mAccountUsageV.setText(getString(R.string.format_ac_usage, setting.getSpaceUsageFormatted(), setting.getSpaceAllocationFormatted()));
        } else {
            mAccountUsageV.setText(R.string.usage_unavailable);
        }
    }

	private void handleServiceError(ServiceError error) {
		final ErrorCode errorCode = error.getErrorCode();
		if (shouldKickToLogin(errorCode)) {
			kickToLoginScreen();
		} else {
			//displayRetry();
		}
	}

	// TODO - the logic needs to be consolidated with that in EntriesFragment
	private boolean shouldKickToLogin(ErrorCode errorCode) {
		return errorCode == ErrorCode.INVALID_USER_TOKEN
				|| errorCode == ErrorCode.INVALID_LOGIN
				|| errorCode == ErrorCode.NOT_LOGGED_IN
				|| errorCode == ErrorCode.FAILED_REGISTRATION
				|| errorCode == ErrorCode.FAILED_REGISTRATION_ACCT_EXISTS
				|| errorCode == ErrorCode.FAILED_DELETE_ACCOUNT
				|| errorCode == ErrorCode.LOGIN_NO_USER
				|| errorCode == ErrorCode.LOGIN_ACCT_PROBLEM
				|| errorCode == ErrorCode.LOGIN_FAILED;
	}

	private void kickToLoginScreen() {
		final FragmentActivity activity = getActivity();
		final Intent intent = new Intent(activity, LoginActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
		activity.finish();
	}
}
