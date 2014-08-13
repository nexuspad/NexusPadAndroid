package com.nexuspad.calendar.view;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.TextView;
import com.nexuspad.datamodel.Recurrence;

/**
 * Created by ren on 8/5/14.
 */
public class RecurrenceTextView extends TextView {

	private static class RecurrenceTextParcelable implements Parcelable {
		public static final Creator<RecurrenceTextParcelable> CREATOR = new Creator<RecurrenceTextParcelable>() {
			@Override
			public RecurrenceTextParcelable createFromParcel(Parcel source) {
				return new RecurrenceTextParcelable(source);
			}

			@Override
			public RecurrenceTextParcelable[] newArray(int size) {
				return new RecurrenceTextParcelable[0];
			}
		};

		private final Recurrence mRecurrence;
		private final Parcelable mParent;

		private RecurrenceTextParcelable(Parcel source) {
			mRecurrence = source.readParcelable(Recurrence.class.getClassLoader());
			mParent = source.readParcelable(Parcelable.class.getClassLoader());
		}

		private RecurrenceTextParcelable(RecurrenceTextView recurrenceTextView, Parcelable parent) {
			mRecurrence = recurrenceTextView.mRecurrence;
			mParent = parent;
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeParcelable(mRecurrence, flags);
			dest.writeParcelable(mParent, flags);
		}
	}

	private Recurrence mRecurrence;

	public RecurrenceTextView(Context context) {
		super(context);
	}

	public RecurrenceTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RecurrenceTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public Parcelable onSaveInstanceState() {
		return new RecurrenceTextParcelable(this, super.onSaveInstanceState());
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		final RecurrenceTextParcelable parcelable = (RecurrenceTextParcelable) state;
		setRecurrence(parcelable.mRecurrence);
		super.onRestoreInstanceState(parcelable.mParent);
	}

	public void setRecurrence(Recurrence recurrence) {
		mRecurrence = recurrence;
	}

	public Recurrence getRecurrence() {
		return mRecurrence;
	}
}
