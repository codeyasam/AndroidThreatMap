package com.projects.codeyasam.threatmap;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by codeyasam on 9/2/16.
 */
public class Session_TM {

    public static final String LOGGED_USER_ID = "logged_user_id";

    public static void logUser(Activity activity, String userId) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(LOGGED_USER_ID, userId);
        editor.commit();
    }

}
