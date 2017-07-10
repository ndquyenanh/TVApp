package com.sec.samsung.schedule;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.sec.samsung.tvcontentsync.BaseActivity;
import com.sec.samsung.tvcontentsync.MediaActivity;
import com.sec.samsung.tvcontentsync.SlideShowActivity;
import com.sec.samsung.tvcontentsync.TvContentSyncApplication;
import com.sec.samsung.utils.Debug;
import com.sec.samsung.utils.Define;
import com.sec.samsung.utils.Utils;

/**
 * Created by Qnv96 on 24-Apr-17.
 */

public class SchedulerService extends Service {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public SchedulerService() {
        Debug.log();
        Utils.appStatus = Define.PLAYING_STATUS;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Debug.log();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        long currentTime = System.currentTimeMillis();
        int isBroadcast;

        scheduleId = intent.getIntExtra(Define.JSON_SCHEDULED_ID, -1);
        file_id = intent.getIntExtra(Define.JSON_DOWNLOAD_FILEID, -1);
        isOverTime = intent.getBooleanExtra(Define.IS_OVER_TIME, false);

        fileName = intent.getStringExtra(Define.JSON_SCHEDULED_FILE_NAME);
        int requestCode = intent.getIntExtra(Define.REQUEST_CODE, -1);
        Debug.log("device time = " + Utils.longToDate(currentTime) + ", requestCode = " + requestCode);

        timeOut = intent.getLongExtra(Define.TIME_OUT, -1);
        folderName = intent.getStringExtra(Define.JSON_SCHEDULED_FOLDER_NAME);
        isBroadcast = intent.getIntExtra(Define.BROADCAST_TYPE, 0);

        mApplication = TvContentSyncApplication.getInstance();

        startTime = intent.getLongExtra(Define.ALARM_TIME, 0);
        timeOffset = intent.getLongExtra(Define.TIME_OFFSET, 0);

        if (isBroadcast == 1) {//lich chen
            mApplication.setBroadcasting(true);
            mApplication.setBroadcastFinishTime(currentTime + timeOut);
            setupMediaFile();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //release lock
                    mApplication.setBroadcasting(false);
                    mApplication.setBroadcastFinishTime(0);
                }
            }, timeOut);
        } else {//lich binh thuong
            if (mApplication.isBroadcasting()) {
                if (currentTime + timeOut > mApplication.getBroadcastFinishTime()) {
                    //van con thoi gian, cho chay tiep
                    timeOut = (currentTime + timeOut) - mApplication.getBroadcastFinishTime();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setupMediaFile();
                        }
                    }, mApplication.getBroadcastFinishTime() - currentTime);
                }
                //da het thoi gian, ket thuc luon
            } else {
                setupMediaFile();
            }
        }

        return START_NOT_STICKY;
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

    private int scheduleId;
    private int file_id;
    private long timeOut;
    private long startTime;
    private long timeOffset;
    private boolean isOverTime;

    private String fileName;
    private String folderName;
    private TvContentSyncApplication mApplication;

    private void setPlayer(BaseActivity a, Class clz){
        if (a == null) {
            playMedia( clz);
            return;
        }

        if (a.getId() != scheduleId){
            playMedia(clz);
        }else {
            Debug.logI(clz.getSimpleName() + " player is running this media " + folderName);
        }
    }

    private void setupMediaFile() {
        BaseActivity a = (BaseActivity) mApplication.getCurActivity();

        if (folderName.contains("video") || folderName.contains("music") || folderName.contains("tonghop")) {
            setPlayer(a, MediaActivity.class);
        } else {
            setPlayer(a, SlideShowActivity.class);
        }

        Debug.log(scheduleId, file_id, folderName);
    }

    private void playMedia(Class cls) {
        Intent intent = new Intent(this, cls);

        intent.putExtra(Define.PLAY_FILE_NAME, folderName);
        intent.putExtra(Define.PLAY_TIME, timeOut);
        intent.putExtra(Define.JSON_SCHEDULED_ID, scheduleId);
        intent.putExtra(Define.JSON_SCHEDULED_FILE_ID, file_id);
        intent.putExtra(Define.ALARM_TIME, startTime);
        intent.putExtra(Define.TIME_OFFSET, timeOffset);
        intent.putExtra(Define.IS_OVER_TIME, isOverTime);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);

        mApplication.updateNotification(this, "Preparing playing " + folderName);
        //Utils.showToast(this, "Preparing playing " + folderName);
        Debug.log(fileName, timeOut);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Debug.logW("");
    }
}
