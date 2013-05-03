/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.app;

import android.app.Application;

import com.edmondapps.utils.android.Logs;
import com.nexuspad.BuildConfig;
import com.nexuspad.db.DatabaseManager;

/**
 * @author Edmond
 * 
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Logs.setShouldLog(BuildConfig.DEBUG);

        // initialize the database
        DatabaseManager.getDb(this);
    }
}
