/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.datamodel;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import com.nexuspad.service.dataservice.ServiceConstants;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Location implements Parcelable {
    public static final Creator<Location> CREATOR = new Creator<Location>() {
        public Location createFromParcel(Parcel in) {
            return new Location(in);
        }

        public Location[] newArray(int size) {
            return new Location[size];
        }
    };

	/**
     * for a generic place name or location *
     */
    private String place;

    private String streetAddress;
    private String city;
    private String province;
    private String postalCode;
    private String country;

    private String latitude;
    private String longitude;

    public Location() {
    }

    public Location(Location aLocation) {
        this.place = aLocation.place;
        this.streetAddress = aLocation.streetAddress;
        this.city = aLocation.city;
        this.province = aLocation.province;
        this.postalCode = aLocation.postalCode;
        this.country = aLocation.country;
        this.latitude = aLocation.latitude;
        this.longitude = aLocation.longitude;
    }

    public Location(JSONObject jsonObj) {
        try {
            if (jsonObj.has(ServiceConstants.ADDRESS)) {
                this.streetAddress = jsonObj.getString(ServiceConstants.ADDRESS);
            }
            if (jsonObj.has(ServiceConstants.CITY)) {
                this.city = jsonObj.getString(ServiceConstants.CITY);
            }
            if (jsonObj.has(ServiceConstants.PROVINCE)) {
                this.province = jsonObj.getString(ServiceConstants.PROVINCE);
            }
            if (jsonObj.has(ServiceConstants.POSTAL_CODE)) {
                this.postalCode = jsonObj.getString(ServiceConstants.POSTAL_CODE);
            }
            if (jsonObj.has(ServiceConstants.COUNTRY)) {
                this.country = jsonObj.getString(ServiceConstants.COUNTRY);
            }
            if (jsonObj.has(ServiceConstants.LATITUDE)) {
                this.latitude = jsonObj.getString(ServiceConstants.LATITUDE);
            }
            if (jsonObj.has(ServiceConstants.LONGITUDE)) {
                this.longitude = jsonObj.getString(ServiceConstants.LONGITUDE);
            }

            if (jsonObj.has(ServiceConstants.ENTRY_LOCATION)) {
                this.place = jsonObj.getString(ServiceConstants.ENTRY_LOCATION);

                // TODO: place can be further parsed into streetAddress, city
                // province etc.
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    protected Location(Parcel in) {
        place = in.readString();
        streetAddress = in.readString();
        city = in.readString();
        province = in.readString();
        postalCode = in.readString();
        country = in.readString();
        latitude = in.readString();
        longitude = in.readString();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(place);
        dest.writeString(streetAddress);
        dest.writeString(city);
        dest.writeString(province);
        dest.writeString(postalCode);
        dest.writeString(country);
        dest.writeString(latitude);
        dest.writeString(longitude);
    }

    public int describeContents() {
        return 0;
    }

    /**
     * @return if all the fields are empty
     */
    public boolean isEmpty() {
        if (!TextUtils.isEmpty(place)) return false;
        if (!TextUtils.isEmpty(streetAddress)) return false;
        if (!TextUtils.isEmpty(city)) return false;
        if (!TextUtils.isEmpty(province)) return false;
        if (!TextUtils.isEmpty(postalCode)) return false;
        if (!TextUtils.isEmpty(country)) return false;
        if (!TextUtils.isEmpty(latitude)) return false;
        if (!TextUtils.isEmpty(longitude)) return false;
        return true;
    }

    /**
     * @return an address string built from street address, city, province, country and postal code
     */
    public String getFullAddress() {
        final StringBuilder builder = new StringBuilder();
	    appendIfNotEmpty(builder, place);
        appendIfNotEmpty(builder, streetAddress);
        appendIfNotEmpty(builder, city);
        appendIfNotEmpty(builder, province);
        appendIfNotEmpty(builder, country);
        appendIfNotEmpty(builder, postalCode);
        return builder.toString();
    }

    public Map<String, String> toMap() {
        Map<String, String> postParams = new HashMap<String, String>();
        if (streetAddress != null) {
            postParams.put(ServiceConstants.ADDRESS, streetAddress);
        }
        if (city != null) {
            postParams.put(ServiceConstants.CITY, city);
        }
        if (province != null) {
            postParams.put(ServiceConstants.PROVINCE, province);
        }
        if (postalCode != null) {
            postParams.put(ServiceConstants.POSTAL_CODE, postalCode);
        }
        if (country != null) {
            postParams.put(ServiceConstants.COUNTRY, country);
        }
        if (latitude != null) {
            postParams.put(ServiceConstants.LATITUDE, latitude);
        }
        if (longitude != null) {
            postParams.put(ServiceConstants.LONGITUDE, longitude);
        }
        if (place != null) {
            postParams.put(ServiceConstants.ENTRY_LOCATION, place);
        }

        return postParams;
    }

    private static StringBuilder appendIfNotEmpty(StringBuilder builder, String s) {
        if (!TextUtils.isEmpty(s)) builder.append(s).append(" ");
        return builder;
    }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || ((Object)this).getClass() != o.getClass()) return false;

		Location location = (Location) o;

		if (city != null ? !city.equals(location.city) : location.city != null) return false;
		if (country != null ? !country.equals(location.country) : location.country != null) return false;
		if (latitude != null ? !latitude.equals(location.latitude) : location.latitude != null) return false;
		if (longitude != null ? !longitude.equals(location.longitude) : location.longitude != null) return false;
		if (place != null ? !place.equals(location.place) : location.place != null) return false;
		if (postalCode != null ? !postalCode.equals(location.postalCode) : location.postalCode != null) return false;
		if (province != null ? !province.equals(location.province) : location.province != null) return false;
		if (streetAddress != null ? !streetAddress.equals(location.streetAddress) : location.streetAddress != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = place != null ? place.hashCode() : 0;
		result = 31 * result + (streetAddress != null ? streetAddress.hashCode() : 0);
		result = 31 * result + (city != null ? city.hashCode() : 0);
		result = 31 * result + (province != null ? province.hashCode() : 0);
		result = 31 * result + (postalCode != null ? postalCode.hashCode() : 0);
		result = 31 * result + (country != null ? country.hashCode() : 0);
		result = 31 * result + (latitude != null ? latitude.hashCode() : 0);
		result = 31 * result + (longitude != null ? longitude.hashCode() : 0);
		return result;
	}

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        if (streetAddress != null) {
            buf.append("StreetAddress:").append(streetAddress);
        }
        if (city != null) {
            buf.append(" City:").append(city);
        }
        if (province != null) {
            buf.append(" Provice:").append(province);
        }
        if (postalCode != null) {
            buf.append(" PostalCode:").append(postalCode);
        }
        if (country != null) {
            buf.append(" Country:").append(country);
        }
        if (latitude != null) {
            buf.append(" Lat:").append(latitude);
        }
        if (longitude != null) {
            buf.append(" Lon:").append(longitude);
        }
        if (place != null) {
            buf.append(" place:").append(place);
        }

        return buf.toString();
    }

    /*
     * Getters and Setters
     */

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

}
