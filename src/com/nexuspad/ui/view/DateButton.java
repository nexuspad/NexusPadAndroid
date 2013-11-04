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

public class DateButton extends DateTimeButton implements DatePickerDialog.OnDateSetListener {
    public DateButton(Context context) {
        super(context);
        mDateFormat = android.text.format.DateFormat.getDateFormat(context);
    }

    public DateButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDateFormat = android.text.format.DateFormat.getDateFormat(context);
    }

    public DateButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mDateFormat = android.text.format.DateFormat.getDateFormat(context);
    }

    @Override
    public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {
        mCalendar.set(year, monthOfYear, dayOfMonth);
        setText(mDateFormat.format(mCalendar.getTime()));
    }
}
