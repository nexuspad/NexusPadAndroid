/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.datamodel;

public class NPModule {

    /**
     * module ids *
     */
    public static final int CONTACT = 1;
    public static final int CALENDAR = 2;
    public static final int BOOKMARK = 3;
    public static final int DOC = 4;
    public static final int UPLOAD = 5;
    public static final int PHOTO = 6;

    public static String getModuleCode(int moduleId) {
        switch (moduleId) {
            case 1:
                return "contact";
            case 2:
                return "calendar";
            case 3:
                return "bookmark";
            case 4:
                return "doc";
            case 5:
                return "upload";
            case 6:
                return "photo";
        }
        return "";
    }
}
