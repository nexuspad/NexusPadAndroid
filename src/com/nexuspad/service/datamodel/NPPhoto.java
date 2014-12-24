/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.datamodel;

import android.os.Parcel;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.nexuspad.service.dataservice.ErrorCode;
import com.nexuspad.service.dataservice.NPException;
import com.nexuspad.service.dataservice.ServiceConstants;
import org.json.JSONException;
import org.json.JSONObject;

public class NPPhoto extends NPEntry {
    public static final Creator<NPPhoto> CREATOR = new Creator<NPPhoto>() {

        @Override
        public NPPhoto createFromParcel(Parcel source) {
            return new NPPhoto(source);
        }

        @Override
        public NPPhoto[] newArray(int size) {
            return new NPPhoto[size];
        }
    };

    /**
     * Image URL for gallery display *
     */
    private String tnUrl;

    /**
     * Image URL for fullscreen display *
     */
    private String photoUrl;

    /**
     * The image for this photo *
     */
    private NPUpload uploadImage;

    private NPPhoto(Parcel in) {
        super(in);
        tnUrl = in.readString();
        photoUrl = in.readString();
        uploadImage = in.readParcelable(NPUpload.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel p, int flags) {
        super.writeToParcel(p, flags);
        p.writeString(tnUrl);
        p.writeString(photoUrl);
        p.writeParcelable(uploadImage, 0);
    }

    public NPPhoto(NPPhoto other) {
        super(other);

        tnUrl = other.tnUrl;
        photoUrl = other.photoUrl;
        uploadImage = new NPUpload(other.getUploadImage());
    }

    public NPPhoto(NPEntry entry) {
        super(entry);
    }

    public static NPPhoto fromEntry(NPEntry entry) {
        if (entry instanceof NPPhoto) {
            return (NPPhoto)entry;
        }
        return new NPPhoto(entry);
    }

    /**
     * Converts a {@link NPUpload} to a {@link com.nexuspad.service.datamodel.NPPhoto}
     *
     * @param upload the source
     */
    public NPPhoto(NPUpload upload) {
        this(upload.getFolder());
        entryId = upload.entryId;
        photoUrl = upload.getLightBoxUrl();
        tnUrl = upload.getTnUrl();
        uploadImage = upload;
    }

    public NPPhoto(NPFolder folder) {
        super(folder, EntryTemplate.PHOTO);
    }

    public NPPhoto(JSONObject jsonObj) throws NPException {
        super(jsonObj, EntryTemplate.PHOTO);

        if (uploadImage == null) {
            uploadImage = new NPUpload();
        }

        try {
            tnUrl = jsonObj.getString(ServiceConstants.UPLOAD_TN_URL);
            uploadImage.setTnUrl(tnUrl);

        } catch (JSONException e) {
            throw new NPException(ErrorCode.ENTRY_MISSING_DATA, "Photo missing " + ServiceConstants.UPLOAD_TN_URL);
        }

        try {
            photoUrl = jsonObj.getString(ServiceConstants.UPLOAD_LIGHTBOX_URL);
            uploadImage.setLightBoxUrl(photoUrl);

        } catch (JSONException e) {
            throw new NPException(ErrorCode.ENTRY_MISSING_DATA, "Photo missing " + ServiceConstants.UPLOAD_LIGHTBOX_URL);
        }

        uploadImage.setParentEntryId(entryId);
    }

    public final Predicate<NPEntry> filterByPhotoId() {
        return new Predicate<NPEntry>() {
            @Override
            public boolean apply(NPEntry o) {
                if (o == null) return false;
                if (!(o instanceof NPPhoto)) return false;
                final NPPhoto photo = (NPPhoto) o;

                if (uploadImage.entryId == null ? photo.uploadImage.entryId != null : !uploadImage.entryId.equals(photo.uploadImage.entryId))
                    return false;
                if (folder == null ? o.folder != null : !folder.filterById().apply(o.folder)) return false;

                return true;
            }
        };
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .omitNullValues()
                .addValue(super.toString())
                .add("tnUrl", tnUrl)
                .add("lightboxUrl", photoUrl)
                .toString();
    }

    public String getTnUrl() {
        return tnUrl;
    }

    public void setTnUrl(String tnUrl) {
        this.tnUrl = tnUrl;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public NPUpload getUploadImage() {
        return uploadImage;
    }

    public void setUploadImage(NPUpload uploadImage) {
        this.uploadImage = uploadImage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NPPhoto)) return false;
        if (!super.equals(o)) return false;

        NPPhoto photo = (NPPhoto) o;

        if (photoUrl != null ? !photoUrl.equals(photo.photoUrl) : photo.photoUrl != null) return false;
        if (tnUrl != null ? !tnUrl.equals(photo.tnUrl) : photo.tnUrl != null) return false;
        if (uploadImage != null ? !uploadImage.equals(photo.uploadImage) : photo.uploadImage != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (tnUrl != null ? tnUrl.hashCode() : 0);
        result = 31 * result + (photoUrl != null ? photoUrl.hashCode() : 0);
        result = 31 * result + (uploadImage != null ? uploadImage.hashCode() : 0);
        return result;
    }
}
