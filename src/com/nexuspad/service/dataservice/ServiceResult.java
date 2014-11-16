package com.nexuspad.service.dataservice;

import android.os.Parcel;
import android.util.Log;
import com.nexuspad.service.datamodel.NPObject;
import org.json.JSONException;
import org.json.JSONObject;

public class ServiceResult extends NPObject {
    public static final Creator<ServiceResult> CREATOR = new Creator<ServiceResult>() {
        @Override
        public ServiceResult createFromParcel(Parcel in) {
            return new ServiceResult(in);
        }

        @Override
        public ServiceResult[] newArray(int size) {
            return new ServiceResult[size];
        }
    };

    /**
     * The service result code 200, 403, application specific etc.
     */
    private int code;

    boolean success;

    /**
     * Result data part
     */
    private JSONObject data;

    /**
     * Result message (error message most likely)
     */
    private String message;

    public ServiceResult() {
    }

    public ServiceResult(JSONObject jsonObj) {
        try {
            code = jsonObj.getInt(ServiceConstants.NP_RESPONSE_CODE);

            if (jsonObj.has(ServiceConstants.NP_RESPONSE_DATA)) {
                data = jsonObj.getJSONObject(ServiceConstants.NP_RESPONSE_DATA);
            }

            success = jsonObj.getString(ServiceConstants.NP_RESPONSE_STATUS).equalsIgnoreCase("success");

            if (jsonObj.has(ServiceConstants.NP_RESPONSE_MESSAGE)) {
                message = jsonObj.getString(ServiceConstants.NP_RESPONSE_MESSAGE);
            }

        } catch (JSONException e) {
            Log.e("ServiceResult", "Error parsing the service response: " + e.getMessage());
        }
    }

    protected ServiceResult(Parcel in) {
        success = in.readByte() != 0x00;
        code = in.readInt();
        try {
            final String s = (String) in.readValue(String.class.getClassLoader());
            data = s == null ? null : new JSONObject(s);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isEntryActionResult() {
        if (data.has(ServiceConstants.ACTION_NAME) && data.has(ServiceConstants.ACTION_RESULT_ENTRY)) {
            return true;
        }
        return false;
    }

    public boolean isFolderActionResult() {
        if (data.has(ServiceConstants.ACTION_NAME) && data.has(ServiceConstants.ACTION_RESULT_FOLDER)) {
            return true;
        }
        return false;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (success ? 0x01 : 0x00));
        dest.writeInt(code);
        dest.writeValue(data == null ? null : data.toString());
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("ServiceResult success:").append(success).append(" code:").append(code).append(" data:").append(data);
        return buf.toString();
    }

    public boolean isSuccessful() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject responseMsgBody) {
        data = responseMsgBody;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
