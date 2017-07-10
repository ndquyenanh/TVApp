package com.sec.samsung.tvcontentsync;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;

import com.sec.samsung.utils.Debug;
import com.sec.samsung.utils.Define;
import com.sec.samsung.utils.LockHome;
import com.sec.samsung.utils.SettingManager;
import com.sec.samsung.utils.Utils;

/**
 * Created by Qnv96 on 24-Dec-16.
 */

public abstract class BaseActivity extends AppCompatActivity {

    public int schedule_id;
    public int file_id;
    public long mTimePlaying;

    public static Handler mHandler;
    public static Runnable mRunnable;
    public TvContentSyncApplication mApp;
    public LockHome mLockHome;
    public boolean running = false;
    public Intent mIntent;
    // public boolean isNavigationbarExist = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.settingWritePermission(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        running = true;
        mIntent = getIntent();
        mApp = TvContentSyncApplication.getInstance();
        mApp.setCurActivity(this);
        mLockHome = new LockHome();
        // isNavigationbarExist = hasNavigationBar();
    }

    protected void hideNavigationBar(){
        //if (isNavigationbarExist){
            View decorView = getWindow().getDecorView();
            int uiOption = View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            decorView.setSystemUiVisibility(uiOption);
        //}
    }

    @Override
    protected void onResume() {
        super.onResume();
        mApp.setCurActivity(this);
        running = true;

        hideNavigationBar();
        if (this instanceof MainActivity) {
            Utils.appStatus = Define.IDLE_STATUS;
        }else {
            Utils.appStatus = Define.PLAYING_STATUS;
            Debug.log("Play file " + getFilePlaying() + " at " + Utils.long2DateTime(System.currentTimeMillis()));
        }

        if(Utils.settingOverlayPermission(this)){
            String model = Utils.getModelName();
            if (model.contains("SM-")){
                Utils.setScreenBright(this);
                mLockHome.setLock(this);
                Utils.setKeyGuard(this);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        running = false;
        if (SettingManager.getModeUser(this)/* && !Utils.isTablet(this)*/){
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            am.moveTaskToFront(getTaskId(), 0);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        running = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        running = false;
        if (mHandler != null && mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
        }
    }

    private boolean doubleBackToExit = false;
    @Override
    public void onBackPressed() {
        if (SettingManager.getModeUser(this)/* && !Utils.isTablet(this)*/) {
            Debug.log();
            return;
        }

        if (doubleBackToExit){
            super.onBackPressed();
            return;
        }

        doubleBackToExit = true;
        //Utils.showToast(this, "Press Back again to exit");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExit = false;
            }
        }, 1000);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (SettingManager.getModeUser(this)/* && !Utils.isTablet(this)*/) {
            Debug.log();
            if (!hasFocus) {
                Intent i = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                sendBroadcast(i);
            }
        } else {
            super.onWindowFocusChanged(hasFocus);
        }
    }

    public int getId(){
        return schedule_id;
    }

    public boolean isRunning(){
        return running;
    }

    /**
     * delay playing time
     */
    public void delayPlaying(){
        boolean t = mIntent.getBooleanExtra(Define.IS_OVER_TIME, false);
        if (t){
            Debug.log("Time over, play now");
            return;
        }

        long start_time = mIntent.getLongExtra(Define.ALARM_TIME, 0);
        long time_offset = mIntent.getLongExtra(Define.TIME_OFFSET, 0);
        long device_time = System.currentTimeMillis();

        long delay_time = Define.TIME_START_BEFORE + start_time - device_time - time_offset - Define.TIME_START_PLAY_BEFORE;
        Debug.log(getFilePlaying()
                + ", time start = " + Utils.long2DateTime(Define.TIME_START_BEFORE + start_time)
                + ", device time = " + Utils.long2DateTime(device_time)
                + ", delay_time = " + delay_time);
        if (delay_time > 0){
            try {
                Thread.sleep(delay_time);
            } catch (InterruptedException e) {
                Debug.logW(e);
            }
        }
    }

    protected boolean hasNavigationBar(){
        // boolean hasMenuKey = ViewConfiguration.get(this).hasPermanentMenuKey();
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
        boolean hasHomeKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME);
        Debug.log(
                //"hasMenuKey = " + hasMenuKey +
                        ", hasBackKey = " + hasBackKey + ", hasHomeKey = " + hasHomeKey);
        return (!(hasBackKey && hasHomeKey));
    }

    public void showProgressBar(boolean isShow){
        if (this instanceof MainActivity){
            ((MainActivity)this).showProgressBarDownload(isShow);
        }
    }

    /**
     * set screen bright, set current activity, lock device
     */
    public void setLockDevice(){
    }

    /**
     * cancel schedule
     */
    public abstract void cancelCalendar();

    /**
     * get file which is playing
     * @return file name
     */
    public abstract String getFilePlaying();

    //    @Override
//    public void onAttachedToWindow() {
//        if (Utils.getModeUser(this)){
//            getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
//                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
//                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
//                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
//            );
//
//            return;
//        }
//
//        super.onAttachedToWindow();
//    }
}
