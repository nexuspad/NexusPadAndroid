package com.nexuspad.service.datamodel;

import android.util.SparseArray;

/**
 * @author ren
 *         <p/>
 *         EntryTemplate defines the types of the entries. For example, Photo
 *         module can have two types
 *         of entriess: photo and albums.
 */
public enum EntryTemplate {

    NOT_ASSIGNED(0),

    CONTACT(101),

    EVENT(201),
    TASK(204),

    BOOKMARK(301),

    NOTE(401),
    DOC(403),

    UPLOAD(501),

    PHOTO(601),
    ALBUM(602);

    private static final SparseArray<EntryTemplate> intToEntryTemplate = createIntToEntryTemplate();

    private static SparseArray<EntryTemplate> createIntToEntryTemplate() {
        final EntryTemplate[] values = EntryTemplate.values();
        final SparseArray<EntryTemplate> array = new SparseArray<EntryTemplate>(values.length);
        for (EntryTemplate type : values) {
            array.put(type.value, type);
        }
        return array;
    }

    private final int value;

    private EntryTemplate(int value) {
        this.value = value;
    }

    public static EntryTemplate fromInt(int i) {
        EntryTemplate type = intToEntryTemplate.get(i);
        if (type == null) {
            return EntryTemplate.NOT_ASSIGNED;
        }
        return type;
    }

    public int getIntValue() {
        return value;
    }
}
