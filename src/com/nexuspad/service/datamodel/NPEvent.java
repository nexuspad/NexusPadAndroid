/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.datamodel;

import android.os.Parcel;
import android.util.Log;
import com.google.common.base.Objects;
import com.nexuspad.service.dataservice.ServiceConstants;
import com.nexuspad.service.util.DateUtil;
import com.nexuspad.service.util.JsonHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class NPEvent extends NPEntry {

	public static final Creator<NPEvent> CREATOR = new Creator<NPEvent>() {
		public NPEvent createFromParcel(Parcel in) {
			return new NPEvent(in);
		}

		public NPEvent[] newArray(int size) {
			return new NPEvent[size];
		}
	};

	/**
	 * the id of this event in the calendar
	 */
	private Date startTime;
	private Date endTime;
	private TimeZone timeZone;
	/**
	 * the time when you change the event the last time.
	 * we will use this time to judge which one is newer between the local event
	 * and the remote one
	 */
	private long lastChangeTime;

	private boolean singleTimeEvent = false;
	private boolean allDayEvent = false;
	private boolean noStartingTime = false;
	private boolean multiDayEvent = false;

	private Recurrence recurrence;
	private String recurId;

	private List<Reminder> reminders;
	private List<Attendee> attendees;

	public NPEvent(NPFolder folder) {
		super(folder, EntryTemplate.EVENT);
	}

	public NPEvent(NPEvent anEvent) {
		super(anEvent);

		startTime = anEvent.startTime;
		endTime = anEvent.endTime;
		timeZone = anEvent.timeZone;

		singleTimeEvent = anEvent.singleTimeEvent;
		allDayEvent = anEvent.allDayEvent;
		noStartingTime = anEvent.noStartingTime;
		multiDayEvent = anEvent.multiDayEvent;

		if (anEvent.recurrence != null) {
			recurrence = new Recurrence(anEvent.recurrence);
		}

		recurId = anEvent.recurId;

		if (anEvent.reminders != null) {
			reminders = new ArrayList<Reminder>(anEvent.reminders);
		}

		if (anEvent.attendees != null) {
			attendees = new ArrayList<Attendee>(anEvent.attendees);
		}
	}

	public NPEvent(JSONObject jsonObj) {
		super(jsonObj, EntryTemplate.EVENT);

		try {
			if (jsonObj.has(ServiceConstants.EVENT_RECUR_ID)) {
				recurId = jsonObj.getString(ServiceConstants.EVENT_RECUR_ID);
			}

			if (jsonObj.has(ServiceConstants.EVENT_TIMEZONE)) {
				timeZone = TimeZone.getTimeZone(jsonObj
						.getString(ServiceConstants.EVENT_TIMEZONE));

			} else {
				timeZone = TimeZone.getDefault();
			}

			if (jsonObj.has(ServiceConstants.EVENT_START_TS)) {
				startTime = DateUtil.dateFromTimestampInSeconds(jsonObj.getLong(ServiceConstants.EVENT_START_TS));
			}

			if (jsonObj.has(ServiceConstants.EVENT_END_TS)) {
				endTime = DateUtil.dateFromTimestampInSeconds(jsonObj.getLong(ServiceConstants.EVENT_END_TS));
			}

			// Essential flags
			if (jsonObj.has(ServiceConstants.EVENT_ALL_DAY)) {
				allDayEvent = jsonObj.getInt(ServiceConstants.EVENT_ALL_DAY) == 1;
			}
			if (jsonObj.has(ServiceConstants.EVENT_NO_TIME)) {
				noStartingTime = jsonObj.getInt(ServiceConstants.EVENT_NO_TIME) == 1;
			}
			if (jsonObj.has(ServiceConstants.EVENT_SINGLE_TIME)) {
				singleTimeEvent = jsonObj.getInt(ServiceConstants.EVENT_SINGLE_TIME) == 1;
			}

			// Recurrence
			if (jsonObj.has(ServiceConstants.EVENT_RECURRENCE)) {
				JSONObject recurJson = jsonObj.getJSONObject(ServiceConstants.EVENT_RECURRENCE);
				recurrence = new Recurrence(recurJson);
			}

			// Reminders
			if (jsonObj.has(ServiceConstants.EVENT_REMINDER)) {
				reminders = new ArrayList<Reminder>();
				JSONArray reminderArray = jsonObj.getJSONArray(ServiceConstants.EVENT_REMINDER);
				for (int i = 0; i < reminderArray.length(); i++) {
					JSONObject reminderJson = reminderArray.getJSONObject(i);
					reminders.add(new Reminder(reminderJson));
				}
			}

			// Attendees
			if (jsonObj.has(ServiceConstants.EVENT_ATTENDEES)) {
				attendees = new ArrayList<Attendee>();
				JSONArray attendeeArray = jsonObj.getJSONArray(ServiceConstants.EVENT_ATTENDEES);
				for (int i = 0; i < attendeeArray.length(); i++) {
					JSONObject attendeeJson = attendeeArray.getJSONObject(i);
					attendees.add(new Attendee(attendeeJson));
				}
			}

		} catch (JSONException e) {
			// Do nothing here.
			//throw new RuntimeException(jsonObj.toString(), e);
		}
	}

	public NPEvent(NPEntry entry) {
		super(entry);
	}

	public static NPEvent fromEntry(NPEntry entry) {
		if (entry instanceof NPEvent) {
			return (NPEvent)entry;
		}
		return new NPEvent(entry);
	}

	/**
	 * Split an event that spans multiple days into an array of events. This is
	 * necessary for displaying event list by date because we want a multi-day
	 * event presents in all days.
	 *
	 * @param anEvent
	 * @return
	 */
	public static List<NPEvent> splitMultiDayEvent(NPEvent anEvent) {
		String startYmd = DateUtil.convertToYYYYMMDD(anEvent.startTime);

		String endYmd = startYmd;

		if (anEvent.endTime != null) {
			endYmd = DateUtil.convertToYYYYMMDD(anEvent.endTime);
		}

		List<NPEvent> events = new ArrayList<NPEvent>();

		if (startYmd.equals(endYmd)) {
			events.add(anEvent);

		} else {

			int numberOfDays = (int) ((anEvent.endTime.getTime() / 1000) - (anEvent.startTime.getTime() / 1000)) / 86400;

			// Add the first day
			NPEvent firstDayEvent = new NPEvent(anEvent);
			// Check if the event has a starting time
			if (DateUtil.minuteOfDate(firstDayEvent.startTime) == 0) {
				firstDayEvent.endTime = firstDayEvent.startTime;
				firstDayEvent.allDayEvent = true;
			} else {
				firstDayEvent.endTime = DateUtil.endOfDateTime(firstDayEvent.startTime);
			}
			events.add(firstDayEvent);

			// Add the days in the middle
			for (int i = 1; i < (numberOfDays - 1); i++) {
				NPEvent aMiddleEvent = new NPEvent(anEvent);
				Date theDate = DateUtil.addDaysTo(anEvent.startTime, i);
				aMiddleEvent.startTime = theDate;
				aMiddleEvent.endTime = theDate;
				aMiddleEvent.allDayEvent = true;

				events.add(aMiddleEvent);
			}

			// Add the last day
			NPEvent lastDayEvent = new NPEvent(anEvent);
			if (DateUtil.isEndOfDateTime(lastDayEvent.endTime)) {
				lastDayEvent.startTime = DateUtil.startOfDateTime(lastDayEvent.startTime);
				lastDayEvent.allDayEvent = true;

			} else {
				lastDayEvent.startTime = DateUtil.startOfDateTime(lastDayEvent.endTime);
			}

			events.add(lastDayEvent);
		}

		return events;
	}

	protected NPEvent(Parcel in) {
		super(in);

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(in.readLong());
		startTime = calendar.getTime();
		calendar.setTimeInMillis(in.readLong());
		endTime = calendar.getTime();
		timeZone = (TimeZone) in.readSerializable();
		lastChangeTime = in.readLong();
		singleTimeEvent = in.readByte() != 0x00;
		allDayEvent = in.readByte() != 0x00;
		noStartingTime = in.readByte() != 0x00;
		multiDayEvent = in.readByte() != 0x00;
		recurrence = in.readParcelable(Recurrence.class.getClassLoader());
		recurId = in.readString();
		reminders = new ArrayList<Reminder>();
		in.readList(reminders, Reminder.class.getClassLoader());
		attendees = new ArrayList<Attendee>();
		in.readList(attendees, Attendee.class.getClassLoader());
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeLong(startTime == null ? 0 : startTime.getTime());
		dest.writeLong(endTime == null ? 0 : endTime.getTime());
		dest.writeSerializable(timeZone);
		dest.writeLong(lastChangeTime);
		dest.writeByte((byte) (singleTimeEvent ? 0x01 : 0x00));
		dest.writeByte((byte) (allDayEvent ? 0x01 : 0x00));
		dest.writeByte((byte) (noStartingTime ? 0x01 : 0x00));
		dest.writeByte((byte) (multiDayEvent ? 0x01 : 0x00));
		dest.writeParcelable(recurrence, 0);
		dest.writeString(recurId);
		dest.writeList(reminders);
		dest.writeList(attendees);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	/**
	 * Check if two events have overlapping schedule.
	 *
	 * @param anotherEvent
	 * @return
	 */
	public boolean overlaps(NPEvent anotherEvent) {
		if (endTime == null || anotherEvent.endTime == null) {
			return false;
		}

		if (anotherEvent.startTime.after(startTime) && anotherEvent.startTime.before(endTime)) {
			return true;
		}

		if (endTime.after(anotherEvent.startTime) && endTime.before(anotherEvent.endTime)) {
			return true;
		}

		return false;
	}

	@Override
	public Date getTimeFilter() {
		return startTime;
	}


	public String getEventTimeDescription() {
		return null;
	}

	public String getEventAttendeeDescription() {
		return null;
	}

	public String getEventReminderDescription() {
		return null;
	}

	@Override
	public Map<String, String> toMap() {
		Map<String, String> postParams = super.toMap();
		if (allDayEvent) {
			postParams.put(ServiceConstants.EVENT_ALL_DAY, String.valueOf(1));
		}
		if (noStartingTime) {
			postParams.put(ServiceConstants.EVENT_NO_TIME, String.valueOf(1));
		}
		if (singleTimeEvent) {
			postParams.put(ServiceConstants.EVENT_SINGLE_TIME,
					String.valueOf(1));
		}

		postParams.put(ServiceConstants.EVENT_START_TS, String.valueOf(startTime.getTime() / 1000));
		postParams.put(ServiceConstants.EVENT_END_TS, String.valueOf(endTime.getTime() / 1000));

		if (timeZone != null) {
			postParams.put(ServiceConstants.EVENT_TIMEZONE, timeZone.getID());
		}

		if (recurrence != null) {
			try {
				JSONObject recurJsonObj = (JSONObject) JsonHelper
						.toJSON(recurrence.toMap());
				postParams.put(ServiceConstants.EVENT_RECURRENCE,
						recurJsonObj.toString());
			} catch (JSONException e) {
				Log.e("Event", "Error exporting recurrence to JSON object.");
			}
		}

		if ((attendees != null) && (attendees.size() > 0)) {
			try {
				JSONArray attendeeObjArr = new JSONArray();
				for (Attendee att : attendees) {
					attendeeObjArr.put(JsonHelper.toJSON(att.toMap()));
				}
				postParams.put(ServiceConstants.EVENT_ATTENDEES,
						attendeeObjArr.toString());

			} catch (JSONException e) {
				Log.e("Event", "Error exporting attendee to JSON object.");
			}
		}

		if ((reminders != null) && (reminders.size() > 0)) {
			try {
				JSONArray reminderObjArr = new JSONArray();
				for (Reminder rem : reminders) {
					reminderObjArr.put(JsonHelper.toJSON(rem
							.toMap()));
				}
				postParams.put(ServiceConstants.EVENT_REMINDER,
						reminderObjArr.toString());

			} catch (JSONException e) {
				Log.e("Event", "Error exporting reminder to JSON object.");
			}
		}

		return postParams;
	}


	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.omitNullValues()
				.add("\nentryId", entryId)
				.add("\nstartTime", startTime)
				.add("\nendTime", endTime)
				.add("\ntimeZone", timeZone)
				.add("\nlastChangeTime", lastChangeTime)
				.add("\nsingleTimeEvent", singleTimeEvent)
				.add("\nallDayEvent", allDayEvent)
				.add("\nnoStartingTime", noStartingTime)
				.add("\nmultiDayEvent", multiDayEvent)
				.add("\nrecurrence", recurrence)
				.add("\nrecurId", recurId)
				.add("\nreminders", reminders)
				.add("\nattendees", attendees)
				.toString();
	}

	/*
	 * Getters and Setters
	 */
	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public boolean isSingleTimeEvent() {
		return singleTimeEvent;
	}

	public void setSingleTimeEvent(boolean singleTimeEvent) {
		this.singleTimeEvent = singleTimeEvent;
	}

	public boolean isAllDayEvent() {
		return allDayEvent;
	}

	public void setAllDayEvent(boolean allDayEvent) {
		this.allDayEvent = allDayEvent;
	}

	public boolean isNoStartingTime() {
		return noStartingTime;
	}

	public void setNoStartingTime(boolean noStartingTime) {
		this.noStartingTime = noStartingTime;
	}

	public Recurrence getRecurrence() {
		return recurrence;
	}

	public void setRecurrence(Recurrence recurrence) {
		this.recurrence = recurrence;
	}

	public List<Reminder> getReminders() {
		return reminders;
	}

	public void addReminder(Reminder reminder) {
		if (reminders == null) {
			reminders = new ArrayList<Reminder>();
		}
		reminders.add(reminder);
	}

	public void setReminders(List<Reminder> reminders) {
		this.reminders = reminders;
	}

	public List<Attendee> getAttendees() {
		return attendees;
	}

	public void addAttendee(Attendee att) {
		if (attendees == null) {
			attendees = new ArrayList<Attendee>();
		}
		attendees.add(att);
	}

	public void setAttendees(List<Attendee> attendees) {
		this.attendees = attendees;
	}

	public boolean isMultiDayEvent() {
		return multiDayEvent;
	}

	public void setMultiDayEvent(boolean multiDayEvent) {
		this.multiDayEvent = multiDayEvent;
	}

	public TimeZone getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}

	public String getRecurId() {
		return recurId;
	}

	public void setRecurId(String recurId) {
		this.recurId = recurId;
	}

	public long getLastChangeTime() {
		return lastChangeTime;
	}

	public void setLastChangeTime(long lastChangeTime) {
		this.lastChangeTime = lastChangeTime;
	}
}
