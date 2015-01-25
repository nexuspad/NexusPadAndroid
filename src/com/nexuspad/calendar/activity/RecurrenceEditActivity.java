package com.nexuspad.calendar.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.nexuspad.calendar.fragment.RecurrenceEditFragment;
import com.nexuspad.common.Constants;
import com.nexuspad.common.activity.DoneDiscardActivity;
import com.nexuspad.service.datamodel.Recurrence;

/**
 * Created by ren on 8/3/14.
 */
public class RecurrenceEditActivity extends DoneDiscardActivity {
	private Recurrence mRecurrence;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setResult(RESULT_CANCELED);
		mRecurrence = getIntent().getParcelableExtra(Constants.KEY_RECURRENCE);

		mParentActivity = EventEditActivity.class;
		super.onCreate(savedInstanceState);
	}

	@Override
	protected Fragment onCreateFragment() {
		final Bundle bundle = new Bundle();
		bundle.putParcelable(Constants.KEY_RECURRENCE, mRecurrence);

		final RecurrenceEditFragment fragment = new RecurrenceEditFragment();
		fragment.setArguments(bundle);

		return fragment;
	}

	@Override
	public void onBackPressed() {
		onDonePressed();
	}

	@Override
	protected void onDonePressed() {
		Intent intent = new Intent();
		Recurrence recurrence = ((RecurrenceEditFragment)getFragment()).getEditedRecurrence();
		intent.putExtra(Constants.KEY_RECURRENCE, recurrence);
		setResult(RESULT_OK, intent);
		finish();
		super.onDonePressed();
	}
}