/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.datamodel;

import android.util.Log;
import com.nexuspad.service.dataservice.ServiceConstants;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Attendee extends NPPerson {

    public enum AttendingStatus {
        NOTINVITED, INVITED, WILLATTEND, WONTATTEND, MAYATTEND;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    private String userName;
    private String email;

    private int attendeeId = 0;
    private int attendeeUserId = 0;
    private int attendeeCalendarId;

    private Attendee.AttendingStatus status;

    private String comment;

    public Attendee() {
        status = AttendingStatus.NOTINVITED;
    }

    public Attendee(JSONObject jsonObj) {
        try {
            if (jsonObj.has(ServiceConstants.EVENT_ATTENDEE_USER_ID)) {
                attendeeUserId = jsonObj.getInt(ServiceConstants.EVENT_ATTENDEE_USER_ID);
            }

            if (jsonObj.has(ServiceConstants.EVENT_ATTENDEE_ATT_STATUS)) {
                status = Attendee.AttendingStatus.values()[jsonObj.getInt(ServiceConstants.EVENT_ATTENDEE_ATT_STATUS)];
            }

            if (jsonObj.has(ServiceConstants.EVENT_ATTENDEE_EMAIL)) {
                email = jsonObj.getString(ServiceConstants.EVENT_ATTENDEE_EMAIL);
            }

            if (jsonObj.has(ServiceConstants.EVENT_ATTENDEE_NAME)) {
                userName = jsonObj.getString(ServiceConstants.EVENT_ATTENDEE_NAME);
            }

            if (jsonObj.has(ServiceConstants.EVENT_ATTENDEE_COMMENT)) {
                comment = jsonObj.getString(ServiceConstants.EVENT_ATTENDEE_COMMENT);
            }

        } catch (JSONException e) {
            Log.e("Attendee", "Error parsing JSONObject:" + e.toString());
        }
    }

    @Override
    public Map<String, String> toMap() {
        Map<String, String> postParams = new HashMap<String, String>();
        if (attendeeUserId != 0) {
            postParams.put(ServiceConstants.EVENT_ATTENDEE_USER_ID, String.valueOf(attendeeUserId));
        }
        if (email != null) {
            postParams.put(ServiceConstants.EVENT_ATTENDEE_EMAIL, email);
        }
        postParams.put(ServiceConstants.EVENT_ATTENDEE_ATT_STATUS, String.valueOf(status.ordinal()));

        if (comment != null) {
            postParams.put(ServiceConstants.EVENT_ATTENDEE_COMMENT, comment);
        }

        return postParams;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        if (email != null) {
            buf.append("Email:").append(email);
        }
        buf.append(" Status:").append(status.toString());
        if (userName != null) {
            buf.append(" UserName:").append(userName);
        }
        if (attendeeUserId != 0) {
            buf.append(" UserId:").append(attendeeUserId);
        }
        return buf.toString();
    }

    /*
     * Getters and Setters
     */
    public int getAttendeeId() {
        return attendeeId;
    }

    public void setAttendeeId(int attendeeId) {
        this.attendeeId = attendeeId;
    }

    public int getAttendeeUserId() {
        return attendeeUserId;
    }

    public void setAttendeeUserId(int attendeeUserId) {
        this.attendeeUserId = attendeeUserId;
    }

    public int getAttendeeCalendarId() {
        return attendeeCalendarId;
    }

    public void setAttendeeCalendarId(int attendeeCalendarId) {
        this.attendeeCalendarId = attendeeCalendarId;
    }

    public Attendee.AttendingStatus getAttendingStatus() {
        return status;
    }

    public void setAttendingStatus(Attendee.AttendingStatus status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
