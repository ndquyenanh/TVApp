package com.sec.samsung.tvcontentsync;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.sec.samsung.download.DownloadFile;
import com.sec.samsung.manager.AliveService;
import com.sec.samsung.utils.Debug;
import com.sec.samsung.utils.Define;
import com.sec.samsung.utils.SettingManager;
import com.sec.samsung.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 * Created by sev_user on 12/7/2016.
 */

public class TvContentSyncApplication extends Application {

    private static TvContentSyncApplication instance;

    private Handler mDebugHandler;
    private Context mCurActivity;
    private StringBuilder mStrBuilder;
    private boolean isUpdateLogScreen = false;
    private ArrayList<DownloadFile> downloadFileList = new ArrayList<>();

    private boolean isBroadcasting;
    private long broadcastFinishTime;

    //public TvContentSyncApplication() {
    //    Debug.log();
    //    downloadFileList = new ArrayList<>();
    //    isUpdateLogScreen = false;
    //}

    public static synchronized TvContentSyncApplication getInstance() {
        if (instance == null) {
            instance = new TvContentSyncApplication();
        }
        return instance;
    }

    public void setBroadcasting(boolean status) {
        this.isBroadcasting = status;
    }

    public boolean isBroadcasting() {
        return isBroadcasting;
    }

    public long getBroadcastFinishTime() {
        return broadcastFinishTime;
    }

    public void setBroadcastFinishTime(long broadcastFinishTime) {
        this.broadcastFinishTime = broadcastFinishTime;
    }

    public void setCurActivity(Context context) {
        mCurActivity = context;
    }

    public Activity getCurActivity() {
        if (mCurActivity instanceof Activity)
            return (Activity) mCurActivity;

        return null;
    }

    public void clearLog() {
        mStrBuilder = new StringBuilder("");
    }

    public String getLog() {
        if (mStrBuilder != null)
            return mStrBuilder.toString();
        else
            return "";
    }

    public void updateLogScreen(String log) {
        try {
            if (mStrBuilder == null)
                mStrBuilder = new StringBuilder("");

            String msgObj = log + System.getProperty("line.separator");
            mStrBuilder.append(msgObj);
            if (mCurActivity instanceof MainActivity) {
                MainActivity ma = (MainActivity) mCurActivity;
                if (mDebugHandler == null) {
                    mDebugHandler = ma.getDebugHandler();
                }

                if (isUpdateLogScreen) {
                    ma.setShowView(isUpdateLogScreen);
                    Message msg = mDebugHandler.obtainMessage();
                    msg.what = 1;
                    msg.obj = msgObj;
                    mDebugHandler.sendMessage(msg);
                } else {
                    ma.setShowView(false);
                }
            }
        } catch (Exception ex) {
            Debug.logW(ex.getMessage());
        }
    }

    @Override
    public void onCreate() {
        // startService(new Intent(this, AliveService.class));
        Debug.log("Init app");
        // Setup handler for uncaught exceptions.
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                try {
                    handleUncaughtException(thread, e);
                    // Utils.showToast(TvContentSyncApplication.this, "Hello from App!!!");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        super.onCreate();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Debug.log();
        Utils.restartApp(getApplicationContext());
    }

    public void handleUncaughtException(Thread thread, Throwable e) throws IOException {
        File f = new File(Define.APP_PATH_LOGS + "log_exception_" + Utils.long2DateTimeFile(System.currentTimeMillis()) + ".txt");
        f.createNewFile();
        e.printStackTrace(new PrintStream(f)); // not all Android versions will print the stack trace automatically
        Debug.logE(e.getMessage());

        Intent intent = new Intent();
        intent.setAction("com.sec.samsung.tvcontentsync.SEND_LOG"); // see step 5.
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // required when starting from Application
        intent.putExtra("exception_name", e.getMessage());
        startActivity(intent);

        System.exit(1); // kill off the crashed app
    }

    public boolean isUpdateLogScreen() {
        return isUpdateLogScreen;
    }

    public void setUpdateLogScreen(boolean updateLogScreen) {
        isUpdateLogScreen = updateLogScreen;
    }

    private boolean isShowHeadUp = false;

    public void updateNotification(Context c, String msg) {
        NotificationManager nm = (NotificationManager) c.getSystemService(NOTIFICATION_SERVICE);
        PendingIntent pi = PendingIntent.getActivity(c, 0, new Intent(c, MainActivity.class), 0);
        int id = R.string.app_name;

        Notification.Builder n = new Notification.Builder(c).
                setContentTitle(msg).
                setContentText("App version " + Utils.getVersionName(c) + "(contact: vuong.quyen@samsung.com)").
                setTicker("setTicker").
                setSmallIcon(R.mipmap.app_icon).
                setOngoing(true).
                setContentIntent(pi).
                // setDefaults(Notification.DEFAULT_ALL).
                        setPriority(Notification.PRIORITY_HIGH);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isShowHeadUp)
            n.setVibrate(new long[0]);

        nm.notify(id, n.build());
    }

    public void updateNotification(Context c, int stt) {
        NotificationManager nm = (NotificationManager) c.getSystemService(NOTIFICATION_SERVICE);
        PendingIntent pi = PendingIntent.getActivity(c, 0, new Intent(c, MainActivity.class), 0);
        int id = R.string.app_name;

        Notification.Builder n = new Notification.Builder(c).
                setContentTitle(Utils.getNameStt(stt) + SettingManager.getIPServer(c)).
                setContentText("App version " + Utils.getVersionName(c) + "(contact: vuong.quyen@samsung.com)").
                setTicker("setTicker").
                setSmallIcon(R.mipmap.app_icon).
                setOngoing(true).
                setContentIntent(pi).
                // setDefaults(Notification.DEFAULT_ALL).
                        setPriority(Notification.PRIORITY_HIGH);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isShowHeadUp)
            n.setVibrate(new long[0]);

        nm.notify(id, n.build());
    }

    public void cancelNotification(Context c) {
        NotificationManager nm = (NotificationManager) c.getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(R.string.app_name);
    }

    public synchronized ArrayList<DownloadFile> getDownloadFileList() {
        return downloadFileList;
    }
}
