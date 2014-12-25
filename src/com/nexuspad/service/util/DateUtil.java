/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.util;

import android.content.Context;
import android.text.TextUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtil {

    public static Date now() {
        Calendar c = Calendar.getInstance();
        return c.getTime();
    }

    /**
     * Get the current timestamp in seconds.
     *
     * @return
     */
    public static long getCurrentTimestamp() {
        Calendar c = Calendar.getInstance();
        return c.getTimeInMillis() / 1000;
    }

    public static long convertToTimestamp(long time) {
        return time / 1000;
    }

	public static Date getDate(int year, int month, int day) {
		Calendar c = Calendar.getInstance();
		c.set(year, month, day);
		return c.getTime();
	}

    /**
     * @param ymd cannot be null or empty
     */
    public static Date parseFromYYYYMMDD(String ymd) {
        if (TextUtils.isEmpty(ymd)) throw new IllegalArgumentException("ymd cannot be null or empty");
        try {
            DateFormat df = new SimpleDateFormat("yyyyMMdd", Locale.US);
            return df.parse(ymd);

        } catch (ParseException e) {
            throw new RuntimeException("DateUtil: Error parsing date string.", e);
        }
    }

    public static String convertToYYYYMMDD(Date time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return sdf.format(time);

        // TODO remember to uncomment this
        //return android.text.format.DateFormat.format("yyyyMMdd", time).toString();
    }

    public static Date dateFromTimestamp(long milliTs) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliTs);
        return calendar.getTime();
    }

    public static Date dateFromTimestampInSeconds(long ts) {
        long milliTs = ts * 1000;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliTs);
        return calendar.getTime();
    }

    public static Date getFirstDateOfTheMonth(long ts) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(ts);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        return calendar.getTime();
    }

    public static Date getEndDateOfTheMonth(long ts) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(ts);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return calendar.getTime();
    }

    public static Date startOfDateTime(Date time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date endOfDateTime(Date time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    public static Date addDaysTo(Date time, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
    }

    public static boolean isSameDay(Date date, Date other) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(date);
        final int firstDate = calendar.get(Calendar.DAY_OF_MONTH);

        calendar.setTime(other);
        final int otherDate = calendar.get(Calendar.DAY_OF_MONTH);

        return firstDate == otherDate;
    }

    public static boolean dateWithinDateRange(Date aDate, Date leftDate, Date rightDate) {
        if (aDate.compareTo(leftDate) >= 0 && aDate.compareTo(rightDate) <= 0) {
            return true;
        }
        return false;
    }

    public static int daysBetween(Date date, Date other) {
        final Calendar calendar = Calendar.getInstance();

        calendar.setTime(date);
        final int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);

        calendar.setTime(other);
        final int otherDayOfYear = calendar.get(Calendar.DAY_OF_YEAR);

        return Math.abs(dayOfYear - otherDayOfYear);
    }

    public static int hourOfDate(Date time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    public static int minuteOfDate(Date time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        return calendar.get(Calendar.MINUTE);
    }

    public static boolean isEndOfDateTime(Date time) {
        int hour = DateUtil.hourOfDate(time);
        if (hour == 24) {
            return true;
        } else if (hour == 23) {
            int minute = DateUtil.minuteOfDate(time);
            if (minute > 55) {
                return true;
            }
        }
        return false;
    }

	public static String displayDate(Context context, Date aDate) {
		DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context.getApplicationContext());
		return dateFormat.format(aDate);
	}

	public static Date parseEventDateStringAndTimeString(String yyyymmdd, String hh24mm, TimeZone timezone) throws ParseException {
		if (hh24mm != null && hh24mm.length() == 4) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HHmm");
			sdf.setTimeZone(timezone);
			return sdf.parse(yyyymmdd + " " + hh24mm);

		} else {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			sdf.setTimeZone(timezone);
			return sdf.parse(yyyymmdd);
		}
	}
}
