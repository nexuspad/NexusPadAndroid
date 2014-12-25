/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.datamodel;

import android.os.Parcel;
import android.util.Log;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Ordering;
import com.nexuspad.service.dataservice.ServiceConstants;
import com.nexuspad.service.util.DateUtil;
import com.nexuspad.service.util.JsonHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.nullToEmpty;

public class NPEntry extends NPObject {
	public static final Creator<NPEntry> CREATOR = new Creator<NPEntry>() {
		@Override
		public NPEntry createFromParcel(Parcel in) {
			return new NPEntry(in);
		}

		@Override
		public NPEntry[] newArray(int size) {
			return new NPEntry[size];
		}
	};

	protected static final Comparator<String> CASE_INSENSITIVE_ORDERING = Ordering.from(String.CASE_INSENSITIVE_ORDER).nullsLast();

	/**
	 * Order by title
	 */
	public static final Comparator<NPEntry> ORDERING_BY_TITLE = new Comparator<NPEntry>() {
		@Override
		public int compare(NPEntry a, NPEntry o) {
			return CASE_INSENSITIVE_ORDERING.compare(a.title, o.title);
		}
	};

	public static final Comparator<NPEntry> ORDERING_BY_UPDATE_TIME = new Comparator<NPEntry>() {
		@Override
		public int compare(NPEntry a, NPEntry o) {
			return -1 * Ordering.natural().nullsLast().compare(a.getLastModifiedTime(), o.getLastModifiedTime());
		}
	};


	protected final EntryTemplate mTemplate;

	protected AccessEntitlement mAccessInfo;

	protected NPFolder folder;

	protected String entryId;

	/**
	 * Sync id is assigned to locally created entry first.
	 */
	protected String syncId = null;

	protected String title;

	protected String colorLabel;
	protected String tags;
	protected String note;

	protected Map<String, Object> featureValues = new HashMap<String, Object>();
	protected List<NPUpload> attachments = new ArrayList<NPUpload>();

	protected Date createTime;
	protected Date lastModifiedTime;

	protected Location location;
	protected String webAddress;

	protected int status;
	protected boolean synced;

	protected List<NPSharing> sharings = new ArrayList<NPSharing>();

	public NPEntry(EntryTemplate template) {
		mTemplate = template;
		location = new Location();
	}

	public NPEntry(NPFolder folder, EntryTemplate template) {
		this.folder = folder;
		mTemplate = template;
		location = new Location();
	}

	public NPEntry(NPEntry anEntry) {
		folder = anEntry.folder;
		mTemplate = anEntry.mTemplate;
		entryId = anEntry.entryId;

		title = anEntry.title;
		attachments = anEntry.attachments;

		tags = anEntry.tags;
		note = anEntry.note;

		createTime = anEntry.createTime;
		lastModifiedTime = anEntry.lastModifiedTime;

		location = anEntry.location;
		colorLabel = anEntry.colorLabel;
		webAddress = anEntry.webAddress;

		featureValues = anEntry.featureValues;

		mAccessInfo = anEntry.getAccessInfo();
	}

	public NPEntry(JSONObject jsonObj, EntryTemplate template) {
		mTemplate = template;
		try {
			folder = new NPFolder();
			NPUser owner = new NPUser();

			if (jsonObj.has(ServiceConstants.OWNER_ID)) {
				owner.setUserId(jsonObj.getInt(ServiceConstants.OWNER_ID));
			}

            /*
             * Access info
             */
			if (mAccessInfo == null) {
				mAccessInfo = new AccessEntitlement();
			}

			mAccessInfo.setOwner(owner);

			// Preserver the values in a local Map
			featureValues = JsonHelper.toMap(jsonObj);

			// Assign the class attributes
			if (jsonObj.has(ServiceConstants.MODULE_ID)) {
				folder.setModuleId(jsonObj.getInt(ServiceConstants.MODULE_ID));
			}

			if (jsonObj.has(ServiceConstants.FOLDER_ID)) {
				folder.setFolderId(jsonObj.getInt(ServiceConstants.FOLDER_ID));
			}

			if (jsonObj.has(ServiceConstants.FOLDER_NAME)) {
				folder.setFolderName(jsonObj.getString(ServiceConstants.FOLDER_NAME));
			}

			entryId = jsonObj.getString(ServiceConstants.ENTRY_ID);

			if (jsonObj.has(ServiceConstants.SYNC_ID)) {
				syncId = jsonObj.getString(ServiceConstants.SYNC_ID);
			}

			if (jsonObj.has(ServiceConstants.TITLE)) {
				title = jsonObj.getString(ServiceConstants.TITLE);
			}

			// Get the location
			location = new Location(jsonObj);

			// Get the create date
			if (jsonObj.has(ServiceConstants.ENTRY_CREATE_TS)) {
				createTime = DateUtil.dateFromTimestampInSeconds(jsonObj.getLong(ServiceConstants.ENTRY_CREATE_TS));
			}

			// Get the last modified time
			if (jsonObj.has(ServiceConstants.ENTRY_MODIFIED_TS)) {
				lastModifiedTime = DateUtil.dateFromTimestampInSeconds(jsonObj.getLong(ServiceConstants.ENTRY_MODIFIED_TS));
			}

			// Get the color label
			if (jsonObj.has(ServiceConstants.COLOR_LABEL)) {
				colorLabel = jsonObj.getString(ServiceConstants.COLOR_LABEL);
			}

			// Get the web address
			if (jsonObj.has(ServiceConstants.ENTRY_WEB_ADDRESS)) {
				webAddress = jsonObj.getString(ServiceConstants.ENTRY_WEB_ADDRESS);
			}

			// Get the tags
			if (jsonObj.has(ServiceConstants.ENTRY_TAGS)) {
				tags = jsonObj.getString(ServiceConstants.ENTRY_TAGS);
			}

			// Get the notes
			if (jsonObj.has(ServiceConstants.ENTRY_NOTE)) {
				note = jsonObj.getString(ServiceConstants.ENTRY_NOTE);
			}

			// Get the attachments
			if (jsonObj.has(ServiceConstants.ENTRY_ATTACHMENTS)) {
				attachments = new ArrayList<NPUpload>();
				JSONArray attachmentArray = new JSONArray(jsonObj.getString(ServiceConstants.ENTRY_ATTACHMENTS));
				for (int i = 0; i < attachmentArray.length(); i++) {
					NPUpload upload = new NPUpload(attachmentArray.getJSONObject(i));

					// Update the parent information
					upload.setParentEntryModule(folder.getModuleId());
					upload.setParentEntryFolder(folder.getFolderId());
					upload.setParentEntryId(entryId);

					attachments.add(upload);
				}
			}

			// Status
			if (jsonObj.has(ServiceConstants.ITEM_STATUS)) {
				status = jsonObj.getInt(ServiceConstants.ITEM_STATUS);
			}

		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	protected NPEntry(Parcel in) {
		mAccessInfo = in.readParcelable(AccessEntitlement.class.getClassLoader());
		folder = in.readParcelable(NPFolder.class.getClassLoader());
		mTemplate = (EntryTemplate) in.readSerializable();
		entryId = in.readString();
		title = in.readString();
		colorLabel = in.readString();
		tags = in.readString();
		note = in.readString();

		attachments = new ArrayList<NPUpload>();
		in.readList(attachments, NPUpload.class.getClassLoader());

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(in.readLong());
		createTime = calendar.getTime();

		location = in.readParcelable(Location.class.getClassLoader());
		webAddress = in.readString();

		sharings = new ArrayList<NPSharing>();
		in.readList(sharings, NPSharing.class.getClassLoader());
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(mAccessInfo, 0);
		dest.writeParcelable(folder, 0);
		dest.writeSerializable(mTemplate);
		dest.writeString(entryId);
		dest.writeString(title);
		dest.writeString(colorLabel);
		dest.writeString(tags);
		dest.writeString(note);
		dest.writeList(attachments);
		dest.writeLong(createTime == null ? 0 : createTime.getTime());
		dest.writeParcelable(location, 0);
		dest.writeString(webAddress);
		dest.writeList(sharings);
	}

	/**
	 * @return a {@link com.google.common.base.Predicate} that matches the {@code entryId}, {@code mTemplate}, and {@code folder}
	 */
	public final Predicate<NPEntry> filterById() {
		return new Predicate<NPEntry>() {
			@Override
			public boolean apply(NPEntry o) {
				if (o == null) return false;

				if (entryId == null ? o.entryId != null : !entryId.equals(o.entryId)) return false;
				if (getModuleId() != o.getModuleId()) return false;

				return true;
			}
		};
	}

	public boolean filterByPattern(Pattern pattern) {
		return pattern.matcher(nullToEmpty(getTitle())).matches() ||
				pattern.matcher(nullToEmpty(getNote())).matches() ||
				pattern.matcher(nullToEmpty(getTags())).matches() ||
				pattern.matcher(nullToEmpty(getWebAddress())).matches();
	}

	public int getModuleId() {
		return folder.getModuleId();
	}

	public int getOwnerId() {
		if (mAccessInfo == null) {
			return 0;
		} else {
			if (mAccessInfo.getOwner() == null) {
				return 0;
			} else {
				return mAccessInfo.getOwner().getUserId();
			}
		}
	}

	public boolean isNewEntry() {
		if (entryId == null || entryId.startsWith("_")) {
			return true;
		}
		return false;
	}

	public String getKeywordFilter() {
		return "";
	}

	public Date getTimeFilter() {
		return lastModifiedTime;
	}

	public String getFeatureValue(String key) {
		if (featureValues.containsKey(key)) {
			return featureValues.get(key).toString();
		} else {
			return "";
		}
	}

	public void setFeatureValue(String key, String value) {
		featureValues.put(key, nullToEmpty(value));
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.omitNullValues()
				.add("\nstatus", status)
				.add("\naccessInfo", mAccessInfo)
				.add("\nfolder", folder)
				.add("\nmTemplate", mTemplate)
				.add("\nentryId", entryId)
				.add("\nsyncId", syncId)
				.add("\ntitle", title)
				.add("\ncolorLabel", colorLabel)
				.add("\ntags", tags)
				.add("\nnote", note)
				.add("\nfeatureValues", featureValues)
				.add("\nattachments", attachments)
				.add("\ncreateTime", createTime)
				.add("\nlocation", location)
				.add("\nwebAddress", webAddress)
				.add("\nsharings", sharings)
				.toString();
	}

	/**
	 * Classes overriding the method must ensure that no values are {@code null}.
	 * Use null-check or {@code nullToEmpty} as needed.
	 *
	 * @return
	 */
	public Map<String, String> toMap() {
		Map<String, String> postParams = new HashMap<String, String>();

		postParams.put(ServiceConstants.MODULE_ID, String.valueOf(folder.getModuleId()));
		postParams.put(ServiceConstants.FOLDER_ID, String.valueOf(folder.getFolderId()));

		if (mTemplate != null) {
			postParams.put(ServiceConstants.TEMPLATE_ID, String.valueOf(mTemplate.getIntValue()));
		}

		if (entryId != null) {
			postParams.put(ServiceConstants.ENTRY_ID, entryId);
		}

		if (title != null) {
			postParams.put(ServiceConstants.TITLE, title);
		}

		if (colorLabel != null) {
			postParams.put(ServiceConstants.COLOR_LABEL, colorLabel);
		}

		if (note != null) {
			postParams.put(ServiceConstants.ENTRY_NOTE, note);
		}

		if (tags != null) {
			postParams.put(ServiceConstants.ENTRY_TAGS, tags);
		}

		if (webAddress != null) {
			postParams.put(ServiceConstants.ENTRY_WEB_ADDRESS, webAddress);
		}

		if (location != null) {
			postParams.putAll(location.toMap());
		}

		if (syncId != null) {
			postParams.put(ServiceConstants.SYNC_ID, syncId);
		}

		return postParams;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (o == null || getClass() != o.getClass()) return false;

		NPEntry npEntry = (NPEntry) o;

		if (entryId != null ? !entryId.equals(npEntry.entryId) : npEntry.entryId != null) return false;
		if (mTemplate != npEntry.mTemplate) return false;
		if (webAddress != null ? !webAddress.equals(npEntry.webAddress) : npEntry.webAddress != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = mTemplate != null ? mTemplate.hashCode() : 0;
		result = 31 * result + (entryId != null ? entryId.hashCode() : 0);
		result = 31 * result + (webAddress != null ? webAddress.hashCode() : 0);
		return result;
	}

	/**
	 * This method print out the name/values pairs that are posted to web
	 * service.
	 */
	public void debugMap() {
		Map<String, String> postParams = toMap();
		StringBuilder buf = new StringBuilder();
		buf.append("\n================ Debug the post param map ===============\n");
		for (Map.Entry<String, String> item : postParams.entrySet()) {
			buf.append(item.getKey()).append(":").append(item.getValue()).append("\n");
		}
		Log.d("NPEntry", buf.toString());
	}

    /*
     * Getters and Setters
     */

	public Map<String, Object> getFeatureValues() {
		return featureValues;
	}

	public void setFeatureValues(Map<String, Object> featureValues) {
		this.featureValues = featureValues;
	}

	public List<NPUpload> getAttachments() {
		return attachments;
	}

	public void addAttachments(Collection<? extends NPUpload> attachments) {
		this.attachments.addAll(attachments);
	}

	public void addAttachment(NPUpload att) {
		attachments.add(att);
	}

	public Location getLocation() {
		return location;
	}

	/**
	 * @param location not null
	 */
	public void setLocation(Location location) {
		this.location = location;
	}

	public String getColorLabel() {
		return colorLabel;
	}

	public void setColorLabel(String colorLabel) {
		this.colorLabel = colorLabel;
	}

	public String getWebAddress() {
		return webAddress;
	}

	public void setWebAddress(String webAddress) {
		this.webAddress = webAddress;
	}

	public AccessEntitlement getAccessInfo() {
		return mAccessInfo;
	}

	public void setAccessInfo(AccessEntitlement accessInfo) {
		this.mAccessInfo = accessInfo;
	}

	public NPFolder getFolder() {
		return folder;
	}

	public void setFolder(NPFolder folder) {
		this.folder = folder;
	}

	public EntryTemplate getTemplate() {
		return mTemplate;
	}

	public String getEntryId() {
		return entryId;
	}

	public void setEntryId(String entryId) {
		this.entryId = entryId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public void setOwner(NPUser owner) {
		if (mAccessInfo == null) {
			mAccessInfo = new AccessEntitlement();
		}
		mAccessInfo.setOwner(owner);
	}

	public HashMap<String, String> toHashMap() {
		return null;
	}

	public List<NPSharing> getSharings() {
		return sharings;
	}

	public void addSharing(NPSharing sharing) {
		if (sharings == null) {
			sharings = new ArrayList<NPSharing>();
		}
		sharings.add(sharing);
	}

	public void setSharings(List<NPSharing> sharings) {
		this.sharings = sharings;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Date getLastModifiedTime() {
		return lastModifiedTime;
	}

	public void setLastModifiedTime(Date modifiedTime) {
		this.lastModifiedTime = modifiedTime;
	}

	public boolean isSynced() {
		return synced;
	}

	public void setSynced(boolean synced) {
		this.synced = synced;
	}

	public String getSyncId() {
		return syncId;
	}

	public void setSyncId(String syncId) {
		this.syncId = syncId;
	}
}
