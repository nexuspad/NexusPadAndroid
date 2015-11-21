/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.datamodel;

import android.os.Parcel;
import com.nexuspad.service.dataservice.ServiceConstants;
import com.nexuspad.service.util.DateUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class NPDoc extends NPEntry {
    @SuppressWarnings("hiding")
    public static final Creator<NPDoc> CREATOR = new Creator<NPDoc>() {
        @Override
        public NPDoc createFromParcel(Parcel source) {
            return new NPDoc(source);
        }

        @Override
        public NPDoc[] newArray(int size) {
            return new NPDoc[size];
        }
    };

    private boolean isHtml;

    public NPDoc(NPFolder folder) {
        super(folder, EntryTemplate.DOC);
    }

    public NPDoc(NPDoc aDoc) {
        super(aDoc);
	    isHtml = aDoc.isHtml;
    }

    public NPDoc(JSONObject json) {
        super(json, EntryTemplate.DOC);

	    isHtml = false;

        if (json.has(ServiceConstants.DOC_FORMAT)) {
	        try {
		        if ("html".equalsIgnoreCase(json.getString(ServiceConstants.DOC_FORMAT))) {
					isHtml = true;
		        }

	        } catch (JSONException e) {

	        }
        }
    }

    private NPDoc(Parcel p) {
        super(p);
	    isHtml = p.readByte() != 0x00;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte((byte) (isHtml ? 0x01 : 0x00));
    }

    public boolean isHtml() {
        return isHtml;
    }

    public void setHtml(boolean isHtml) {
        this.isHtml = isHtml;
    }

    @Override
    public Map<String, String> toMap() {
        Map<String, String> postParams = super.toMap();

        if (folder.getModuleId() == ServiceConstants.JOURNAL_MODULE) {
            postParams.put(ServiceConstants.JOURNAL_DATE, DateUtil.convertToYYYYMMDD(createTime));
        }

        return postParams;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NPDoc)) return false;
        if (!super.equals(o)) return false;

        NPDoc doc = (NPDoc) o;

        if (isHtml != doc.isHtml) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (isHtml ? 1 : 0);
        return result;
    }
}
