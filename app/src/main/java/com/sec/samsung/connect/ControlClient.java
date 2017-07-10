package com.sec.samsung.connect;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;


import com.sec.samsung.download.DownloadFile;
import com.sec.samsung.download.DownloadService;
import com.sec.samsung.manager.AliveService;
import com.sec.samsung.schedule.ScheduleDBManager;
import com.sec.samsung.schedule.ScheduleItem;
import com.sec.samsung.schedule.ScheduleManager;
import com.sec.samsung.tvcontentsync.BaseActivity;
import com.sec.samsung.tvcontentsync.MainActivity;
import com.sec.samsung.tvcontentsync.R;
import com.sec.samsung.tvcontentsync.TvContentSyncApplication;
import com.sec.samsung.utils.Debug;
import com.sec.samsung.utils.Define;
import com.sec.samsung.utils.JsonMessageManager;
import com.sec.samsung.utils.SettingManager;
import com.sec.samsung.utils.Utils;

import org.apache.commons.net.ftp.FTPClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sev_user on 11/21/2016.
 */

public class ControlClient {
    //implements ScheduleManager.IShowToast

    public final static int PORT = 9999;
    public static String IPADDR = null;

    private static final int CONNECT = 1;
    private static final int DOWNLOAD = 2;
    private static final int SCHEDULE = 3;
    private static final int ERROR = 4;
    private static final int CLOSE = 5;
    private static final int CANCEL_DOWNLOAD = 6;
    private static final int DELETE_FOLDER = 7;
    private static final int UPDATE_APP = 8;
    private static final int REMOVE_GROUP = 9;
    private static final int CHECK_CONNECT = 10;
    private static final int SHOW_LOG = 11;
    private static final int ENABLE_SOUND = 12;
    private static final int BACK_GROUND = 13;
    private static final int RESTART_APP = 14;
    private static final int DELETE_SCHEDULER = 15;

    //
    private static final int SEND_MSG_TO_SERVER = 16;

    private static final String ADD = "add";
    private static final String UPDATE = "edit";
    private static final String DELETE = "delete";

    private Socket socket;
    private BufferedInputStream bis;
    private BufferedOutputStream bos;
    private static ControlClient mInstance = null;
    private static ControlClientHandler mHandler = null;
    private ConnectClientThread connectThread = null;
    private DataClientThread dataThread = null;
    private boolean stopFlag;
    private boolean isThreadDataRunning = false;

    private int mFileId = -1;

    private Context mContext;
    private TvContentSyncApplication mApp;
    private static DownloadManager downloadManager = null;
    private boolean isReceiveConnectedMessage = false;
    private CountDownTimer checkConnectTimer = null;
    //private ToastHandler mToastHandler;

    public ControlClient(Context context) {
        Debug.log("ControlClient()");
        mHandler = new ControlClientHandler();
        mHandler.start();

        // mToastHandler = new ToastHandler();
        mContext = context;

        mApp = TvContentSyncApplication.getInstance();
        if (downloadManager == null) {
            downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        }

        if (mScheduleManager == null) {
            mScheduleManager = new ScheduleManager(mContext);
        }

        dbManager = mScheduleManager.getDbManager();
        //mScheduleManager.setOnShowToastListener(this);
    }

    public void updateNotify(Context c, int stt) {
        if (mApp != null) {
            if (stt < 0)
                mApp.cancelNotification(c);
            else
                mApp.updateNotification(c, stt);
        }
    }

    public static String getIPADDR() {
        return IPADDR;
    }

    public void setIPADDR(String iPADDR) {
        IPADDR = iPADDR;
    }

    public static synchronized ControlClient getInstance(Context context) {
        synchronized (ControlClient.class) {
            if (mInstance == null) {
                mInstance = new ControlClient(context);
            }
        }
        return mInstance;
    }

    public void setFileID(int file_id) {
        mFileId = file_id;
    }

    public synchronized void start() {
        Debug.log("start()");
        // Utils.mConnStatus = Define.CONNECTING_STATUS;

        if (connectThread == null && !isThreadDataRunning) {
            Debug.log("connectThread == null new");
            isReceiveConnectedMessage = false;
            connectThread = new ConnectClientThread();
            connectThread.setName("CONNECT_CLIENT_THREAD");
            connectThread.start();
        } else {
            Debug.log("connectThread is running");
        }
    }

    /**
     * show toast
     *
     * @param s msg to show
     */
//    @Override
//    public void showToast(String s) {
//        // mToastHandler.sendMessage(mToastHandler.obtainMessage(0, s));
//        showActionNoti(-1, s);
//    }

//    private void showActionNoti(int id, String msg) {
//        Intent i = new Intent(Define.SHOW_TOAST);
//        i.putExtra(Define.ID_SHOW_TOAST, id);
//        i.putExtra(Define.MSG_SHOW_TOAST, msg);
//        mContext.sendBroadcast(i);
//    }

//    private void showErrorConnectNoti(Exception ex) {
//        Intent i = new Intent(CheckConnectService.NOTI_CONNECT);
//        i.putExtra(Define.MSG_SHOW_TOAST, ex.getMessage());
//        mContext.sendBroadcast(i);
//    }

    private class ControlClientHandler extends HandlerThread implements Handler.Callback {

        private Handler h;

        public ControlClientHandler() {
            super("ControlClientHandler");
            Debug.log();
        }

        private int getWhat(String type) {
            if (type.equalsIgnoreCase(Define.JSON_CONNECT))
                return CONNECT;

            if (type.equalsIgnoreCase(Define.JSON_DOWNLOAD))
                return DOWNLOAD;

            if (type.equalsIgnoreCase(Define.JSON_DOWNLOAD_CANCEL))
                return CANCEL_DOWNLOAD;

            if (type.equalsIgnoreCase(Define.JSON_SCHEDULED))
                return SCHEDULE;

            if (type.equalsIgnoreCase(Define.JSON_DELETED_FOLDER))
                return DELETE_FOLDER;

            if (type.equalsIgnoreCase(Define.JSON_UPDATE))
                return UPDATE_APP;

            if (type.equalsIgnoreCase(Define.JSON_REMOVE_GROUP))
                return REMOVE_GROUP;

            if (type.equalsIgnoreCase(Define.JSON_CHECK_CONNECT))
                return CHECK_CONNECT;

            if (type.equalsIgnoreCase(Define.JSON_SETTING_LOG))
                return SHOW_LOG;

            if (type.equalsIgnoreCase(Define.JSON_SETTING_BG))
                return BACK_GROUND;

            if (type.equalsIgnoreCase(Define.JSON_SETTING_SOUND))
                return ENABLE_SOUND;

            if (type.equalsIgnoreCase(Define.JSON_RESTART_APP))
                return RESTART_APP;

            if (type.equalsIgnoreCase(Define.JSON_DELETE_SCHEDULER))
                return DELETE_SCHEDULER;

            Debug.logW("Unknown!!!");
            return -1;
        }

        public void sendHandleMsg(String type, JSONObject j) {
            if (h != null) {
                int what = getWhat(type);
                Debug.log("what = " + what);

                if (what != -1) {
                    h.sendMessage(h.obtainMessage(what, j));
                }
            } else {
                Debug.logW("Null");
            }
        }

        public void sendMessage(int what, Object o) {
            h.sendMessage(h.obtainMessage(what, o));
        }

        public void sendEmptyMessage(int what) {
            h.sendEmptyMessage(what);
        }

        @Override
        protected void onLooperPrepared() {
            super.onLooperPrepared();
            h = new Handler(getLooper(), this);
        }

        @Override
        public boolean handleMessage(Message msg) {
            //Debug.log("handleMessage");
            switch (msg.what) {
                case ERROR:
                case CLOSE:
                    Debug.logW("Error occurs = " + msg.obj);
                    setStopControlThreads();
                    break;

                case CONNECT:
                    handleConnectMessage(msg.obj);
                    break;

                case BACK_GROUND:
                case DOWNLOAD:
                case UPDATE_APP:
                    handleDownloadFile(msg.obj, msg.what);
                    break;

                case CANCEL_DOWNLOAD:
                    handleCancelDownload(msg.obj);
                    break;

                case DELETE_FOLDER:
                    handleDeletedFile(msg.obj);
                    break;

                case SCHEDULE:
                    handleScheduled(msg.obj);
                    break;

                case REMOVE_GROUP:
                    handleRemoveGroup();
                    break;

                case CHECK_CONNECT:
                    Utils.sendCheckConnect2Server(mContext);
                    break;

                case SHOW_LOG:
                    handleLog(msg.obj);
                    break;

                case ENABLE_SOUND:
                    handleSound(msg.obj);
                    break;

                case RESTART_APP:
                    handleRestartApp();
                    break;

                case DELETE_SCHEDULER:
                    handleDeleteScheduler(msg.obj);
                    break;

                case SEND_MSG_TO_SERVER:
                    sendMessageData(msg.obj.toString());
                    break;

                default:
                    Debug.log("Unknown msg");
                    break;
            }

            return false;
        }
    }

    private void handleDeleteScheduler(Object o) {
        Debug.log("o = " + o);
        if (o == null) {
            Debug.logW("null");
            return;
        }

        // mToastHandler.sendMessage(mToastHandler.obtainMessage(0, "Delete scheduler"));
        //showActionNoti(-1, "Delete scheduler");

        JSONObject j = (JSONObject) o;
        try {
            int id = j.getInt(Define.JSON_SCHEDULED_ID);
            String val = j.getString(Define.JSON_IS_DELETE_FOLDER);

            Activity ba = TvContentSyncApplication.getInstance().getCurActivity();
            if (ba != null && ba instanceof BaseActivity) {
                BaseActivity a = (BaseActivity) ba;
                if (a.getId() == id) {
                    a.cancelCalendar();
                    Debug.log("Cancel calendar running!!");
                } else {
                    Debug.log("Do nothing, id = " + id);
                }
            } else {
                Debug.logW("ba == null");
            }

            if (val.equals("True")) {
                ScheduleItem si = mScheduleManager.getScheduleItem(id);
                File folder = new File(Define.APP_PATH + si.getFolderName());
                checkDeletedFolder(folder);
            }

            cancelSchedule(id);
        } catch (Exception e) {
            Debug.logW(e);
        }
    }

    private void handleRestartApp() {
        SettingManager.saveSetting(mContext, Define.TIME_OF_SERVER_NOW, Utils.TIME_OFF_SET + System.currentTimeMillis());
        Utils.restartDevice();
//        if (Utils.appStatus != Define.PLAYING_STATUS /*&& Utils.appServiceStatus != Define.DOWNLOADING_STATUS*/) {
//            Debug.log("Restart app");
//            SettingManager.saveSetting(mContext, Define.JSON_RESTART_APP, true);
//            Utils.restartApp(mContext);
//            Utils.restartDevice();
//        } else {
//            Debug.log("App is playing ");
//             mToastHandler.sendMessage(mToastHandler.obtainMessage(0, "App is playing or downloading"));
//            showActionNoti(-1, "App is playing or downloading");
//        }
    }

    private void handleLog(Object o) {
        Debug.log("o = " + o);
        if (o == null) {
            Debug.logW("o Null");
            return;
        }

        JSONObject j = (JSONObject) o;
        Debug.log("j = " + j.toString());
        try {
            String name = j.getString(Define.FTP_NAME);
            String pwd = j.getString(Define.FTP_PASSWORD);
            getLog(name, pwd);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            Debug.logW(e.getMessage());
        }
        //uploadFile();

//        JSONObject j = (JSONObject) o;
//        if (j == null) {
//            return;
//        }
//
//        try {
//            boolean isShow = j.getBoolean(Define.JSON_VALUE);
//            mApp.setUpdateLogScreen(isShow);
//            Debug.log("Show log " + isShow);
//        } catch (JSONException e) {
//            Debug.logW(e);
//        }
    }

    private void handleSound(Object o) {
        Debug.log("o = " + o);
        JSONObject j = (JSONObject) o;
        if (j == null) {
            return;
        }

        try {
            boolean isSound = j.getBoolean(Define.JSON_VALUE);
            // mToastHandler.sendMessage(mToastHandler.obtainMessage(0, "Turn on Sound " + isSound));
            //showActionNoti(-1, "Turn on Sound " + isSound);
            AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            if (isSound) {
                am.setStreamVolume(AudioManager.STREAM_SYSTEM, 5, 0);
                am.setStreamVolume(AudioManager.STREAM_MUSIC, 5, 0);
            } else {
                am.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0);
                am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
            }
        } catch (JSONException e) {
            Debug.logW(e);
        }
    }

    private void handleRemoveGroup() {
        Debug.log("handleRemoveGroup");
        // mToastHandler.sendMessage(mToastHandler.obtainMessage(0, "handleRemoveGroup"));
        //showActionNoti(-1, "handleRemoveGroup");
//        if (mContext instanceof MainActivity){
//           ((MainActivity) mContext).deleteAndCancelCalendar();
//        }

        deleteAndCancelCalendar();

        BaseActivity a = (BaseActivity) TvContentSyncApplication.getInstance().getCurActivity();
        if (a != null) {
            a.cancelCalendar();
        } else {
            Debug.logW("a == null");
        }


        File[] folders = new File(Define.APP_PATH).listFiles();
        for (File folder : folders) {
            checkDeletedFolder(folder);
        }
    }

    private void handleConnectMessage(Object obj) {
        JSONObject receiveJson = (JSONObject) obj;
        Debug.log("obj = " + obj);

        if (receiveJson == null) {
            Debug.logW("receiveJson is null");
            return;
        }

        try {
            boolean connected = receiveJson.getBoolean(Define.JSON_CONNECTED);
            String dt_sv = receiveJson.getString(Define.JSON_SERVER_DATETIME_NOW);
            long dt_sv_long = Utils.dateToLong(dt_sv);
            scheduleAll(dt_sv_long, System.currentTimeMillis());

            // Utils.setTimeToDevice(dt_sv_long);

            //Intent i = new Intent(Define.CHECK_SERVER_CONN);
            // i.putExtra(Define.SERVER_ISCONNECTED, connected);
            // i.putExtra(Define.TIME_OF_SERVER_NOW, dt_sv_long);
            //mContext.sendBroadcast(i);

            // SettingManager.saveSetting(mContext, Define.TIME_OF_SERVER_NOW, dt_sv_long);

            // Thread.sleep(12);
            Utils.sendConnectStt(mContext, connected);

            if (!connected) {
//                if (mHandler != null) {
//                    mHandler.sendMessage(mHandler.obtainMessage(CLOSE, null));
//                }

                mHandler.sendEmptyMessage(CLOSE);
            }
        } catch (JSONException e) {
            Debug.logW(e);
            // } catch (InterruptedException e) {
            //     Debug.logW(e);
        }
    }

    // Thread for socket connect
    public class ConnectClientThread extends Thread {

        public ConnectClientThread() {
            Debug.log();
        }

        @Override
        public void run() {
            Debug.log("ConnectClientThread run()");

            try {
                Debug.log("run() IPADDR : " + IPADDR);
                // sock = new Socket(IPADDR, PORT);
                socket = new Socket();
                socket.connect(new InetSocketAddress(IPADDR, PORT));
                // Debug.log("run() sock connected getInetAddress "+sock.getInetAddress());

                stopFlag = true;
                bis = new BufferedInputStream(socket.getInputStream());
                bos = new BufferedOutputStream(socket.getOutputStream());

                Debug.log("Socket connected");
                onSocketConnected();

                if (dataThread == null) {
                    Debug.log("dataThread == null new");
                    dataThread = new DataClientThread();
                    dataThread.setName("DATA_CLIENT_THREAD");
                    dataThread.start();
                } else {
                    Debug.log("dataThread is running!!!");
                }
            } catch (IOException e) {
                Debug.log("Utils.mConnStatus = " + Utils.mConnStatus);
                if (Utils.mConnStatus != Define.CONNECTING_STATUS) {
                    Utils.sendConnectStt(mContext, false);
                }

                String errMsg = e.getMessage();
                Debug.logE("IOException : " + errMsg, e);

//                Intent i = new Intent(CheckConnectService.NOTI_CONNECT);
//                i.putExtra(Define.MSG_SHOW_TOAST, errMsg);
//                mContext.sendBroadcast(i);
                // showErrorConnectNoti(e);

                String errStr = "";
                if (errMsg.contains("ETIMEDOUT"))
                    errStr = "ETIMEDOUT";
                else if (errMsg.contains("ECONNREFUSED"))
                    errStr = "ECONNREFUSED";

//                if (mHandler != null) {
//                    mHandler.sendMessage(mHandler.obtainMessage(ERROR, errStr));
//                }
                mHandler.sendMessage(ERROR, errStr);

                //boolean checkNw = Pattern.compile(Pattern.quote("unreach"), Pattern.CASE_INSENSITIVE).matcher(errMsg).find();
                //if (!checkNw) {
                Utils.startCheckConnectService(mContext);
                // }

                if (!Utils.isServiceRunning(PinkService.class, mContext))
                    mContext.startService(new Intent(mContext, PinkService.class));
            } catch (Exception e) {
                Debug.logE("Exception : " + e.getMessage(), e);
                if (Utils.mConnStatus != Define.CONNECTING_STATUS) {
                    // Utils.mConnStatus = Define.CONNECTING_STATUS;
                    Utils.sendConnectStt(mContext, false);
                }

                cancelAllDownload();
                Utils.startCheckConnectService(mContext);
                if (!Utils.isServiceRunning(PinkService.class, mContext))
                    mContext.startService(new Intent(mContext, PinkService.class));
            } finally {
                Debug.log("ConnectClientThread finally");
                checkFinish();
            }
        }
    }

    // Thread for receive message
    public class DataClientThread extends Thread {

        public DataClientThread() {
            Debug.log();
        }

        @Override
        public void run() {
            Debug.log("DataClientThread run()");
            isThreadDataRunning = true;

            Exception ex = null;
            try {
                byte[] tempBuf;
                while (!Thread.currentThread().isInterrupted()) {
                    if (bis != null) {
                        String fullStr = "";
                        tempBuf = new byte[1024];

                        int jsonLength = bis.read(tempBuf, 0, tempBuf.length);
                        // read message length
                        if (jsonLength > 0) {
                            byte[] jsonBytes = getbytes(tempBuf, 0, jsonLength);
                            fullStr = new String(jsonBytes, 0, jsonLength, "UTF-8");
                            Debug.logD("jsonLength : " + jsonLength);
                        } else {
                            Debug.logE("when pear socket closed return -1 !!!");
                            if (Utils.mConnStatus != Define.CONNECTING_STATUS) {
                                Utils.sendConnectStt(mContext, false);
                            }

                            String errStr = "BUF_READ_ERROR";
//                            if (mHandler != null) {
//                                mHandler.sendMessage(mHandler.obtainMessage(ERROR, errStr));
//                            }

                            mHandler.sendMessage(ERROR, errStr);

                            Thread.currentThread().interrupt();
                        }

                        boolean check = checkIncludeScheduler(fullStr);
                        Debug.logI("check Included commands: " + check);
                        Debug.log("FullStr = " + fullStr);
                        if (check) {
                            JSONObject msg = JsonMessageManager.createJsonIncluded(mContext, fullStr);
                            final String data = msg.toString();
                            sendMessageData(data);
                            Debug.logW(mContext.getResources().getString(R.string.include_cmd, data));

//                            if (mContext instanceof BaseActivity) {
//                                ((BaseActivity) mContext).runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        Utils.showToastLong(mContext, R.string.include_cmd, data);
//                                    }
//                                });
//                            }
                            // Utils.showToast(mContext, "Included Command from server");
                            // mToastHandler.sendMessage(mToastHandler.obtainMessage(R.string.include_cmd, data));
                            //showActionNoti(R.string.include_cmd, data);
                        }

                        if (fullStr != null && !fullStr.isEmpty() && !check) {
                            JSONObject jsonObj = new JSONObject(fullStr);
                            // Message msg = new Message();
                            String type;

                            try {
                                type = jsonObj.getString(Define.JSON_TYPE);
                            } catch (Exception e) {
                                Debug.logW(e);
                                type = "JSONtypeMismatch";
                            }

                            mHandler.sendHandleMsg(type, jsonObj);

//                            if (type.equalsIgnoreCase(Define.JSON_CONNECT)) {
//                                if (mHandler != null) {
////                                    msg.what = CONNECT;
////                                    msg.obj = jsonObj;
////                                    mHandler.sendMessage(msg);
//                                    mHandler.sendMessage(mHandler.obtainMessage(CONNECT, jsonObj));
//                                }
//                                //return;
//                            }
//
//                            if (type.equalsIgnoreCase(Define.JSON_DOWNLOAD)) {
//                                if (mHandler != null) {
////                                    msg.what = DOWNLOAD;
////                                    msg.obj = jsonObj;
////                                    mHandler.sendMessage(msg);
//
//                                    mHandler.sendMessage(mHandler.obtainMessage(DOWNLOAD, jsonObj));
//                                }
//                                //return;
//                            }
//
//                            if (type.equalsIgnoreCase(Define.JSON_DOWNLOAD_CANCEL)) {
//                                if (mHandler != null) {
//                                    msg.what = CANCEL_DOWNLOAD;
//                                    msg.obj = jsonObj;
//                                    mHandler.sendMessage(msg);
//                                }
//                                //return;
//                            }
//
//                            if (type.equalsIgnoreCase(Define.JSON_SCHEDULED)) {
//                                if (mHandler != null) {
//                                    msg.what = SCHEDULE;
//                                    msg.obj = jsonObj;
//                                    mHandler.sendMessage(msg);
//                                }
//                                //return;
//                            }
//
//                            if (type.equalsIgnoreCase(Define.JSON_DELETED_FOLDER)) {
//                                if (mHandler != null) {
//                                    msg.what = DELETE_FOLDER;
//                                    msg.obj = jsonObj;
//                                    mHandler.sendMessage(msg);
//                                }
//                                //return;
//                            }
//
//                            if (type.equalsIgnoreCase(Define.JSON_UPDATE)) {
//                                if (mHandler != null) {
//                                    msg.what = UPDATE_APP;
//                                    msg.obj = jsonObj;
//                                    mHandler.sendMessage(msg);
//                                }
//                                //return;
//                            }
//
//                            if (type.equalsIgnoreCase(Define.JSON_REMOVE_GROUP)) {
//                                if (mHandler != null) {
//                                    msg.what = REMOVE_GROUP;
//                                    msg.obj = jsonObj;
//                                    mHandler.sendMessage(msg);
//                                }
//                                //return;
//                            }
//
//                            if (type.equalsIgnoreCase(Define.JSON_CHECK_CONNECT)) {
//                                if (mHandler != null) {
//                                    msg.what = CHECK_CONNECT;
//                                    msg.obj = jsonObj;
//                                    mHandler.sendMessage(msg);
//                                }
//                                //return;
//                            }
//
//                            if (type.equalsIgnoreCase(Define.JSON_SETTING_LOG)) {
//                                if (mHandler != null) {
//                                    msg.what = SHOW_LOG;
//                                    msg.obj = jsonObj;
//                                    mHandler.sendMessage(msg);
//                                }
//                                //return;
//                            }
//
//                            if (type.equalsIgnoreCase(Define.JSON_SETTING_SOUND)) {
//                                if (mHandler != null) {
//                                    msg.what = ENABLE_SOUND;
//                                    msg.obj = jsonObj;
//                                    mHandler.sendMessage(msg);
//                                }
//                                //return;
//                            }
//
//                            if (type.equalsIgnoreCase(Define.JSON_SETTING_BG)) {
//                                if (mHandler != null) {
//                                    msg.what = BACK_GROUND;
//                                    msg.obj = jsonObj;
//                                    mHandler.sendMessage(msg);
//                                }
//                                //return;
//                            }
//
//                            if (type.equalsIgnoreCase(Define.JSON_RESTART_APP)) {
//                                if (mHandler != null) {
//                                    msg.what = RESTART_APP;
//                                    msg.obj = jsonObj;
//                                    mHandler.sendMessage(msg);
//                                }
//                            }
//
//                            if (type.equalsIgnoreCase(Define.JSON_DELETE_SCHEDULER)) {
//                                if (mHandler != null) {
//                                    msg.what = DELETE_SCHEDULER;
//                                    msg.obj = jsonObj;
//                                    mHandler.sendMessage(msg);
//                                }
//                            }
                        }
                    }
                }
            } catch (IOException e) {
                Debug.logE("DataClientThread IOException : " + e.getMessage());
                e.printStackTrace();
                ex = e;
                String errStr = "";

                if (e.getMessage().contains("ETIMEDOUT"))
                    errStr = "ETIMEDOUT";
                else if (e.getMessage().contains("ECONNRESET"))
                    errStr = "ECONNRESET";
                else if (e.getMessage().contains("Socket closed"))
                    errStr = "SOCKETCLOSED";

//                if (mHandler != null) {
//                    mHandler.sendMessage(mHandler.obtainMessage(ERROR, errStr));
//                }

                mHandler.sendMessage(ERROR, errStr);
            } catch (JSONException e) {
                Debug.logE("DataServerThread JSONException : " + e.getMessage());
                e.printStackTrace();
                ex = e;
            } catch (Exception e) {
                Debug.logE("DataClientThread Exception : " + e.getMessage());
                e.printStackTrace();
                ex = e;
            } finally {
                Debug.log("DataClientThread finally " + ex);
                Debug.log("Utils.mConnStatus = " + Utils.mConnStatus);
                if (Utils.mConnStatus != Define.CONNECTING_STATUS) {
                    // Utils.mConnStatus = Define.CONNECTING_STATUS;
                    Utils.sendConnectStt(mContext, false);
                }

                // showActionNoti(-1, ex.getMessage());
                //showErrorConnectNoti(ex);
                cancelAllDownload();

                //if (!(ex instanceof  JSONException)){
                Utils.startCheckConnectService(mContext);
                closeSocket();

                if (!Utils.isServiceRunning(PinkService.class, mContext))
                    mContext.startService(new Intent(mContext, PinkService.class));
                //}
            }
        }
    }

//    private void sendHandleMsg(String type, JSONObject j){
//        if (mHandler != null) {
//            int what = getWhat(type);
//            Debug.log("what = " + what);
//
//            if (what != -1){
//                mHandler.sendMessage(mHandler.obtainMessage(what, j));
//            }
//        }else {
//            Debug.logW("Null");
//        }
//    }

//    private int getWhat(String type){
//        if (type.equalsIgnoreCase(Define.JSON_CONNECT))
//            return CONNECT;
//
//        if (type.equalsIgnoreCase(Define.JSON_DOWNLOAD))
//            return DOWNLOAD;
//
//        if (type.equalsIgnoreCase(Define.JSON_DOWNLOAD_CANCEL))
//            return CANCEL_DOWNLOAD;
//
//        if (type.equalsIgnoreCase(Define.JSON_SCHEDULED))
//            return SCHEDULE;
//
//        if (type.equalsIgnoreCase(Define.JSON_DELETED_FOLDER))
//            return DELETE_FOLDER;
//
//        if (type.equalsIgnoreCase(Define.JSON_UPDATE))
//            return UPDATE_APP;
//
//        if (type.equalsIgnoreCase(Define.JSON_REMOVE_GROUP))
//            return REMOVE_GROUP;
//
//        if (type.equalsIgnoreCase(Define.JSON_CHECK_CONNECT))
//            return CHECK_CONNECT;
//
//        if (type.equalsIgnoreCase(Define.JSON_SETTING_LOG))
//            return SHOW_LOG;
//
//        if (type.equalsIgnoreCase(Define.JSON_SETTING_BG))
//            return BACK_GROUND;
//
//        if (type.equalsIgnoreCase(Define.JSON_SETTING_SOUND))
//            return ENABLE_SOUND;
//
//        if (type.equalsIgnoreCase(Define.JSON_RESTART_APP))
//            return RESTART_APP;
//
//        if (type.equalsIgnoreCase(Define.JSON_DELETE_SCHEDULER))
//            return DELETE_SCHEDULER;
//
//        Debug.logW("Unknown!!!");
//        return -1;
//    }

    private boolean checkIncludeScheduler(String str) {
        int count = 0;
        Matcher m = Pattern.compile("type").matcher(str);
        while (m.find()) {
            count++;
        }

        Debug.logI("Count = " + count);
        if (count != 1)
            return true;

        return false;
    }

    private void onSocketConnected() {
        // Intent intent = CheckConnectService.createConnectService(mContext, null);
        // mContext.stopService(intent);
        Debug.log("Socket connected so stop CheckConnectService");
        Intent i = new Intent(Define.ACTION_STOP_CONNECT_THREAD);
        mContext.sendBroadcast(i);
        mApp.updateNotification(mContext, Define.CONNECTED_STATUS);

        Debug.log("is app running = " + Utils.getAppRunning());
        if (!Utils.getAppRunning()) {
            restartApp();
        }

        // Utils.sendConnectStt(mContext, true);
        sendMessageData(JsonMessageManager.createRequestMessage(mContext).toString());
        if (mFileId != -1) {
            sendMessageData(JsonMessageManager.createUpdateResponse(mContext, mFileId, true).toString());
        }

        startConnectingTimer();
        if (!Utils.isServiceRunning(AliveService.class, mContext)) {
            mContext.startService(new Intent(mContext, AliveService.class));
        }

        if (Utils.isServiceRunning(PinkService.class, mContext)) {
            mContext.stopService(new Intent(mContext, PinkService.class));
        }
    }

    public void sendMessage2Server(String msg) {
        if (mHandler == null) {
            Debug.logW("mHandler == null");
        } else {
            mHandler.sendMessage(SEND_MSG_TO_SERVER, msg);
        }
    }

    // to send message
    private void sendMessageData(String message) {
        try {
            if (bos != null) {
                message += "\r\n";
                Debug.log("send message : " + message);
                byte[] results = message.toString().getBytes(Charset.forName("UTF-8"));

                try {
                    Debug.log("write to output stream");
                    bos.write(results);
                    bos.flush();
                    //mPrintWriter.print(message+"\r\n" );
                    //mPrintWriter.flush();
                    //dot.writeUTF(message+"\r\n");
                    //dot.flush();
                } catch (Exception e) {
                    Debug.logW("sendMessage IOException", e);

                    String errStr = "";
                    if (e.getMessage().contains("EPIPE"))
                        errStr = "EPIPE";

//                    if (mHandler != null) {
//                        mHandler.sendMessage(mHandler.obtainMessage(ERROR, errStr));
//                    }

                    mHandler.sendMessage(ERROR, errStr);

                    closeSocket();
                }
            } else {
                Debug.logW("sendMessage bos == null");
            }
        } catch (Exception ex) {
            Debug.logE(ex.getMessage());
        }
    }

    public static byte[] getbytes(byte src[], int offset, int length) {
        byte dest[] = new byte[length];
        System.arraycopy(src, offset, dest, 0, length);
        return dest;
    }

    public void setStopControlThreads() {
        Debug.log("Stop ControlThreads");
        if (!stopFlag) {
            Debug.log("stopFlag false");
        }

        if (socket != null) {
            try {
                socket.close();
                socket = null;
                Debug.log("sock = null");
            } catch (IOException e) {
                Debug.logE("Stop ControlThreads sock.close() : " + e.getMessage(), e);
            }
        }

        if (dataThread != null) {
            dataThread.interrupt();
            Debug.log("dataThread.interrupt()");
        }
    }

    public void checkFinish() {
        Debug.log("finished checking connect!!!");

        new Thread(new Runnable() {

            @Override
            public void run() {
                Debug.log("finish thread running!!!");

                if (connectThread != null) {
                    try {
                        connectThread.join();
                    } catch (InterruptedException e) {
                        Debug.logE("InterruptedException : " + e.getMessage(), e);
                    }

                    Debug.log("connectThread = null");
                    connectThread = null;
                }
            }
        }).start();
    }

    public synchronized void closeSocket() {
        Debug.log("close Socket");
//        if (mHandler != null) {
//            mHandler.sendMessage(mHandler.obtainMessage(CLOSE, null));
//        }

        mHandler.sendEmptyMessage(CLOSE);

        stopFlag = false;
        try {
            if (bis != null) {
                bis.close();
                bis = null;
            }

            if (bos != null) {
                bos.close();
                bos = null;
            }

            if (socket != null) {
                socket.close();
                socket = null;
            }

            new Thread(new Runnable() {

                @Override
                public void run() {

                    if (dataThread != null) {

                        if (isThreadDataRunning) {
                            // to avoid entered here two times before thread is finished
                            isThreadDataRunning = false;
                            Debug.log("dataThread != null");
                            // dataThread.interrupt();

                            try {
                                Debug.log("destroy dataThread");
                                dataThread.join();
                            } catch (InterruptedException e) {
                                Debug.logE("close Socket InterruptedException : " + e.getMessage());
                            }

                            Debug.log("releasing dataThread");
                            dataThread = null;
                        }
                    }
                }
            }).start();

            //connectThread = null;
        } catch (Exception e) {
            Debug.logW(e);
        }
    }

    public boolean isConnected() {
        boolean isConnected = false;
        if (socket != null) {
            isConnected = socket.isConnected();
        }

        return isConnected;
    }

    private void restartApp() {
        Debug.log("Launching app!!");
        Intent startIntent = new Intent(mContext, MainActivity.class);
        startIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(startIntent);
    }

    private void startConnectingTimer() {
        if (checkConnectTimer == null) {
            Debug.log("Timer -- Start");
            Looper.prepare();

            checkConnectTimer = new CountDownTimer(3 * 3000, 3000) {

                @Override
                public void onTick(long millisUntilFinished) {
                    Debug.log("isReceiveConnectedMessage = " + isReceiveConnectedMessage);
                    if (isReceiveConnectedMessage) {
                        cancelConnectingTimer();
                    } else {
                        // send message connect to server
                        sendMessageData(JsonMessageManager.createRequestMessage(mContext).toString());
                    }
                }

                @Override
                public void onFinish() {
                    Debug.log("Timer -- Finish");
                    // cancel connecting timer
                    cancelConnectingTimer();
                    // reconnect to web socket
                    if (!isReceiveConnectedMessage) {
                        closeSocket();
                        Utils.startCheckConnectService(mContext);
                        if (!Utils.isServiceRunning(PinkService.class, mContext))
                            mContext.startService(new Intent(mContext, PinkService.class));
                    }
                }
            }.start();
        }
    }

    private void cancelConnectingTimer() {
        if (checkConnectTimer != null) {
            Debug.log("Timer -- Cancel");
            checkConnectTimer.cancel();
            checkConnectTimer = null;
        }
    }

    private void handleDeletedFile(Object obj) {
        JSONObject j = (JSONObject) obj;
        if (j == null) {
            Debug.logW("Null");
            return;
        }

        try {
            String fileName = j.getString(Define.JSON_DOWNLOAD_FOLDERNAME);
            // int file_id = j.getInt(Define.JSON_DOWNLOAD_FILEID);
            String folderName = j.getString(Define.JSON_SCHEDULED_FOLDER_NAME);
            File file;
            if (TextUtils.isEmpty(folderName)) {
                file = new File(Define.APP_PATH + fileName);
            } else {
                file = new File(Define.APP_PATH + folderName);
            }

            checkDeletedFolder(file);
            // Utils.sendDeletedFileResponse2Server(mContext, file_id);
        } catch (JSONException e) {
            Debug.logW(e);
        }
    }

    private void checkDeletedFolder(File folder) {
        if (deleteFolder(folder)) {
            Debug.log("Deleted file/folder OK");
        } else {
            Debug.logW("File/Folder does not exist " + folder.getAbsolutePath());
        }
    }

    private boolean deleteFolder(File folder) {
        if (!folder.isDirectory()) {
            if (folder.exists()) {
                folder.delete();
                return true;
            }

            return false;
        }

        if (folder.exists()) {
            File[] fs = folder.listFiles();

            for (int i = 0; i < fs.length; i++) {
                File f = fs[i];
                f.delete();
            }

            return folder.delete();
        } else {
            return false;
        }
    }

    private void handleDownloadFile(Object obj, int action) {
        JSONObject receiveJson = (JSONObject) obj;
        Debug.logI("obj = " + obj);
        if (receiveJson == null) {
            Debug.logW("receiveJson download is null!!!!");
            return;
        }

        String downloadPath = "";
        String fileUrl = "";
        String fileName = "";
        String fileExtension = "";
        String folderName = "";
        int fileID = 0;
        long fileSize = 0;
        boolean isUpdate = false;
        try {
            if (action == DOWNLOAD) {
                fileUrl = receiveJson.getString(Define.JSON_DOWNLOAD_URL);
                fileID = receiveJson.getInt(Define.JSON_DOWNLOAD_FILEID);
                fileSize = receiveJson.getLong(Define.JSON_DOWNLOAD_FILESIZE);

                long available = Utils.sd_card_free();
                Debug.log("available = " + Utils.bytesToHuman(available));
                Debug.log("file_size = " + Utils.bytesToHuman(fileSize));
                if (available < fileSize) {
                    Utils.sendOutOfSpace2Server(mContext);
                    Debug.logE("Out of Space on device!!!!!!!");
                    return;
                }

                fileExtension = receiveJson.getString(Define.JSON_DOWNLOAD_FILEEXTENSION);
                folderName = receiveJson.getString(Define.JSON_DOWNLOAD_FOLDERNAME);

                if (TextUtils.isEmpty(folderName)) {
                    downloadPath = Define.DOWNLOAD_PATH;
                } else {
                    downloadPath = Define.DOWNLOAD_PATH + "/" + folderName;
                }

                fileName = receiveJson.getString(Define.JSON_DOWNLOAD_FILENAME);
                File f = new File(Define.DOWNLOAD_PATH + fileName);
                if (f.exists() && f.isFile()) {
                    if (f.length() == fileSize) {
                        Debug.logI("File exists!");
                        Utils.sendDownloadResponse2Server(mContext, fileID, true, folderName);
                        return;
                    }
                }
            } else if (action == UPDATE_APP) {
                isUpdate = true;
                fileUrl = receiveJson.getString(Define.JSON_UPDATE_URL);
                fileID = receiveJson.getInt(Define.JSON_UPDATE_SOFTWARE_ID);
                fileSize = receiveJson.getLong(Define.JSON_UPDATE_FILESIZE);
                fileName = receiveJson.getString(Define.JSON_UPDATE_FILENAME);
                fileExtension = receiveJson.getString(Define.JSON_UPDATE_FILEEXTENSION);

                downloadPath = Define.DOWNLOAD_PATH;
            }
        } catch (Exception e) {
            Debug.logE(e);
            return;
        }

        try {
            if (fileUrl.contains(" ")) {
                fileUrl = fileUrl.replaceAll(" ", "%20");
            }

//            Uri uri = Uri.parse(fileUrl);
//            DownloadManager.Request request = new DownloadManager.Request(uri);
//            request.setTitle("Download");
//            request.setDescription(fileName);

            //Delete file if exist before enqueue
            File file = new File(Environment.getExternalStorageDirectory() + downloadPath + "/" + fileName);
            if (file.exists())
                file.delete();

            // request.setDestinationInExternalPublicDir(downloadPath, fileName);
            DownloadManager.Request r = getRequest(fileUrl, downloadPath, fileName);
            long downloadID = downloadManager.enqueue(r);

            DownloadFile downloadFile = new DownloadFile(downloadID, fileID, fileUrl, fileName, fileExtension, folderName, fileSize, isUpdate);
            downloadFile.setDownloadPath(downloadPath);
            mApp.getDownloadFileList().add(downloadFile);

            if (!Utils.isServiceRunning(DownloadService.class, mContext))
                mContext.startService(new Intent(mContext, DownloadService.class));

//            if (mContext instanceof BaseActivity) {
//                ((BaseActivity) mContext).runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Utils.showToastLong(mContext, R.string.dl_start, downloadFile.getFileName());
//                    }
//                });
//            }
            //mToastHandler.sendMessage(mToastHandler.obtainMessage(R.string.dl_start, downloadFile.getFileName()));
            //showActionNoti(R.string.dl_start, downloadFile.getFileName());
        } catch (Exception ex) {
            Debug.logW(ex);
        }
    }

    private DownloadManager.Request getRequest(String fileUrl, String path, String name) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fileUrl));
        request.setTitle("Download");
        request.setDescription(name);
        request.setDestinationInExternalPublicDir(path, name);

        return request;
    }

    private void cancelAllDownload() {
        Debug.log("cancel all download");
        try {
            if (Utils.isServiceRunning(DownloadService.class, mContext)) {
                mContext.stopService(new Intent(mContext, DownloadService.class));
            }

            for (DownloadFile f : mApp.getDownloadFileList()) {
                downloadManager.remove(f.getDownloadId());
            }

            mApp.getDownloadFileList().clear();
//            Process process = Runtime.getRuntime().exec("su");
//            DataOutputStream dos = new DataOutputStream(process.getOutputStream());
//            dos.writeBytes("adb shell" + "\n");
//            dos.flush();
//            dos.writeBytes("am force-stop com.android.providers.downloads" + "\n");
//            dos.flush();
        } catch (Exception ex) {
            Debug.logW(ex.getMessage());
        }
    }

    private void handleCancelDownload(Object obj) {
        JSONObject receiveJson = (JSONObject) obj;
        if (receiveJson == null) {
            Debug.log("receiveJson cancel download is null");
            return;
        }

        try {
//            int fileID = receiveJson.getInt(Define.JSON_DOWNLOAD_FILEID);
//            Debug.logD("Cancel ID = " + fileID);
//            ArrayList<DownloadFile> fileList = new ArrayList<>();
//            for (DownloadFile f : mApp.downloadFileList) {
//                if (f.getFileID() == fileID) {
//                    fileList.add(f);
//                    downloadManager.remove(f.getDownloadId());
//                    Debug.logD("Cancel download file: " + f.getFileName());
//
//                    final String str = f.getFileName();
//                    Utils.sendCancelDownloadResponse2Server(mContext, f.getFileID());

//                    if (mContext instanceof BaseActivity) {
//                        ((BaseActivity) mContext).runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Utils.showToastLong(mContext, R.string.dl_end, str);
//                            }
//                        });
//                    }

            //mToastHandler.sendMessage(mToastHandler.obtainMessage(R.string.dl_end, str));
//                    showActionNoti(R.string.dl_end, str);
//                }
//            }
//
//            for (DownloadFile file4Del : fileList)
//                mApp.downloadFileList.remove(file4Del);

            cancelAllDownload();

            String f = receiveJson.getString(Define.JSON_DOWNLOAD_FOLDERNAME);
            String[] folders = f.split(";");
            for (int i = 0; i < folders.length; i++) {
                String str = folders[i];
                File folder = new File(Define.APP_PATH + str);
                checkDeletedFolder(folder);
            }
        } catch (JSONException e) {
            Debug.logW(e.getLocalizedMessage());
        } catch (Exception ex) {
            Debug.logW(ex);
        }
    }

    private ScheduleDBManager dbManager;

    private void handleScheduled(Object obj) {
        Debug.log("obj = " + obj);
        JSONObject j = (JSONObject) obj;
        try {
            String action = j.getString(Define.JSON_SCHEDULED_ACTION);

            int id = j.getInt(Define.JSON_SCHEDULED_ID);

            String file_extension = j.getString(Define.JSON_SCHEDULED_FILE_EXTENSION);
            int file_id = j.getInt(Define.JSON_SCHEDULED_FILE_ID);
            String file_name = j.getString(Define.JSON_SCHEDULED_FILE_NAME);
            String folder_name = j.getString(Define.JSON_SCHEDULED_FOLDER_NAME);

            String start_date = j.getString(Define.JSON_SCHEDULED_START_DATE);
            String end_date = j.getString(Define.JSON_SCHEDULED_END_DATE);
            String start_time = j.getString(Define.JSON_SCHEDULED_START_TIME);
            String end_time = j.getString(Define.JSON_SCHEDULED_END_TIME);

            int broadcast = j.getInt(Define.JSON_SCHEDULED_BROADCAST);
            String server_time_str = j.getString(Define.JSON_SCHEDULED_DATE_TIME_NOW);
            long device_time = System.currentTimeMillis();
            Debug.log("server_time = " + server_time_str + ", device_time = " + Utils.longToDate(device_time));

            // long curTime = Utils.getSynchronizeTime(date_time_now);

            long server_time = Utils.dateToLong(server_time_str);

            SettingManager.saveSetting(mContext, Define.TIME_OF_CLIENT_NOW, device_time);
            // SettingManager.saveSetting(mContext, Define.TIME_OF_SERVER_NOW, server_time);
            // Utils.setTimeToDevice(server_time);

            // device_time = System.currentTimeMillis();
            // Debug.log("server_time = " + server_time_str + ", device_time 2 = " + Utils.longToDate(device_time));

            if (action.equalsIgnoreCase(ADD)) {//add lich moi
                ScheduleItem item = new ScheduleItem(id, file_extension, file_id, file_name, folder_name,
                        start_date, end_date, start_time, end_time, broadcast);
                dbManager.addSchedule(item);

                scheduleAll(server_time, device_time);
//                if (mContext instanceof MainActivity) {
//                    Debug.log("Scheduled calendar");
//                    ((MainActivity) mContext).scheduleAll(server_time, device_time);
//                }else {
//                    if (mContext instanceof CheckConnectService){
//                        Debug.logW("CheckConnectService");
//                    }else if (mContext instanceof AliveService){
//                        Debug.logW("v");
//                    }
//
//                    Debug.logW("Unable to schedule calendar!!!");
//                }
            } else if (action.equalsIgnoreCase(UPDATE)) {//cap nhat lich da co
                ScheduleItem item = new ScheduleItem(id, file_extension, file_id, file_name, folder_name,
                        start_date, end_date, start_time, end_time, broadcast);
                dbManager.updateSchedule(item);

                scheduleAll(server_time, device_time);
//                if (mContext instanceof MainActivity) {
//                    ((MainActivity) mContext).scheduleAll(server_time, device_time);
//                }else {
//                    Debug.logW("Unable to UPDATE schedule calendar!!!");
//                }
            } else if (action.equalsIgnoreCase(DELETE)) {//xoa lich
                BaseActivity ba = (BaseActivity) TvContentSyncApplication.getInstance().getCurActivity();
                if (ba != null && ba.getId() == id) {
                    ba.cancelCalendar();
                    Debug.log("Cancel calendar running!!");
                } else {
                    Debug.log("Do nothing, id = " + id);
                }

                cancelSchedule(id);
//                if (mContext instanceof MainActivity) {
//                    ((MainActivity) mContext).cancelSchedule(id);
//                }else {
//                    Debug.logW("Unable to DELETE schedule calendar!!!");
//                }
            }
        } catch (JSONException e) {
            Debug.logE(e);
        } catch (Exception ex) {
            Debug.logW(ex);
        }
    }

    private  ScheduleManager mScheduleManager;

    /*tao lich cho tat ca */
    public void scheduleAll(long serverTime, long deviceTime) {
        Debug.log("deviceTime = " + Utils.long2DateTime(deviceTime) + ", serverTime = " + Utils.long2DateTime(serverTime));
        if (mScheduleManager != null)
            mScheduleManager.scheduleAll(serverTime, deviceTime);
        else
            Debug.logW("mScheduleManager = null");
    }

    public void cancelSchedule(int scheduleId) {
        //xoa lich nay khoi co so du lieu
        if (mScheduleManager != null)
            mScheduleManager.cancelSchedule(scheduleId, true);
        else
            Debug.logW("mScheduleManager = null");
    }

    public void deleteAndCancelCalendar() {
        Debug.log("Delete all scheduler!!!");
        mScheduleManager.deleteAllCalendar();
    }

//    private class ToastHandler extends Handler {
//
//        public ToastHandler() {
//            super(Looper.getMainLooper());
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//
//            if (msg.what == 0){
//                Utils.showToast(mContext, msg.obj.toString());
//            }else {
//                Utils.showToastLong(mContext, msg.what, msg.obj.toString());
//            }
//        }
//    }

    public int uploadFile(final String sourceFileUri, String upLoadServerUri) {
        String fileName = sourceFileUri;
        int serverResponseCode = 0;
        // http://androidexample.com/Upload_File_To_Server_-_Android_Example/index.php?view=article_discription&aid=83
        // http://www.codicode.com/art/upload_files_from_android_to_a_w.aspx
        // https://www.codepuppet.com/2013/03/26/android-uploading-a-file-to-a-php-server/
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);

        if (!sourceFile.isFile()) {
            Debug.logE("uploadFile", "Source File not exist :" + sourceFileUri);
            //showActionNoti(-1, "Source File not exist :" + sourceFileUri);
            return 0;
        } else {
            try {

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                //dos.writeBytes("Content-Disposition: form-data; name=" + sourceFileUri + ";filename=""+ fileName + """ + lineEnd);


                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Debug.logI("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                if (serverResponseCode == 200) {
                    //showActionNoti(-1, "File Upload Complete.");
                    Debug.log("File Upload Complete.");
                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {
                ex.printStackTrace();
                // showActionNoti(-1, "MalformedURLException Exception : check script url.");
                Debug.logE("Upload file to server", "error: " + ex.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                //showActionNoti(-1, "Got Exception : see logcat ");
                Debug.logE("Upload file to server Exception", "Exception : "
                        + e.getMessage());
            }

            return serverResponseCode;

        } // End else block
    }

    protected void uploadFile() {
        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;
        // DataInputStream inputStream = null;
        File[] files = new File(Define.APP_PATH_LOGS).listFiles();
        String pathToOurFile = files[0].getAbsolutePath();
        String urlServer = "http://107.113.186.175";
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        Debug.log("tester", pathToOurFile);
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(
                    pathToOurFile));
            URL url = new URL(urlServer);
            connection = (HttpURLConnection) url.openConnection();
            // Allow Inputs & Outputs
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            // Enable POST method
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type",
                    "multipart/form-data;boundary=" + boundary);
            outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream
                    .writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\""
                            + pathToOurFile + "\"" + lineEnd);
            outputStream.writeBytes(lineEnd);
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];
            // Read file
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            while (bytesRead > 0) {
                outputStream.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + twoHyphens
                    + lineEnd);
            // Responses from the server (code and message)
            @SuppressWarnings("unused")
            int serverResponseCode = connection.getResponseCode();
            @SuppressWarnings("unused")
            String serverResponseMessage = connection.getResponseMessage();
            Debug.log("uploadFile", "HTTP Response is : "
                    + serverResponseMessage + ": " + serverResponseCode);
            fileInputStream.close();
            outputStream.flush();
            outputStream.close();
            Debug.log("tester", pathToOurFile);
        } catch (Exception ex) {
            // Exception handling
            ex.printStackTrace();
            Debug.logE("Upload file to server Exception", "Exception : "
                    + ex.getMessage());
        }
    }

    private void getLog(String name, String pwd) {
        FTPClient ftp = new FTPClient();
        try {
            ftp.connect(SettingManager.getIPServer(mContext));
            //String name = SettingManager.getSetting(mContext, Define.FTP_NAME, String.class).toString().trim();
            //String pwd = SettingManager.getSetting(mContext, Define.FTP_PASSWORD, String.class).toString().trim();
            Debug.log(name, pwd);
            if (name == "" || pwd == "") {
                name = "hoc";
                pwd = "123";
            }

            ftp.login(name, pwd);
            File f = Utils.saveLogFile();
            FileInputStream fis = new FileInputStream(f);
            ftp.storeFile(f.getName(), fis);

            fis.close();
            ftp.logout();
            ftp.disconnect();
        } catch (Exception e) {
            Debug.logW(e.getMessage());
        }
    }
}