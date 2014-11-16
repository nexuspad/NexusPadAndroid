/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.app;

import android.app.Activity;
import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.widget.Toast;
import com.nexuspad.R;
import com.nexuspad.service.datastore.db.DatabaseManager;

import java.util.regex.Pattern;

/**
 * @author Edmond
 */
public class App extends Application {
    private static Typeface sRobotoLight;

    @Override
    public void onCreate() {
        super.onCreate();

        // initialize the database
        DatabaseManager.getDb(this);

        sRobotoLight = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
    }

    public static <C> C getCallbackOrThrow(Activity activity, Class<? extends C> callbackClass) {
        if (callbackClass.isInstance(activity)) {
            return callbackClass.cast(activity);
        } else {
            throw new IllegalStateException(activity + " must implement EntryDetailCallback.");
        }
    }

    public static void sendEmail(String address, Context context) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setType("plain/text");
        intent.setData(Uri.fromParts("mailto", address, null));
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException a) {
            // very rare, but anyway
            Toast.makeText(context, R.string.err_no_email_app, Toast.LENGTH_SHORT).show();
        }
    }

    public static Typeface getRobotoLight() {
        return sRobotoLight;
    }

    /**
     * A pattern that is used for searching.<br/>
     * If query is {@code "hell"}, it will match {@code "hell"}, {@code "hello"}, {@code "freaking hell"}, etc…; it will
     * <i>not</i> match {@code "ell"}, {@code "ohell"}, {@code "bell"}, etc….
     *
     * @param query the search keyword, you may consider trimming the query for better result matching
     * @return a case insensitive {@code Pattern} that matches "(.*?\s|\s*)query.*" where "query" is the parameter query
     */
    public static Pattern createSearchPattern(String query) {
        query = Pattern.quote(query);    // sanitize
        return Pattern.compile("(.*?\\s|\\s*)" + query + ".*", Pattern.CASE_INSENSITIVE);
    }

    /**
     * add {@code "http://"} in front of an url if a schema is not already in place.
     * </br>
     * It uses the {@code Pattern ".+://.*"} to check if the schema is in place.
     *
     * @param url the url ({@code "http://"} is optional)
     * @return the new {@code String} that contains {@code "http://"} in front, or the original {@code String} if no
     * changes were made.
     */
    public static String addSchemaIfRequired(String url) {
        return Pattern.compile(".+://.*").matcher(url).matches() ? url : "http://" + url;
    }
}
