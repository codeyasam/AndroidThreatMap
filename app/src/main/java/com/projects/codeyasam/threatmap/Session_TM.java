package com.projects.codeyasam.threatmap;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by codeyasam on 9/2/16.
 */
public class Session_TM {

    public static final String LOGGED_USER_ID = "logged_user_id";
    public static final String LOGGED_USER_TYPE = "logged_user_type";

    //only for register, since admin wont be able to register in app as an admin
    public static void logUser(Activity activity, String userId) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(LOGGED_USER_TYPE, "CLIENT");
        editor.putString(LOGGED_USER_ID, userId);
        editor.commit();
    }

    //to identify when logging in since users are separated in 2 tables (was not choice)
    public static void logUser(Activity activity, String userId, String userType) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(LOGGED_USER_TYPE, userType);
        editor.putString(LOGGED_USER_ID, userId);
        editor.commit();
    }

}
