/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.datamodel;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.TimeZone;

public class NPUser implements Parcelable {

    public static final Creator<NPUser> CREATOR = new Creator<NPUser>() {
        public NPUser createFromParcel(Parcel in) {
            return new NPUser(in);
        }

        public NPUser[] newArray(int size) {
            return new NPUser[size];
        }
    };

    private int userId;
    private String userName;
    private String password;
    private String email;

    private String firstName;
    private String middleName;
    private String lastName;
    private String profileImageUrl;

    private TimeZone timeZone;
    private String languageCode;
    private String countryCode;

    private String sessionId;
    private String padDbId;
    private String padHost;

    private UserSetting setting;

    public NPUser() {
        userId = 0;
        setting = new UserSetting();
    }

    public NPUser(int userId) {
        this.userId = userId;
    }

    public NPUser(NPUser aUser) {
        userId = aUser.userId;
        userName = aUser.userName;
        password = aUser.password;
        email = aUser.email;
        sessionId = aUser.sessionId;

        firstName = aUser.firstName;
        middleName = aUser.middleName;
        lastName = aUser.lastName;
        profileImageUrl = aUser.profileImageUrl;

        if (aUser.timeZone != null) {
            timeZone = TimeZone.getTimeZone(aUser.timeZone.getID());
        }

        languageCode = aUser.languageCode;
        countryCode = aUser.countryCode;

        padDbId = aUser.padDbId;
        padHost = aUser.padHost;
    }

    protected NPUser(Parcel in) {
        userId = in.readInt();
        userName = in.readString();
        password = in.readString();
        email = in.readString();
        firstName = in.readString();
        middleName = in.readString();
        lastName = in.readString();
        profileImageUrl = in.readString();
        timeZone = (TimeZone) in.readSerializable();
        languageCode = in.readString();
        countryCode = in.readString();
        sessionId = in.readString();
        padDbId = in.readString();
        padHost = in.readString();
        setting = in.readParcelable(UserSetting.class.getClassLoader());
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(userId);
        dest.writeString(userName);
        dest.writeString(password);
        dest.writeString(email);
        dest.writeString(firstName);
        dest.writeString(middleName);
        dest.writeString(lastName);
        dest.writeString(profileImageUrl);
        dest.writeSerializable(timeZone);
        dest.writeString(languageCode);
        dest.writeString(countryCode);
        dest.writeString(sessionId);
        dest.writeString(padDbId);
        dest.writeString(padHost);
        dest.writeParcelable(setting, 0);
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + userId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NPUser other = (NPUser) obj;
        if (userId != other.userId)
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("Username: ").append(userName).append(" Email:").append(email).append(" UserId:").append(userId).
                append(" SessionId:").append(sessionId).append(" PadHost:").append(padHost).
                append(" FirstName:").append(firstName).append(" LastName:").append(lastName).
                append(" Timezone:").append(timeZone.getDisplayName());
        return buf.toString();
    }

    /*
     * Getters and Setters
     */

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSessionId() {
        if (sessionId == null) return "";
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getPadDbId() {
        return padDbId;
    }

    public void setPadDbId(String padDbId) {
        this.padDbId = padDbId;
    }

    public String getPadHost() {
        return padHost;
    }

    public void setPadHost(String padHost) {
        this.padHost = padHost;
    }

    public UserSetting getSetting() {
        return setting;
    }

    public void setSetting(UserSetting setting) {
        this.setting = setting;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

}
