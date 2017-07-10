package com.sec.samsung.manager;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;

import com.sec.samsung.connect.CheckConnectService;
import com.sec.samsung.connect.ControlClient;
import com.sec.samsung.connect.PinkService;
import com.sec.samsung.tvcontentsync.MainActivity;
import com.sec.samsung.tvcontentsync.R;
import com.sec.samsung.tvcontentsync.TvContentSyncApplication;
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
 * Created by Qnv96 on 11-May-17.
 */

public class AliveService extends Service {

    private Timer mTimer;
//    private Timer mCheckANRTimer;

    public AliveService() {
        Debug.log();
        Utils.appServiceStatus = Define.IDLE_SERVICE_STATUS;
    }

    private NotificationManager mNM;
    // private TvContentSyncApplication mApp;
    private static ControlClient mControlClient;

    private void showNotification() {
        Debug.log();
        int NOTIFICATION = R.string.app_name;

        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = "App version " + Utils.getVersionName(this);

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.app_icon)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setOngoing(true)
                .setContentTitle("Please contact us: vuong.quyen@samsung.com")  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Debug.log();

//        while (true){
//            boolean conn = mControlClient.isConnected();
//            Debug.log("conn = " + conn);
//            if (conn)
//                continue;
//            else {
//                mControlClient.closeSocket();
//                Utils.startCheckConnectService(this);
//                break;
//            }
//        }

        return START_NOT_STICKY;
    }

    private Context mContext;
    //private ToastReceiver receiver;

    @Override
    public void onCreate() {
        Debug.log();
        long TIME_FOR_CHECK = 20 * 1000;

        mContext = this;
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.  We put an icon in the status bar.
        // showNotification();

        //mApp = TvContentSyncApplication.getInstance();
        //mApp.updateNotification(mContext, Define.CONNECTED_STATUS);
        if (mControlClient == null) {
            mControlClient = ControlClient.getInstance(mContext);
        }

        mTimer = new Timer();
        //receiver = new ToastReceiver();
        //registerReceiver(receiver, new IntentFilter(Define.SHOW_TOAST));

        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Socket soc = new Socket();
                try {
                    soc.connect(new InetSocketAddress(SettingManager.getIPServer(mContext), ControlClient.PORT));
                    if (Utils.appStatus == Define.IDLE_STATUS)
                        Debug.log("Connected OK " + Utils.getNameAppStatus(Utils.appStatus));

                    if (!Utils.isAppRunning){
                        restartApp();
                    }
                } catch (IOException e) {
                    Debug.logW(e);
                    if (!Utils.isServiceRunning(PinkService.class, mContext)){
                        startService(new Intent(mContext, PinkService.class));
                    }

                    if (Utils.mConnStatus != Define.CONNECTING_STATUS){
                        mControlClient.closeSocket();
                        Utils.startCheckConnectService(mContext);
                    }
                }finally {
                    try {
                        soc.close();
                        soc = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        },10, TIME_FOR_CHECK);

//        mCheckANRTimer = new Timer();
//        mCheckANRTimer.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                Debug.logV("Send to service check");
//                sendBroadcast(new Intent("com.sec.samsung.tvsync.data"));
//            }
//        }, 0, 5000);

        super.onCreate();
    }

    private void restartApp() {
        Debug.log("Launching app!!");
        Intent startIntent = new Intent(mContext, MainActivity.class);
        startIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(startIntent);
    }

    @Override
    public void onDestroy() {
        Debug.log("onDestroy");
        //mApp.updateNotification(mContext, Define.CONNECTING_STATUS);

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        mControlClient = null;

//        if (mCheckANRTimer != null) {
//            mCheckANRTimer.cancel();
//            mCheckANRTimer = null;
//        }
//        if (receiver != null) {
//            unregisterReceiver(receiver);
//            receiver = null;
//        }

        //mApp = null;
        stopSelf();
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Debug.logW("Service removed");
        Intent restartServiceTask = new Intent(getApplicationContext(), CheckConnectService.class);
        restartServiceTask.setAction(Define.ACTION_START_CONNECT_THREAD);

        restartServiceTask.setPackage(getPackageName());
        PendingIntent restartPendingIntent =PendingIntent.getService(getApplicationContext(), 1,restartServiceTask, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager myAlarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        myAlarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartPendingIntent);

        super.onTaskRemoved(rootIntent);
    }

    private static class ToastReceiver extends BroadcastReceiver{

        public ToastReceiver() {
        }

        /**
         * This method is called when the BroadcastReceiver is receiving an Intent
         * broadcast.  During this time you can use the other methods on
         * BroadcastReceiver to view/modify the current result values.  This method
         * is always called within the main thread of its process, unless you
         * explicitly asked for it to be scheduled on a different thread using
         * {@link Context#registerReceiver(BroadcastReceiver, * IntentFilter, String, Handler)}. When it runs on the main
         * thread you should
         * never perform long-running operations in it (there is a timeout of
         * 10 seconds that the system allows before considering the receiver to
         * be blocked and a candidate to be killed). You cannot launch a popup dialog
         * in your implementation of onReceive().
         * <p>
         * <p><b>If this BroadcastReceiver was launched through a &lt;receiver&gt; tag,
         * then the object is no longer alive after returning from this
         * function.</b>  This means you should not perform any operations that
         * return a result to you asynchronously -- in particular, for interacting
         * with services, you should use
         * {@link Context#startService(Intent)} instead of
         * {@link Context#bindService(Intent, ServiceConnection, int)}.  If you wish
         * to interact with a service that is already running, you can use
         * {@link #peekService}.
         * <p>
         * <p>The Intent filters used in {@link Context#registerReceiver}
         * and in application manifests are <em>not</em> guaranteed to be exclusive. They
         * are hints to the operating system about how to find suitable recipients. It is
         * possible for senders to force delivery to specific recipients, bypassing filter
         * resolution.  For this reason, {@link #onReceive(Context, Intent) onReceive()}
         * implementations should respond only to known actions, ignoring any unexpected
         * Intents that they may receive.
         *
         * @param context The Context in which the receiver is running.
         * @param intent  The Intent being received.
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getStringExtra(Define.MSG_SHOW_TOAST);
            int id = intent.getIntExtra(Define.ID_SHOW_TOAST, -1);

            if (id == -1){
                //Utils.showToast(context, msg);
            }else {
                //Utils.showToastLong(context, id, msg);
            }

        }
    }
}
