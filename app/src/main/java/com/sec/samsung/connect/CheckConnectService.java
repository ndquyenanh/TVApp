package com.sec.samsung.connect;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.SystemClock;

import com.sec.samsung.manager.AliveService;
import com.sec.samsung.utils.Debug;
import com.sec.samsung.utils.Define;
import com.sec.samsung.utils.SettingManager;
import com.sec.samsung.utils.Utils;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class CheckConnectService extends Service {



    /**
     * The maximum time in milliseconds to wait for a connection to succeed before closing and retrying.
     */
    private int timeoutInterval = 2000;

    private int mCountRequestConnect = 0;
    private static final int MAX_REQUEST_COUNT = 6;
    //public static final String NOTI_CONNECT = "com.checkconnectservice.show.toast";
    // private static int mNotiCount = 0;

    // private TVWebSocketManager mWebSocketManager;
    private RequestConnectThread mRequestConnectThread;
    private ControlClient mControlClient;
    private Context mContext;

    public static Intent createConnectService(Context c, String action) {
        Debug.log(action);
        Intent intent = new Intent(c, CheckConnectService.class);
        if (action != null) {
            intent.setAction(action);
        }

        return intent;
    }

    public CheckConnectService() {
        Debug.log();
        Utils.appServiceStatus = Define.REQUESTING_CONNECT_STATUS;
    }

//    private Timer mCheckANRTimer;

    @Override
    public void onCreate() {
        super.onCreate();
        Debug.log("Start CheckConnectService");
        // mWebSocketManager = TVWebSocketManager.getInstance(this);
        try {
            mContext = this;
            mControlClient = ControlClient.getInstance(mContext);
            registerReceivers();

//            mCheckANRTimer = new Timer();
//            mCheckANRTimer.scheduleAtFixedRate(new TimerTask() {
//                @Override
//                public void run() {
//                    Debug.logV("Send to service check");
//                    sendBroadcast(new Intent("com.sec.samsung.tvsync.data"));
//                }
//            }, 0, 5000);
        }catch (Exception ex){
            Debug.logW(ex.getMessage());
            // restartApp();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            Debug.logD("action = " + action);

            if (Define.ACTION_START_CONNECT_THREAD.equalsIgnoreCase(action)) {
                startCheckConnectThread();
            } else if (Define.ACTION_REFRESH_CONNECT_TIME.equalsIgnoreCase(action)) {
                refreshConnectTime();
            }
        }

        return START_STICKY;
    }

    private void startCheckConnectThread() {
        Utils.mConnStatus = Define.CONNECTING_STATUS;
        mControlClient.updateNotify(this, Define.CONNECTING_STATUS);

        if (Utils.isServiceRunning(AliveService.class, mContext)) {
            mContext.stopService(new Intent(mContext, AliveService.class));
        }

        String ip = SettingManager.getIPServer(this);
        Debug.log("ip = " + ip);
        if (ip == null || ip.length() < 2) {
            Debug.logW("Ip address is not valid");
            return;
        }

        // if (mWebSocketManager.isConnected() ) {
        // stopSelf();
        // return;
        // }

        if (mControlClient.isConnected()) {
            Debug.log("Stop CheckConnectService");
            stopSelf();
            return;
        }

        stopRequestConnectThread();
        mRequestConnectThread = new RequestConnectThread();
        mRequestConnectThread.start();
    }

    private void refreshConnectTime() {
        Debug.logD("start check connect thread again");
        startCheckConnectThread();
    }

    @Override
    public void onDestroy() {
        Debug.logW("onDestroy checkConnect Service");
        super.onDestroy();
        //stopCheckConnectService();
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
           mBroadcastReceiver = null;
        }

        Utils.appServiceStatus = Define.IDLE_SERVICE_STATUS;

//        if (mCheckANRTimer != null) {
//            mCheckANRTimer.cancel();
//            mCheckANRTimer = null;
//        }
    }

    private void stopCheckConnectService() {
        stopRequestConnectThread();
        this.stopSelf();
        Utils.mCountConnect = 0;
    }

    private void stopRequestConnectThread() {
        Debug.log("destroy mRequestConnectThread");
        if (mRequestConnectThread != null) {
            mRequestConnectThread.stopSelf();
            mRequestConnectThread = null;
        }
    }

    private void registerReceivers() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Define.ACTION_STOP_CONNECT_THREAD);
        //intentFilter.addAction(NOTI_CONNECT);
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Debug.log("mBroadcastReceiver", "action : " + action);

            if (action.equals(Define.ACTION_STOP_CONNECT_THREAD)) {
                stopCheckConnectService();
            }
//            else if (action.equals(NOTI_CONNECT)) {
//                String msg = intent.getStringExtra(Define.MSG_SHOW_TOAST);
//                if (msg == null) {
//                    Utils.showToastLong(context, "Connecting to server " + SettingManager.getIPServer(context));
//                } else {
//                    Utils.showToastLong(context, msg);
//                }
//            }
        }
    };

    public class RequestConnectThread extends Thread {
        private int reconnectAttempts;
        private boolean isRunning;
        // private boolean isReset;

        public RequestConnectThread() {
            isRunning = true;

            Debug.log("mCountRequestConnect = " + mCountRequestConnect);
            if (mCountRequestConnect < MAX_REQUEST_COUNT) {
                reconnectAttempts = mCountRequestConnect;
                // isReset = true;
            } else {
                reconnectAttempts = 0;
                // isReset = false;
            }
        }

        @Override
        public void run() {
            while (isRunning) {

                /**
                 * The number of milliseconds to delay before attempting to reconnect.
                 */
                int reconnectInterval = 2 * 1000;

                /**
                 * The maximum number of milliseconds to delay a reconnection attempt.
                 */
                int maxReconnectInterval = 2 * 60 * 1000;

                /**
                 * The rate of increase of the reconnect delay. Allows reconnect attempts to back off when problems persist.
                 */
                float reconnectDecay = 2.0f;

                int TIMES_TO_RESTART = 12;

                // if (mWebSocketManager.isConnected()) {
                // stopSelf();
                // return;
                // }

                if (mControlClient.isConnected()) {
                    Debug.log("Device is connected, so return");
                    stopSelf();
                    return;
                }

                long timeout = (long) (reconnectInterval * Math.pow(reconnectDecay, reconnectAttempts));
                timeout = timeout > maxReconnectInterval ? maxReconnectInterval : timeout;
                reconnectAttempts++;
                mCountRequestConnect = reconnectAttempts;

                Utils.mCountConnect++;
                Debug.log("RequestConnectThread running!!! " + Utils.mCountConnect);
                if (Utils.mCountConnect >= TIMES_TO_RESTART){
                    // restartApp();
                }

//                mNotiCount++;
//                if (mNotiCount % 6 == 0){
//                    sendBroadcast(new Intent(NOTI_CONNECT));
//                    Debug.log("mNotiCount = " + mNotiCount);
//                }

                //if (isReset){
                //   timeout = 60 * 1000;
                //  isReset = false;
                //}

                Debug.log("RequestConnectThread", "timeout = " + timeout + ", reconnectAttempts = " + reconnectAttempts);

                if (!isRunning)
                    break;

                try {
                    sleep(timeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (!isRunning)
                    break;

                Debug.logD("request connect to server");
                // mWebSocketManager.connectWebSocket("ws://" + SettingManager.getIPServer(CheckConnectService.this) + ":9999");
                mControlClient.setIPADDR(SettingManager.getIPServer(mContext));
                mControlClient.start();
            }
        }

        public void stopSelf() {
            isRunning = false;
        }
    }

    private void restartApp() {
        if (Utils.appStatus != Define.PLAYING_STATUS){
            try {
                Debug.logE("Restart app");
                deleteLOG();

                Utils.saveLogFile();
                SettingManager.saveSetting(mContext, Define.JSON_RESTART_APP, true);
                mControlClient.updateNotify(mContext, -1);
                Utils.mConnStatus = 0;
            }catch (Exception e){
                Debug.logW(e.getMessage());
            }

            Utils.restartApp(mContext);
        }
    }

    private void deleteLOG(){
        try {
            File logs = new File(Define.APP_PATH_LOGS);
            if (!logs.exists())
                return;

            File[] files = logs.listFiles();
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                file.delete();
            }
        }catch (Exception e){
            Debug.logW(e.getMessage());
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Debug.logW("Service removed");
        Intent restartServiceTask = new Intent(getApplicationContext(), this.getClass());
        restartServiceTask.setAction(Define.ACTION_START_CONNECT_THREAD);

        restartServiceTask.setPackage(getPackageName());
        PendingIntent restartPendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceTask, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager myAlarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        myAlarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartPendingIntent);

        super.onTaskRemoved(rootIntent);
    }

//    private static BroadcastReceiver receiver = new BroadcastReceiver() {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Utils.showToast(context, "Connecting to server " + SettingManager.getIPServer(context));
//        }
//    };
}

