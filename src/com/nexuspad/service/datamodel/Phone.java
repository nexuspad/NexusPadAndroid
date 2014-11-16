package com.nexuspad.service.datamodel;

import android.os.Parcel;
import org.json.JSONObject;

public class Phone extends NPItem {

    public enum Type {
        HOME, MOBILE, WORK, FAX, SKPYE
    }

    public Phone() {
        super(ItemType.PHONE);
    }

    public Phone(String number) {
        super(ItemType.PHONE, number);
    }

    public Phone(JSONObject jsonObj) {
        super(ItemType.PHONE, jsonObj);
    }

    public Phone(Parcel src) {
        super(src);
    }

    public static final Creator<Phone> CREATOR = new Creator<Phone>() {
        @Override
        public Phone createFromParcel(Parcel src) {
            return new Phone(src);
        }

        @Override
        public Phone[] newArray(int len) {
            return new Phone[len];
        }
    };
}
