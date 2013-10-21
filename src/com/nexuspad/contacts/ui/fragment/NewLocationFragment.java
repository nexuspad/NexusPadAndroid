package com.nexuspad.contacts.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.nexuspad.R;
import com.nexuspad.datamodel.Location;

import static com.edmondapps.utils.android.view.ViewUtils.findView;

/**
 * Author: edmond
 */
@FragmentName(NewLocationFragment.TAG)
public final class NewLocationFragment extends DialogFragment {
    public static final String TAG = "NewLocationFragment";
    public static final String KEY_LOCATION = "key_location";

    public static NewLocationFragment of(Location location) {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_LOCATION, location);

        final NewLocationFragment fragment = new NewLocationFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private Location mLocation;

    private EditText mStreetAddressV;
    private EditText mCityV;
    private EditText mStateV;
    private EditText mZipCodeV;
    private EditText mCountryV;

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
        return inflater.inflate(R.layout.location_new_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mStreetAddressV = findView(view, R.id.txt_street_address);
        mCityV = findView(view, R.id.txt_city);
        mStateV = findView(view, R.id.txt_state);
        mZipCodeV = findView(view, R.id.txt_zip_code);
        mCountryV = findView(view, R.id.txt_country);

        updateUI();
        super.onViewCreated(view, savedInstanceState);
    }

    private void updateUI() {
        if (mLocation != null) {
            mStreetAddressV.setText(mLocation.getStreetAddress());
            mCityV.setText(mLocation.getCity());
            mStateV.setText(mLocation.getProvince());
            mZipCodeV.setText(mLocation.getPostalCode());
            mCountryV.setText(mLocation.getCountry());
        }
    }

    public final Location getEditedLocation() {
        final Location location = mLocation == null ? new Location() : new Location(mLocation);
        location.setStreetAddress(mStreetAddressV.getText().toString());
        location.setCity(mCityV.getText().toString());
        location.setProvince(mStateV.getText().toString());
        location.setPostalCode(mZipCodeV.getText().toString());
        location.setCountry(mCountryV.getText().toString());
        return location;
    }
}
