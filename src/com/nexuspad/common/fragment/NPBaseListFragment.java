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
public class NPBaseListFragment extends Fragment {
	public static final String TAG = NPBaseListFragment.class.getSimpleName();

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
	protected void hideProgressIndicatorAndShowMainList() {
		if (mLoadingUiManager != null)
			mLoadingUiManager.showListView();
	}

	protected void hideProgressIndicatorAndShowEmptyFolder() {
		if (mLoadingUiManager != null)
			mLoadingUiManager.showEmptyFolderView();
	}

	protected void showProgressIndicator() {
		if (mLoadingUiManager != null)
			mLoadingUiManager.showProgressView();
	}

	protected void displayRetry() {
		if (mLoadingUiManager != null)
			mLoadingUiManager.showRetryView();
	}

	protected static final class LoadingUiManager {
		private final View mMainListViewFrame;
		private final View mRetryView;
		private final View mProgressView;

		private final View mEmptyFolderView;

		private final Animation mFadeOutAnimation;
		private final Animation mFadeInAnimation;

		protected LoadingUiManager(View listV, View emptyFolderView, View retryV, View progressV, View.OnClickListener onRetryListener) {
			mMainListViewFrame = listV;
			mEmptyFolderView = emptyFolderView;
			mRetryView = retryV;
			mProgressView = progressV;

			mProgressView.setVisibility(View.VISIBLE);
			mRetryView.setVisibility(View.GONE);
			mMainListViewFrame.setVisibility(View.GONE);

			if (mEmptyFolderView != null) {
				mEmptyFolderView.setVisibility(View.GONE);
			}

			final Context context = listV.getContext();
			mFadeOutAnimation = loadAnimation(context, android.R.anim.fade_out);
			mFadeInAnimation = loadAnimation(context, android.R.anim.fade_in);

			mRetryView.findViewById(R.id.btn_retry).setOnClickListener(onRetryListener);
		}

		protected void showListView() {
			boolean isRetryVisible = mRetryView.getVisibility() == View.VISIBLE;
			boolean isProgressVisible = mProgressView.getVisibility() == View.VISIBLE;
			boolean isEmptyFolderViewVisible = mEmptyFolderView != null && mEmptyFolderView.getVisibility() == View.VISIBLE ? true : false;

			if (isEmptyFolderViewVisible) {
				fadeOut(mEmptyFolderView);
			}

			if (isRetryVisible) {
				fadeOut(mRetryView);
			}

			if (isProgressVisible) {
				fadeOut(mProgressView);
			}

			fadeIn(mMainListViewFrame);
		}

		protected void showEmptyFolderView() {
			boolean isListVisible = mMainListViewFrame.getVisibility() == View.VISIBLE;
			boolean isProgressVisible = mProgressView.getVisibility() == View.VISIBLE;

			if (isListVisible) {
				fadeOut(mMainListViewFrame);
			}
			if (isProgressVisible) {
				fadeOut(mProgressView);
			}

			fadeIn(mEmptyFolderView);
		}

		protected void showRetryView() {
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

		protected void showProgressView() {
			boolean isListVisible = mMainListViewFrame.getVisibility() == View.VISIBLE;
			boolean isRetryVisible = mRetryView.getVisibility() == View.VISIBLE;
			boolean isEmptyFolderViewVisible = mEmptyFolderView != null && mEmptyFolderView.getVisibility() == View.VISIBLE ? true : false;

			if (isEmptyFolderViewVisible) {
				fadeOut(mEmptyFolderView);
			}

			if (isListVisible) {
				fadeOut(mMainListViewFrame);
			}
			if (isRetryVisible) {
				fadeOut(mRetryView);
			}

			fadeIn(mProgressView);
		}

		protected void hideProgressFrame() {
			fadeOut(mProgressView);
		}

		private void fadeOut(View view) {
			if (view == null) return;
			view.startAnimation(mFadeOutAnimation);
			view.setVisibility(View.GONE);
		}

		private void fadeIn(View view) {
			if (view == null) return;
			view.setVisibility(View.VISIBLE);
			view.startAnimation(mFadeInAnimation);
		}
	}
}
