/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.datamodel;

import android.os.Parcel;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;
import com.nexuspad.service.dataservice.ServiceConstants;
import com.nexuspad.service.util.JsonHelper;
import com.nexuspad.service.util.Logs;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.nullToEmpty;

public class NPPerson extends NPEntry {
    @SuppressWarnings("hiding")
    public static final Creator<NPPerson> CREATOR = new Creator<NPPerson>() {
        @Override
        public NPPerson createFromParcel(Parcel src) {
            return new NPPerson(src);
        }

        @Override
        public NPPerson[] newArray(int count) {
            return new NPPerson[count];
        }
    };

    public static final String TAG = "Contact";

    /**
     * Order by last name, then first name, and middle name
     */
    public static final Comparator<NPPerson> ORDERING_BY_LAST_NAME = new Comparator<NPPerson>() {
        @Override
        public int compare(NPPerson a, NPPerson o) {
            return ComparisonChain.start()
                    .compare(a.mLastName, o.mLastName, CASE_INSENSITIVE_ORDERING)
                    .compare(a.mFirstName, o.mFirstName, CASE_INSENSITIVE_ORDERING)
                    .compare(a.mMiddleName, o.mMiddleName, CASE_INSENSITIVE_ORDERING)
                    .result();
        }
    };

    private String mProfileImageUrl;

    private final ArrayList<Phone> mPhones = new ArrayList<Phone>();
    private final ArrayList<Email> mEmails = new ArrayList<Email>();

    private String mFirstName;
    private String mLastName;
    private String mMiddleName;
	private String mFullName;

    private String mBusinessName;

    private String mCountryCode;
    private String mLanguageCode;

    public NPPerson() {
        super(EntryTemplate.CONTACT);
    }

    public NPPerson(NPFolder folder) {
        super(folder, EntryTemplate.CONTACT);
    }

    public NPPerson(NPPerson other) {
        super(other);
        this.mProfileImageUrl = other.mProfileImageUrl;
        this.mFirstName = other.mFirstName;
        this.mLastName = other.mLastName;
        this.mMiddleName = other.mMiddleName;
        this.mBusinessName = other.mBusinessName;
        this.mCountryCode = other.mCountryCode;
        this.mLanguageCode = other.mLanguageCode;
    }

    public NPPerson(JSONObject jsonObj) {
        super(jsonObj, EntryTemplate.CONTACT);

        // Get the profile photo
        if (jsonObj.has(ServiceConstants.PROFILE_PHOTO_URL)) {
            try {
                mProfileImageUrl = jsonObj.getString(ServiceConstants.PROFILE_PHOTO_URL);
            } catch (JSONException e) {
                Logs.e(TAG, e);
            }
        }

        try {
            // Names
            if (jsonObj.has(ServiceConstants.FIRST_NAME)) {
                mFirstName = jsonObj.getString(ServiceConstants.FIRST_NAME);
            }
            if (jsonObj.has(ServiceConstants.LAST_NAME)) {
                mLastName = jsonObj.getString(ServiceConstants.LAST_NAME);
            }
            if (jsonObj.has(ServiceConstants.MIDDLE_NAME)) {
                mMiddleName = jsonObj.getString(ServiceConstants.MIDDLE_NAME);
            }

	        if (jsonObj.has(ServiceConstants.FULL_NAME)) {
		        mFullName = jsonObj.getString(ServiceConstants.FULL_NAME);
	        }

	        if (jsonObj.has(ServiceConstants.BUSINESS_NAME)) {
                mBusinessName = jsonObj
                        .getString(ServiceConstants.BUSINESS_NAME);
            }

            if (jsonObj.has(ServiceConstants.CONTACT_WEBSITE)) {
                webAddress = jsonObj.getString(ServiceConstants.CONTACT_WEBSITE);
            }

            // Parse mPhones
            if (jsonObj.has(ServiceConstants.CONTACT_PHONE)) {
                JSONArray phoneArray = jsonObj.getJSONArray(ServiceConstants.CONTACT_PHONE);
                for (int i = 0; i < phoneArray.length(); i++) {
                    JSONObject phoneJsonObj = phoneArray.getJSONObject(i);
                    Phone phoneInfo = new Phone(phoneJsonObj);
                    mPhones.add(phoneInfo);
                }
            }

            // Parse email addresses
            if (jsonObj.has(ServiceConstants.CONTACT_EMAIL)) {
                JSONArray emailArray = jsonObj.getJSONArray(ServiceConstants.CONTACT_EMAIL);
                for (int i = 0; i < emailArray.length(); i++) {
                    JSONObject emailJsonObj = emailArray.getJSONObject(i);
                    Email emailInfo = new Email(emailJsonObj);
                    mEmails.add(emailInfo);
                }
            }

            // Parse the address and populate the location
            if (jsonObj.has(ServiceConstants.ADDRESS)) {
                location.setStreetAddress(jsonObj.getString(ServiceConstants.ADDRESS));
            }

            if (jsonObj.has(ServiceConstants.CITY)) {
                location.setCity(jsonObj.getString(ServiceConstants.CITY));
            }

            if (jsonObj.has(ServiceConstants.PROVINCE)) {
                location.setProvince(jsonObj.getString(ServiceConstants.PROVINCE));
            }

            if (jsonObj.has(ServiceConstants.POSTAL_CODE)) {
                location.setPostalCode(jsonObj.getString(ServiceConstants.POSTAL_CODE));
            }

        } catch (JSONException e) {
            Logs.e("Contact", "Error parsing JSON object: " + e.toString());
        }
    }

    protected NPPerson(Parcel in) {
        super(in);
        mProfileImageUrl = in.readString();
        in.readList(mPhones, Phone.class.getClassLoader());
        in.readList(mEmails, Email.class.getClassLoader());
        mFirstName = in.readString();
        mLastName = in.readString();
        mMiddleName = in.readString();
	    mFullName = in.readString();
        mBusinessName = in.readString();
        mCountryCode = in.readString();
        mLanguageCode = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(mProfileImageUrl);
        dest.writeList(mPhones);
        dest.writeList(mEmails);
        dest.writeString(mFirstName);
        dest.writeString(mLastName);
        dest.writeString(mMiddleName);
	    dest.writeString(mFullName);
        dest.writeString(mBusinessName);
        dest.writeString(mCountryCode);
        dest.writeString(mLanguageCode);
    }

    @Override
    public boolean filterByPattern(Pattern pattern) {
        return super.filterByPattern(pattern) ||
                pattern.matcher(nullToEmpty(getFirstName())).matches() ||
                pattern.matcher(nullToEmpty(getLastName())).matches() ||
                pattern.matcher(nullToEmpty(getMiddleName())).matches() ||
                pattern.matcher(nullToEmpty(getBusinessName())).matches() ||
                filterByNPItem(pattern, mPhones) ||
                filterByNPItem(pattern, mEmails);
    }

    private boolean filterByNPItem(Pattern pattern, Iterable<? extends NPItem> items) {
        for (NPItem item : items) {
            if (pattern.matcher(nullToEmpty(item.getValue())).matches()) return true;
        }
        return false;
    }

    @Override
    public Map<String, String> toMap() {
        Map<String, String> postParams = super.toMap();

        try {
            postParams.put(ServiceConstants.PROFILE_PHOTO_URL, nullToEmpty(mProfileImageUrl));
            postParams.put(ServiceConstants.FIRST_NAME, nullToEmpty(mFirstName));
            postParams.put(ServiceConstants.LAST_NAME, nullToEmpty(mLastName));
            postParams.put(ServiceConstants.MIDDLE_NAME, nullToEmpty(mMiddleName));
            postParams.put(ServiceConstants.BUSINESS_NAME, nullToEmpty(mBusinessName));

            if (!Strings.isNullOrEmpty(webAddress)) {
                postParams.put(ServiceConstants.CONTACT_WEBSITE, webAddress);
            }

            postParams.put(ServiceConstants.COUNTRY_CODE, nullToEmpty(mCountryCode));
            postParams.put(ServiceConstants.LANGUAGE_CODE, nullToEmpty(mLanguageCode));

            if (mPhones.size() > 0) {
                JSONArray phoneJsonObjArr = new JSONArray();
                for (NPItem phone : mPhones) {
                    phoneJsonObjArr.put(JsonHelper.toJSON(phone.toMap()));
                }
                postParams.put(ServiceConstants.CONTACT_PHONE,
                        phoneJsonObjArr.toString());
            }

            if (mEmails.size() > 0) {
                JSONArray emailJsonObjArr = new JSONArray();
                for (NPItem email : mEmails) {
                    emailJsonObjArr.put(JsonHelper.toJSON(email.toMap()));
                }
                postParams.put(ServiceConstants.CONTACT_EMAIL,
                        emailJsonObjArr.toString());
            }

        } catch (JSONException e) {
            Logs.e("Contact", "Error exporting recurrence to JSON object.");
        }

        return postParams;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .omitNullValues()
                .add("\naccessInfo", mAccessInfo)
                .add("\nfolder", folder.getFolderId())
                .add("\nmTemplate", mTemplate)
                .add("\nentryId", entryId)
                .add("\nsyncId", syncId)
                .add("\nsynced", synced)
                .add("\ncolorLabel", colorLabel)
                .add("\ntags", tags)
                .add("\nnote", note)
                .add("\ncreateTime", createTime)
                .add("\nlocation", location)
                .add("\nwebAddress", webAddress)
                .add("\ntitle", title)
                .add("\nLast Name", mLastName)
                .add("\nFirst Name", mFirstName)
                .add("\nMiddle Name", mMiddleName)
                .add("\nEmails", mEmails)
                .add("\nPhones", mPhones)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NPPerson)) return false;
        if (!super.equals(o)) return false;

        NPPerson contact = (NPPerson) o;

        if (mBusinessName != null ? !mBusinessName.equals(contact.mBusinessName) : contact.mBusinessName != null)
            return false;
        if (mCountryCode != null ? !mCountryCode.equals(contact.mCountryCode) : contact.mCountryCode != null)
            return false;
        if (mEmails != null ? !mEmails.equals(contact.mEmails) : contact.mEmails != null) return false;
        if (mFirstName != null ? !mFirstName.equals(contact.mFirstName) : contact.mFirstName != null) return false;
        if (mLanguageCode != null ? !mLanguageCode.equals(contact.mLanguageCode) : contact.mLanguageCode != null)
            return false;
        if (mLastName != null ? !mLastName.equals(contact.mLastName) : contact.mLastName != null) return false;
        if (mMiddleName != null ? !mMiddleName.equals(contact.mMiddleName) : contact.mMiddleName != null) return false;
        if (mPhones != null ? !mPhones.equals(contact.mPhones) : contact.mPhones != null) return false;
        if (mProfileImageUrl != null ? !mProfileImageUrl.equals(contact.mProfileImageUrl) : contact.mProfileImageUrl != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (mProfileImageUrl != null ? mProfileImageUrl.hashCode() : 0);
        result = 31 * result + mPhones.hashCode();
        result = 31 * result + mEmails.hashCode();
        result = 31 * result + (mFirstName != null ? mFirstName.hashCode() : 0);
        result = 31 * result + (mLastName != null ? mLastName.hashCode() : 0);
        result = 31 * result + (mMiddleName != null ? mMiddleName.hashCode() : 0);
        result = 31 * result + (mBusinessName != null ? mBusinessName.hashCode() : 0);
        result = 31 * result + (mCountryCode != null ? mCountryCode.hashCode() : 0);
        result = 31 * result + (mLanguageCode != null ? mLanguageCode.hashCode() : 0);
        return result;
    }

    /*
         * Getters and Setters
         */
    public String getProfileImageUrl() {
        return mProfileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.mProfileImageUrl = profileImageUrl;
    }

    public ArrayList<Phone> getPhones() {
        return mPhones;
    }

    public void addPhone(Phone phone) {
        if (!mPhones.contains(phone)) {
            mPhones.add(phone);
        }
    }

    public ArrayList<Email> getEmails() {
        return mEmails;
    }

    public void addEmail(Email email) {
        if (!mEmails.contains(email)) {
            mEmails.add(email);
        }
    }

    public String getFirstName() {
        return nullToEmpty(mFirstName);
    }

    public void setFirstName(String firstName) {
        this.mFirstName = firstName;
    }

    public String getLastName() {
        return nullToEmpty(mLastName);
    }

    public void setLastName(String lastName) {
        this.mLastName = lastName;
    }

    public String getMiddleName() {
        return nullToEmpty(mMiddleName);
    }

    public void setMiddleName(String middleName) {
        this.mMiddleName = middleName;
    }

	public String getFullName() {
		return mFullName;
	}

    public String getCountryCode() {
        return mCountryCode;
    }

    public void setCountryCode(String countryCode) {
        this.mCountryCode = countryCode;
    }

    public String getLanguageCode() {
        return mLanguageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.mLanguageCode = languageCode;
    }

    public String getBusinessName() {
        return mBusinessName;
    }

    public void setBusinessName(String businessName) {
        this.mBusinessName = businessName;
    }

}
