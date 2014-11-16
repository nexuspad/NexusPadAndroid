package com.nexuspad.calendar.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.nexuspad.calendar.fragment.RecurrenceEditFragment;
import com.nexuspad.common.activity.DoneDiscardActivity;
import com.nexuspad.common.annotation.ParentActivity;
import com.nexuspad.service.datamodel.Recurrence;

/**
 * Created by ren on 8/3/14.
 */
@ParentActivity(EventEditActivity.class)
public class RecurrenceEditActivity extends DoneDiscardActivity {
	public static final String KEY_RECURRENCE = "key_recurrence";

	private Recurrence mRecurrence;

	public static Intent of(Context context, Recurrence recurrence) {
		final Intent intent = new Intent(context, RecurrenceEditActivity.class);
		intent.putExtra(KEY_RECURRENCE, recurrence);
		return intent;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setResult(RESULT_CANCELED);
		mRecurrence = getIntent().getParcelableExtra(KEY_RECURRENCE);
		super.onCreate(savedInstanceState);
	}

	@Override
	protected Fragment onCreateFragment() {
		return RecurrenceEditFragment.of(mRecurrence);
	}

	@Override
	public void onBackPressed() {
		onDonePressed();
	}

	@Override
	protected void onDonePressed() {
		Intent intent = new Intent();
		Recurrence recurrence = ((RecurrenceEditFragment)getFragment()).getEditedRecurrence();
		intent.putExtra(KEY_RECURRENCE, recurrence);
		setResult(RESULT_OK, intent);
		finish();
		super.onDonePressed();
	}
}