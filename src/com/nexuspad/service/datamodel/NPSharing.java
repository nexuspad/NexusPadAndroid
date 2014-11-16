/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.datamodel;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import com.nexuspad.service.dataservice.ServiceConstants;
import org.json.JSONException;
import org.json.JSONObject;

public class NPSharing implements Parcelable {

    public static final Creator<NPSharing> CREATOR = new Creator<NPSharing>() {
        public NPSharing createFromParcel(Parcel in) {
            return new NPSharing(in);
        }

        public NPSharing[] newArray(int size) {
            return new NPSharing[size];
        }
    };

    private int moduleId;
    private int folderId;
    private String entryId;

    private NPUser owner;
    private NPUser receiver;
    private String permission;

    /**
     * "public", or just an email address
     */
    private String receiverKey;

    public NPSharing() {

    }

    public NPSharing(JSONObject jsonObj) {
        try {
            moduleId = jsonObj.getInt(ServiceConstants.MODULE_ID);
            folderId = jsonObj.getInt(ServiceConstants.FOLDER_ID);
            entryId = jsonObj.getString(ServiceConstants.ENTRY_ID);
            int ownerId = jsonObj.getInt(ServiceConstants.OWNER_ID);
            owner = new NPUser(ownerId);

            int receiverId = jsonObj.getInt(ServiceConstants.RECEIVER_ID);
            receiver = new NPUser(receiverId);

            receiverKey = jsonObj.getString(ServiceConstants.RECEIVER_KEY);

        } catch (JSONException je) {
            Log.e("Folder", "Error building Folder object using the dictionary...");
        }
    }

    protected NPSharing(Parcel in) {
        moduleId = in.readInt();
        folderId = in.readInt();
        entryId = in.readString();
        owner = in.readParcelable(NPUser.class.getClassLoader());
        receiver = in.readParcelable(NPUser.class.getClassLoader());
        permission = in.readString();
        receiverKey = in.readString();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(moduleId);
        dest.writeInt(folderId);
        dest.writeString(entryId);
        dest.writeParcelable(owner, 0);
        dest.writeParcelable(receiver, 0);
        dest.writeString(permission);
        dest.writeString(receiverKey);
    }

    public int describeContents() {
        return 0;
    }

    public int getModuleId() {
        return moduleId;
    }

    public void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }

    public int getFolderId() {
        return folderId;
    }

    public void setFolderId(int folderId) {
        this.folderId = folderId;
    }

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public NPUser getOwner() {
        return owner;
    }

    public void setOwner(NPUser owner) {
        this.owner = owner;
    }

    public NPUser getReceiver() {
        return receiver;
    }

    public void setReceiver(NPUser receiver) {
        this.receiver = receiver;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getReceiverKey() {
        return receiverKey;
    }

    public void setReceiverKey(String receiverKey) {
        this.receiverKey = receiverKey;
    }

}
