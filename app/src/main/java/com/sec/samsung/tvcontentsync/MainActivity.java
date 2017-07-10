package com.sec.samsung.tvcontentsync;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.sec.samsung.connect.CheckConnectService;
import com.sec.samsung.connect.ControlClient;
import com.sec.samsung.download.DownloadFile;
import com.sec.samsung.download.DownloadService;
import com.sec.samsung.manager.AliveService;
import com.sec.samsung.schedule.ScheduleDBManager;
import com.sec.samsung.schedule.ScheduleManager;
import com.sec.samsung.utils.Debug;
import com.sec.samsung.utils.Define;
import com.sec.samsung.utils.SettingManager;
import com.sec.samsung.utils.Utils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private Context mContext;

    // private ScheduleManager mScheduleManager;
    // private DownloadManager downloadManager;

//    public TextView getTxtDebug() {
//        return mTxtDebug;
//    }
//
//    public ScrollView getScrollText() {
//        return mScrollText;
//    }

    public void setShowView(boolean show) {
        if (mTxtDebug == null || mScrollText == null) {
            return;
        }

        if (show) {
            mTxtDebug.setVisibility(View.VISIBLE);
            mScrollText.setVisibility(View.VISIBLE);
        } else {
            mTxtDebug.setText("");
            mTxtDebug.setVisibility(View.GONE);
            mScrollText.setVisibility(View.GONE);
        }
    }

    public void showProgressBarDownload(boolean isShow) {
        setProgressBarIndeterminateVisibility(isShow);
    }

    private TextView mTxtDebug;
    private ScrollView mScrollText;

    // private TVWebSocketManager mWebSocketManager;
    private ControlClient mControlClient;
    //private int mCount = 0;
    //private String preDownloadSoFar = "";
    private PowerReceiver mPowerReceiver;
    private ActionBar mAB;
    // private Timer mTimer;

    private EditText mIpServerEt;
    private AlertDialog mDialog;
    // private boolean isUpdated = false;
    // private boolean isShowLog = false;

    /* private MenuItem mConnMenuItem;
    private MenuItem mSavelogMenuItem;
    private MenuItem mDellogMenuItem;
    private MenuItem mNetwMenuItem; */

//    private Handler mDebugHandler = new Handler() {
//
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            // if (isShowLog){
//                String log = (String) msg.obj;
//                updateLogScreen(log);
//            // }
//        }
//    };

    private DebugHandler mDH;

    private static class DebugHandler extends Handler {

        /**
         * Default constructor associates this handler with the {@link Looper} for the
         * current thread.
         * <p>
         * If this thread does not have a looper, this handler won't be able to receive messages
         * so an exception is thrown.
         */
        public DebugHandler() {
        }

        private WeakReference<TextView> mTv;
        private WeakReference<ScrollView> mSv;

        public void attach(TextView tv, ScrollView sv) {
            mTv = new WeakReference<>(tv);
            mSv = new WeakReference<>(sv);
        }

        public void detach() {
            mTv.clear();
            mSv.clear();
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    updateLogScreen(msg.obj.toString());
                    break;

                default:
                    break;
            }
        }

        private void updateLogScreen(String log) {
            TextView tv = mTv.get();
            final ScrollView sv = mSv.get();
            if (sv == null || tv == null) {
                return;
            }

            tv.append(log);
            sv.post(new Runnable() {
                @Override
                public void run() {
                    sv.fullScroll(View.FOCUS_DOWN);
                }
            });
        }
    }

    //private Handler mCheckDlHandler;
    //private Runnable mCheckDlRunnable;
    //private long mTimeCheckDl = 30 * 1000;
    //private long mTimeCheckDlDelay = 1 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utils.setAppRunning(true);
        super.onCreate(savedInstanceState);
        mContext = this;
        initialize();

        handleInfo();
        Intent i = new Intent("com.qnv96.apkinstaller.CheckService");
        i.setPackage(Define.SERVICE_INSTALLER_PKG);
        startService(i);
        //startService(new Intent("com.qnv96.apkinstaller.ServiceInstaller"));
    }

    private void initialize() {
        requestPermission();

        // requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mAB = getSupportActionBar();
        Drawable drawable = getResources().getDrawable(R.mipmap.app_icon);
        mAB.setLogo(drawable);
        mAB.setDisplayUseLogoEnabled(true);
        mAB.setDisplayShowHomeEnabled(true);
        // mAB.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#330000ff")));
        // mAB.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#550000ff")));

        //mScheduleManager = new ScheduleManager(this);
        mTxtDebug = (TextView) findViewById(R.id.txt_debug);
        mScrollText = (ScrollView) findViewById(R.id.scroll_txt_debug);
        if (mApp.isUpdateLogScreen()) {
            mTxtDebug.setVisibility(View.VISIBLE);
            mScrollText.setVisibility(View.VISIBLE);
        } else {
            mTxtDebug.setVisibility(View.GONE);
            mScrollText.setVisibility(View.GONE);
        }

        //if (!Utils.isPackageInstalled(Define.SERVICE_INSTALLER_PKG, getPackageManager())) {
            Utils.installService(this);
        //}

        //downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        //IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        //registerReceiver(downloadReceiver, filter);

//        mCheckDlHandler = new Handler();
//        mCheckDlRunnable = new Runnable() {
//            @Override
//            public void run() {
//                // checkDownloadStatus();
//                mCheckDlHandler.postDelayed(this, mTimeCheckDl);
//            }
//        };
//        mCheckDlHandler.postDelayed(mCheckDlRunnable, mTimeCheckDlDelay);

        // mWebSocketManager = TVWebSocketManager.getInstance(this);
        mControlClient = ControlClient.getInstance(this);
        mPowerReceiver = new PowerReceiver();
        registerReceiver(mPowerReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Define.CHECK_SERVER_CONN);
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        intentFilter.addAction(Define.CHECK_NETWORK_CONN);
        registerReceiver(ConnectReceiver, intentFilter);

        // mTimer = new Timer();
        // mTimer.scheduleAtFixedRate(new TimerTask() {
        //     @Override
        //     public void run() {
        //        Debug.log("Check connect: " + mControlClient.isConnected());
        //    }
        // }, 0, Define.CHECK_CONNECT_TIMER);

        File logFolder = new File(Define.APP_PATH_LOGS);
        if (!logFolder.exists()) {
            if (!logFolder.mkdirs()) {
                Debug.logW("Unable to create folder!!!");
            }
        }

        Debug.log("Utils.mConnStatus = " + Utils.mConnStatus);
        if (Utils.mConnStatus == 0)
            Utils.mConnStatus = Define.DISCONNECTED_STATUS;

        // mApp.updateNotification(this, Utils.getNameStt(Define.DISCONNECTED_STATUS) + SettingManager.getIPServer(this));
        Debug.log("initializing");
        mDH = new DebugHandler();

//        if (Utils.isConnectedNetwork(this)){
//            String ip = SettingManager.getSetting(this, MY_IP, String.class);
//            if (ip.equals(" "))
//                SettingManager.saveSetting(this, MY_IP, Utils.getIPAddress(true));
//        }
        // mDH.attach(mTxtDebug, mScrollText);
    }

    // private static final String MY_IP = "my_ip";

    private static final long TIME_FOR_RESTART = 50 * 1000;
    private void handleInfo() {
        String boot = mIntent.getStringExtra(Define.BOOT_COMPLETE);
        if (boot != null && boot.equalsIgnoreCase(Define.BOOT_OK)) {
            Debug.log("Start after boot complete!!!");
            long sv_time = SettingManager.getSetting(this, Define.TIME_OF_SERVER_NOW, Long.class);

            if (sv_time > 0) {
                sv_time += TIME_FOR_RESTART;
                mControlClient.scheduleAll(sv_time, System.currentTimeMillis());
            }

            if (Utils.isConnectedNetwork(this)) {
                String ip = SettingManager.getIPServer(this);
                startConnect(ip);
            } else {
                setTitleAB(R.string.nw_error);
            }
        } else {
            if (!Utils.isConnectedNetwork(this) /*&& !SettingManager.getModeUser(this)*/) {
                setTitleAB(R.string.nw_error);
            } else {
                int ver_code_pre = SettingManager.getSetting(this, Define.APK_PREV_VER, Integer.class);
                int sw_id = SettingManager.getSetting(this, Define.APK_UPDATE_SWID, Integer.class);
                String ip = SettingManager.getIPServer(this);
                Debug.log(ver_code_pre, sw_id, Utils.getVersionCode(this), ip);

                if (ver_code_pre != -1) {
                    mControlClient.setFileID(sw_id);
                    SettingManager.saveSetting(this, Define.APK_PREV_VER, -1);
                    Debug.log("Launch app after updated!!!");

                    startConnect(ip);
                    if (ver_code_pre != Utils.getVersionCode(this)) {
                        Debug.logI("Update OK");
                        Utils.sendUpdateResponse2Server(mContext, sw_id, true);
                    } else {
                        Debug.logW("Update failed");
                        Utils.sendUpdateResponse2Server(mContext, sw_id, false);
                    }
                } else {
                    boolean restart = SettingManager.getSetting(this, Define.JSON_RESTART_APP, Boolean.class);
                    if (restart) {
                        Debug.log("App relaunch");
                        String ip1 = SettingManager.getIPServer(this);
                        startConnect(ip1);

                        SettingManager.saveSetting(this, Define.JSON_RESTART_APP, false);
                    } else {
                        Debug.log("start normal");
                    }
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Debug.log();
//        String boot = mIntent.getStringExtra(Define.BOOT_COMPLETE);
//        if (boot != null && boot.equalsIgnoreCase(Define.BOOT_OK)) {
//            Debug.log("Start after boot complete!!!");
//            long sv_time = SettingManager.getSetting(this, Define.TIME_OF_SERVER_NOW, Long.class);
//
//            if (sv_time > 0){
//                // add time to restart device : 40s
//                sv_time += 40000;
//                mControlClient.scheduleAll(sv_time, System.currentTimeMillis());
//            }
//
//            /*long cl;
//            long sv = SettingManager.getSetting(this, Define.TIME_OF_SERVER_NOW, Long.class);
//
//            if (Utils.isConnectedNetwork(this)){
//                cl = System.currentTimeMillis();
//            }else {
//                cl = SettingManager.getSetting(this, Define.TIME_OF_CLIENT_NOW, Long.class);
//            }
//
//            Debug.log("cl = " + cl);
//            Debug.log("sv = " + sv);
//            if (cl > 0 && sv > 0) {
//                scheduleAll(sv, cl);
//            }*/
//
//            if (Utils.isConnectedNetwork(this)){
//                String ip = SettingManager.getIPServer(this);
//                startConnect(ip);
//                //isNetworkOK = true;
//            }else {
//                setTitleAB(R.string.nw_error);
//                //isNetworkOK = false;
//            }
//        } else {
//            if (!Utils.isConnectedNetwork(this) /*&& !SettingManager.getModeUser(this)*/) {
//                setTitleAB(R.string.nw_error);
//                //isNetworkOK = false;
//                // mConnMenuItem.setVisible(true);
//                /*Utils.showAlert(this, getResources().getString(R.string.connect_network), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
//                        finish();
//                    }
//                }, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        finish();
//                    }
//                });*/
//            }else {
//                //isNetworkOK = true;
//                int ver_code_pre = SettingManager.getSetting(this, Define.APK_PREV_VER, Integer.class);
//                int sw_id = SettingManager.getSetting(this, Define.APK_UPDATE_SWID, Integer.class);
//                String ip = SettingManager.getIPServer(this);
//                Debug.log(ver_code_pre, sw_id, Utils.getVersionCode(this), ip);
//
//                if (ver_code_pre != -1) {
//                    // mWebSocketManager.setFileID(sw_id);
//                    mControlClient.setFileID(sw_id);
//                    SettingManager.saveSetting(this, Define.APK_PREV_VER, -1);
//                    Debug.log("Launch app after updated!!!");
//                    // isUpdated = true;
//
//                    // long cl = System.currentTimeMillis();
//                    // long sv = SettingManager.getSetting(this, Define.TIME_OF_SERVER_NOW, Long.class);
//
//                    // Debug.log("cl = " + cl);
//                    // Debug.log("sv = " + sv);
//                    // if (cl > 0 && sv > 0) {
//                    //     scheduleAll(sv, cl);
//                    // }
//
//                    startConnect(ip);
//                    if (ver_code_pre != Utils.getVersionCode(this)) {
//                        Debug.logI("Update OK");
//                        Utils.sendUpdateResponse2Server(mContext, sw_id, true);
//                    } else {
//                        Debug.logW("Update failed");
//                        Utils.sendUpdateResponse2Server(mContext, sw_id, false);
//                    }
//                } else {
//                    boolean restart = SettingManager.getSetting(this, Define.JSON_RESTART_APP, Boolean.class);
//                    if (restart){
//                        Debug.log("App relaunch");
//                        String ip1 = SettingManager.getIPServer(this);
////                        long sv = SettingManager.getSetting(this, Define.TIME_OF_SERVER_NOW, Long.class);
////                        long device = System.currentTimeMillis();
////                        mControlClient.scheduleAll(sv + device, device);
//                        startConnect(ip1);
//
//                        SettingManager.saveSetting(this, Define.JSON_RESTART_APP, false);
//                    }else {
//                        Debug.log("start normal");
//                    }
//                    // isUpdated = false;
//                }
//            }
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Debug.log();

        // setTitleAB(mWebSocketManager.isConnected());
        if (Utils.isConnectedNetwork(this)) {
            setTitleAB(mControlClient.isConnected());
        } else {
            setTitleAB(R.string.nw_error);
        }

        if (SettingManager.getIPServer(this).equals(" ")) {
            Utils.showToast(this, getResources().getString(R.string.ip_sv_addr));
        }

        if (mTxtDebug != null) {
            mTxtDebug.setText(mApp.getLog());
        }

        mDH.attach(mTxtDebug, mScrollText);
//        if (Utils.mConnStatus == Define.CONNECTING_STATUS){
//            if (!Utils.isServiceRunning(CheckConnectService.class, this)){
//                Debug.logE("Run here");
//                Utils.startCheckConnectService(this);
//            }
//        }

        // Utils.appStatus = Define.IDLE_STATUS;

        /* if (checkMenuNotNull()){
            setVisibleMenuItem(Utils.isConnectedNetwork(this));
        } */
    }

    @Override
    protected void onPause() {
        super.onPause();
        Debug.log();
        mDH.detach();
    }

    /* private void setVisibleMenuItem(boolean visibleMenuItem){
        if (!visibleMenuItem){
            setTitleAB("Network unavailable!!!");
        }

        mNetwMenuItem.setVisible(!visibleMenuItem);
        mDellogMenuItem.setVisible(visibleMenuItem);
        mSavelogMenuItem.setVisible(visibleMenuItem);
        mConnMenuItem.setVisible(visibleMenuItem);
    } */

    private void setTitleAB(boolean isConnect) {
        String connect;
        if (isConnect) {
            connect = "Connected";
        } else {
            if (Utils.mConnStatus == Define.DISCONNECTED_STATUS) {
                connect = "Disconnected";
            } else if (Utils.mConnStatus == Define.CONNECTING_STATUS) {
                connect = "Connecting...";
            } else {
                connect = "Unknown";
            }
        }

        mAB.setTitle(getResources().getString(R.string.application_name) +
                "(v" + Utils.getVersionName(this) + ")" +
                // "-" + SettingManager.getIPServer(this) +
                "-" + connect);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Debug.logW("Destroy app!!");

        mApp.clearLog();
        Utils.setAppRunning(false);
        destroyReceiver();
        // mTimer.cancel();
        Debug.resetLog();
        // SettingManager.saveSetting(this, Define.TIME_OF_CLIENT_NOW, System.currentTimeMillis());
        // mDebugHandler.removeCallbacks(null);
        // mDebugHandler = null;
        //mCheckDlHandler.removeCallbacks(mCheckDlRunnable);
        //mCheckDlHandler = null;

        // mControlClient.closeSocket();
        // mControlClient.checkFinish();
        // mControlClient.setStopControlThreads();
        // mControlClient = null;
        // sendBroadcast(new Intent(Define.ACTION_STOP_CONNECT_THREAD));
        // mScheduleManager = null;
        //for (DownloadFile df : mApp.downloadFileList){
        //    downloadManager.remove(df.getDownloadId());
        //}
        //downloadManager = null;

        mDH.removeMessages(1);
//        mDH.post(new Runnable() {
//            @Override
//            public void run() {
//                Looper.myLooper().quit();
//            }
//        });
        mDH = null;
    }

    @Override
    public void cancelCalendar() {
        Debug.log("Cancel Calendar, do nothing here!!!!!");
    }

    @Override
    public String getFilePlaying() {
        return null;
    }

    public Handler getDebugHandler() {
        return mDH;
    }

//    private void updateLogScreen(String log) {
//        if (mTxtDebug != null) {
//            mTxtDebug.append(log);
//        }
//
//        if (mScrollText != null) {
//            mScrollText.post(new Runnable() {
//                @Override
//                public void run() {
//                    mScrollText.fullScroll(View.FOCUS_DOWN);
//                }
//            });
//        }
//    }

    /*tao lich cho tat ca */
//    public void scheduleAll(long serverTime, long deviceTime) {
//        Debug.log("deviceTime = " + Utils.long2DateTime(deviceTime) + ", serverTime = " + Utils.long2DateTime(serverTime));
//        if (mScheduleManager != null)
//            mScheduleManager.scheduleAll(serverTime, deviceTime);
//        else
//            Debug.logW("mScheduleManager = null");
//    }

//    public void cancelSchedule(int scheduleId) {
//        //xoa lich nay khoi co so du lieu
//        if (mScheduleManager != null)
//            mScheduleManager.cancelSchedule(scheduleId, true);
//        else
//            Debug.logW("mScheduleManager = null");
//    }

    private void startConnect(String ip) {
        SettingManager.saveIPServer(this, ip);
        if (Utils.mConnStatus == Define.NETWORK_ERROR_STATUS) {
            Utils.showToast(this, "Network not available, does not click more");
            return;
        }

        if (ip != null && ip.length() > 5 && Utils.mConnStatus == Define.DISCONNECTED_STATUS) {
            Utils.startCheckConnectService(this);
            Utils.mConnStatus = Define.CONNECTING_STATUS;
            setTitleAB(false);
            // mApp.updateNotification(this, Utils.mConnStatus);
        }
    }

    private void disconnect() {
        // mWebSocketManager.disconnectWebSocket();
        Debug.log("Refresh connect");
        mControlClient.closeSocket();
    }

//    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
//            ArrayList<DownloadFile> downloadFileComplete = new ArrayList<>();
//
//            for (DownloadFile file : mApp.downloadFileList) {
//                if (file.getDownloadId() == referenceId) {
//                    downloadFileComplete.add(file);
//                    DownloadManager.Query query = new DownloadManager.Query();
//                    query.setFilterById(referenceId);
//
//                    Cursor c = downloadManager.query(query);
//                    boolean downloadStatus = false;
//                    if (c != null && c.moveToFirst()) {
//
//                        int status = c.getInt(c.getColumnIndex(android.app.DownloadManager.COLUMN_STATUS));
//                        switch (status) {
//                            case DownloadManager.STATUS_SUCCESSFUL:
//                                downloadStatus = true;
//                                break;
//
//                            case DownloadManager.STATUS_FAILED:
//                                downloadStatus = false;
//                                break;
//
//                            default:
//                                break;
//                        }
//                    }
//
//                    String msg = "Download " + downloadStatus + " file " + file.getFileName();
//                    Debug.log(msg);
//                    Utils.showToast(mContext, msg);
//                    mApp.updateNotification(mContext, msg);
//                    /*if (downloadStatus) {
//                        Debug.log(getResources().getString(R.string.download_success, file.getFileName()));
//                        Utils.showToastShort(mContext, R.string.download_success, file.getFileName());
//                    } else {
//                        Debug.logW(getResources().getString(R.string.download_failed, file.getFileName()));
//                        Utils.showToastShort(mContext, R.string.download_failed, file.getFileName());
//                    }*/
//
//                    if (file.isDownload4Update()) {
//                        // Call update app when downloadStatus = true
//                        if (downloadStatus)
//                            Utils.callUpdate(mContext, file.getFileName(), file.getFileID());
//                    } else
//                        Utils.sendDownloadResponse2Server(mContext, file.getFileID(), downloadStatus);
//                }
//            }
//
//            for (DownloadFile file4Del : downloadFileComplete)
//                mApp.downloadFileList.remove(file4Del);
//        }
//    };
//
//    private void checkDownloadStatus() {
//        Debug.log("checkDownloadStatus");
//        DownloadManager.Query query = new DownloadManager.Query();
//        query.setFilterByStatus(DownloadManager.STATUS_RUNNING);
//        Cursor cursor = downloadManager.query(query);
//        try {
//            if (cursor != null && cursor.moveToFirst()) {
//                int downloadIDIndex = cursor.getColumnIndex(DownloadManager.COLUMN_ID);
//                long downloadID = cursor.getInt(downloadIDIndex);
//                int downloadSoFarIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
//                String downloadSoFar = cursor.getString(downloadSoFarIndex);
//
//                for (DownloadFile f : mApp.downloadFileList) {
//                    if (f.getDownloadId() == downloadID) {
//                        String log = "Name = " + f.getFileName() + " Current = " + getPercentDl(downloadSoFar, f);
//                        Debug.logD(log);
//                        mApp.updateNotification(this, log);
//
//                        if (f.isDownload4Update())
//                            Utils.sendReportUpdateResponse2Server(mContext, f.getFileID(), Long.parseLong(downloadSoFar));
//                        else
//                            Utils.sendReportDownloadResponse2Server(mContext, f.getFileID(), Long.parseLong(downloadSoFar));
//
//                        if (preDownloadSoFar.equals(downloadSoFar)) {
//                            mCount++;
//                        } else {
//                            mCount = 0;
//                        }
//
//                        preDownloadSoFar = downloadSoFar;
//                        if (mCount == 3) {
//                            //Send message download failed to server
//                            if (f.isDownload4Update())
//                                Utils.sendUpdateResponse2Server(mContext, f.getFileID(), false);
//                            else
//                                Utils.sendDownloadResponse2Server(mContext, f.getFileID(), false);
//
//                            mCount = 0;
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            Debug.logE(e.toString());
//        } finally {
//            if (cursor != null)
//                cursor.close();
//        }
//    }
//
//    private String getPercentDl(String s, DownloadFile df) {
//        long x = Long.parseLong(s);
//        long y = df.getFileSize();
//        float z = ((float) x / (float) y) * 100;
//        String rs = z + "%";
//        return rs;
//    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // Android M Permission check
            boolean hasReadExternal = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
            boolean hasWRiteExternal = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
            boolean hasInternet = checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED;
            boolean hasKeyGuard = checkSelfPermission(Manifest.permission.DISABLE_KEYGUARD) != PackageManager.PERMISSION_GRANTED;
            boolean hasWakeLock = checkSelfPermission(Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED;
            boolean hasReOderTask = checkSelfPermission(Manifest.permission.REORDER_TASKS) != PackageManager.PERMISSION_GRANTED;
            boolean hasSystemAlert = checkSelfPermission(Manifest.permission.SYSTEM_ALERT_WINDOW) != PackageManager.PERMISSION_GRANTED;
            boolean hasWriteSetting = checkSelfPermission(Manifest.permission.WRITE_SETTINGS) != PackageManager.PERMISSION_GRANTED;
            boolean hasReadLog = checkSelfPermission(Manifest.permission.READ_LOGS) != PackageManager.PERMISSION_GRANTED;
            boolean hasAccessWifi = checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED;
            boolean hasNwWifi = checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED;
            boolean hasBoot = checkSelfPermission(Manifest.permission.RECEIVE_BOOT_COMPLETED) != PackageManager.PERMISSION_GRANTED;
            List<String> permissions = new ArrayList<>();

            if (hasReadExternal) permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            if (hasWRiteExternal) permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasInternet) permissions.add(Manifest.permission.INTERNET);
            if (hasKeyGuard) permissions.add(Manifest.permission.DISABLE_KEYGUARD);
            if (hasWakeLock) permissions.add(Manifest.permission.WAKE_LOCK);
            if (hasReOderTask) permissions.add(Manifest.permission.REORDER_TASKS);
            if (hasSystemAlert) permissions.add(Manifest.permission.SYSTEM_ALERT_WINDOW);
            if (hasWriteSetting) permissions.add(Manifest.permission.WRITE_SETTINGS);
            if (hasReadLog) permissions.add(Manifest.permission.READ_LOGS);
            if (hasAccessWifi) permissions.add(Manifest.permission.ACCESS_WIFI_STATE);
            if (hasNwWifi) permissions.add(Manifest.permission.ACCESS_NETWORK_STATE);
            if (hasBoot) permissions.add(Manifest.permission.RECEIVE_BOOT_COMPLETED);

            if (!permissions.isEmpty())
                requestPermissions(permissions.toArray(new String[permissions.size()]), 1);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_mn, menu);
        /* mNetwMenuItem = menu.findItem(R.id.mn_conn_nw);
        mConnMenuItem = menu.findItem(R.id.mn_connect);
        mSavelogMenuItem = menu.findItem(R.id.mn_get_log);
        mDellogMenuItem = menu.findItem(R.id.mn_clear_log); */

        return super.onCreateOptionsMenu(menu);
    }

    /* private boolean checkMenuNotNull(){
        if (mNetwMenuItem != null && mConnMenuItem != null && mSavelogMenuItem != null && mDellogMenuItem != null) {
            return true;
        }

        return false;
    } */

    // private boolean isNetworkOK = false;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.mn_connect:
                if (Utils.mConnStatus != Define.DISCONNECTED_STATUS) {
                    Utils.showToast(this, "Connecting to Server, does not click more");
                }
                // else if (!isNetworkOK){
                // Utils.showToast(this, "Network error, does not click more");
                // }
                else {
                    String ip = SettingManager.getIPServer(this).trim();
                    startConnect(ip);
                    Utils.showToast(this, "Started connecting to Server " + ip);
                }
                break;

            case R.id.mn_clear_log:
                if (mTxtDebug != null)
                    mTxtDebug.setText("");

                mApp.clearLog();
                Debug.resetLog();
                break;

            case R.id.mn_dis_connect:
                disconnect();
                break;

            case R.id.mn_chang_mode:
                startActivity(new Intent(this, SettingActivity.class));
                break;

            case R.id.mn_enable_log:
                if (item.isChecked()) {
                    item.setChecked(false);
                    // mTxtDebug.setVisibility(View.INVISIBLE);
                    // isShowLog = false;
                    mApp.setUpdateLogScreen(false);
                    Debug.log("Hide log");
                } else {
                    // isShowLog = true;
                    item.setChecked(true);
                    // mTxtDebug.setVisibility(View.VISIBLE);
                    mApp.setUpdateLogScreen(true);
                    Debug.log("Show log");
                }
                break;

            case R.id.mn_get_log:
                Utils.showToast(this, "Saved log in " + Define.APP_PATH_LOGS + " !!!");
                Utils.saveLogFile();
                break;

            case R.id.mn_ip_sv_addr:
                AlertDialog.Builder a = new AlertDialog.Builder(this);
                View ipView = LayoutInflater.from(this).inflate(R.layout.layout_ip_sv_addr, null, false);
                mIpServerEt = (EditText) ipView.findViewById(R.id.et_ip_sv_addr);
                mIpServerEt.setText(SettingManager.getIPServer(this));
                Button btnOK = (Button) ipView.findViewById(R.id.btn_save_ip_sv);
                Button btnCancel = (Button) ipView.findViewById(R.id.btn_cancel);
                btnOK.setOnClickListener(this);
                btnCancel.setOnClickListener(this);
                a.setView(ipView);
                a.setIcon(R.mipmap.app_icon);
                a.setTitle(R.string.app_name);
                mDialog = a.create();
                mDialog.show();
                break;

            case R.id.mn_conn_nw:
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                break;

            case R.id.mn_close:
                mApp.cancelNotification(this);
                finish();
                System.exit(0);
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

//    private void setTitleAB(String title) {
//        mAB.setTitle(title);
//    }

    private void setTitleAB(int id) {
        mAB.setTitle(getResources().getString(id));
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_save_ip_sv:
                String ip = mIpServerEt.getText().toString();
                if (TextUtils.isEmpty(ip)) {
                    Utils.showToast(this, getResources().getString(R.string.ip_sv_null));
                } else {
                    if (mControlClient.isConnected()) {
                        disconnect();
                        // setTitleAB(mControlClient.isConnected());
                        // mApp.updateNotification(this, Utils.getNameStt(Define.CONNECTING_STATUS) + ip);
                    }

                    SettingManager.saveIPServer(mContext, ip);
                    setTitleAB(mControlClient.isConnected());
                    mApp.updateNotification(this, Utils.getNameStt(Define.CONNECTING_STATUS) + ip);
                    mDialog.dismiss();
                    Utils.showToast(this, getResources().getString(R.string.ip_sv_ok));
                    // hideNavigationBar();
                }
                break;

            case R.id.btn_cancel:
                mDialog.dismiss();
                // hideNavigationBar();
                break;

            default:
                break;
        }
    }

    private class PowerReceiver extends BroadcastReceiver {

        public PowerReceiver() {
            if (SettingManager.getModeUser(mContext))
                Debug.log();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (SettingManager.getModeUser(context)) {
                Debug.log("Turn on Screen");
                Utils.turnOnScreen(mContext);
            } else {
                return;
            }
        }
    }

    private BroadcastReceiver ConnectReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            boolean conn = false;

            if (action.equals(Define.CHECK_SERVER_CONN)) {
                conn = intent.getBooleanExtra(Define.SERVER_ISCONNECTED, false);

                if (conn /*&& Utils.isRoot*/) {
                    // if (!isUpdated){
                    // long sv = SettingManager.getSetting(mContext, Define.TIME_OF_SERVER_NOW, Long.class);
//                    long sv_time = intent.getLongExtra(Define.TIME_OF_SERVER_NOW, -1);
//                    if (sv_time > 0)
//                        scheduleAll(sv_time, System.currentTimeMillis());

                    Utils.mConnStatus = Define.CONNECTED_STATUS;
                    // mApp.updateNotification(mContext, Utils.getNameStt(Define.CONNECTED_STATUS) + SettingManager.getIPServer(mContext));
                    // }
                } else {
                    Utils.mConnStatus = Define.CONNECTING_STATUS;
                    // setTitleAB(conn);
                    // mApp.updateNotification(mContext, Utils.getNameStt(Define.CONNECTING_STATUS) + SettingManager.getIPServer(mContext));
                }

                setTitleAB(conn);
            } else if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
                if (!intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)) {
                    Utils.showToast(mContext, "Wifi disconnected!!!");
                }
            } else if (action.equals(Define.CHECK_NETWORK_CONN)) {
                boolean m = intent.getBooleanExtra(Define.DATA_CONN, false);
                if (!m) {
                    setTitleAB(R.string.nw_error);
                    // mControlClient.closeSocket();
                    // mControlClient.setStopControlThreads();
                    // mControlClient.checkFinish();
                    // isNetworkOK = false;
                    Debug.logW("Network unavailable so stop check service");
                    stopConnectService();
                    Utils.mConnStatus = Define.NETWORK_ERROR_STATUS;
                    // mApp.updateNotification(mContext, getResources().getString(R.string.nw_error));
                } else {
//                    String ip = SettingManager.getSetting(mContext, MY_IP, String.class);
//                    if (ip.equals(" "))
//                        SettingManager.saveSetting(mContext, MY_IP, Utils.getIPAddress(true));

                    // isNetworkOK = true;
                    Utils.mConnStatus = Define.CONNECTING_STATUS;
                    setTitleAB(mControlClient.isConnected());
                    Utils.startCheckConnectService(mContext);
                    // mApp.updateNotification(mContext, Utils.getNameStt(Define.CONNECTING_STATUS) + SettingManager.getIPServer(mContext));
                }
            }

            Debug.log(action, "conn = " + conn);
        }
    };

    private void destroyReceiver() {
        if (ConnectReceiver != null) {
            unregisterReceiver(ConnectReceiver);
            ConnectReceiver = null;
        }

        if (mPowerReceiver != null) {
            unregisterReceiver(mPowerReceiver);
            mPowerReceiver = null;
        }

//        if (downloadReceiver != null) {
//            unregisterReceiver(downloadReceiver);
//            downloadReceiver = null;
//        }
    }

    private void stopConnectService() {
        if (Utils.isServiceRunning(CheckConnectService.class, this)) {
            sendBroadcast(new Intent(Define.ACTION_STOP_CONNECT_THREAD));
        }

        if (Utils.isServiceRunning(AliveService.class, this))
            stopService(new Intent(this, AliveService.class));

        if (Utils.isServiceRunning(DownloadService.class, this))
            stopService(new Intent(this, DownloadService.class));
    }

//    public void deleteDatabase() {
//        ScheduleDBManager dbManager = new ScheduleDBManager(this);
//        dbManager.deleteAll();
//        Utils.showToast(this, "Clear DB");
//    }

//    @Override
//    public boolean onMenuOpened(int featureId, Menu menu) {
//         super.onMenuOpened(featureId, menu);
//        Debug.log();
//        isNavigationBarVisibility = true;
//        return true;
//    }

    //private boolean isNavigationBarVisibility = false;

//    @Override
//    public void onOptionsMenuClosed(Menu menu) {
//        super.onOptionsMenuClosed(menu);
//        Debug.log();
//    }
//
//    @Override
//    public void onPanelClosed(int featureId, Menu menu) {
//        super.onPanelClosed(featureId, menu);
//        Debug.log();
//        if (isNavigationBarVisibility){
//            hideNavigationBar();
//            isNavigationBarVisibility = false;
//        }
//    }

    //    public void deleteAndCancelCalendar() {
//        mScheduleManager.deleteAllCalendar();
//    }
}
