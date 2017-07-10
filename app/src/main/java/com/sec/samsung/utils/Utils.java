package com.sec.samsung.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.StatFs;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.sec.samsung.connect.CheckConnectService;
import com.sec.samsung.connect.ControlClient;
import com.sec.samsung.device.DeviceInfoDefine;
import com.sec.samsung.schedule.ScheduleDBManager;
import com.sec.samsung.tvcontentsync.BaseActivity;
import com.sec.samsung.tvcontentsync.R;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

public class Utils {

    private static final String DATE_FORMAT             = "yyyy_MM_dd-HH_mm_ss";
    public static final String DATE_FORMAT_FULL         = "yyyy/MM/dd HH:mm:ss";
    public static final String DATE_TIME_FORMAT_FULL    = "yyyy/MM/dd HH:mm:ss.SSS";
    public static boolean isAppRunning                  = false;
    public static boolean isRoot                        = false;

    // App status
    public static int mConnStatus                       = 0;
    public static int appStatus                         = 0;
    public static int appServiceStatus                  = 0;

    // App count connect
    public static int mCountConnect                     = 0;

    // Time offset
    public static long TIME_OFF_SET                     = 0;

    public static String getUserFriendlyDisplayName(Context context) {
        String userFriendlyDisplayName = "";
        userFriendlyDisplayName = DeviceInfoDefine.getUserFriendlyDisplayName(context);
        if (userFriendlyDisplayName == null || userFriendlyDisplayName.isEmpty()) {
            return getDeviceName(context);
        } else {
            return userFriendlyDisplayName;
        }
    }

    public static String getModelName() {
        String modelName = new String(android.os.Build.MODEL);
        // Debug.log("[getModelName] > modelName : " + modelName);
        return modelName;
    }

    public static String getDeviceName(Context context) {
        String deviceName = Settings.System.getString(context.getContentResolver(), "device_name");
        if (deviceName == null) {
            if (Build.MODEL != null) {
                deviceName = Build.MODEL;
            } else {
                deviceName = "Samsung Mobile";
            }
        }
        return deviceName;
    }

    public static String getSystemProperty(String strParam) {
        Class<?> Klass = null;
        String strRet = "";
        try {
            if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                Klass = Class.forName("android.os.SemSystemProperties");
            } else {
                Klass = Class.forName("android.os.SystemProperties");
            }
            Method method = Klass.getMethod("get", String.class);
            strRet = (String) method.invoke(Klass, strParam);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            Debug.logW(e);
        }
        return strRet;
    }

    public static String getSerialNumber() {
        String serialNumber = "";
        serialNumber = DeviceInfoDefine.getSerialNumber();
        return serialNumber;
    }

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showToastLong(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static void showToastLong(Context context, int msg, @Nullable String txt) {
        Toast.makeText(context, context.getResources().getString(msg, txt), Toast.LENGTH_SHORT).show();
    }

    public static void showToastShort(Context context, int msg, @Nullable String txt) {
        Toast.makeText(context, context.getResources().getString(msg, txt), Toast.LENGTH_SHORT).show();
    }

    public static void installService(Context context) {
        final File file = new File(Define.APP_PATH + Define.SERVICE_INSTALLER_APK);
        File folderApp = new File(Define.APP_PATH);
        if (!folderApp.exists()) {
            if (!folderApp.mkdir()) {
                Debug.logW("Can not create folder app!!!");
                return;
            }
        }

        try {
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    Debug.logW("Can  not create file");
                    return;
                } else {
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    InputStream inputStream = context.getAssets().open(Define.SERVICE_INSTALLER_APK);

                    int read;
                    byte[] buffer = new byte[1024];
                    while ((read = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, read);
                    }

                    fileOutputStream.flush();
                    fileOutputStream.close();
                    inputStream.close();
                }
            }

            new Thread() {
                @Override
                public void run() {
                    super.run();

                    try {
                        String command = "pm install -r " + file.getAbsolutePath();
                        Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", command});
                        proc.waitFor();
                        Debug.logI("Install OK");
                        isRoot = true;
                    } catch (InterruptedException | IOException e) {
                        Debug.logW(e);
                        isRoot = false;
                    }
                }
            }.start();
        } catch (Exception e) {
            Debug.logW(e);
        }
    }

    /**
     * check package is installed
     *
     * @param packagename
     * @param packageManager
     * @return
     */
    public static boolean isPackageInstalled(String packagename, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
            Debug.log("Service installed");
            isRoot = true;
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Debug.log("Service not installed");
            isRoot = false;
            return false;
        }
    }

    /**
     * send response scheduled result to server
     *
     * @param context
     * @param file_id
     * @param scheduled_id
     * @param success
     */
    public static void sendScheduledResponse2Server(Context context, int file_id, int scheduled_id, boolean success) {
        ControlClient m = ControlClient.getInstance(context);
        if (m.isConnected()) {
            JSONObject object = JsonMessageManager.createScheduledResponse(context, file_id, scheduled_id, success);
            m.sendMessage2Server(object.toString());
            Debug.log("connected, send sendScheduledResponse2Server OK:" + object.toString());
        } else {
            Debug.logW("Not connected!!!");
        }
    }

    public static void sendDownloadResponse2Server(Context context, int file_id, boolean success, String folderName) {
        ControlClient m = ControlClient.getInstance(context);
        if (m.isConnected()) {
            JSONObject object = JsonMessageManager.createDownloadResponse(context, file_id, success, folderName);
            m.sendMessage2Server(object.toString());
            Debug.log("connected, sendDownloadResponse2Server OK: " + object.toString());
        } else {
            Debug.logW("Not connected!!!");
        }
    }

    public static void sendCancelDownloadResponse2Server(Context context, int file_id) {
        ControlClient m = ControlClient.getInstance(context);
        if (m.isConnected()) {
            JSONObject object = JsonMessageManager.createCancelDownloadResponse(context, file_id);
            m.sendMessage2Server(object.toString());
            Debug.log("connected, sendCancelDownloadResponse2Server OK: " + object.toString());
        } else {
            Debug.logW("Not connected!!!");
        }
    }

    public static void sendReportDownloadResponse2Server(Context context, int file_id, long size_downloaded) {
        ControlClient m = ControlClient.getInstance(context);
        if (m.isConnected()) {
            JSONObject object = JsonMessageManager.createReportDownloadResponse(context, file_id, size_downloaded);
            m.sendMessage2Server(object.toString());
            Debug.log("connected size_downloaded: " + bytesToHuman(size_downloaded));
        } else {
            Debug.logW("Not connected!!!");
        }
    }

    public static void sendUpdateResponse2Server(Context context, int file_id, boolean success) {
        ControlClient m = ControlClient.getInstance(context);
        if (m.isConnected()) {
            JSONObject object = JsonMessageManager.createUpdateResponse(context, file_id, success);
            m.sendMessage2Server(object.toString());
            Debug.log("connected, sendUpdateResponse2Server OK: " + object.toString());
        } else {
            Debug.logW("Not connected!!!");
        }
    }

    public static void sendReportUpdateResponse2Server(Context context, int file_id, long size_downloaded) {
        ControlClient m = ControlClient.getInstance(context);
        if (m.isConnected()) {
            JSONObject object = JsonMessageManager.createReportUpdateResponse(context, file_id, size_downloaded);
            m.sendMessage2Server(object.toString());
            Debug.log("connected send to server  size_downloaded = " + bytesToHuman(size_downloaded));
        } else {
            Debug.logW("Not connected!!!");
        }
    }


    public static void sendDeletedFileResponse2Server(Context context, int file_id) {
        ControlClient m = ControlClient.getInstance(context);
        if (m.isConnected()) {
            JSONObject object = JsonMessageManager.createDeletedFileResponse(context, file_id);
            m.sendMessage2Server(object.toString());
            Debug.log("connected, sendDeletedFileResponse2Server OK: " + object.toString());
        } else {
            Debug.logW("Not connected!!!");
        }
    }

    public static void sendOutOfSpace2Server(Context c) {
        ControlClient cc = ControlClient.getInstance(c);
        if (cc.isConnected()) {
            JSONObject object = JsonMessageManager.createOutOfSpace(c);
            cc.sendMessage2Server(object.toString());
            Debug.log("connected, sendOutOfSpace2Server OK: " + object.toString());
        } else {
            Debug.logW("Not connected!!!");
        }
    }

    public static void sendCheckConnect2Server(Context c) {
        ControlClient cc = ControlClient.getInstance(c);
        if (cc.isConnected()) {
            JSONObject object = JsonMessageManager.createCheckConnect(c);
            cc.sendMessage2Server(object.toString());
            Debug.log("connected, sendCheckConnect2Server OK: " + object.toString());
        } else {
            Debug.logW("Not connected!!!");
        }
    }

    /*Neu co thoi gian sync tu sever hay internet thi doi lai ham nay*/
    public static long getSynchronizeTime(String dateStr) {
        long time = -1;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_FULL);
            Date date = sdf.parse(dateStr);
            time = date.getTime();
        } catch (ParseException e) {
            Debug.log("cannot parse date " + dateStr);
        }
        return time;
    }

    public static String longToDate(long time) {
        return DateFormat.format(DATE_FORMAT_FULL, time).toString();
    }

    public static String long2DateTime(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT_FULL);
        return sdf.format(time);
    }

    public static String long2DateTimeFile(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.format(time);
    }

    public static long dateToLong(String dateStr) {
        long time = -1;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_FULL);
            Date date = sdf.parse(dateStr);
            time = date.getTime();
        } catch (ParseException e) {
            Debug.log("cannot parse date " + dateStr);
        }
        return time;
    }

    public static long advanceDay(String date) {
        long time = dateToLong(date);
        return time + AlarmManager.INTERVAL_DAY;
    }

    public static long backDay(String date) {
        long time = dateToLong(date);
        return time - AlarmManager.INTERVAL_DAY;
    }

    public static int createRequestId(int scheduleId, int idx) {
        return scheduleId * 1000 + idx;
    }

    public static String combileDateAndTime(String date, String time) {
        return date + " " + time;
    }

    public static void setAppRunning(boolean isRunning) {
        isAppRunning = isRunning;
    }

    public static boolean getAppRunning() {
        return isAppRunning;
    }

    public static boolean isServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> ls = manager.getRunningServices(Integer.MAX_VALUE);
        if (ls == null || ls.size() == 0) {
            return false;
        }

        for (ActivityManager.RunningServiceInfo service : ls) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isActivityRunning(Context context, Class<?> activityClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> ls = manager.getRunningTasks(Integer.MAX_VALUE);

        for (ActivityManager.RunningTaskInfo info : ls) {
            if (info.topActivity.getClassName().equalsIgnoreCase(activityClass.getName())) {
                return true;
            }
        }

        return false;
    }

    public static void startCheckConnectService(Context context) {
        if (isServiceRunning(CheckConnectService.class, context)) {
            Debug.logD("CheckConnectService is already running. Refresh connect time");
            Intent intent = CheckConnectService.createConnectService(context, Define.ACTION_REFRESH_CONNECT_TIME);
            context.startService(intent);
        } else {
            Intent intent = CheckConnectService.createConnectService(context, Define.ACTION_START_CONNECT_THREAD);
            context.startService(intent);
        }
    }

    private static Uri getAudioContentUri(Context context, File audioFile) {
        String filePath = audioFile.getAbsolutePath();
        Debug.log(filePath);

        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Audio.Media._ID}, MediaStore.Audio.Media.DATA + "=? ", new String[]{filePath}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            cursor.close();
            return Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "" + id);
        } else {
            if (audioFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Audio.Media.DATA, filePath);
                return context.getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    public static Bitmap getAudioThumbnail(Context context, File audioFile) {
        Bitmap bmp = null;
        Uri uri = getAudioContentUri(context, audioFile);

        if (uri != null) {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(context, uri);
            byte[] buf = mmr.getEmbeddedPicture();
            bmp = BitmapFactory.decodeByteArray(buf, 0, buf.length, new BitmapFactory.Options());
        }

        return bmp;
    }

    public static Bitmap getAudioThumbnail(String audioPath) {
        try {
            Debug.log(audioPath);
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(audioPath);
            byte[] buf = mmr.getEmbeddedPicture();
            Bitmap bmp = BitmapFactory.decodeByteArray(buf, 0, buf.length, new BitmapFactory.Options());
            return bmp;
        } catch (Exception ex) {
            Debug.logW(ex);
            return null;
        }
    }

    public static void setScreenBright(Activity a) {
        Window w = a.getWindow();
        int f1 = WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
        int f2 = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        int f3 = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        int f4 = WindowManager.LayoutParams.FLAG_FULLSCREEN;

        if (!isTablet(a)) {
            if (SettingManager.getModeUser(a)) {
                w.addFlags(f1 | f2 | f3 | f4);
                Settings.System.putInt(a.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 50);
            } else {
                Settings.System.putInt(a.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 100);
                int f = w.getAttributes().flags;
                if ((f & f1) != 0) w.clearFlags(f1);
                if ((f & f2) != 0) w.clearFlags(f2);
                if ((f & f3) != 0) w.clearFlags(f3);
                // if ((f & f4) != 0) w.clearFlags(f4);
            }
        }
    }

    public static void turnOnScreen(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "on");
        wl.acquire();
    }

    public static void setKeyGuard(Context context) {
        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("lock");

        if (SettingManager.getModeUser(context) && !isTablet(context))
            kl.disableKeyguard();
        else
            kl.reenableKeyguard();
    }

    public static String convertMili2DateTime(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_FULL);
        return sdf.format(time);
    }

    public static void setTimeToDevice(final long server_time) {
        /*SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT_FULL);
        Debug.log("server time = " + sdf.format(server_time));

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    String sTime = Utils.convertMili2DateTime(server_time);
                    Debug.log("sTime = " + sTime);

                    Process p = Runtime.getRuntime().exec("su");
                    DataOutputStream dos = new DataOutputStream(p.getOutputStream());
                    String date = sTime.replaceAll("/", "");
                    date = date.replaceAll(" ", ".");
                    date = date.replaceAll(":", "");
                    Debug.log("date = " + date);
                    dos.writeBytes("date -s " + date + "; \n");
                } catch (IOException e) {
                    Debug.logW(e);
                }
            }
        }).start();*/
    }

    public static boolean isConnectedNetwork(Context c) {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null && ni.isConnected()) {
            return true;
        }

        return false;
    }

    public static void showAlert(Context c, String msg, DialogInterface.OnClickListener listener, DialogInterface.OnClickListener listener1) {
        AlertDialog.Builder b = new AlertDialog.Builder(c);
        b.setTitle(c.getResources().getString(R.string.app_name)).setIcon(R.mipmap.app_icon).
                setMessage(msg).setPositiveButton("OK", listener).
                setNegativeButton("Close", listener1);
        b.show();
        b = null;
    }

    public static void showAlert(Context c, String title, String msg) {
        AlertDialog.Builder b = new AlertDialog.Builder(c);
        b.setTitle(title).setIcon(R.mipmap.app_icon).
                setMessage(msg).setMessage(msg).setPositiveButton("OK", null);
        b.show();
        b = null;
    }

    /**
     * auto update, not need call {@link Utils#backUpDb(Context)}
     *
     * @param c
     * @param fileName
     * @param fileID
     */
    public static void callUpdate(Context c, String fileName, int fileID) {
        if (!isRoot){
            try {
                showToast(c, "Device not rooted!!!");
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setDataAndType((Uri.fromFile(new File(Define.APP_PATH + fileName))), "application/vnd.android.package-archive");
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                c.startActivity(i);
                if (c instanceof Activity)
                    ((Activity)c).finish();

                System.exit(0);
            }catch (Exception ex){
                Debug.logW(ex.getMessage());
            }

            return;
        }

        try {
            SettingManager.saveSetting(c, new String[]{Define.APK_UPDATE_SWID, Define.APK_PREV_VER, Define.IP_ADDRESS},
                    fileID, getVersionCode(c), SettingManager.getIPServer(c));

            Intent i = new Intent(Define.SERVICE_INSTALLER_NAME);
            i.setPackage(Define.SERVICE_INSTALLER_PKG);

            i.putExtra(Define.APK_UPDATE_PATH, Define.APP_PATH + fileName);
            i.putExtra(Define.APK_UPDATE_PKG, c.getPackageName());
            c.startService(i);

            if (c instanceof Activity)
                ((Activity)c).finish();

            System.exit(0);
        }catch (Exception ex){
            Debug.logW(ex.getMessage());
        }
    }

    public static int getVersionCode(Context c) {
        try {
            PackageInfo pi = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
            int verCode = pi.versionCode;
            return verCode;
        } catch (PackageManager.NameNotFoundException e) {
            Debug.logW(e);
            return -1;
        }
    }

    public static void backUpDb(Context c) {
        Debug.log("Back up db");
        File src = new File(Environment.getDataDirectory() + "/data/" + c.getPackageName() + "/databases/" + ScheduleDBManager.DATABASE_NAME);
        File dst = new File(Define.APP_PATH + ScheduleDBManager.DATABASE_NAME);
        if (!dst.exists()) {
            try {
                if (!dst.createNewFile()) {
                    return;
                }

                copyFile(src, dst);
            } catch (IOException e) {
                Debug.logW(e);
            }
        }
    }

    public static void restoreDb(Context c) {
        Debug.log("Restore db");
        File dst = new File(Environment.getDataDirectory() + "/data/" + c.getPackageName() + "/databases/" + ScheduleDBManager.DATABASE_NAME);
        File src = new File(Define.APP_PATH + ScheduleDBManager.DATABASE_NAME);
        if (!dst.exists()) {
            try {
                if (!dst.createNewFile()) {
                    return;
                }

                copyFile(src, dst);
            } catch (IOException e) {
                Debug.logW(e);
            }
        }
    }

    private static void copyFile(File src, File dst) {
        try {
            InputStream is = new FileInputStream(src);
            OutputStream os = new FileOutputStream(dst);
            byte[] buf = new byte[1024];
            int len;
            while ((len = is.read(buf)) > 0) {
                os.write(buf, 0, len);
            }

            os.flush();
            os.close();
            is.close();
        } catch (FileNotFoundException e) {
            Debug.logW(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void settingWritePermission(Activity a) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(a) && !isTablet(a)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + a.getPackageName()));
                a.startActivity(intent);
                showToast(a, "Please enable Write Setting permission!!!");
                a.finish();
                System.exit(0);
            }
        }
    }

    public static boolean settingOverlayPermission(Activity a) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && SettingManager.getModeUser(a) && !isTablet(a)) {
            if (!Settings.canDrawOverlays(a)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + a.getPackageName()));
                a.startActivity(intent);
                showToast(a, "Please enable Overlay Setting permission!!!");
                return false;
            }
        }

        return true;
    }

    public static boolean isTablet(Context context) {
        /* boolean rs = (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE; */
        return false;
    }

    public static File saveLogFile() {
        try {
            File folder = new File(Define.APP_PATH_LOGS);
            if (!folder.exists())
                if (!folder.mkdirs())
                    return null;

            String ip = getIPAddress(true);
            File logFile = new File(Define.APP_PATH_LOGS + "log_" + ip +"--" + Utils.long2DateTimeFile(System.currentTimeMillis()) + ".txt");
            logFile.createNewFile();
            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(logFile));
            osw.write(Debug.mTxtLog.toString());
            osw.flush();
            osw.close();
            Debug.resetLog();

            return logFile;
        } catch (IOException e) {
            Debug.logW(e.getMessage());
            return null;
        }
    }

    public static void extractLogToFile(Context c) throws IOException {
        // method as shown above
        PackageManager manager = c.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(c.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e2) {
            Debug.logE(e2);
        }

        String model = Build.MODEL;
        if (!model.startsWith(Build.MANUFACTURER))
            model = Build.MANUFACTURER + " " + model;

        // Make file name - file must be saved to external storage or it wont be readable by
        // the email app.
        // String path = Define.APP_PATH;
        String fullName = Define.APP_PATH_LOGS + "log_full_" + long2DateTimeFile(System.currentTimeMillis()) + ".txt";

        // Extract to file.
        File file = new File(fullName);
        if (!file.createNewFile()) {
            return;
        }

        InputStreamReader reader = null;
        FileWriter writer = null;
        try {
            // For Android 4.0 and earlier, you will get all app's log output, so filter it to
            // mostly limit it to your app's output.  In later versions, the filtering isn't needed.
            String cmd = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) ?
                    "logcat -d -v time MyApp:v dalvikvm:v System.err:v *:s" :
                    "logcat -d -v time";

            // get input stream
            Process process = Runtime.getRuntime().exec(cmd);
            reader = new InputStreamReader(process.getInputStream());

            // write output stream
            writer = new FileWriter(file);
            writer.write("Android version: " + Build.VERSION.SDK_INT + "\n");
            writer.write("Device: " + model + "\n");
            writer.write("App version: " + (info == null ? "(null)" : info.versionCode) + "\n");

            char[] buffer = new char[10000];
            do {
                int n = reader.read(buffer, 0, buffer.length);
                if (n == -1)
                    break;
                writer.write(buffer, 0, n);
            } while (true);

            reader.close();
            writer.close();
        } catch (IOException e) {
            if (writer != null)
                try {
                    writer.close();
                } catch (IOException e1) {
                    Debug.logE(e1);
                }
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e1) {
                    Debug.logE(e1);
                }

            // You might want to write a failure message to the log here.
            showToast(c, "get log fail!!! " + e.getLocalizedMessage());
        }
    }

    public static void sendConnectStt(Context c, boolean conn) {
        Intent i = new Intent(Define.CHECK_SERVER_CONN);
        i.putExtra(Define.SERVER_ISCONNECTED, conn);
        c.sendBroadcast(i);
    }

    public static String getVersionName(Context c) {
        PackageManager pm = c.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(c.getPackageName(), 0);
            String ver = pi.versionName;
            return ver;
        } catch (PackageManager.NameNotFoundException e) {
            Debug.logW(e);
            return null;
        }
    }

    public static long getAvailableStorage() {
        StatFs s = new StatFs(Environment.getRootDirectory().getAbsolutePath());
        long availableStorage = s.getBlockSize() * s.getAvailableBlocks();
        long megaAvailable = availableStorage / (1024 * 1024);
        return megaAvailable;
    }

    public static String floatForm(double d) {
        return new DecimalFormat("#.##").format(d);
    }

    public static String bytesToHuman(long size) {
        long Kb = 1 * 1024;
        long Mb = Kb * 1024;
        long Gb = Mb * 1024;
        long Tb = Gb * 1024;
        long Pb = Tb * 1024;
        long Eb = Pb * 1024;

        if (size < Kb) return floatForm(size) + " byte";
        if (size >= Kb && size < Mb) return floatForm((double) size / Kb) + " Kb";
        if (size >= Mb && size < Gb) return floatForm((double) size / Mb) + " Mb";
        if (size >= Gb && size < Tb) return floatForm((double) size / Gb) + " Gb";
        if (size >= Tb && size < Pb) return floatForm((double) size / Tb) + " Tb";
        if (size >= Pb && size < Eb) return floatForm((double) size / Pb) + " Pb";
        if (size >= Eb) return floatForm((double) size / Eb) + " Eb";

        return "???";
    }

    public static double bytesToHumanDouble(long size) {
        long Kb = 1 * 1024;
        long Mb = Kb * 1024;
        long Gb = Mb * 1024;

        double rs = (double) (size / Gb);
        return rs;
    }

    public static long sd_card_free() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        int availBlocks = stat.getAvailableBlocks();
        int blockSize = stat.getBlockSize();
        long free_memory = (long) availBlocks * (long) blockSize;

        return free_memory;
    }

    public static void restartDevice() {
        try {
            Debug.logW("Restart device now");
            Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", "reboot"});
            proc.waitFor();
        } catch (Exception ex) {
            Debug.logW(ex.getMessage());
        }
    }

    public static void shutdownDevice() {
        try {
            Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", "reboot -p"});
            proc.waitFor();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String getFileExt(File f){
        String fileName = f.getName();
        String rs = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
        return rs;
    }

    public static final long TIME_TO_RESTART = 200;
    public static void restartApp(Context c){
        Intent i = c.getPackageManager().getLaunchIntentForPackage(c.getPackageName());
        PendingIntent pi = PendingIntent.getActivity(c, 123456, i, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            am.setExact(AlarmManager.RTC, System.currentTimeMillis() + TIME_TO_RESTART, pi);
        else
            am.set(AlarmManager.RTC, System.currentTimeMillis() + TIME_TO_RESTART, pi);
        // Runtime.getRuntime().exit(0);
        System.exit(0);
    }

    public static String getNameStt(int mConnStatus){
        switch (mConnStatus){

            case Define.CONNECTED_STATUS:
                return "Connected to ";

            case Define.CONNECTING_STATUS:
                return "Connecting to ";

            case Define.DISCONNECTED_STATUS:
                return "Disconnected to ";

            default:
                return "Unknown...";
        }
    }

    public static String getNameAppStatus(int appStatus){
        switch (appStatus){

            case Define.PLAYING_STATUS:
                return "Playing media";

            case Define.IDLE_STATUS:
                return "Idle";

            case Define.DOWNLOADING_STATUS:
                return "Downloading...";

            case Define.REQUESTING_CONNECT_STATUS:
                return "Connecting...";

            default:
                return "Unknown";
        }
    }

    public static void showToastFromBg(final Context c, final String msg){
//        if (c instanceof BaseActivity){
//            ((BaseActivity)c).runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(c, msg, Toast.LENGTH_SHORT).show();
//                }
//            });
//        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(c, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static boolean isVideo(Context c, File test){
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(c, Uri.fromFile(test));
        String video = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO);
        Debug.log(video);
        if ("yes".equalsIgnoreCase(video))
            return true;

        return false;
    }

//    public static  String getLocalIpAddress() {
//        try {
//            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
//                NetworkInterface intf = en.nextElement();
//                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
//                    InetAddress inetAddress = enumIpAddr.nextElement();
//                    if (!inetAddress.isLoopbackAddress()) {
//                        String ip = Formatter.formatIpAddress(inetAddress.hashCode());
//                        return ip;
//                    }
//                }
//            }
//        } catch (SocketException ex) {
//        }
//        return null;
//    }

    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }
}