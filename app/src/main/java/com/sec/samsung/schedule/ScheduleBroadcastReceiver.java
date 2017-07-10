package com.sec.samsung.schedule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;

import com.sec.samsung.tvcontentsync.AudioActivity;
import com.sec.samsung.tvcontentsync.BaseActivity;
import com.sec.samsung.tvcontentsync.SlideShowActivity;
import com.sec.samsung.tvcontentsync.TvContentSyncApplication;
import com.sec.samsung.tvcontentsync.VideoActivity;
import com.sec.samsung.utils.Debug;
import com.sec.samsung.utils.Define;
import com.sec.samsung.utils.Utils;

import java.util.Arrays;

/**
 * Created by sev_user on 11/28/2016.
 */

public class ScheduleBroadcastReceiver extends BroadcastReceiver {

    private int scheduleId;
    private int file_id;
    private long timeOut;
    private long startTime;
    private long timeOffset;
    private int isBroadcast;

    private String fileName;
    private String folderName;
    private TvContentSyncApplication mApplication;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        long currentTime = System.currentTimeMillis();
        Debug.log("device time = " + Utils.longToDate(currentTime));

        scheduleId = intent.getIntExtra(Define.JSON_SCHEDULED_ID, -1);
        file_id = intent.getIntExtra(Define.JSON_DOWNLOAD_FILEID, -1);

        fileName = intent.getStringExtra(Define.JSON_SCHEDULED_FILE_NAME);
        final int requestCode = intent.getIntExtra(Define.REQUEST_CODE, -1);
        timeOut = intent.getLongExtra(Define.TIME_OUT, -1);
        folderName = intent.getStringExtra(Define.JSON_SCHEDULED_FOLDER_NAME);
        isBroadcast = intent.getIntExtra(Define.BROADCAST_TYPE, 0);

        mApplication = TvContentSyncApplication.getInstance();
        BaseActivity a = (BaseActivity) mApplication.getCurActivity();

        startTime = intent.getLongExtra(Define.ALARM_TIME, 0);
        timeOffset = intent.getLongExtra(Define.TIME_OFFSET, 0);

        if (isBroadcast == 1){//lich chen
            mApplication.setBroadcasting(true);
            mApplication.setBroadcastFinishTime(currentTime + timeOut);
            setupMediaFile(context, intent, requestCode);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //release lock
                    mApplication.setBroadcasting(false);
                    mApplication.setBroadcastFinishTime(0);
                }
            }, timeOut);
        }else{//lich binh thuong
            if (mApplication.isBroadcasting()){
                if (currentTime + timeOut > mApplication.getBroadcastFinishTime()){
                    //van con thoi gian, cho chay tiep
                    timeOut = (currentTime + timeOut) - mApplication.getBroadcastFinishTime();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setupMediaFile(context, intent, requestCode);
                        }
                    }, mApplication.getBroadcastFinishTime() - currentTime);
                }
                //da het thoi gian, ket thuc luon
            }else{
                setupMediaFile(context, intent, requestCode);
            }
        }
    }
    private void setupMediaFile(Context context, Intent intent, int requestCode){
        BaseActivity a = (BaseActivity) mApplication.getCurActivity();

        Debug.log(scheduleId, file_id, folderName, requestCode);
        if (!TextUtils.isEmpty(folderName)){
            if (a.getId() != scheduleId){
                playMedia(context, SlideShowActivity.class);
            }else {
                Debug.logW("Slideshow is running this folder " + folderName);
            }
        }else {
            String file_ex = intent.getStringExtra(Define.JSON_DOWNLOAD_FILEEXTENSION);
            if (Arrays.asList(Define.AUDIO_FILE_EX).contains(file_ex.toLowerCase())){
                if (a.getId() != scheduleId){
                    playMedia(context, AudioActivity.class);
                }else {
                    Debug.logW("Audio is running this audio " + fileName);
                }
            }else {
                if (a.getId() != scheduleId){
                    /*(!(a instanceof VideoActivity)) || ((a instanceof VideoActivity) && */
                    playMedia(context,VideoActivity.class);
                }else {
                    Debug.logW("Video is running this video " + fileName);
                }
            }
        }

    }

    private void playMedia(Context context, Class cls) {
        Debug.log(fileName, timeOut);
        Intent intent = new Intent(context, cls);
        if (TextUtils.isEmpty(folderName)){
            intent.putExtra(Define.PLAY_FILE_NAME, fileName);
        }else {
            intent.putExtra(Define.PLAY_FILE_NAME, folderName);
        }

        intent.putExtra(Define.PLAY_TIME, timeOut);
        intent.putExtra(Define.JSON_SCHEDULED_ID, scheduleId);
        intent.putExtra(Define.JSON_SCHEDULED_FILE_ID, file_id);
        intent.putExtra(Define.ALARM_TIME, startTime);
        intent.putExtra(Define.TIME_OFFSET, timeOffset);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivity(intent);
    }
}
