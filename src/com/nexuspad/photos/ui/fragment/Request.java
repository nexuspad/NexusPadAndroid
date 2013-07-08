/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.photos.ui.fragment;

import java.io.File;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * @author Edmond
 */
public class Request implements Comparable<Request> {
    private static final String[] FILE_PATH_COLUMN = new String[] {MediaStore.Images.Media.DATA};

    private final long mTimeStamp;
    private final Uri mUri;

    private File mFile;

    public Request(Uri uri) {
        mUri = uri;
        mTimeStamp = System.currentTimeMillis();
    }

    public File getFile(Context c) {
        if (mFile == null) {
            Cursor cursor = c.getContentResolver().query(mUri, FILE_PATH_COLUMN, null, null, null);

            String filePath = null;
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    filePath = cursor.getString(cursor.getColumnIndex(FILE_PATH_COLUMN[0]));
                }
                cursor.close();
            }
            if (filePath == null) {
                filePath = mUri.getPath();
            }

            mFile = new File(filePath);
        }
        return mFile;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ( (mUri == null) ? 0 : mUri.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Request other = (Request)obj;
        if (mUri == null) {
            if (other.mUri != null) {
                return false;
            }
        } else if (!mUri.equals(other.mUri)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Request o) {
        if (mTimeStamp == o.mTimeStamp) {
            return 0;
        } else if (mTimeStamp > o.mTimeStamp) {
            return 1;
        } else {
            return -1;
        }
    }
}
