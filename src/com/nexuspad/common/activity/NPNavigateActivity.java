/*
 * Copyright 2013 Edmond Chui
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nexuspad.common.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import com.nexuspad.common.annotaion.ParentActivity;

/**
 * A class which simplifies the "up" button implementation.
 * <p>
 * If the subclass has a annotation, the "up" button will
 * behave as specified in the Android Design Guideline, which starts the parent
 * {@code Activity} with the correct {@code Intent} flags, and calls
 * {@link #finish()} on the current {@code Activity}.
 * <p>
 * Alternatively, you can pass in the parent {@code Activity} class as a
 * {@code Serializable} extra with the key {@link #KEY_PARENT_ACTIVITY}. This
 * will take precedence over the annotation.
 * <p>
 * You may adjust these behaviours using {@link #onUpPressed()} and
 * {@link #getGoBackIntent(Class)}.
 *
 * @author Edmond
 *
 */
public abstract class NPNavigateActivity extends FragmentActivity {
    /**
     * The key for passing a parent Activity with {@code Intent}.
     *
     * @see android.content.Intent#putExtra(String, java.io.Serializable)
     * @see android.os.Bundle#putSerializable(String, java.io.Serializable)
     */
    public static final String KEY_PARENT_ACTIVITY = "parent_activity";

    private Class<?> mParentActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mParentActivity = (Class<?>)getIntent().getSerializableExtra(KEY_PARENT_ACTIVITY);
        if (mParentActivity == null) {
            ParentActivity upAnnotaion = getClass().getAnnotation(ParentActivity.class);
            if (upAnnotaion != null) {
                mParentActivity = upAnnotaion.value();
            }
        }

        if (mParentActivity != null) {
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (!onUpPressed()) {
                    if (mParentActivity != null) {
                        startActivity(getGoBackIntent(mParentActivity));
                        finish();
                        return true;
                    }
                }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Called every time the "up" button is selected.<br/>
     *
     * @return true if this event is consumed
     */
    protected boolean onUpPressed() {
        return false;
    }

    /**
     * Subclasses may override this method to return a custom {@link android.content.Intent}.
     * This is called every time {@link #onUpPressed()} returns false.
     *
     * @param activity
     * @return
     *         a non-null {@code Activity} {@link android.content.Intent} which will be passed
     *         to {@link #startActivity(android.content.Intent)}.
     */
    protected Intent getGoBackIntent(Class<?> activity) {
        Intent intent = new Intent(this, activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    protected Class<?> getGoBackActivity() {
        return mParentActivity;
    }
}
