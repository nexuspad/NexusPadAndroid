package com.nexuspad.calendar.view;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.Button;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public abstract class DateTimeButton extends Button {
    protected static class DateTimeButtonParcelable implements Parcelable {
        public static final Parcelable.Creator<DateTimeButtonParcelable> CREATOR = new Creator<DateTimeButtonParcelable>() {
            @Override
            public DateTimeButtonParcelable createFromParcel(Parcel source) {
                return new DateTimeButtonParcelable(source);
            }

            @Override
            public DateTimeButtonParcelable[] newArray(int size) {
                return new DateTimeButtonParcelable[size];
            }
        };

        private final long time;
        private final Parcelable parent;

        private DateTimeButtonParcelable(long time, Parcelable parent) {
            this.parent = parent;
            this.time = time;
        }

        public DateTimeButtonParcelable(Parcel source) {
            time = source.readLong();
            parent = source.readParcelable(Parcelable.class.getClassLoader());
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel p, int flags) {
            p.writeLong(time);
            p.writeParcelable(parent, flags);
        }
    }

    protected final Calendar mCalendar = Calendar.getInstance();
    protected DateFormat mDateFormat;

    public DateTimeButton(Context context) {
        super(context);
    }

    public DateTimeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DateTimeButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setTime(Date time) {
        setTime(time.getTime());
    }

    public void setTime(long time) {
        mCalendar.setTimeInMillis(time);
        setText(mDateFormat.format(mCalendar.getTime()));
    }

    public Date getTime() {
        return mCalendar.getTime();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        return new DateTimeButtonParcelable(mCalendar.getTimeInMillis(), super.onSaveInstanceState());
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        final DateTimeButtonParcelable parcelable = (DateTimeButtonParcelable) state;
        setTime(parcelable.time);
        super.onRestoreInstanceState(parcelable.parent);
    }
}
