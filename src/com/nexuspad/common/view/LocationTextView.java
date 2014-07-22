package com.nexuspad.common.view;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.TextView;
import com.nexuspad.datamodel.Location;

public class LocationTextView extends TextView {
    private static class LocationTextViewParcelable implements Parcelable {
        public static final Parcelable.Creator<LocationTextViewParcelable> CREATOR = new Parcelable.Creator<LocationTextViewParcelable>() {
            @Override
            public LocationTextViewParcelable createFromParcel(Parcel in) {
                return new LocationTextViewParcelable(in);
            }

            @Override
            public LocationTextViewParcelable[] newArray(int size) {
                return new LocationTextViewParcelable[size];
            }
        };

        private LocationTextViewParcelable(Parcel in) {
            mLocation = in.readParcelable(Location.class.getClassLoader());
            mParent = in.readParcelable(Parcelable.class.getClassLoader());
        }

        private final Location mLocation;
        private final Parcelable mParent;

        private LocationTextViewParcelable(LocationTextView locationTextView, Parcelable parent) {
            mParent = parent;
            mLocation = locationTextView.mLocation;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(mLocation, flags);
            dest.writeParcelable(mParent, flags);
        }
    }

    private Location mLocation;

    public LocationTextView(Context context) {
        super(context);
    }

    public LocationTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LocationTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        return new LocationTextViewParcelable(this, super.onSaveInstanceState());
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        final LocationTextViewParcelable parcelable = (LocationTextViewParcelable) state;
        setLocation(parcelable.mLocation);
        super.onRestoreInstanceState(parcelable.mParent);
    }

    public void setLocation(Location location) {
        mLocation = location;
        setText(location.getFullAddress());
    }

    public Location getLocation() {
        return mLocation;
    }
}
