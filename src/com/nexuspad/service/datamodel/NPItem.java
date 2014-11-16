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

import java.util.HashMap;
import java.util.Map;

public class NPItem implements Parcelable {

    public enum ItemType {
        PHONE, EMAIL
    }

    private final ItemType type;
    private String value;
	private String formattedValue;
    private String label;

    public NPItem(ItemType type) {
        this.type = type;
    }

    public NPItem(ItemType type, String value) {
        this.type = type;
        this.value = value;
    }

    public NPItem(ItemType type, JSONObject jsonObj) {
        this.type = type;
        try {
            if (jsonObj.has(ServiceConstants.NP_ITEM_VALUE)) {
                value = jsonObj.getString(ServiceConstants.NP_ITEM_VALUE);
            }

	        if (jsonObj.has(ServiceConstants.NP_ITEM_FORMATTED_VALUE)) {
		        formattedValue = jsonObj.getString(ServiceConstants.NP_ITEM_FORMATTED_VALUE);
	        } else {
		        formattedValue = value;
	        }

	        if (jsonObj.has(ServiceConstants.NP_ITEM_LABEL)) {
                label = jsonObj.getString(ServiceConstants.NP_ITEM_LABEL);
            }

        } catch (JSONException e) {
            Log.e("NPItem",
                    "Error creating NPItem from JSONObject: " + e.toString());
        }
    }

    protected NPItem(Parcel src) {
        value = src.readString();
	    formattedValue = src.readString();
        type = (ItemType) src.readSerializable();
        label = src.readString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NPItem)) return false;

        NPItem basicItem = (NPItem) o;

        if (label != null ? !label.equals(basicItem.label) : basicItem.label != null) return false;
        if (type != basicItem.type) return false;
        if (value != null ? !value.equals(basicItem.value) : basicItem.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (label != null ? label.hashCode() : 0);
        return result;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(value);
	    dest.writeString(formattedValue);
        dest.writeSerializable(type);
        dest.writeString(label);
    }

    @Override
    public String toString() {
        return type + "|" + value + "|" + label;
    }

    public Map<String, String> toMap() {
        Map<String, String> postParams = new HashMap<String, String>();

        postParams.put(ServiceConstants.NP_ITEM_VALUE, value);
        postParams.put(ServiceConstants.ITEM_TYPE, type.name());

        if (label != null) {
            postParams.put(ServiceConstants.NP_ITEM_LABEL, label);
        }

        return postParams;
    }

    /*
     * Getters and Setters
     */

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

	public String getFormattedValue() {
		return formattedValue;
	}

	public void setFormattedValue(String formattedValue) {
		this.formattedValue = formattedValue;
	}

    public ItemType getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
