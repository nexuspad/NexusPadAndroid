package com.nexuspad.service.dataservice;

import android.util.SparseArray;

public enum ErrorCode {

    INTERNAL_ERROR(9999),
    UNKNOWN_ERROR(9998),

    // -- Permission errors
    INVALID_PERMISSION(401),
    NOT_AUTHENTICATED(403),
    ITEM_NOT_EXIST(404),
    INTERNAL_SERVER_ERROR(500),

    // -- Error code 1xxx are for account related errors
    INVALID_USER_TOKEN(1001),
    INVALID_LOGIN(1005),
    NOT_LOGGED_IN(1009),

    FAILED_REGISTRATION(1010),
    FAILED_REGISTRATION_ACCT_EXISTS(1011),

    FAILED_DELETE_ACCOUNT(1015),

    LOGIN_NO_USER(1021),
    LOGIN_ACCT_PROBLEM(1022),
    LOGIN_FAILED(1023),

    USER_DOES_NOT_EXIST(1040),

    INVALID_PAD_DB(1500),

    // -- Error code 2xxx are for general entry issues
    ENTRY_NOT_FOUND(2001),
    ENTRY_REMOVED(2002),
    ENTRY_INACTIVE(2003),
    ENTRY_NO_READ_PERMISSION(2005),
    ENTRY_NO_WRITE_PERMISSION(2006),
    ENTRY_NO_PUBLIC_READ(2015),
    ENTRY_NO_ADD_PERMISSION(2007),
    ENTRY_SUBMISSION_FAILED(2010),
    ENTRY_SUBMISSION_VALIDATION_FAILED(2011),
    ENTRY_DELETION_FAILED(2012),

    ENTRY_MISSING_DATA(2014),

    FOLDER_NOT_FOUND(2021),
    FOLDER_NO_READ_PERMISSION(2025),
    FOLDER_NO_WRITE_PERMISSION(2026),
    FOLDER_NO_ADD_PERMISSION(2027),
    FOLDER_SUBMISSION_FAILED(2030),
    FOLDER_UPDATE_FAILED(2031),
    FOLDER_DELETION_FAILED(2040),
    FOLDER_NO_PUBLIC_READ(2045),

    MODULE_NO_PUBLIC_ACCESS(2055),

    MISSING_PARAM(2080),
    HTML_NAME_TAKEN(2090),

    // -- Operational error
    NEED_CONFIRMATION(3010),

    // -- Connection error
    CONNECTION_TIMEOUT(4010);

    private static final SparseArray<ErrorCode> intToErrorCode = new SparseArray<ErrorCode>();

    static {
        for (ErrorCode type : ErrorCode.values()) {
            intToErrorCode.put(type.value, type);
        }
    }

    private int value;

    private ErrorCode(int value) {
        this.value = value;
    }

    public int getIntValue() {
        return value;
    }

    public static ErrorCode fromInt(int i) {
        ErrorCode type = intToErrorCode.get(i);
        if (type == null) {
            return ErrorCode.UNKNOWN_ERROR;
        }
        return type;
    }
}
