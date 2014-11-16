/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.datamodel;

import android.os.Parcel;
import org.json.JSONObject;

public class NPBookmark extends NPEntry {
    public static final Creator<NPBookmark> CREATOR = new Creator<NPBookmark>() {
        @Override
        public NPBookmark createFromParcel(Parcel source) {
            return new NPBookmark(source);
        }

        @Override
        public NPBookmark[] newArray(int size) {
            return new NPBookmark[size];
        }
    };

    private NPBookmark(Parcel p) {
        super(p);
    }

    public NPBookmark(NPFolder folder) {
        super(folder, EntryTemplate.BOOKMARK);
    }

    public NPBookmark(NPBookmark aBookmark) {
        super(aBookmark);
    }

    public NPBookmark(JSONObject jsonObj) {
        super(jsonObj, EntryTemplate.BOOKMARK);
    }
}
