/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.datamodel;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import com.nexuspad.service.dataservice.ServiceConstants;
import com.nexuspad.service.util.DateUtil;
import com.nexuspad.service.util.JsonHelper;
import com.nexuspad.service.util.LocalizedString;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class Recurrence implements Parcelable {

    public static final Creator<Recurrence> CREATOR = new Creator<Recurrence>() {
        @Override
        public Recurrence createFromParcel(Parcel in) {
            return new Recurrence(in);
        }

        @Override
        public Recurrence[] newArray(int size) {
            return new Recurrence[size];
        }
    };

    public enum Pattern {
        NOREPEAT, DAILY, WEEKDAILY, WEEKLY, MONTHLY, YEARLY;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    public enum MonthlyRepeat {
        DAY_OF_MONTH, DAY_OF_WEEK;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    private Recurrence.Pattern pattern = Recurrence.Pattern.NOREPEAT;
    private int interval = 1;
    private int recurrenceTimes = 1;
    private Date endDate;
    private boolean repeatForever = false;

    private List<String> weeklyDays;
    private Recurrence.MonthlyRepeat monthlyRepeatType;

    public Recurrence() {

    }

    /**
     * Copy constructor
     *
     * @param recur
     */
    public Recurrence(Recurrence recur) {
        pattern = recur.pattern;
        interval = recur.interval;
        recurrenceTimes = recur.recurrenceTimes;
        endDate = recur.endDate;
        repeatForever = recur.repeatForever;

        weeklyDays = recur.weeklyDays == null ? new ArrayList<String>() : new ArrayList<String>(recur.weeklyDays);
        monthlyRepeatType = recur.monthlyRepeatType;
    }

    /**
     * Create object based on JSON object.
     *
     * @param jsonObj
     */
    public Recurrence(JSONObject jsonObj) {
        try {
            if (jsonObj.has(ServiceConstants.EVENT_RECUR_PATTERN)) {
                pattern = Recurrence.Pattern.values()[jsonObj.getInt(ServiceConstants.EVENT_RECUR_PATTERN)];
            }

            if (pattern != Recurrence.Pattern.NOREPEAT) {
                if (jsonObj.has(ServiceConstants.EVENT_RECUR_INTERVAL)) {
                    interval = JsonHelper.getIntValue(jsonObj, ServiceConstants.EVENT_RECUR_INTERVAL);
                }

                if (jsonObj.has(ServiceConstants.EVENT_RECUR_TIMES)) {
                    recurrenceTimes = JsonHelper.getIntValue(jsonObj, ServiceConstants.EVENT_RECUR_TIMES);
                }

                if (jsonObj.has(ServiceConstants.EVENT_RECUR_ENDDATE)) {
                    final String endDateYmd = jsonObj.getString(ServiceConstants.EVENT_RECUR_ENDDATE);
	                if (!TextUtils.isEmpty(endDateYmd)) {
		                endDate = DateUtil.parseFromYYYYMMDD(endDateYmd);
	                }
                }

                if (recurrenceTimes == -1 && endDate == null) {
                    repeatForever = true;
                }

                // Sanity check
                if (interval == -1) {
                    pattern = Recurrence.Pattern.NOREPEAT;
                }
            }

        } catch (JSONException e) {
            Log.e("Recurrence", "Error parsing JSONObject:" + e.toString() + "\n" + jsonObj.toString());
        }
    }

    protected Recurrence(Parcel in) {
        pattern = (Pattern) in.readSerializable();
        interval = in.readInt();
        recurrenceTimes = in.readInt();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(in.readLong());
        endDate = calendar.getTime();

        repeatForever = in.readByte() != 0x00;
        weeklyDays = new ArrayList<String>();
        in.readList(weeklyDays, null);
        monthlyRepeatType = (MonthlyRepeat) in.readSerializable();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(pattern);
        dest.writeInt(interval);
        dest.writeInt(recurrenceTimes);
        dest.writeLong(endDate == null ? 0 : endDate.getTime());
        dest.writeByte((byte) (repeatForever ? 0x01 : 0x00));
        dest.writeList(weeklyDays);
        dest.writeSerializable(monthlyRepeatType);
    }

    @Override
    public int describeContents() {
        return 0;
    }

	public boolean isValid() {
		if (pattern == Pattern.NOREPEAT) {
			return true;
		} else {
			// Check recurrence end
			if (recurrenceTimes <= 0 && endDate == null && repeatForever == false) {
				return false;
			}
			return true;
		}
	}

    /**
     * Convert to name/value pairs.
     *
     * @return
     */
    public Map<String, String> toMap() {
        Map<String, String> postParams = new HashMap<String, String>();

        if (pattern == Recurrence.Pattern.NOREPEAT) {
            postParams.put(ServiceConstants.EVENT_RECUR_PATTERN, String.valueOf(pattern));
            return postParams;
        }

        postParams.put(ServiceConstants.EVENT_RECUR_PATTERN, String.valueOf(pattern));
        postParams.put(ServiceConstants.EVENT_RECUR_INTERVAL, String.valueOf(interval));

        if (pattern == Recurrence.Pattern.WEEKLY || pattern == Recurrence.Pattern.WEEKDAILY) {
            if (weeklyDays != null && weeklyDays.size() > 0) {
                postParams.put(ServiceConstants.EVENT_RECUR_WEEKLYDAYS, TextUtils.join(",", weeklyDays));
            }
        }

        if (pattern == Recurrence.Pattern.MONTHLY) {
            postParams.put(ServiceConstants.EVENT_RECUR_MONTHLY_REPEATBY, String.valueOf(monthlyRepeatType));
        }

        if (repeatForever) {
            postParams.put(ServiceConstants.EVENT_RECUR_FOREVER, "1");
        } else if (endDate != null) {
            postParams.put(ServiceConstants.EVENT_RECUR_ENDDATE, DateUtil.convertToYYYYMMDD(endDate));
        } else if (recurrenceTimes != -1) {
            postParams.put(ServiceConstants.EVENT_RECUR_TIMES, String.valueOf(recurrenceTimes));
        }

        return postParams;
    }

    /**
     * Description.
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        switch (pattern) {
            case NOREPEAT:
                return LocalizedString.text("No repeat");

            case DAILY:
                if (interval == 1) {
                    buf.append(LocalizedString.text("Repeat daily"));
                } else {
                    buf.append(String.format(LocalizedString.text("Repeat every %d days"), interval));
                }
                break;

            case WEEKDAILY:
                if (interval == 1) {
                    buf.append(LocalizedString.text("Repeat daily on weekdays"));
                } else {
                    buf.append(String.format(LocalizedString.text("Repeat every %d weekdays"), interval));
                }
                break;

            case WEEKLY:
                if (interval == 1) {
                    buf.append(LocalizedString.text("Repeat weekly"));
                } else {
                    buf.append(String.format(LocalizedString.text("Repeat every %d weeks"), interval));
                }
                break;

            case MONTHLY:
                if (interval == 1) {
                    buf.append(LocalizedString.text("Repeat monthly"));
                } else {
                    buf.append(String.format(LocalizedString.text("Repeat every %d months"), interval));
                }
                break;

            case YEARLY:
                if (interval == 1) {
                    buf.append(LocalizedString.text("Repeat yearly"));
                } else {
                    buf.append(String.format(LocalizedString.text("Repeat every %d years"), interval));
                }
                break;
        }

	    if (recurrenceTimes > 0) {
		    buf.append(LocalizedString.text(" For %d times")).append(recurrenceTimes);
	    } else {
		    buf.append(LocalizedString.text(" Until ")).append(endDate);
	    }

        return buf.toString();
    }

    /**
     * Get the available patterns.
     *
     * @return
     */
    public static List<String> recurrencePatterns() {
        List<String> patterns = new ArrayList<String>();
        patterns.add(LocalizedString.text("Daily"));
        patterns.add(LocalizedString.text("Weekly"));
        patterns.add(LocalizedString.text("Monthly"));
        patterns.add(LocalizedString.text("Yearly"));
        return patterns;
    }

    /*
     * Getters and Setters
     */
    public Recurrence.Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Recurrence.Pattern pattern) {
        this.pattern = pattern;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

	public void reduceInterval() {
		interval--;
		if (interval < 1) interval = 1;
	}

	public void increaseInterval() {
		interval++;
	}

    public int getRecurrenceTimes() {
        return recurrenceTimes;
    }

    public void setRecurrenceTimes(int recurrenceTimes) {
        this.recurrenceTimes = recurrenceTimes;
    }

	public void reduceRecurrenceTimes() {
		recurrenceTimes--;
		if (recurrenceTimes < 1) recurrenceTimes = 1;
	}

	public void increaseRecurrenceTimes() {
		recurrenceTimes++;
	}

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public boolean isRepeatForever() {
        return repeatForever;
    }

    public void setRepeatForever(boolean repeatForever) {
        this.repeatForever = repeatForever;
    }

    public List<String> getWeeklyDays() {
        return weeklyDays;
    }

    public void setWeeklyDays(List<String> weeklyDays) {
        this.weeklyDays = weeklyDays;
    }

    public Recurrence.MonthlyRepeat getMonthlyRepeatType() {
        return monthlyRepeatType;
    }

    public void setMonthlyRepeatType(Recurrence.MonthlyRepeat monthlyRepeatType) {
        this.monthlyRepeatType = monthlyRepeatType;
    }
}
