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

public class Reminder {

    public enum Unit {
        MINUTE, HOUR, DAY;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    private int unitCount;
    private Reminder.Unit unit;

    private int offsetTimestamp;
    private String deliverAddress;

    public Reminder() {

    }

    public Reminder(JSONObject jsonObj) {
        try {
            if (jsonObj.has(ServiceConstants.EVENT_REMINDER_OFFSET_TS)) {
                offsetTimestamp = jsonObj.getInt(ServiceConstants.EVENT_REMINDER_OFFSET_TS);

                int days = offsetTimestamp / 86400;
                if (days > 0) {
                    unit = Reminder.Unit.DAY;
                    unitCount = days;

                } else {
                    int hours = offsetTimestamp / 3600;
                    if (hours > 0) {
                        unit = Reminder.Unit.HOUR;
                        unitCount = hours;

                    } else {
                        int minutes = offsetTimestamp / 60;
                        if (minutes > 0) {
                            unit = Reminder.Unit.MINUTE;
                            unitCount = minutes;
                        }
                    }
                }

                if (jsonObj.has(ServiceConstants.EVENT_REMINDER_ADDRESS)) {
                    deliverAddress = jsonObj.getString(ServiceConstants.EVENT_REMINDER_ADDRESS);
                }
            }

        } catch (JSONException e) {
            Log.e("Reminder", "Error parsing JSONObject:" + e.toString());
        }
    }

    public Map<String, String> toMap() {
        Map<String, String> postParams = new HashMap<String, String>();
        postParams.put(ServiceConstants.EVENT_REMINDER_ADDRESS, deliverAddress);
        postParams.put(ServiceConstants.EVENT_REMINDER_OFFSET_TS, String.valueOf(offsetTimestamp));
        return postParams;
    }

    private void calculateOffsetTimestamp() {
        if (unit == Reminder.Unit.MINUTE) {
            offsetTimestamp = unitCount * 60;

        } else if (unit == Reminder.Unit.HOUR) {
            offsetTimestamp = unitCount * 3600;

        } else if (unit == Reminder.Unit.DAY) {
            offsetTimestamp = unitCount * 86400;
        }
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("Deliver address:").append(deliverAddress).append(" ").append(unitCount).append(" ").append(unit.toString()).append(" before.");
        return buf.toString();
    }

    /*
     * Getters and Setters
     */
    public int getUnitCount() {
        return unitCount;
    }

    public void setUnitCount(int unitCount) {
        this.unitCount = unitCount;
        this.calculateOffsetTimestamp();
    }

    public Reminder.Unit getUnit() {
        return unit;
    }

    public void setUnit(Reminder.Unit unit) {
        this.unit = unit;
        this.calculateOffsetTimestamp();
    }

    public int getOffsetTimestamp() {
        return offsetTimestamp;
    }

    public void setOffsetTimestamp(int offsetTimestamp) {
        this.offsetTimestamp = offsetTimestamp;
    }

    public String getDeliverAddress() {
        return deliverAddress;
    }

    public void setDeliverAddress(String deliverAddress) {
        this.deliverAddress = deliverAddress;
    }
}
