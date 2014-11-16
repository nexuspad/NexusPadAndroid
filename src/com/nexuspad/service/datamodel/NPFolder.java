/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.datamodel;

import android.content.Context;
import android.os.Parcel;
import android.util.Log;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.nexuspad.R;
import com.nexuspad.service.dataservice.ServiceConstants;
import com.nexuspad.service.util.Logs;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class NPFolder extends NPObject implements Iterable<NPFolder> {
    public static final String TAG = "Folder";

    public static final Creator<NPFolder> CREATOR = new Creator<NPFolder>() {
        public NPFolder createFromParcel(Parcel in) {
            return new NPFolder(in);
        }

        public NPFolder[] newArray(int size) {
            return new NPFolder[size];
        }
    };

    private AccessEntitlement accessInfo;

    private int moduleId = 0;

    private int folderId = -1;

    /**
     * Sync id is assigned to locally created entry first.
     */
    private int syncId = 0;

    private String folderName;
    private String folderCode;
    private String colorLabel;

    private int parentId;

    private int status;
    private boolean synced;

    private Date lastModified;

    /**
     * keep the parent folder information for convenience of traveling through
     * folders
     */
    private NPFolder parent;

    private ArrayList<NPFolder> subFolders = new ArrayList<NPFolder>();

    public static final int ROOT_FOLDER = 0;

    /**
     * Use {@link #rootFolderOf(int, android.content.Context)} when possible.
     *
     * @param moduleId one of the {@code *_MODULE} constants in
     *                 {@link com.nexuspad.service.dataservice.ServiceConstants}
     * @return the root folder for the module
     */
    public static NPFolder rootFolderOf(int moduleId) {
        return rootFolderOf(moduleId, null);
    }

    /**
     * Use this method when possible, the folder name is created with string resources.
     *
     * @param moduleId one of the {@code *_MODULE} constants in
     *                 {@link com.nexuspad.service.dataservice.ServiceConstants}
     * @param context  for getting {@code String}s
     * @return the root folder for the module
     */
    public static NPFolder rootFolderOf(int moduleId, Context context) {
        final NPFolder rootFolder = new NPFolder(moduleId, ROOT_FOLDER);
        rootFolder.folderCode = "home";
        rootFolder.folderName = context == null ? "ROOT" : context.getString(getFolderNameFor(moduleId));
        return rootFolder;
    }

    private static int getFolderNameFor(int moduleId) {
        switch (moduleId) {
            case ServiceConstants.CONTACT_MODULE:
                return R.string.my_contacts;
            case ServiceConstants.CALENDAR_MODULE:
                return R.string.my_activities;
            case ServiceConstants.BOOKMARK_MODULE:
                return R.string.my_bookmarks;
            case ServiceConstants.DOC_MODULE:
                return R.string.my_docs;
            case ServiceConstants.PHOTO_MODULE:
                return R.string.my_photos;
            default:
                Logs.w(TAG, "no folder name found for module: " + moduleId);
                return R.string.root;
        }
    }

    public NPFolder() {
    }

    /**
     * A new folder without folder Id assigned.
     *
     * @param moduleId
     */
    public NPFolder(int moduleId) {
        this.moduleId = moduleId;
    }

    public NPFolder(int moduleId, int folderId) {
        this.moduleId = moduleId;
        this.folderId = folderId;
    }

    public NPFolder(JSONObject jsonObj) {
        try {
            moduleId = jsonObj.getInt(ServiceConstants.MODULE_ID);
            folderId = jsonObj.getInt(ServiceConstants.FOLDER_ID);
            folderName = jsonObj.getString(ServiceConstants.FOLDER_NAME);
            parentId = jsonObj.getInt(ServiceConstants.FOLDER_PARENT_ID);

            if (jsonObj.has(ServiceConstants.COLOR_LABEL)) {
                colorLabel = jsonObj.getString(ServiceConstants.COLOR_LABEL);
            }

            if (jsonObj.has(ServiceConstants.SYNC_ID)) {
                syncId = jsonObj.getInt(ServiceConstants.SYNC_ID);
            }

        } catch (JSONException je) {
            System.out.println(je);
            Log.e("Folder", "Error building Folder object using the dictionary...");
        }
    }

    public NPFolder(NPFolder aFolder) {
        accessInfo = aFolder.accessInfo;
        moduleId = aFolder.moduleId;
        folderId = aFolder.folderId;
        folderName = aFolder.folderName;
        folderCode = aFolder.folderCode;
        colorLabel = aFolder.colorLabel;

        parentId = aFolder.parentId;
        parent = aFolder.parent;
        subFolders = aFolder.subFolders;
    }

    protected NPFolder(Parcel in) {
        accessInfo = in.readParcelable(AccessEntitlement.class.getClassLoader());
        moduleId = in.readInt();
        folderId = in.readInt();
        folderName = in.readString();
        folderCode = in.readString();
        colorLabel = in.readString();
        parentId = in.readInt();
        parent = in.readParcelable(NPFolder.class.getClassLoader());
        subFolders = new ArrayList<NPFolder>();
        in.readList(subFolders, NPFolder.class.getClassLoader());
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(accessInfo, 0);
        dest.writeInt(moduleId);
        dest.writeInt(folderId);
        dest.writeString(folderName);
        dest.writeString(folderCode);
        dest.writeString(colorLabel);
        dest.writeInt(parentId);
        dest.writeParcelable(parent, 0);
        dest.writeList(subFolders);
    }

    /**
     * same as {@code getSubFolders().iterator()}
     */
    @Override
    public Iterator<NPFolder> iterator() {
        return subFolders.iterator();
    }

    public Map<String, String> toMap() {
        Map<String, String> postParams = new HashMap<String, String>();
        postParams.put(ServiceConstants.MODULE_ID, String.valueOf(moduleId));
        if (folderId > 0) {
            postParams.put(ServiceConstants.FOLDER_ID, String.valueOf(folderId));
        }
        postParams.put(ServiceConstants.FOLDER_NAME, folderName);
        postParams.put(ServiceConstants.FOLDER_PARENT_ID, String.valueOf(parentId));
        postParams.put(ServiceConstants.OWNER_ID, String.valueOf(accessInfo.getOwner().getUserId()));

        if (syncId != 0) {
            postParams.put(ServiceConstants.SYNC_ID, String.valueOf(syncId));
        }

        return postParams;
    }

    public String getDisplayName() {
        return null;
    }

    public void addSubFolder(NPFolder aFolder) {
        if (subFolders == null) {
            subFolders = new ArrayList<NPFolder>();
        }
        if (!subFolders.contains(aFolder)) {
            subFolders.add(aFolder);
        }
    }

    public void removeSubFolder(NPFolder aFolder) {
        if (subFolders != null) {
            subFolders.remove(aFolder);
        }
    }


    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("ModuleId:").append(moduleId).append(" FolderId:").append(folderId).append(" SyncId:").append(syncId).
                append(" code:").append(folderCode).append(" name:").append(folderName).
                append(" Parent:").append(parentId);

        if (accessInfo != null) {
            buf.append(accessInfo.toString());
        }

        buf.append(" Subfolders:");
        if (subFolders != null) {
            for (NPFolder folder : subFolders) {
                buf.append(folder.getFolderId()).append(",");
            }
        }
        buf.append("\n");

        return buf.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public Predicate<NPFolder> filterById() {
        return new Predicate<NPFolder>() {
            @Override
            public boolean apply(NPFolder folder) {
                if (folder == null) return false;

                if (folderId != folder.folderId) return false;
                if (moduleId != folder.moduleId) return false;

                return true;
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NPFolder)) return false;

        NPFolder folder = (NPFolder) o;

        if (folderId != folder.folderId) return false;
        if (moduleId != folder.moduleId) return false;
        if (parentId != folder.parentId) return false;
        if (accessInfo != null ? !accessInfo.equals(folder.accessInfo) : folder.accessInfo != null) return false;
        if (folderCode != null ? !folderCode.equals(folder.folderCode) : folder.folderCode != null) return false;
        if (colorLabel != null ? !colorLabel.equals(folder.colorLabel) : folder.colorLabel != null) return false;
        if (folderName != null ? !folderName.equals(folder.folderName) : folder.folderName != null) return false;
        if (parent != null ? !parent.equals(folder.parent) : folder.parent != null) return false;
        if (subFolders != null ? !subFolders.equals(folder.subFolders) : folder.subFolders != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = accessInfo != null ? accessInfo.hashCode() : 0;
        result = 31 * result + moduleId;
        result = 31 * result + folderId;
        result = 31 * result + (folderName != null ? folderName.hashCode() : 0);
        result = 31 * result + (folderCode != null ? folderCode.hashCode() : 0);
        result = 31 * result + (colorLabel != null ? colorLabel.hashCode() : 0);
        result = 31 * result + parentId;
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        result = 31 * result + (subFolders != null ? subFolders.hashCode() : 0);
        return result;
    }

    private static ArrayList<NPFolder> uniqueFolders(List<NPFolder> in) {
        final ArrayList<NPFolder> out = new ArrayList<NPFolder>(in.size());
        for (NPFolder subFolder : in) {
            if (!Iterables.tryFind(out, subFolder.filterById()).isPresent()) {
                out.add(subFolder);
            }
        }
        return out;
    }

    public boolean isRootFolder() {
        return getFolderId() == ROOT_FOLDER;
    }

    /*
     * Getters and Setters
     */
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

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getFolderCode() {
        return folderCode;
    }

    public void setFolderCode(String folderCode) {
        this.folderCode = folderCode;
    }

    public String getColorLabel() {
        return colorLabel;
    }

    public void setColorLabel(String folderColor) {
        this.colorLabel = folderColor;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public ArrayList<NPFolder> getSubFolders() {
        final List<NPFolder> unique = uniqueFolders(subFolders);
        subFolders.clear();
        subFolders.addAll(unique);
        return subFolders;
    }

    public void setSubFolders(ArrayList<NPFolder> subFolders) {
        final List<NPFolder> unique = uniqueFolders(subFolders);
        this.subFolders.clear();
        this.subFolders.addAll(unique);
    }

    public AccessEntitlement getAccessInfo() {
        return accessInfo;
    }

    public void setAccessInfo(AccessEntitlement accessInfo) {
        this.accessInfo = accessInfo;
    }

    public void setOwner(NPUser owner) {
        if (accessInfo == null) {
            accessInfo = new AccessEntitlement();
        }
        accessInfo.setOwner(owner);
    }

    public void setParent(NPFolder parent) {
        this.parent = parent;
        parentId = parent.getFolderId();
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isSynced() {
        return synced;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public int getSyncId() {
        return syncId;
    }

    public void setSyncId(int syncId) {
        this.syncId = syncId;
    }

}
