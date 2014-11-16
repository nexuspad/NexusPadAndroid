package com.nexuspad.service.dataservice;

import android.util.Log;
import com.nexuspad.service.datamodel.EntryFactory;
import com.nexuspad.service.datamodel.NPEntry;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Handles entry add/update/delete response.
 *
 * @author ren
 */
public class EntryActionResult extends ServiceResult {

    private String actionName;

    private boolean actionSuccessful;
    private int errorCode;

    private NPEntry updatedEntry;

    public EntryActionResult(JSONObject jsonObj) {
        actionSuccessful = false;

        try {
            actionName = jsonObj.getString(ServiceConstants.ACTION_NAME);

            if (jsonObj.getString(ServiceConstants.NP_RESPONSE_STATUS).equalsIgnoreCase("success")) {
                actionSuccessful = true;
            }
        } catch (JSONException e) {
        }

        if (jsonObj.has(ServiceConstants.ACTION_RESULT_ENTRY)) {
            // Entry detail for local refresh.
            try {
                JSONObject entryPart = jsonObj.getJSONObject(ServiceConstants.ACTION_RESULT_ENTRY);
                updatedEntry = EntryFactory.jsonToEntry(entryPart);

            } catch (JSONException e) {
                Log.e("ActionResult", "Failed to parse the action result: " + e.toString());
            }
        }
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("ActionName:").append(actionName).append(" success:")
                .append(actionSuccessful);
        if (updatedEntry != null) {
            buf.append(" Entry:").append(updatedEntry);
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

    public NPEntry getUpdatedEntry() {
        return updatedEntry;
    }

    public void setUpdatedEntry(NPEntry updatedEntry) {
        this.updatedEntry = updatedEntry;
    }
}
