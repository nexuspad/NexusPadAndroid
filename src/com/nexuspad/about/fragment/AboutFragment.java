/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.about.fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.nexuspad.R;
import com.nexuspad.account.AccountManager;
import com.nexuspad.app.App;
import com.nexuspad.datamodel.NPUser;
import com.nexuspad.datamodel.UserSetting;
import com.nexuspad.dataservice.NPException;
import com.squareup.picasso.Picasso;

import static com.edmondapps.utils.android.view.ViewUtils.findView;

/**
 * @author Edmond
 * 
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

        mAccountEmailV = findView(view, R.id.lbl_ac_email);
        mAccountUsageV = findView(view, R.id.lbl_ac_usage);
        mProfileImageV = findView(view, android.R.id.icon);
        mNameV = findView(view, R.id.lbl_ac_name);
        mUsernameV = findView(view, R.id.lbl_ac_username);

        loadUser();
        installListeners(view);
        updateUI();
    }

    private void loadUser() {
        try {
            mUser = AccountManager.currentAccount();
        } catch (NPException e) {
            // I thought I am logged in
            throw new AssertionError(e);
        }
    }

    private void installListeners(View parent) {
        findView(parent, R.id.frame_ac_email).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmailTo(mUser.getEmail());
            }
        });
        findView(parent, R.id.lbl_email_support).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmailTo(SUPPORT_EMAIL);
            }
        });
        findView(parent, R.id.lbl_terms_of_use).setOnClickListener(new OnClickListener() {
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

        if ( (firstName != null) && (lastName != null)) {
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
            long usage = setting.getSpaceUsage();
            long allocation = setting.getSpaceAllocation();
            mAccountUsageV.setText(getString(R.string.format_ac_usage, usage, allocation));
        } else {
            mAccountUsageV.setText(R.string.usage_unavailable);
        }
    }
}
