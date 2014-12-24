/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.datamodel;

import android.util.Log;
import com.nexuspad.service.dataservice.NPException;
import com.nexuspad.service.dataservice.ServiceConstants;
import com.nexuspad.service.util.Logs;
import org.json.JSONException;
import org.json.JSONObject;

public class EntryFactory {

    public static NPEntry getEntryObj(int moduleId, int entryType) {
        NPFolder folder = NPFolder.rootFolderOf(moduleId);

        switch (moduleId) {
            case ServiceConstants.CONTACT_MODULE:
                return new NPPerson(folder);

            case ServiceConstants.CALENDAR_MODULE:
                return new NPEvent(folder);

            case ServiceConstants.BOOKMARK_MODULE:
                return new NPBookmark(folder);

            case ServiceConstants.DOC_MODULE:
                return new NPDoc(folder);

            case ServiceConstants.UPLOAD_MODULE:
                return new NPUpload(folder);

            case ServiceConstants.PHOTO_MODULE:
                if (entryType == EntryTemplate.PHOTO.getIntValue()) {
                    return new NPPhoto(folder);
                } else if (entryType == EntryTemplate.ALBUM.getIntValue()) {
                    return new NPAlbum(folder);
                }
                throw new AssertionError("unexpected entryType: " + entryType);

            case ServiceConstants.JOURNAL_MODULE:
                return new NPJournal(folder);
            default:
                return new NPEntry(folder, EntryTemplate.NOT_ASSIGNED);
        }
    }

    public static NPEntry copyEntry(NPObject e) {
        if (e instanceof NPPerson) {
            return new NPPerson((NPPerson)e);

        } else if (e instanceof  NPEvent) {
            return new NPEvent((NPEvent)e);

        } else if (e instanceof NPJournal) {
            return new NPJournal((NPJournal)e);

        } else if (e instanceof NPPhoto) {
            Log.i("------------------", "----------");
            return new NPPhoto((NPPhoto)e);

        } else if (e instanceof NPAlbum) {
            return new NPAlbum((NPAlbum)e);

        } else if (e instanceof NPBookmark) {
            return new NPBookmark((NPBookmark)e);

        } else if (e instanceof NPDoc) {
            return new NPDoc((NPDoc)e);
        }

        throw new IllegalArgumentException("Entry type not supported: " + e.getClass().getSimpleName());
    }

    public static NPEntry jsonToEntry(JSONObject jsonObj) {
        if (!jsonObj.has(ServiceConstants.MODULE_ID)) {
            return null;
        }

        int moduleId = 0;
        try {
            moduleId = jsonObj.getInt(ServiceConstants.MODULE_ID);
        } catch (JSONException e) {
            Log.e("EntryFactory", "Error parsing the module ID: ", e);
        }

        if (moduleId > 0) {
            return EntryFactory.jsonToEntry(moduleId, jsonObj);
        }

        return null;
    }

    public static NPEntry jsonToEntry(int moduleId, JSONObject jsonObj) {
        EntryTemplate templateId = EntryTemplate.NOT_ASSIGNED;

        if (jsonObj.has(ServiceConstants.TEMPLATE_ID)) {
            try {
                templateId = EntryTemplate.fromInt(jsonObj.getInt(ServiceConstants.TEMPLATE_ID));

            } catch (JSONException e) {
                templateId = EntryTemplate.NOT_ASSIGNED;
            } catch (NumberFormatException nfe) {
                templateId = EntryTemplate.NOT_ASSIGNED;
            }
        }

        /*
         * The templateId returned from web service can be NOT_ASSIGNED, when the json response is from
         * entry delete action.
         *
         */

        switch (moduleId) {
            case ServiceConstants.CONTACT_MODULE:
                return new NPPerson(jsonObj);

            case ServiceConstants.CALENDAR_MODULE:
                return new NPEvent(jsonObj);

            case ServiceConstants.BOOKMARK_MODULE:
                return new NPBookmark(jsonObj);

            case ServiceConstants.DOC_MODULE:
                return new NPDoc(jsonObj);

            case ServiceConstants.UPLOAD_MODULE:
                return new NPUpload(jsonObj);

            case ServiceConstants.PHOTO_MODULE:
                if (templateId == EntryTemplate.PHOTO) {
                    try {
                        return new NPPhoto(jsonObj);
                    } catch (NPException e) {
                        Logs.e("EntryFactory", "Error creating Photo object.", e);
                        return null;
                    }

                } else if (templateId == EntryTemplate.ALBUM) {
                    try {
                        return new NPAlbum(jsonObj);
                    } catch (NPException e) {
                        Logs.e("EntryFactory", "Error creating Album object.", e);
                        return null;
                    }

                } else {
                    return new NPEntry(jsonObj, templateId);
                }

            case ServiceConstants.JOURNAL_MODULE:
                return new NPJournal(jsonObj);
        }

        return null;
    }
}
