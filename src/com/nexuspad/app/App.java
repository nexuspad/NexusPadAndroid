/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.app;

import android.app.Activity;
import android.app.Application;
import android.graphics.Typeface;
import com.edmondapps.utils.android.Logs;
import com.nexuspad.BuildConfig;
import com.nexuspad.db.DatabaseManager;

/**
 * @author Edmond
 */
public class App extends Application {
    private static Typeface sRobotoLight;

    @Override
    public void onCreate() {
        super.onCreate();
        Logs.setShouldLog(BuildConfig.DEBUG);

        // initialize the database
        DatabaseManager.getDb(this);

        sRobotoLight = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
    }

    public static <C> C getCallback(Activity activity, Class<? extends C> callbackClass) {
        if (callbackClass.isInstance(activity)) {
            return callbackClass.cast(activity);
        } else {
            throw new IllegalStateException(activity + " must implement Callback.");
        }
    }

    public static Typeface getRobotoLight() {
        return sRobotoLight;
    }
}
