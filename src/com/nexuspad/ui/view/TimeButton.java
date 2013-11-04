package com.nexuspad.ui.view;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.Button;
import com.android.datetimepicker.date.DatePickerDialog;
import com.android.datetimepicker.time.RadialPickerLayout;
import com.android.datetimepicker.time.TimePickerDialog;

import java.text.DateFormat;
import java.util.Calendar;

public class TimeButton extends DateTimeButton implements TimePickerDialog.OnTimeSetListener {

    public TimeButton(Context context) {
        super(context);
        mDateFormat = android.text.format.DateFormat.getTimeFormat(context);
    }

    public TimeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDateFormat = android.text.format.DateFormat.getTimeFormat(context);
    }

    public TimeButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mDateFormat = android.text.format.DateFormat.getTimeFormat(context);
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        mCalendar.set(Calendar.MINUTE, minute);
        setText(mDateFormat.format(mCalendar.getTime()));
    }
}
