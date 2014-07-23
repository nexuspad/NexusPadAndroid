package com.nexuspad.common.fragment;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ListView;
import com.nexuspad.R;

import static android.view.animation.AnimationUtils.loadAnimation;

/**
 * Created by ren on 7/23/14.
 */
public class NPBaseFragment extends Fragment {
	public static final String TAG = NPBaseFragment.class.getSimpleName();

	private LoadingUiManager mLoadingUiManager;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list_content, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final View listFrame = view.findViewById(R.id.frame_list);
		final View progressFrame = view.findViewById(R.id.frame_progress);
		final View retryFrame = view.findViewById(R.id.frame_retry);

		if (listFrame != null && progressFrame != null && retryFrame != null) {
			mLoadingUiManager = new LoadingUiManager(listFrame, retryFrame, progressFrame, new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mLoadingUiManager.fadeInProgressFrame();
					onRetryClicked(v);
				}
			});
		}
	}

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
	protected void fadeInListFrame() {
		mLoadingUiManager.fadeInListFrame();
	}

	protected void fadeInProgressFrame() {
		mLoadingUiManager.fadeInProgressFrame();
	}

	protected void fadeInRetryFrame() {
		mLoadingUiManager.fadeInRetryFrame();
	}

	protected static final class LoadingUiManager {
		private final View mListV;
		private final View mRetryV;
		private final View mProgressV;
		private final Animation mFadeOutA;
		private final Animation mFadeInA;

		protected LoadingUiManager(View listV, View retryV, View progressV, View.OnClickListener onRetryListener) {
			mListV = listV;
			mRetryV = retryV;
			mProgressV = progressV;

			mProgressV.setVisibility(View.VISIBLE);
			mRetryV.setVisibility(View.GONE);
			mListV.setVisibility(View.GONE);

			final Context context = listV.getContext();
			mFadeOutA = loadAnimation(context, android.R.anim.fade_out);
			mFadeInA = loadAnimation(context, android.R.anim.fade_in);

			mRetryV.findViewById(R.id.btn_retry).setOnClickListener(onRetryListener);
		}

		protected void fadeInListFrame() {
			boolean isRetryVisible = mRetryV.getVisibility() == View.VISIBLE;
			boolean isProgressVisible = mProgressV.getVisibility() == View.VISIBLE;

			if (isRetryVisible) {
				fadeOut(mRetryV);
			}
			if (isProgressVisible) {
				fadeOut(mProgressV);
			}

			fadeIn(mListV);
		}

		protected void fadeInRetryFrame() {
			boolean isListVisible = mListV.getVisibility() == View.VISIBLE;
			boolean isProgressVisible = mProgressV.getVisibility() == View.VISIBLE;

			if (isListVisible) {
				fadeOut(mListV);
			}
			if (isProgressVisible) {
				fadeOut(mProgressV);
			}

			fadeIn(mRetryV);
		}

		protected void fadeInProgressFrame() {
			boolean isListVisible = mListV.getVisibility() == View.VISIBLE;
			boolean isRetryVisible = mRetryV.getVisibility() == View.VISIBLE;

			if (isListVisible) {
				fadeOut(mListV);
			}
			if (isRetryVisible) {
				fadeOut(mRetryV);
			}

			fadeIn(mProgressV);
		}

		private void fadeOut(View view) {
			view.startAnimation(mFadeOutA);
			view.setVisibility(View.GONE);
		}

		private void fadeIn(View view) {
			view.setVisibility(View.VISIBLE);
			view.startAnimation(mFadeInA);
		}
	}
}
