package com.nexuspad.service.dataservice;

import android.os.Parcel;
import android.os.Parcelable;
import org.json.JSONException;
import org.json.JSONObject;

public class ServiceError implements Parcelable {
    public static final Creator<ServiceError> CREATOR = new Creator<ServiceError>() {
        public ServiceError createFromParcel(Parcel in) {
            return new ServiceError(in);
        }

        public ServiceError[] newArray(int size) {
            return new ServiceError[size];
        }
    };

    private ErrorCode errorCode;
    private String message;

    public ServiceError(ErrorCode errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public ServiceError(JSONObject jsonObj) {
        errorCode = ErrorCode.UNKNOWN_ERROR;

        if (jsonObj.has(ServiceConstants.NP_RESPONSE_CODE)) {
            try {
                errorCode = ErrorCode.fromInt(jsonObj.getInt(ServiceConstants.NP_RESPONSE_CODE));
                message = jsonObj.getString(ServiceConstants.NP_RESPONSE_DATA);

            } catch (JSONException e1) {
                // Do nothing here. errorCode remains UNKNOWN_ERROR
            }
        } else {
            message = "Internal error.";
        }
    }

    protected ServiceError(Parcel in) {
        errorCode = (ErrorCode) in.readSerializable();
        message = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(errorCode);
        dest.writeString(message);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /*
         * Getters and Setters
         */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ServiceError: code: " + errorCode + " message: " + message;
    }
}
