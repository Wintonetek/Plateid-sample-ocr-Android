package com.kernal.permission;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

public class SharedPreferencesHelper {
    private static SharedPreferencesHelper sharedPreferencesHelper = null;
    private static SharedPreferences sp = null;
    private static SharedPreferences.Editor editor = null;

    private static final String SHARE_NAME = "plateid";

    private static void createSp(Context context) {
        if (null == sp || null == editor) {
            sp = context.getSharedPreferences(SHARE_NAME, Context.MODE_PRIVATE);
            editor = sp.edit();
        }
    }

    public static void putInt(Activity activity, String key, int value) {
        createSp(activity);
        editor.putInt(key, value);
        editor.commit();
    }

    public static int getInt(Activity activity, String key, int defaultValue) {
        createSp(activity);
        return sp.getInt(key, defaultValue);
    }

    public static void putLong(Activity activity, String key, Long value) {
        createSp(activity);
        editor.putLong(key, value);
        editor.commit();
    }

    public static long getLong(Activity activity, String key, int defaultValue) {
        createSp(activity);
        return sp.getLong(key, defaultValue);
    }

    public static void putString(Activity activity, String key, String value) {
        createSp(activity);
        editor.putString(key, value);
        editor.commit();
    }

    public static String getString(Activity activity, String key, String defaultValue) {
        createSp(activity);
        return sp.getString(key, defaultValue);
    }

    public static void putFloat(Activity activity, String key, float value) {
        createSp(activity);
        editor.putFloat(key, value);
        editor.commit();
    }

    public static boolean isKeyExist(Activity activity, String key) {
        createSp(activity);
        return sp.contains(key);
    }

    public static float getFloat(Activity activity, String key, float defaultValue) {
        createSp(activity);
        return sp.getFloat(key, defaultValue);
    }

    public static void putBoolean(Activity activity, String key, boolean value) {
        createSp(activity);
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static boolean getBoolean(Activity activity, String key, boolean defaultValue) {
        createSp(activity);
        return sp.getBoolean(key, defaultValue);
    }

    public static void remove(String key) {
        if (null != editor) {
            editor.remove(key);
            editor.commit();
        }
    }
}