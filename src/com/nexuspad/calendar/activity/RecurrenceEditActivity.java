package com.nexuspad.calendar.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.nexuspad.common.activity.DoneDiscardActivity;
import com.nexuspad.datamodel.Recurrence;

/**
 * Created by ren on 8/3/14.
 */
public class RecurrenceEditActivity extends DoneDiscardActivity {

	private Recurrence mRecurrence;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected Fragment onCreateFragment() {
		return null;
	}
}