/*
 * Copyright 2012 Roman Nurik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nexuspad.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.TextView;
import com.nexuspad.R;

public class UndoBarController {
    private View mBarView;
    private TextView mMessageView;
    private ViewPropertyAnimator mBarAnimator;
    private Handler mHideHandler = new Handler();

    private UndoBarListener mUndoBarListener;

    // State objects
    private Intent mUndoToken;
    private CharSequence mUndoMessage;

    public interface UndoBarListener {
        void onUndoBarShown(Intent token);

        void onUndoBarHidden(Intent token);

        void onUndoButtonClicked(Intent token);
    }

    public UndoBarController(View undoBarView, UndoBarListener undoBarListener) {
        mBarView = undoBarView;
        mBarAnimator = mBarView.animate();
        mUndoBarListener = undoBarListener;

        mMessageView = (TextView) mBarView.findViewById(R.id.undobar_message);
        mBarView.findViewById(R.id.undobar_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideUndoBar(false);
                mUndoBarListener.onUndoButtonClicked(mUndoToken);
            }
        });

        hideUndoBar(true);
    }

    public void showUndoBar(boolean immediate, CharSequence message, final Intent undoToken) {
        final Resources resources = mBarView.getResources();

        mUndoToken = undoToken;
        mUndoMessage = message;
        mMessageView.setText(mUndoMessage);

        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, resources.getInteger(R.integer.undobar_hide_delay));

        mBarView.setVisibility(View.VISIBLE);
        if (immediate) {
            mBarView.setAlpha(1);
            mUndoBarListener.onUndoBarShown(undoToken);
        } else {
            mBarAnimator.cancel();
            mBarAnimator
                    .alpha(1)
                    .setDuration(resources.getInteger(android.R.integer.config_shortAnimTime))
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mUndoBarListener.onUndoBarShown(undoToken);
                        }
                    });
        }
    }

    public void hideUndoBar(boolean immediate) {
        mHideHandler.removeCallbacks(mHideRunnable);
        if (immediate) {
            mBarView.setVisibility(View.GONE);
            mBarView.setAlpha(0);

            mUndoBarListener.onUndoBarHidden(mUndoToken);

            mUndoMessage = null;
            mUndoToken = null;

        } else {
            mBarAnimator.cancel();
            mBarAnimator
                    .alpha(0)
                    .setDuration(mBarView.getResources().getInteger(android.R.integer.config_shortAnimTime))
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mBarView.setVisibility(View.GONE);

                            mUndoBarListener.onUndoBarHidden(mUndoToken);

                            mUndoMessage = null;
                            mUndoToken = null;
                        }
                    });
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putCharSequence("undo_message", mUndoMessage);
        outState.putParcelable("undo_token", mUndoToken);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mUndoMessage = savedInstanceState.getCharSequence("undo_message");
            mUndoToken = savedInstanceState.getParcelable("undo_token");

            if (mUndoToken != null || !TextUtils.isEmpty(mUndoMessage)) {
                showUndoBar(true, mUndoMessage, mUndoToken);
            }
        }
    }

    private Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hideUndoBar(false);
        }
    };
}
