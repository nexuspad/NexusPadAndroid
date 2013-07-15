/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.photos;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import com.edmondapps.utils.android.service.FileUploadService;
import com.nexuspad.datamodel.Folder;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * @author Edmond
 */
public class Request implements Comparable<Request> {
    private static final String[] FILE_PATH_COLUMN = new String[]{MediaStore.Images.Media.DATA};

    private final long mTimeStamp;
    private final Uri mUri;
    private final Folder mFolder;

    private File mFile;
    private WeakReference<FileUploadService.Callback> mCallback;
    private boolean mCancelled;

    public Request(Uri uri, Folder folder) {
        this(uri, folder, null);
    }

    public Request(Uri uri, Folder folder, FileUploadService.Callback callback) {
        mUri = uri;
        mFolder = folder;
        mCallback = new WeakReference<FileUploadService.Callback>(callback);
        mTimeStamp = System.currentTimeMillis();
    }

    /**
     * This method may block.
     *
     * @param c a {@link Context}
     * @return a {@link File} represented by the {@link Uri}
     */
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

    public void setCallback(FileUploadService.Callback callback) {
        mCallback = new WeakReference<FileUploadService.Callback>(callback);
    }

    public FileUploadService.Callback getCallback() {
        return new FileUploadService.Callback() {
            @Override
            public boolean onProgress(long progress, long total) {
                final FileUploadService.Callback callback = mCallback.get();
                if (callback != null) {
                    return callback.onProgress(progress, total);
                }
                return true;
            }

            @Override
            public void onDone(boolean success) {
                final FileUploadService.Callback callback = mCallback.get();
                if (callback != null) {
                    callback.onDone(success);
                }
            }
        };
    }

    public Folder getFolder() {
        return mFolder;
    }

    public Uri getUri() {
        return mUri;
    }

    public long getTimeStamp() {
        return mTimeStamp;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((mUri == null) ? 0 : mUri.hashCode());
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
        Request other = (Request) obj;
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
