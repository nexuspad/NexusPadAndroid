package com.nexuspad.service.dataservice;

import android.util.Log;
import com.nexuspad.service.datamodel.AccessEntitlement;
import com.nexuspad.service.datamodel.NPFolder;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Handle Folder add/update/delete actions.
 *
 * @author ren
 */
public class FolderActionResult extends ServiceResult {

    private String actionName;

    private boolean actionSuccessful;
    private int errorCode;

    private NPFolder updatedFolder;

    public FolderActionResult(JSONObject jsonObj) {
        actionSuccessful = false;

        try {
            actionName = jsonObj.getString(ServiceConstants.ACTION_NAME);

            if (jsonObj.getString(ServiceConstants.NP_RESPONSE_STATUS).equalsIgnoreCase("success")) {
                actionSuccessful = true;
            }

        } catch (JSONException e) {
        }

        // Folder detail for local refresh
        try {
            JSONObject folderPart = jsonObj.getJSONObject(ServiceConstants.ACTION_RESULT_FOLDER);

            updatedFolder = new NPFolder();

            updatedFolder.setModuleId(folderPart.getInt(ServiceConstants.MODULE_ID));
            updatedFolder.setFolderId(folderPart.getInt(ServiceConstants.FOLDER_ID));
            updatedFolder.setFolderName(folderPart.getString(ServiceConstants.FOLDER_NAME));
            updatedFolder.setParentId(folderPart.getInt(ServiceConstants.FOLDER_PARENT_ID));

            final JSONObject accessInfoPart = folderPart.getJSONObject(ServiceConstants.ACCESS_INFO);
            updatedFolder.setAccessInfo(new AccessEntitlement(accessInfoPart));

        } catch (JSONException e) {
            Log.e("ActionResult", "Failed to parse the action result: " + e.toString());
        }
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("ActionName:").append(actionName).append(" success:").append(actionSuccessful);

        if (updatedFolder != null) {
            buf.append(" Folder:").append(updatedFolder);
        }
        return buf.toString();
    }

    /*
     * Getters and Setters
     */
    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public boolean isActionSuccessful() {
        return actionSuccessful;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public NPFolder getUpdatedFolder() {
        return updatedFolder;
    }

    public void setUpdatedFolder(NPFolder updatedFolder) {
        this.updatedFolder = updatedFolder;
    }
}
