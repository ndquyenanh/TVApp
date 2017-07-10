package com.sec.samsung.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Qnv96 on 06-Jan-17.
 */

public class SettingManager {

    public static SharedPreferences mSharedPreferences;

    public static <T> void saveSetting(Context c, String[] keys, T... values) {
        if (mSharedPreferences == null) {
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(c);
        }

        SharedPreferences.Editor e = mSharedPreferences.edit();
        for (int i = 0; i < keys.length; i++) {
            if (values[i] instanceof Integer) {
                e.putInt(keys[i], Integer.parseInt(values[i] + ""));
            } else if (values[i] instanceof String) {
                e.putString(keys[i], values[i].toString());
            } else if (values[i] instanceof Boolean) {
                e.putBoolean(keys[i], (Boolean) values[i]);
            }else if (values[i] instanceof Long){
                e.putLong(keys[i], (Long)values[i]);
            }
        }

        e.commit();
    }

    public static <T> void saveSetting(Context c, String key, T value) {
        if (mSharedPreferences == null) {
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(c);
        }

        SharedPreferences.Editor e = mSharedPreferences.edit();
        if (value instanceof Integer) {
            e.putInt(key, Integer.parseInt(value + ""));
        } else if (value instanceof String) {
            e.putString(key, value.toString());
        } else if (value instanceof Boolean) {
            e.putBoolean(key, (Boolean) value);
        } else if (value instanceof Long) {
            e.putLong(key, (Long) value);
        } else if (value instanceof Float) {
            e.putFloat(key, (Float) value);
        }

        e.commit();
    }

    public static String getStringInfoFromPreference(Context context, String key) {
        if (mSharedPreferences == null) {
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }

        String result = mSharedPreferences.getString(key, " ");
        return result;
    }

    public static <T> T getSetting(Context context, String key, Class clz) {
        if (mSharedPreferences == null) {
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }

        if (clz.getSimpleName().equals("String")) {
            String result = mSharedPreferences.getString(key, " ");
            return (T) result;
        }

        if (clz.getSimpleName().equals("Integer")) {
            int result = mSharedPreferences.getInt(key, -1);
            return (T) Integer.valueOf(result);
        }

        if (clz.getSimpleName().equals("Boolean")) {
            boolean result = mSharedPreferences.getBoolean(key, false);
            return (T) Boolean.valueOf(result);
        }

        if (clz.getSimpleName().equals("Long")) {
            long result = mSharedPreferences.getLong(key, -1);
            return (T) Long.valueOf(result);
        }

        if (clz.getSimpleName().equals("Float")) {
            float result = mSharedPreferences.getFloat(key, -1.0f);
            return (T) Float.valueOf(result);
        }

        return null;
    }

    public static void setStringInfoToPreference(Context context, String key, String value) {
        if (mSharedPreferences == null) {
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }

        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putString(key, value);
        edit.commit();
    }

    public static boolean getModeUser(Context c) {
        if (mSharedPreferences == null) {
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(c);
        }

        return mSharedPreferences.getBoolean(Define.USER_MODE, false);
    }

    public static String getIPServer(Context c) {
        if (mSharedPreferences == null) {
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(c);
        }

        return mSharedPreferences.getString(Define.IP_ADDRESS, "107.113.112.18");
    }

    public static void saveIPServer(Context c, String ip) {
        Debug.log("IP = " + ip);
        if (mSharedPreferences == null) {
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(c);
        }

        mSharedPreferences.edit().putString(Define.IP_ADDRESS, ip).commit();
    }

    public static boolean isLaunchWithDevice(Context c) {
        if (mSharedPreferences == null) {
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(c);
        }

        return mSharedPreferences.getBoolean("start_boot", true);
    }
}
