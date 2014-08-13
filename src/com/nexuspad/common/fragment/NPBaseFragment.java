package com.nexuspad.common.fragment;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ListView;
import com.nexuspad.R;

import static android.view.animation.AnimationUtils.loadAnimation;

/**
 * Created by ren on 7/23/14.
 */
public class NPBaseFragment extends Fragment {
	public static final String TAG = NPBaseFragment.class.getSimpleName();

	protected LoadingUiManager mLoadingUiManager;

	protected void onRetryClicked(View button) {
	}

	/**
	 * This method will be called when an item in the list is selected.
	 * Subclasses should override. Subclasses can call
	 * getListView().getItemAtPosition(position) if they need to access the
	 * data associated with the selected item.
	 *
	 * @param l        The ListView where the click happened
	 * @param v        The view that was clicked within the ListView
	 * @param position The position of the view in the list
	 * @param id       The row id of the item that was clicked
	 * @see UndoBarFragment#onListItemClick(android.widget.ListView, View, int, long)
	 */
	public void onListItemClick(ListView l, View v, int position, long id) {
	}

	/**
	 * Fade out the progress or retrying screen element.
	 */
	protected void dismissProgressIndicator() {
		if (mLoadingUiManager != null)
			mLoadingUiManager.fadeInListFrame();
	}

	protected void displayProgressIndicator() {
		if (mLoadingUiManager != null)
			mLoadingUiManager.fadeInProgressFrame();
	}

	protected void displayRetry() {
		if (mLoadingUiManager != null)
			mLoadingUiManager.fadeInRetryFrame();
	}

	protected static final class LoadingUiManager {
		private final View mMainListViewFrame;
		private final View mRetryView;
		private final View mProgressView;
		private final Animation mFadeOutAnimation;
		private final Animation mFadeInAnimation;

		protected LoadingUiManager(View listV, View retryV, View progressV, View.OnClickListener onRetryListener) {
			mMainListViewFrame = listV;
			mRetryView = retryV;
			mProgressView = progressV;

			mProgressView.setVisibility(View.VISIBLE);
			mRetryView.setVisibility(View.GONE);
			mMainListViewFrame.setVisibility(View.GONE);

			final Context context = listV.getContext();
			mFadeOutAnimation = loadAnimation(context, android.R.anim.fade_out);
			mFadeInAnimation = loadAnimation(context, android.R.anim.fade_in);

			mRetryView.findViewById(R.id.btn_retry).setOnClickListener(onRetryListener);
		}

		protected void fadeInListFrame() {
			boolean isRetryVisible = mRetryView.getVisibility() == View.VISIBLE;
			boolean isProgressVisible = mProgressView.getVisibility() == View.VISIBLE;

			if (isRetryVisible) {
				fadeOut(mRetryView);
			}

			if (isProgressVisible) {
				fadeOut(mProgressView);
			}

			fadeIn(mMainListViewFrame);
		}

		protected void fadeInRetryFrame() {
			boolean isListVisible = mMainListViewFrame.getVisibility() == View.VISIBLE;
			boolean isProgressVisible = mProgressView.getVisibility() == View.VISIBLE;

			if (isListVisible) {
				fadeOut(mMainListViewFrame);
			}
			if (isProgressVisible) {
				fadeOut(mProgressView);
			}

			fadeIn(mRetryView);
		}

		protected void fadeInProgressFrame() {
			boolean isListVisible = mMainListViewFrame.getVisibility() == View.VISIBLE;
			boolean isRetryVisible = mRetryView.getVisibility() == View.VISIBLE;

			if (isListVisible) {
				fadeOut(mMainListViewFrame);
			}
			if (isRetryVisible) {
				fadeOut(mRetryView);
			}

			fadeIn(mProgressView);
		}

		protected void fadeOutProgressFrame() {
			fadeOut(mProgressView);
		}

		private void fadeOut(View view) {
			view.startAnimation(mFadeOutAnimation);
			view.setVisibility(View.GONE);
		}

		private void fadeIn(View view) {
			view.setVisibility(View.VISIBLE);
			view.startAnimation(mFadeInAnimation);
		}
	}
}
