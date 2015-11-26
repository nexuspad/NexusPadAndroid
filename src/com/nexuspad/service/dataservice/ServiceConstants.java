/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.dataservice;

public abstract class ServiceConstants {
    private ServiceConstants() {
        throw new AssertionError("nice try");
    }

    public static final String NP_ENV = "";
    public static final boolean NP_SSL = false;

    public static final String NP_UUID = "uuid";
    public static final String NP_UTOKEN = "utoken";

    public static final int NP_SERVICE_NOT_AVAILABLE = 100;
    public static final String NP_SERVICE_200 = "200";
    public static final String NP_SERVICE_403 = "403";

    public static final String NP_RESPONSE_STATUS = "status";
    public static final String NP_RESPONSE_CODE = "code";
    public static final String NP_RESPONSE_DATA = "data";
    public static final String NP_RESPONSE_MESSAGE = "message";

    // Account
    public static final String ACCT_LOGIN = "login";
    public static final String ACCT_USER_NAME = "user_name";
    public static final String ACCT_PASSWORD = "password";
    public static final String ACCT_EMAIL = "email";

    public static final String ACCT_USER_ID = "user_id";
    public static final String ACCT_SESSION_ID = "session_id";
    public static final String ACCT_PAD_HOST = "padhost";

	public static final String ACCT_PROFILE_IMAGE_URL = "profile_photo_url";
    public static final String ACCT_SPACE_ALLOCATION = "space_allocation";
    public static final String ACCT_SPACE_USAGE = "space_usage";
	public static final String ACCT_SPACE_ALLOCATION_FORMATTED = "space_allocation_formatted";
	public static final String ACCT_SPACE_USAGE_FORMATTED = "space_usage_formatted";

    // General
    public static final int CONTACT_MODULE = 1;
    public static final int CALENDAR_MODULE = 2;
    public static final int BOOKMARK_MODULE = 3;
    public static final int DOC_MODULE = 4;
    public static final int UPLOAD_MODULE = 5;
    public static final int PHOTO_MODULE = 6;
    public static final int JOURNAL_MODULE = 7;

    public static final int ROOT_FOLDER = 0;

    public static final String ITEM_STATUS = "status";
    public static final int ITEM_ACTIVE = 0;
    public static final int ITEM_DELETED = 25;

    public static final String SYNC_ID = "sync_id";

    public static final String COLOR_LABEL = "color_label";


    // Access entitlement information
    public static final String ACCESS_INFO = "access_info";
    public static final String OWNER_ID = "owner_id";
    public static final String VIEWER_ID = "viewer_id";
    public static final String READ = "read";
    public static final String WRITE = "write";

    // Actions
    public static final String ACTION_NAME = "action_name";
    public static final String ACTION_SUCCESS = "success";
    public static final String ACTION_ERROR_CODE = "action_error_code";
    public static final String ACTION_RESULT_ENTRY = "entry";
    public static final String ACTION_RESULT_FOLDER = "folder";

    public static final String ACTION_ENTRY_DELETE = "delete_entry";
    public static final String ACTION_ENTRY_UPDATE = "update_entry";
    public static final String ACTION_ENTRY_ADD = "add_entry";

    public static final String ACTION_FOLDER_DELETE = "delete_folder";
    public static final String ACTION_FOLDER_UPDATE = "update_folder";
    public static final String ACTION_FOLDER_ADD = "new_folder";

    // Folder
    public static final String FOLDERS = "folders";
    public static final String FOLDER_CODE = "folder_code";
    public static final String FOLDER_NAME = "folder_name";
    public static final String FOLDER_PARENT_ID = "parent_id";

    // Listing
    public static final String PARAM_FOLDER_ID = "folder_id";
    public static final String PARAM_TYPE = "entry_type";
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_COUNT = "count";
    public static final String PARAM_START_DATE = "start_date";
    public static final String PARAM_END_DATE = "end_date";
    public static final String PARAM_KEYWORD = "keyword";

    public static final String MODULE_ID = "module_id";
    public static final String FOLDER_ID = "folder_id";
    public static final String TEMPLATE_ID = "template_id";
    public static final String ENTRY_ID = "entry_id";
    public static final String TITLE = "title";

    public static final String LIST_SUMMARY = "list_summary";
    public static final String LIST_TOTAL_COUNT = "total_count";
    public static final String LIST_COUNT_PER_PAGE = "count_per_page";
    public static final String LIST_PAGE_ID = "page_id";
    public static final String LIST_START_DATE = "start_date";
    public static final String LIST_END_DATE = "end_date";
    public static final String LIST_ENTRIES = "entries";
    public static final String LIST_SUB_FOLDERS = "sub_folders";

    public static final String SEARCH_KEYWORD = "keyword";

    // Entry
    public static final String ENTRY_CREATE_TS = "create_ts";
    public static final String ENTRY_MODIFIED_TS = "modified_ts";

    public static final String ENTRY_PART = "entry";
    public static final String ENTRY_TAGS = "tags";
    public static final String ENTRY_NOTE = "note";
    public static final String ENTRY_ATTACHMENTS = "attachments";
    public static final String ENTRY_WEB_ADDRESS = "web_address";
    public static final String ENTRY_LOCATION = "location";

    // Basic item
    public static final String NP_ITEM_NAME = "name";
    public static final String NP_ITEM_VALUE = "value";
	public static final String NP_ITEM_FORMATTED_VALUE = "formatted_value";
    public static final String NP_ITEM_LABEL = "label";
    public static final String ITEM_TYPE = "type";
    public static final String ITEM_SUBTYPE = "subtype";

    // Contact
    public static final String FIRST_NAME = "first_name";
    public static final String LAST_NAME = "last_name";
    public static final String MIDDLE_NAME = "mi";
	public static final String FULL_NAME = "full_name";
    public static final String BUSINESS_NAME = "business";
    public static final String CONTACT_WEBSITE = "contact_website";
    public static final String ADDRESS = "address";
    public static final String CITY = "city";
    public static final String PROVINCE = "province";
    public static final String POSTAL_CODE = "postal_code";
    public static final String COUNTRY = "country";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String PROFILE_PHOTO_URL = "profile_photo_url";

    public static final String LOCALE = "locale";
    public static final String TIMEZONE = "timezone";
    public static final String COUNTRY_CODE = "country";
    public static final String LANGUAGE_CODE = "language";

    public static final String CONTACT_EMAIL = "email";
    public static final String CONTACT_PHONE = "phone";

    // Event
    public static final String EVENT_START_TS = "start_ts";
    public static final String EVENT_END_TS = "end_ts";
    public static final String EVENT_TIMEZONE = "timezone";
    public static final String EVENT_ALL_DAY = "all_day";
    public static final String EVENT_NO_TIME = "no_starting_time";
    public static final String EVENT_SINGLE_TIME = "single_time";
    public static final String EVENT_RECUR_ID = "recur_id";
    public static final String EVENT_RECURRENCE = "recurrence";
    public static final String EVENT_REMINDER = "reminders";
    public static final String EVENT_ATTENDEES = "attendees";

    public static final String EVENT_RECUR_PATTERN = "pattern";
    public static final String EVENT_RECUR_INTERVAL = "interval";
    public static final String EVENT_RECUR_MONTHLY_REPEATBY = "monthly_repeat_by";
    public static final String EVENT_RECUR_WEEKLYDAYS = "weekly_days";
    public static final String EVENT_RECUR_ENDDATE = "repeat_end_date";
    public static final String EVENT_RECUR_TIMES = "repeat_times";
    public static final String EVENT_RECUR_FOREVER = "repeat_forever";

    public static final String EVENT_REMINDER_RECEIVER_ID = "receiver_id";
    public static final String EVENT_REMINDER_OFFSET_TS = "offset_ts";
    public static final String EVENT_REMINDER_ADDRESS = "deliver_address";

    public static final String EVENT_ATTENDEE_USER_ID = "attendee_user_id";
    public static final String EVENT_ATTENDEE_EMAIL = "attendee_email";
    public static final String EVENT_ATTENDEE_NAME = "attendee_name";
    public static final String EVENT_ATTENDEE_COMMENT = "attendee_comment";
    public static final String EVENT_ATTENDEE_ATT_STATUS = "attendee_status";

    // Journal
    public static final String JOURNAL_DATE = "journal_date";

    // Doc
	public static final String DOC_FORMAT = "format";

    // Photo

    // Upload
    public static final String UPLOAD_TN_URL = "tn_url";
    public static final String UPLOAD_LIGHTBOX_URL = "lightbox_url";
    public static final String UPLOAD_DOWNLOAD_URL = "download_url";
    public static final String UPLOAD_FILE_NAME = "file_name";
    public static final String UPLOAD_FILE_TYPE = "file_type";
    public static final String UPLOAD_FILE_SIZE = "file_size";
    public static final String UPLOAD_FILE_LINK = "file_link";

    // Sharing
    public static final String RECEIVER_ID = "receiver_id";
    public static final String RECEIVER_KEY = "receiver_key";
    public static final String PERMISSION = "permission";
}
