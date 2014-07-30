package com.nexuspad.contacts.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.nexuspad.common.activity.DoneDiscardActivity;
import com.nexuspad.common.annotation.ParentActivity;
import com.nexuspad.contacts.fragment.LocationEditFragment;
import com.nexuspad.datamodel.Location;

/**
 * Author: edmond
 */
@ParentActivity(ContactEditActivity.class)
public class LocationEditActivity extends DoneDiscardActivity {
    public static final String KEY_LOCATION = "key_location";

    public static Intent of(Context context, Location location) {
        final Intent intent = new Intent(context, LocationEditActivity.class);
        intent.putExtra(KEY_LOCATION, location);
        return intent;
    }

    private Location mLocation;

    @Override
    protected void onCreate(Bundle savedState) {
        setResult(RESULT_CANCELED);
        mLocation = getIntent().getParcelableExtra(KEY_LOCATION);
        super.onCreate(savedState);
    }

    @Override
    protected Fragment onCreateFragment() {
        return LocationEditFragment.of(mLocation);
    }

    @Override
    public void onBackPressed() {
        onDonePressed();
    }

    @Override
    protected void onDonePressed() {
        final Location location = ((LocationEditFragment) getFragment()).getEditedLocation();
        final Intent data = new Intent();
        data.putExtra(KEY_LOCATION, location);
        setResult(RESULT_OK, data);
        finish();
        super.onDonePressed();
    }
}
