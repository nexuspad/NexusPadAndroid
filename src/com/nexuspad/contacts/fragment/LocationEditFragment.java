package com.nexuspad.contacts.fragment;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import com.nexuspad.R;
import com.nexuspad.common.annotation.FragmentName;
import com.nexuspad.service.datamodel.Location;

/**
 * Author: edmond
 */
@FragmentName(LocationEditFragment.TAG)
public final class LocationEditFragment extends DialogFragment {
    public static final String TAG = "LocationEditFragment";
    public static final String KEY_LOCATION = "key_location";

    public static LocationEditFragment of(Location location) {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_LOCATION, location);

        final LocationEditFragment fragment = new LocationEditFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private Location mLocation;

    private EditText mStreetAddressView;
    private EditText mCityView;
    private EditText mStateView;
    private EditText mZipCodeView;
    private EditText mCountryView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle arguments = getArguments();
        if (arguments != null) {
            mLocation = arguments.getParcelable(KEY_LOCATION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.contact_address_edit_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mStreetAddressView = (EditText)view.findViewById(R.id.txt_street_address);
        mCityView = (EditText)view.findViewById(R.id.txt_city);
        mStateView = (EditText)view.findViewById(R.id.txt_state);
        mZipCodeView = (EditText)view.findViewById(R.id.txt_zip_code);
        mCountryView = (EditText)view.findViewById(R.id.txt_country);

        updateUI();
        super.onViewCreated(view, savedInstanceState);
    }

    private void updateUI() {
        if (mLocation != null) {
            mStreetAddressView.setText(mLocation.getStreetAddress());
            mCityView.setText(mLocation.getCity());
            mStateView.setText(mLocation.getProvince());
            mZipCodeView.setText(mLocation.getPostalCode());
            mCountryView.setText(mLocation.getCountry());
        }
    }

    public Location getEditedLocation() {
        final Location location = mLocation == null ? new Location() : new Location(mLocation);
        location.setStreetAddress(mStreetAddressView.getText().toString());
        location.setCity(mCityView.getText().toString());
        location.setProvince(mStateView.getText().toString());
        location.setPostalCode(mZipCodeView.getText().toString());
        location.setCountry(mCountryView.getText().toString());
        return location;
    }
}
