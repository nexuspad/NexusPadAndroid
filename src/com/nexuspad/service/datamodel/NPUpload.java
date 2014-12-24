/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.datamodel;

import android.os.Parcel;
import com.google.common.base.Objects;
import com.nexuspad.service.dataservice.ServiceConstants;
import com.nexuspad.service.util.Logs;
import org.json.JSONException;
import org.json.JSONObject;

public class NPUpload extends NPEntry {
    @SuppressWarnings("hiding")
    public static final Creator<NPUpload> CREATOR = new Creator<NPUpload>() {

        @Override
        public NPUpload createFromParcel(Parcel source) {
            return new NPUpload(source);
        }

        @Override
        public NPUpload[] newArray(int size) {
            return new NPUpload[size];
        }
    };

    private int parentEntryModule;
    private int parentEntryFolder;
    private String parentEntryId;

    private String fileName;
    private String fileType;
    private long fileSize;

    private String tnUrl;
    private String lightBoxUrl;
    private String downloadLink;

    // this file is just created locally, waiting to be uploaded
    private boolean mIsJustCreated = false;

    public NPUpload() {
        super(EntryTemplate.UPLOAD);
    }

    public NPUpload(NPUpload other) {
        super(other);

        parentEntryModule = other.parentEntryModule;
        parentEntryFolder = other.parentEntryFolder;
        parentEntryId = other.parentEntryId;

        fileName = other.fileName;
        fileType = other.fileType;
        fileSize = other.fileSize;

        tnUrl = other.tnUrl;
        lightBoxUrl = other.lightBoxUrl;
        downloadLink = other.downloadLink;
    }

    public NPUpload(NPFolder folder) {
        super(folder, EntryTemplate.UPLOAD);
    }

    public NPUpload(JSONObject jsonObj) {
        super(jsonObj, EntryTemplate.UPLOAD);
        try {
            setEntryId(jsonObj.getString(ServiceConstants.ENTRY_ID));
            if (jsonObj.has(ServiceConstants.UPLOAD_TN_URL)) {
                tnUrl = jsonObj.getString(ServiceConstants.UPLOAD_TN_URL);
            }

            if (jsonObj.has(ServiceConstants.UPLOAD_LIGHTBOX_URL)) {
                lightBoxUrl = jsonObj.getString(ServiceConstants.UPLOAD_LIGHTBOX_URL);
            }

            if (jsonObj.has(ServiceConstants.UPLOAD_DOWNLOAD_URL)) {
                downloadLink = jsonObj.getString(ServiceConstants.UPLOAD_DOWNLOAD_URL);
            }

            if (jsonObj.has(ServiceConstants.UPLOAD_FILE_NAME)) {
                fileName = jsonObj.getString(ServiceConstants.UPLOAD_FILE_NAME);
            }

            if (jsonObj.has(ServiceConstants.UPLOAD_FILE_TYPE)) {
                fileType = jsonObj.getString(ServiceConstants.UPLOAD_FILE_TYPE);
            }

            if (jsonObj.has(ServiceConstants.UPLOAD_FILE_SIZE)) {
                fileSize = jsonObj.getLong(ServiceConstants.UPLOAD_FILE_SIZE);
            }

            if (jsonObj.has(ServiceConstants.UPLOAD_FILE_LINK)) {
                downloadLink = jsonObj.getString(ServiceConstants.UPLOAD_FILE_LINK);
            }

        } catch (JSONException e) {
            Logs.e("NPUpload", "Error creating NPUpload object: ", e);
        }
    }

    private NPUpload(Parcel p) {
        super(p);
        parentEntryModule = p.readInt();
        parentEntryFolder = p.readInt();
        parentEntryId = p.readString();
        fileName = p.readString();
        fileType = p.readString();
        fileSize = p.readLong();
        tnUrl = p.readString();
        lightBoxUrl = p.readString();
        downloadLink = p.readString();
    }

    @Override
    public void writeToParcel(Parcel p, int flags) {
        super.writeToParcel(p, flags);
        p.writeInt(parentEntryModule);
        p.writeInt(parentEntryFolder);
        p.writeString(parentEntryId);
        p.writeString(fileName);
        p.writeString(fileType);
        p.writeLong(fileSize);
        p.writeString(tnUrl);
        p.writeString(lightBoxUrl);
        p.writeString(downloadLink);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .omitNullValues()
                .add("super", super.toString())
                .add("parentEntryModule", parentEntryModule)
                .add("parentEntryFolder", parentEntryFolder)
                .add("parentEntryId", parentEntryId)
                .add("fileName", fileName)
                .add("fileType", fileType)
                .add("fileSize", fileSize)
                .add("tnUrl", tnUrl)
                .add("lightBoxUrl", lightBoxUrl)
                .add("downloadLink", downloadLink)
                .toString();
    }

    /*
     * Getters and Setters
     */

    public int getParentEntryModule() {
        return parentEntryModule;
    }

    public void setParentEntryModule(int parentEntryModule) {
        this.parentEntryModule = parentEntryModule;
    }

    public int getParentEntryFolder() {
        return parentEntryFolder;
    }

    public void setParentEntryFolder(int parentEntryFolder) {
        this.parentEntryFolder = parentEntryFolder;
    }

    public String getParentEntryId() {
        return parentEntryId;
    }

    public void setParentEntryId(String parentEntryId) {
        this.parentEntryId = parentEntryId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getTnUrl() {
        return tnUrl;
    }

    public void setTnUrl(String tnUrl) {
        this.tnUrl = tnUrl;
    }

    public String getLightBoxUrl() {
        return lightBoxUrl;
    }

    public void setLightBoxUrl(String lightBoxUrl) {
        this.lightBoxUrl = lightBoxUrl;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }

    public boolean isJustCreated() {
        return mIsJustCreated;
    }

    public void setJustCreated(boolean isJustCreated) {
        mIsJustCreated = isJustCreated;
    }

}
