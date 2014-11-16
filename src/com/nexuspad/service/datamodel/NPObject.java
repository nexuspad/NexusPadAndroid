package com.nexuspad.service.datamodel;

import android.os.Parcelable;

public abstract class NPObject implements Parcelable {

    public NPObject() {
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
