package com.nexuspad.service.dataservice;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import com.nexuspad.service.datamodel.NPEntry;
import com.nexuspad.service.datamodel.NPFolder;
import com.nexuspad.service.datamodel.NPModule;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Handles file uploading into folder and entry.
 *
 * @author ren
 */
public class EntryUploadService {
    public static final String TAG = "EntryUploadService";

    private final Context context;

    public EntryUploadService(Context ctx) {
        context = ctx;
    }

    /**
     * Add a file to Doc/Photo module.
     *
     * @param fileToUpload
     * @param folder
     */
    @Deprecated
    public void addUploadToFolder(File fileToUpload, NPFolder folder) {
        addUploadToFolder(fileToUpload, folder, null);
    }

    /**
     * Add a file to Doc/Photo module.
     *
     * @param fileToUpload
     * @param folder
     * @return
     */
    public AsyncTask<?, ?, ?> addUploadToFolder(File fileToUpload, NPFolder folder, NPUploadHelper.Callback c) {
        String uploadUrl = "/" + NPModule.getModuleCode(folder.getModuleId()) + "?folder_id=" + folder.getFolderId();
        String realUrl = null;

        try {
            realUrl = NPWebServiceUtil.fullUrlWithAuthenticationTokens(uploadUrl, context);
        } catch (NPException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return NPUploadHelper.uploadFileAsync(fileToUpload, realUrl, c);

    }

    /**
     * Attach a file to Doc/Album.
     *
     * @param fileToUpload
     * @param entry
     * @throws com.nexuspad.service.dataservice.NPException
     */
    public AsyncTask<?, ?, ?> addUploadToEntry(File fileToUpload, NPEntry entry, NPUploadHelper.Callback c) {
        final String uploadUrl = "/" + NPModule.getModuleCode(entry.getFolder().getModuleId()) + "/" + entry.getEntryId();

        String realUrl = null;
        try {
            realUrl = NPWebServiceUtil.fullUrlWithAuthenticationTokens(uploadUrl, context);
        } catch (NPException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return NPUploadHelper.uploadFileAsync(fileToUpload, realUrl, c);
    }

    /**
     * Add a photo to folder.
     *
     * @param bitmap
     * @param folder
     * @throws com.nexuspad.service.dataservice.NPException
     */
    public void addPhotoToFolder(Bitmap bitmap, NPFolder folder) throws NPException {
        File tmpFile = storeBitmapToTmpFile(bitmap);
        if (tmpFile != null) {
            Log.d("", tmpFile.getAbsolutePath());
            addUploadToFolder(tmpFile, folder);
        }
    }

    /**
     * Add a photo to an album.
     *
     * @param bitmap
     * @param entry
     * @throws com.nexuspad.service.dataservice.NPException
     */
    public void addPhotoToEntry(Bitmap bitmap, NPEntry entry) throws NPException {
        File tmpFile = storeBitmapToTmpFile(bitmap);
        if (tmpFile != null) {
            addUploadToEntry(tmpFile, entry, null);
        }
    }

    private File storeBitmapToTmpFile(Bitmap bitmap) {
        /*
         * Store the bitmap to a tmp jpeg file
         */
        File outputDir = context.getCacheDir();
        File outputFile = null;

        try {
            outputFile = File.createTempFile("tmp_image", ".jpeg", outputDir);
            FileOutputStream fOut = new FileOutputStream(outputFile);
            boolean result = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            Log.d("", "bitmap compress result is: " + result);
            fOut.flush();
            fOut.close();

        } catch (IOException e) {
            Log.e("NPUploader", "Error storing the Bitmap to local temp file directory:" + e.toString());
        }

        return outputFile;
    }
}
