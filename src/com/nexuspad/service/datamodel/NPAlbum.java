/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.datamodel;

import android.os.Parcel;
import android.util.Log;
import com.google.common.base.Objects;
import com.nexuspad.service.dataservice.ErrorCode;
import com.nexuspad.service.dataservice.NPException;
import com.nexuspad.service.dataservice.ServiceConstants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NPAlbum extends NPEntry {
	public static Creator<NPAlbum> CREATOR = new Creator<NPAlbum>() {
		@Override
		public NPAlbum createFromParcel(Parcel source) {
			return new NPAlbum(source);
		}

		@Override
		public NPAlbum[] newArray(int size) {
			return new NPAlbum[size];
		}
	};

	/**
	 * Image URL for gallery display *
	 */
	private String tnUrl;
	private List<NPPhoto> photos;


	private NPAlbum(Parcel p) {
		super(p);
		tnUrl = p.readString();
		photos = new ArrayList<NPPhoto>();
		p.readList(photos, NPPhoto.class.getClassLoader());
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(tnUrl);
		dest.writeList(photos);
	}

	public NPAlbum(NPAlbum album) {
		super(album);
		tnUrl = album.tnUrl;
	}

	public NPAlbum(NPFolder folder) {
		super(folder, EntryTemplate.ALBUM);
	}

	public NPAlbum(JSONObject jsonObj) throws NPException {
		super(jsonObj, EntryTemplate.ALBUM);

		try {
			tnUrl = jsonObj.getString(ServiceConstants.UPLOAD_TN_URL);

	        /*
	         * parse "photos"
	         */
			photos = new ArrayList<NPPhoto>();
			if (jsonObj.has("photos")) {
				JSONArray photosJsonArr = jsonObj.getJSONArray("photos");
				for (int i = 0; i < photosJsonArr.length(); i++) {
					NPPhoto p = new NPPhoto(photosJsonArr.getJSONObject(i));
					photos.add(p);
				}
			}

			if (jsonObj.has("attachments")) {
				JSONArray photosJsonArr = jsonObj.getJSONArray("attachments");
				for (int i = 0; i < photosJsonArr.length(); i++) {
					NPPhoto p = new NPPhoto(photosJsonArr.getJSONObject(i));
					photos.add(p);
				}
			}

		} catch (JSONException e) {
			Log.i("NPAlbum", e.toString());
			throw new NPException(ErrorCode.ENTRY_MISSING_DATA, "Album missing " + ServiceConstants.UPLOAD_TN_URL);
		}
	}

	public String getTnUrl() {
		return tnUrl;
	}

	public void setTnUrl(String tnUrl) {
		this.tnUrl = tnUrl;
	}

	public List<NPPhoto> getPhotos() {
		return photos;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.omitNullValues()
				.addValue(super.toString())
				.add(" album tnUrl", tnUrl)
				.toString();
	}
}
