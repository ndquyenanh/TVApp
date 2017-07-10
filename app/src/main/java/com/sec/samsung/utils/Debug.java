package com.sec.samsung.utils;

import android.util.Log;

import com.sec.samsung.tvcontentsync.TvContentSyncApplication;

/**
 * Created by sev_user on 11/16/2016.
 */

public class Debug {
    private static final String TAG = "TVContentSync";
    public static StringBuffer mTxtLog = new StringBuffer();

    public static void resetLog(){
        mTxtLog = new StringBuffer();
    }

    private static void updateLogScreen(String log){
        TvContentSyncApplication.getInstance().updateLogScreen(log);
    }

    public static void log(String text) {
        Log.d(TAG + convertText(), convertMethod() + text);
        mTxtLog.append(TAG + convertText() + "_" + convertMethod() + text).append("\n");
        updateLogScreen(convertMethod() + text);
    }

    public static void logV(String text) {
        Log.v(TAG + convertText(), convertMethod() + text);
        mTxtLog.append(TAG + convertText() + "_" + convertMethod() + text).append("\n");
        updateLogScreen(convertMethod() + text);
    }

    public static <T> void logV(T...ts) {
        StringBuffer buffer = new StringBuffer();
        for (T t : ts){
            buffer.append(t + ", ");
        }

        Log.v(TAG + convertText(), convertMethod() + buffer.toString());
        mTxtLog.append(TAG + convertText() + "_" + convertMethod() + buffer.toString()).append("\n");
        updateLogScreen(convertMethod() + buffer.toString());
    }

    public static void log() {
        Log.d(TAG + convertText(), convertMethod());
        mTxtLog.append(TAG + convertText() + "_" + convertMethod()).append("\n");
        updateLogScreen(convertMethod());
    }

    public static <T> void log(T...ts) {
        StringBuffer buffer = new StringBuffer();
        for (T t : ts){
            buffer.append(t + ", ");
        }

        Log.d(TAG + convertText(), convertMethod() + buffer.toString());
        mTxtLog.append(TAG + convertText() + "_" + convertMethod() + buffer.toString()).append("\n");
        updateLogScreen(convertMethod() + buffer.toString());
    }

    public static void logI(String text) {
        Log.i(TAG + convertText(), convertMethod() + text);
        updateLogScreen(convertMethod() + text);
        mTxtLog.append(TAG + convertText() + "_" + convertMethod() + text).append("\n");
    }

    public static <T> void logI(T...ts) {
        StringBuffer buffer = new StringBuffer();
        for (T t : ts){
            buffer.append(t + ", ");
        }

        Log.i(TAG + convertText(), convertMethod() + buffer.toString());
        updateLogScreen(convertMethod() + buffer.toString());
        mTxtLog.append(TAG + convertText() + "_" + convertMethod() + buffer.toString()).append("\n");
    }

    public static void logW(String text) {
        Log.w(TAG + convertText(), convertMethod() + text);
        updateLogScreen(convertMethod() + text);
        mTxtLog.append(TAG + convertText() + "_" + convertMethod() + text).append("\n");
    }

    public static void logE(String text) {
        Log.e(TAG + convertText(), convertMethod() + text);
        updateLogScreen(convertMethod() + text);
        mTxtLog.append(TAG + convertText() + "_" + convertMethod() + text).append("\n");
    }

    public static void logE(Exception e) {
        Log.e(TAG + convertText(), convertMethod() + e.getLocalizedMessage());
        updateLogScreen(convertMethod() + e.getLocalizedMessage());
        e.printStackTrace();
        mTxtLog.append(TAG + convertText() + "_" + convertMethod() + e.getLocalizedMessage()).append("\n");
    }

    public static void logW(Exception e) {
        Log.w(TAG + convertText(), convertMethod() + e.getMessage());
        e.printStackTrace();
        updateLogScreen(convertMethod() + e.getLocalizedMessage());
        mTxtLog.append(TAG + convertText() + "_" + convertMethod() + e.getLocalizedMessage()).append("\n");
    }

    public static void log(String subTag, String text) {
        Log.d(TAG + convertText(), convertMethod() + subTag + " " + text);
        updateLogScreen(convertMethod() + subTag + " " + text);
        mTxtLog.append(TAG + convertText() + "_" + convertMethod() + text).append("\n");
    }

    public static void logW(String subTag, String text) {
        Log.w(TAG + convertText(), convertMethod() + subTag + " " + text);
        updateLogScreen(convertMethod() + subTag + " " + text);
        mTxtLog.append(TAG + convertText() + "_" + convertMethod() + text).append("\n");
    }

    public static void logE(String subTag, String text) {
        Log.e(TAG + convertText(), convertMethod() + subTag + " " + text);
        updateLogScreen(convertMethod() + subTag + " " + text);
        mTxtLog.append(TAG + convertText() + "_" + convertMethod() + text).append("\n");
    }

    public static void logW(String text, Exception ex) {
        Log.w(TAG + convertText(), convertMethod() + text + " " + ex.getLocalizedMessage());
        ex.printStackTrace();
        updateLogScreen(convertMethod() + text + " " + ex);
        mTxtLog.append(TAG + convertText() + "_" + convertMethod() + text).append("\n");
    }

    public static void logW(String text, String subText, Exception ex) {
        Log.w(TAG + convertText(), convertMethod() + text + " " + subText + " " + ex);
        updateLogScreen(convertMethod() + text + " " + subText + " " + ex);
        mTxtLog.append(TAG + convertText() + "_" + convertMethod() + text).append("\n");
    }

    public static void logE(String text, Exception ex) {
        Log.e(TAG + convertText(), convertMethod() + text + " " + ex);
        updateLogScreen(convertMethod() + text + " " + ex);
        ex.printStackTrace();
        mTxtLog.append(TAG + convertText() + "_" + convertMethod() + text).append("\n");
    }

    public static void logD(String text) {
        Log.d(TAG + convertText(), convertMethod() + text);
        updateLogScreen(convertMethod() + text);
        mTxtLog.append(TAG + convertText() + "_" + convertMethod() + text).append("\n");
    }

    public static void logD(String subTag, String text) {
        Log.d(TAG + convertText(), convertMethod() + subTag + " " + text);
        updateLogScreen(convertMethod() + subTag + " " + text);
        mTxtLog.append(TAG + convertText() + "_" + convertMethod() + text).append("\n");
    }

    private static String convertText() {
        StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[4];
        String[] fileName = stackTrace.getFileName().split(".java");
        return "_" + fileName[0];
    }

    private static String convertMethod() {
        StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[4];
        return "[" + stackTrace.getLineNumber() + ":" + stackTrace.getMethodName() + "] ";
    }
}

