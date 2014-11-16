/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.datamodel;

import android.os.Parcel;
import android.os.Parcelable;

public class UserSetting implements Parcelable {

    public static final Creator<UserSetting> CREATOR = new Creator<UserSetting>() {
        public UserSetting createFromParcel(Parcel in) {
            return new UserSetting(in);
        }

        public UserSetting[] newArray(int size) {
            return new UserSetting[size];
        }
    };

    private long spaceAllocation;
    private long spaceUsage;

	private String spaceAllocationFormatted;
	private String spaceUsageFormatted;

    public UserSetting() {
    }

    protected UserSetting(Parcel in) {
        spaceAllocation = in.readLong();
        spaceUsage = in.readLong();
	    spaceAllocationFormatted = in.readString();
	    spaceUsageFormatted = in.readString();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(spaceAllocation);
        dest.writeLong(spaceUsage);
	    dest.writeString(spaceAllocationFormatted);
	    dest.writeString(spaceUsageFormatted);
    }

    public int describeContents() {
        return 0;
    }

    public long getSpaceAllocation() {
        return spaceAllocation;
    }

    public void setSpaceAllocation(long spaceAllocation) {
        this.spaceAllocation = spaceAllocation;
    }

    public long getSpaceUsage() {
        return spaceUsage;
    }

    public void setSpaceUsage(long spaceUsage) {
        this.spaceUsage = spaceUsage;
    }


	public void setSpaceAllocationFormatted(String spaceAllocationFormatted) {
		this.spaceAllocationFormatted = spaceAllocationFormatted;
	}

	public String getSpaceAllocationFormatted() {

		return spaceAllocationFormatted;
	}

	public String getSpaceUsageFormatted() {
		return spaceUsageFormatted;
	}

	public void setSpaceUsageFormatted(String spaceUsageFormatted) {
		this.spaceUsageFormatted = spaceUsageFormatted;
	}

    @Override
    public String toString() {
        return "Space allocation: " + this.spaceAllocation + " Space usage: " + this.spaceUsage;
    }
}
