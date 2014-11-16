package com.nexuspad.service.datamodel;

import android.os.Parcel;
import org.json.JSONObject;

public class Email extends NPItem {

    public enum Type {
        WORK, PERSONAL;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    public Email() {
        super(ItemType.EMAIL);
    }

    public Email(String value) {
        super(ItemType.EMAIL, value);
    }

    public Email(JSONObject jsonObj) {
        super(ItemType.EMAIL, jsonObj);
    }

    public Email(Parcel src) {
        super(src);
    }

    public static final Creator<Email> CREATOR = new Creator<Email>() {

        @Override
        public Email createFromParcel(Parcel src) {
            return new Email(src);
        }

        @Override
        public Email[] newArray(int len) {
            return new Email[len];
        }
    };
}
