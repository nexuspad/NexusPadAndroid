/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.common.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.nexuspad.R;
import com.nexuspad.common.utils.UndoBarController;

/**
 * Unlike {@link android.support.v4.app.ListFragment}, this {@code Fragment}
 * does not throw if your layout does not contain {@link android.R.id#list}
 *
 * @author Edmond
 */
public abstract class UndoBarFragment extends NPBaseFragment implements UndoBarController.UndoBarListener {
	public static final String TAG = UndoBarFragment.class.getSimpleName();

	private UndoBarController mUndoBarController;

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mUndoBarController != null) {
			mUndoBarController.onSaveInstanceState(outState);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		/*
		 * The undo bar for folder deletion.
		 */
		final View undoBarV = view.findViewById(R.id.undobar);
		if (undoBarV != null) {
			mUndoBarController = new UndoBarController(undoBarV, this);
			mUndoBarController.onRestoreInstanceState(savedInstanceState);
		}
	}

	protected void hideUndoBar(boolean immediate) {
		getUndoBarController().hideUndoBar(immediate);
	}

	protected void showUndoBar(boolean immediate, CharSequence message, Intent undoToken) {
		getUndoBarController().showUndoBar(immediate, message, undoToken);
	}

	protected UndoBarController getUndoBarController() {
		if (mUndoBarController == null) {
			throw new IllegalStateException("the layout does not contain R.id.undobar");
		}
		return mUndoBarController;
	}
}