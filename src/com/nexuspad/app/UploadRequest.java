/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.app;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import com.nexuspad.service.datamodel.NPFolder;
import com.nexuspad.service.datamodel.NPEntry;
import com.nexuspad.service.dataservice.NPUploadHelper;

import java.io.File;
import java.lang.ref.WeakReference;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Edmond
 */
public class UploadRequest {
    private static final String[] FILE_PATH_COLUMN = new String[]{MediaStore.Images.Media.DATA};

    /**
     * The destination of the uploading file
     */
    public enum Target {
        FOLDER, ENTRY
    }

    public static UploadRequest forFolder(Uri uri, NPFolder folder, NPUploadHelper.Callback callback) {
        return new UploadRequest(uri, folder, Target.FOLDER, callback);
    }

    public static UploadRequest forEntry(Uri uri, NPEntry entry, NPUploadHelper.Callback callback) {
        return new UploadRequest(uri, entry, Target.ENTRY, callback);
    }

    private final long mTimeStamp;
    private final Uri mUri;
    private final NPEntry mNPEntry;
    private final NPFolder mFolder;
    private final Target mTarget;

    private File mFile;
    private WeakReference<NPUploadHelper.Callback> mCallback;

    private UploadRequest(Uri uri, NPFolder folder, Target target, NPUploadHelper.Callback callback) {
        mNPEntry = null;
        mUri = checkNotNull(uri);
        mFolder = checkNotNull(folder);
        mTarget = checkNotNull(target);
        mCallback = new WeakReference<NPUploadHelper.Callback>(callback);
        mTimeStamp = System.nanoTime();
    }

    private UploadRequest(Uri uri, NPEntry entry, Target target, NPUploadHelper.Callback callback) {
        mFolder = null;
        mUri = checkNotNull(uri);
        mNPEntry = checkNotNull(entry);
        mTarget = checkNotNull(target);
        mCallback = new WeakReference<NPUploadHelper.Callback>(callback);
        mTimeStamp = System.nanoTime();
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

    public void setCallback(NPUploadHelper.Callback callback) {
        mCallback = new WeakReference<NPUploadHelper.Callback>(callback);
    }

    public NPUploadHelper.Callback getCallback() {
        return new NPUploadHelper.Callback() {
            @Override
            public boolean onProgress(long progress, long total) {
                final NPUploadHelper.Callback callback = mCallback.get();
	            return callback == null || callback.onProgress(progress, total);
            }

            @Override
            public void onDone(boolean success) {
                final NPUploadHelper.Callback callback = mCallback.get();
                if (callback != null) {
                    callback.onDone(success);
                }
            }
        };
    }

    /**
     * @return a {@link com.nexuspad.service.datamodel.NPFolder} if {@link #getTarget()} is {@link Target#FOLDER}; null otherwise
     */
    public NPFolder getFolder() {
        return mFolder;
    }

    /**
     * @return a {@link NPEntry} if {@link #getTarget()} is {@link Target#ENTRY}; null otherwise
     */
    public NPEntry getNPEntry() {
        return mNPEntry;
    }

    public Uri getUri() {
        return mUri;
    }

    public long getTimeStamp() {
        return mTimeStamp;
    }

    public Target getTarget() {
        return mTarget;
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
        UploadRequest other = (UploadRequest) obj;
        if (mUri == null) {
            if (other.mUri != null) {
                return false;
            }
        } else if (!mUri.equals(other.mUri)) {
            return false;
        }
        return true;
    }

    // XXX remove
    public int compareTo(UploadRequest o) {
        if (mTimeStamp == o.mTimeStamp) {
            return 0;
        } else if (mTimeStamp > o.mTimeStamp) {
            return 1;
        } else {
            return -1;
        }
    }
}
