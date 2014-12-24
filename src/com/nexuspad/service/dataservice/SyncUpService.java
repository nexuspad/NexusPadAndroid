/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.dataservice;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.toolbox.RequestFuture;
import com.nexuspad.service.datamodel.NPEntry;
import com.nexuspad.service.datamodel.NPFolder;
import com.nexuspad.service.dataservice.FolderService.FolderUpdateRequest;
import com.nexuspad.service.datastore.EntryStore;
import com.nexuspad.service.datastore.FolderStore;
import com.nexuspad.service.util.Logs;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SyncUpService implements Runnable {

    private final Context mContext;
    private int ownerId;

    public SyncUpService(int ownerId, Context context) {
        this.ownerId = ownerId;
        mContext = context;
    }

    @Override
    public void run() {
        Logs.i("SyncUpService", "Start running sync up service...");
        postUnsyncedEntries();
        postUnsyncedFolders();
    }

    public void postUnsyncedEntries() {
        List<NPEntry> entries = new ArrayList<NPEntry>();

        try {
            Logs.d("SyncUpService", "Retrieving unsynced entries...");
            entries = EntryStore.getUnsyncedEntries(ownerId);
        } catch (NPException e) {
            Logs.e("SyncUpService", "Error getting unsynced entries", e);
        }

        for (NPEntry entry : entries) {
            try {
                updateEntry(entry);

            } catch (InterruptedException e) {
                Logs.e("SyncUpService", "Error posting unsynced entry: " + entry, e);

            } catch (ExecutionException e) {
                Logs.e("SyncUpService", "Error posting unsynced entry: " + entry, e);

            } catch (NPException e) {
                Logs.e("SyncUpService", "Error posting unsynced entry: " + entry, e);
            }
        }
    }

    /**
     * Post to web service and update data store upon successful response.
     *
     * @param entry
     * @throws InterruptedException
     * @throws java.util.concurrent.ExecutionException
     * @throws NPException
     */
    private void updateEntry(NPEntry entry) throws InterruptedException, ExecutionException, NPException {
        String entryUrl = EntryService.entryBaseUri(entry.getTemplate());

        if (!isNewEntry(entry)) {
            entryUrl = entryUrl + "/" + entry.getEntryId();
        }

        entryUrl = NPWebServiceUtil.fullUrlWithAuthenticationTokens(entryUrl, mContext);

        RequestFuture<String> future = RequestFuture.newFuture();

        EntryService.EntryUpdateRequest request = null;

        if (entry.getStatus() == ServiceConstants.ITEM_DELETED) {
            Logs.d("SyncUpService", "Delete entry through web service: " + entry.getEntryId());
            request = new EntryService.EntryUpdateRequest(entry, Request.Method.DELETE, null, entryUrl, future, future);
        } else {
            Logs.d("SyncUpService", "Post entry to web service: EntryId: " + entry.getEntryId() + " SyncId: " + entry.getSyncId());
            request = new EntryService.EntryUpdateRequest(entry, Request.Method.POST, null, entryUrl, future, future);
        }

        NPWebServiceUtil.getRequestQueue(mContext).add(request);

        String responseString = future.get();

        try {
            JSONObject response = new JSONObject(responseString);
            ServiceResult serviceResult = new ServiceResult(response);

            if (serviceResult.isSuccessful()) {
                EntryActionResult actionResult = new EntryActionResult(serviceResult.getData());

                if (ServiceConstants.ACTION_ENTRY_DELETE.equals(actionResult.getActionName())) {
                    // Delete entry from the local data store
                    EntryStore.delete(actionResult.getUpdatedEntry());

                } else if (ServiceConstants.ACTION_ENTRY_ADD.equals(actionResult.getActionName()) ||
                        ServiceConstants.ACTION_ENTRY_UPDATE.equals(actionResult.getActionName())) {
                    // Update the entry record in data store
                    NPEntry updatedEntry = actionResult.getUpdatedEntry();
                    updatedEntry.setSynced(true);
                    EntryStore.update(updatedEntry);

                }
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Determine if the entry is new.
     *
     * @param e
     * @return
     */
    private boolean isNewEntry(NPEntry e) {
        if (e.getEntryId().startsWith("_")) {
            return true;
        }
        return false;
    }

    /**
     * Post the unsynced folders.
     */
    public void postUnsyncedFolders() {
        List<NPFolder> folders = FolderStore.getUnsyncedFolders(ownerId);

        for (NPFolder folder : folders) {
            try {
                updateFolder(folder);

            } catch (NPException e) {
                Logs.e("SyncUpService", "Error posting unsynced folder: " + folder, e);

            } catch (InterruptedException e) {
                Logs.e("SyncUpService", "Error posting unsynced folder: " + folder, e);

            } catch (ExecutionException e) {
                Logs.e("SyncUpService", "Error posting unsynced folder: " + folder, e);

            }
        }
    }


    /**
     * Post to web service and update data store upon successful response.
     *
     * @param folder
     * @throws NPException
     * @throws InterruptedException
     * @throws java.util.concurrent.ExecutionException
     */
    private void updateFolder(NPFolder folder) throws NPException, InterruptedException, ExecutionException {
        String folderUrl = FolderService.folderUri(folder);

        if (!isNewFolder(folder)) {
            folderUrl = NPWebServiceUtil.appendParam(folderUrl, "folder_id", String.valueOf(folder.getFolderId()));
        }

        folderUrl = NPWebServiceUtil.fullUrlWithAuthenticationTokens(folderUrl, mContext);

        RequestFuture<String> future = RequestFuture.newFuture();

        FolderUpdateRequest request = null;

        if (folder.getStatus() == ServiceConstants.ITEM_DELETED) {
            request = new FolderUpdateRequest(folder, Request.Method.DELETE, null, folderUrl, future, future);
        } else {
            request = new FolderUpdateRequest(folder, Request.Method.POST, null, folderUrl, future, future);
        }

        NPWebServiceUtil.getRequestQueue(mContext).add(request);

        String responseString = future.get();

        try {
            JSONObject response = new JSONObject(responseString);
            ServiceResult serviceResult = new ServiceResult(response);

            if (serviceResult.isSuccessful()) {
                FolderActionResult actionResult = new FolderActionResult(serviceResult.getData());

                if (ServiceConstants.ACTION_ENTRY_DELETE.equals(actionResult.getActionName())) {
                    // Delete entry from the local data store
                    FolderStore.delete(actionResult.getUpdatedFolder());

                } else if (ServiceConstants.ACTION_ENTRY_ADD.equals(actionResult.getActionName()) ||
                        ServiceConstants.ACTION_ENTRY_UPDATE.equals(actionResult.getActionName())) {
                    // Update the entry record in data store
                    NPFolder updatedFolder = actionResult.getUpdatedFolder();
                    updatedFolder.setSynced(true);
                    FolderStore.update(updatedFolder);

                }
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    /**
     * Determine if the folder is new.
     *
     * @param f
     * @return
     */
    private boolean isNewFolder(NPFolder f) {
        if (f.getFolderId() > 9000) {
            return true;
        }
        return false;
    }

}
