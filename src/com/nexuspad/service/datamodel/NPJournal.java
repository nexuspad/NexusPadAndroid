/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.datamodel;

import android.os.Parcel;
import com.nexuspad.service.dataservice.ServiceConstants;
import com.nexuspad.service.util.DateUtil;
import org.json.JSONObject;

import java.util.Map;

public class NPJournal extends NPEntry {
	private String journalYmd;

    @SuppressWarnings("hiding")
    public static final Creator<NPJournal> CREATOR = new Creator<NPJournal>() {
        @Override
        public NPJournal createFromParcel(Parcel source) {
            return new NPJournal(source);
        }

        @Override
        public NPJournal[] newArray(int size) {
            return new NPJournal[size];
        }
    };

    private NPJournal(Parcel p) {
        super(p);
        folder = NPFolder.rootFolderOf(NPModule.JOURNAL);
    }

    public NPJournal() {
        super(EntryTemplate.JOURNAL);
        folder = NPFolder.rootFolderOf(NPModule.JOURNAL);
    }

    public NPJournal(NPJournal other) {
        super(other);
        journalYmd = other.journalYmd;
    }

    public NPJournal(NPFolder folder) {
        super(folder, EntryTemplate.JOURNAL);
    }

    public NPJournal(JSONObject jsonObj) {
        super(jsonObj, EntryTemplate.JOURNAL);
        folder = NPFolder.rootFolderOf(NPModule.JOURNAL);
        journalYmd = this.getFeatureValue(ServiceConstants.JOURNAL_DATE);
    }

    public NPJournal(NPEntry anEntry) {
        super(anEntry);
        folder = NPFolder.rootFolderOf(NPModule.JOURNAL);
        journalYmd = this.getFeatureValue(ServiceConstants.JOURNAL_DATE);
    }

	public static NPJournal fromEntry(NPEntry entry) {
		if (entry instanceof NPJournal) {
			return (NPJournal)entry;
		}
		return new NPJournal(entry);
	}

    @Override
    public String toString() {
        return "journal_date:" + journalYmd + "\n" + super.toString();
    }

    @Override
    public Map<String, String> toMap() {
        Map<String, String> postParams = super.toMap();
        postParams.put(ServiceConstants.JOURNAL_DATE, DateUtil.convertToYYYYMMDD(createTime));
        return postParams;
    }

	public void setJournalYmd(String ymd) {
		journalYmd = ymd;
	}

    public String getJournalYmd() {
	    if (journalYmd == null) {
		    return DateUtil.convertToYYYYMMDD(createTime);
	    }
	    return journalYmd;
    }
}
