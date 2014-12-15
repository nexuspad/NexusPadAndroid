/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.datamodel;

import com.google.common.collect.Iterables;
import com.nexuspad.service.dataservice.ServiceConstants;
import com.nexuspad.service.util.Logs;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EntryList <T extends NPEntry> implements Serializable {
    /**
     * Serializable UID
     */
    private static final long serialVersionUID = 6221375555744355157L;

    private AccessEntitlement accessInfo;

    private NPFolder folder = new NPFolder();

    private EntryTemplate mEntryTemplate;
    private String startYmd;
    private String endYmd;

    private String keyword;

    private int totalCount;
    private int countPerPage;
    private int pageId;

	private boolean entryUpdated;

    private List<T> entries = new ArrayList<T>();

    public void initWithJSONObject(JSONObject data) {
        try {
            JSONObject summaryObject = data.getJSONObject(ServiceConstants.LIST_SUMMARY);

            /*
             * The summary
             */
            if (summaryObject.has(ServiceConstants.TEMPLATE_ID)) {
                String tplIdStr = summaryObject.getString(ServiceConstants.TEMPLATE_ID);
                try {
                    mEntryTemplate = EntryTemplate.fromInt(Integer.parseInt(tplIdStr));
                } catch (NumberFormatException nfe) {
                    mEntryTemplate = EntryTemplate.NOT_ASSIGNED;
                }
            }

            setPageId(summaryObject.getInt(ServiceConstants.LIST_PAGE_ID));
            setTotalCount(summaryObject.getInt(ServiceConstants.LIST_TOTAL_COUNT));
            setCountPerPage(summaryObject.getInt(ServiceConstants.LIST_COUNT_PER_PAGE));

	        if (summaryObject.has(ServiceConstants.LIST_START_DATE)) {
                setStartYmd(summaryObject.getString(ServiceConstants.LIST_START_DATE));
            }

            if (summaryObject.has(ServiceConstants.LIST_END_DATE)) {
                setEndYmd(summaryObject.getString(ServiceConstants.LIST_END_DATE));
            }

            if (summaryObject.has(ServiceConstants.SEARCH_KEYWORD)) {
                keyword = summaryObject.getString(ServiceConstants.SEARCH_KEYWORD);
            }

            /*
             * The access info
             */
            accessInfo = new AccessEntitlement();
            if (data.has(ServiceConstants.ACCESS_INFO)) {
                JSONObject accessPart = data.getJSONObject(ServiceConstants.ACCESS_INFO);
                int ownerId = accessPart.getInt(ServiceConstants.OWNER_ID);
                int viewerId = accessPart.getInt(ServiceConstants.VIEWER_ID);

                accessInfo.setOwner(new NPUser(ownerId));
                accessInfo.setViewer(new NPUser(viewerId));

                if (accessPart.has(ServiceConstants.READ) && accessPart.getInt(ServiceConstants.READ) == 1) {
                    accessInfo.setRead(true);
                }

                if (accessPart.has(ServiceConstants.WRITE) && accessPart.getInt(ServiceConstants.WRITE) == 1) {
                    accessInfo.setWrite(true);
                }
            }

            folder.setAccessInfo(accessInfo);

            /*
             * The folders
             */
            if (data.has(ServiceConstants.LIST_SUB_FOLDERS)) {
                JSONArray foldersArray = data.getJSONArray(ServiceConstants.LIST_SUB_FOLDERS);
                int folderCount = foldersArray.length();
                for (int i = 0; i < folderCount; i++) {
                    JSONObject folderJsonObj = (JSONObject) foldersArray.get(i);
                    NPFolder aFolder = new NPFolder(folderJsonObj);
                    aFolder.setAccessInfo(accessInfo);
                    folder.addSubFolder(aFolder);
                }
            }

            /*
             * The entries.
             */
            JSONArray entriesArray = data.getJSONArray(ServiceConstants.LIST_ENTRIES);

            int entryCount = entriesArray.length();
            for (int i = 0; i < entryCount; i++) {
                JSONObject entryJsonObj = (JSONObject) entriesArray.get(i);

                NPEntry e = EntryFactory.jsonToEntry(entryJsonObj);

                if (e == null) {        // We don't like this one
                    continue;
                }

                // Assign the list access info if entry has none
                if (e.getOwnerId() == 0) {
                    e.setAccessInfo(accessInfo);
                }

                if (e.mTemplate == EntryTemplate.EVENT) {
                    List<NPEvent> events = NPEvent.splitMultiDayEvent((NPEvent) e);
	                for (NPEvent event : events) {
		                entries.add((T) event);
	                }

                } else {
                    entries.add((T) e);
                }
            }

        } catch (JSONException je) {
            Logs.e("EntryList", "Error building EntryList object using the dictionary..." + je.toString());
        }
    }

	public boolean isEmpty() {
		int itemsCount = entries.size() + (folder.getSubFolders() == null ? 0 : folder.getSubFolders().size());
		if (itemsCount == 0) {
			return true;
		}
		return false;
	}

	public boolean hasMoreEntriesToLoad() {
		if (totalCount > entries.size()) return true;
		return false;
	}

	public boolean removeEntryFromList(NPEntry e) {
		if (Iterables.removeIf(entries, e.filterById())) {
			totalCount--;
			return true;
		}
		return false;
	}

	public void addOrUpdateEntry(NPEntry e) {
		if (!Iterables.tryFind(entries, e.filterById()).isPresent()) {
			if (entries.size() == 0) {
				((List<NPEntry>)entries).add(e);
			} else {
				((List<NPEntry>)entries).add(0, e);
			}

			totalCount++;

		} else {
			List<T> updatedEntries = new ArrayList<T>();

			for (NPEntry anEntry : entries) {
				if (anEntry.getEntryId().equals(e.getEntryId()) && anEntry.getModuleId() == e.getModuleId()) {
					if ((e.getModuleId() == NPModule.CONTACT || e.getModuleId() == NPModule.PHOTO) ||
							(anEntry.getFolder().getFolderId() == e.getFolder().getFolderId())) {
						updatedEntries.add((T)e);
					}
				} else {
					updatedEntries.add((T)anEntry);
				}
			}

			entries = updatedEntries;
		}
	}

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("ModuleId:").append(folder.getModuleId()).append(" FolderId:").append(folder.getFolderId()).
                append(" Type:").append(mEntryTemplate).append(" Page:").append(pageId).append(" Total:").append(totalCount).append(" CCP:").append(countPerPage).
                append("\n");

        if (folder.getSubFolders() != null) {
            for (NPFolder f : folder.getSubFolders()) {
                buf.append(f).append("\n");
            }
        }

        for (NPEntry e : entries) {
            buf.append(e).append("\n");
        }

        return buf.toString();
    }

    /*
     * Setters and Getters
     */
    public NPFolder getFolder() {
        return folder;
    }

    public void setFolder(NPFolder folder) {
        this.folder = folder;
    }

    public EntryTemplate getEntryTemplate() {
        return mEntryTemplate;
    }

    public void setEntryTemplate(EntryTemplate entryTemplate) {
        this.mEntryTemplate = entryTemplate;
    }

    public String getStartYmd() {
        return startYmd;
    }

    public void setStartYmd(String startYmd) {
        this.startYmd = startYmd;
    }

    public String getEndYmd() {
        return endYmd;
    }

    public void setEndYmd(String endYmd) {
        this.endYmd = endYmd;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getCountPerPage() {
        return countPerPage;
    }

    public void setCountPerPage(int countPerPage) {
        this.countPerPage = countPerPage;
    }

    public int getPageId() {
        return pageId;
    }

    public void setPageId(int pageId) {
        this.pageId = pageId;
    }

    public List<T> getEntries() {
        return entries;
    }

    public void setEntries(ArrayList<T> entries) {
        this.entries = entries;
    }

    public AccessEntitlement getAccessInfo() {
        return accessInfo;
    }

    public void setAccessInfo(AccessEntitlement accessInfo) {
        this.accessInfo = accessInfo;
    }

	public boolean isEntryUpdated() {
		return entryUpdated;
	}

	public void setEntryUpdated(boolean entryUpdated) {
		this.entryUpdated = entryUpdated;
	}

}
