/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.datamodel;

import android.os.Parcel;
import android.os.Parcelable;
import com.nexuspad.service.dataservice.ServiceConstants;
import org.json.JSONException;
import org.json.JSONObject;

public class AccessEntitlement implements Parcelable {

    public static final Creator<AccessEntitlement> CREATOR = new Creator<AccessEntitlement>() {
        public AccessEntitlement createFromParcel(Parcel in) {
            return new AccessEntitlement(in);
        }

        public AccessEntitlement[] newArray(int size) {
            return new AccessEntitlement[size];
        }
    };

    private NPUser owner;
    private NPUser viewer;

    private boolean read;
    private boolean write;

    public AccessEntitlement() {
    }

    protected AccessEntitlement(Parcel in) {
        owner = in.readParcelable(NPUser.class.getClassLoader());
        viewer = in.readParcelable(NPUser.class.getClassLoader());
        read = in.readByte() != 0x00;
        write = in.readByte() != 0x00;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(owner, 0);
        dest.writeParcelable(viewer, 0);
        dest.writeByte((byte) (read ? 0x01 : 0x00));
        dest.writeByte((byte) (write ? 0x01 : 0x00));
    }

    public int describeContents() {
        return 0;
    }

    public AccessEntitlement(NPUser owner, NPUser viewer) {
        this.owner = owner;
        this.viewer = viewer;
    }

    public AccessEntitlement(JSONObject jsonObj) throws JSONException {
        int ownerId = jsonObj.getInt(ServiceConstants.OWNER_ID);
        int viewerId = jsonObj.getInt(ServiceConstants.VIEWER_ID);

        owner = new NPUser(ownerId);
        viewer = new NPUser(viewerId);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        if (owner != null) {
            buf.append("owner: ").append(owner.getUserId());
        }
        if (viewer != null) {
            buf.append(" viewer:").append(viewer.getUserId());
        }
        return buf.toString();
    }

    public NPUser getOwner() {
        return owner;
    }

    public void setOwner(NPUser owner) {
        this.owner = owner;
    }

    public NPUser getViewer() {
        return viewer;
    }

    public void setViewer(NPUser viewer) {
        this.viewer = viewer;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isWrite() {
        return write;
    }

    public void setWrite(boolean write) {
        this.write = write;
    }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || ((Object) this).getClass() != o.getClass()) return false;

		AccessEntitlement that = (AccessEntitlement) o;

		if (read != that.read) return false;
		if (write != that.write) return false;
		if (!owner.equals(that.owner)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = owner.hashCode();
		result = 31 * result + (read ? 1 : 0);
		result = 31 * result + (write ? 1 : 0);
		return result;
	}
}
