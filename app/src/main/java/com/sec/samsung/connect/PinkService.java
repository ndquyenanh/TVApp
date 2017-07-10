package com.sec.samsung.connect;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.sec.samsung.utils.Debug;
import com.sec.samsung.utils.Define;
import com.sec.samsung.utils.SettingManager;
import com.sec.samsung.utils.Utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Qnv96 on 06/08/2017.
 */

public class PinkService extends Service {

    private Context mContext;
    private static int mCount = 0;
    private static final long TIME_INTERVAL = 10 * 1000;
    private static final int MAX_TO_RESTART = 5;

    public PinkService() {
        Debug.log();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Debug.log();
        mContext = this;

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                //Socket soc = new Socket();
                try {
                    if (executeCommand()){
                        mCount++;
                        Debug.log("mCount = " + mCount);
                        if (mCount >= MAX_TO_RESTART && Utils.mConnStatus != Define.CONNECTED_STATUS && Utils.appStatus != Define.PLAYING_STATUS) {
                            SettingManager.saveSetting(mContext, Define.TIME_OF_SERVER_NOW, Utils.TIME_OFF_SET + System.currentTimeMillis());
                            Utils.restartDevice();
                        }
                    }
                } catch (Exception ex) {
                    Debug.logW(ex.getMessage());
                }
            }
        }, 50, TIME_INTERVAL);
    }

    private boolean executeCommand(){
        Debug.log("executeCommand");
        Runtime runtime = Runtime.getRuntime();
        try
        {
            Process  mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 " + SettingManager.getIPServer(mContext));
            int mExitValue = mIpAddrProcess.waitFor();
            Debug.log(" mExitValue "+mExitValue);
            if(mExitValue==0){
                return true;
            }else{
                return false;
            }
        }
        catch (InterruptedException ignore)
        {
            ignore.printStackTrace();
            Debug.logW(ignore.getMessage());
        }
        catch (IOException e)
        {
            Debug.logW(e.getMessage());
        }
        return false;
    }

    private Timer mTimer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Debug.log();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Debug.log();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    /**
     * Return the communication channel to the service.  May return null if
     * clients can not bind to the service.  The returned
     * {@link IBinder} is usually for a complex interface
     * that has been <a href="{@docRoot}guide/components/aidl.html">described using
     * aidl</a>.
     * <p>
     * <p><em>Note that unlike other application components, calls on to the
     * IBinder interface returned here may not happen on the main thread
     * of the process</em>.  More information about the main thread can be found in
     * <a href="{@docRoot}guide/topics/fundamentals/processes-and-threads.html">Processes and
     * Threads</a>.</p>
     *
     * @param intent The Intent that was used to bind to this service,
     *               as given to {@link Context#bindService
     *               Context.bindService}.  Note that any extras that were included with
     *               the Intent at that point will <em>not</em> be seen here.
     * @return Return an IBinder through which clients can call on to the
     * service.
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
