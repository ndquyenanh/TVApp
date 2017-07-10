package com.sec.samsung.schedule;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import com.sec.samsung.tvcontentsync.BaseActivity;
import com.sec.samsung.tvcontentsync.MediaActivity;
import com.sec.samsung.tvcontentsync.R;
import com.sec.samsung.utils.Debug;
import com.sec.samsung.utils.Define;
import com.sec.samsung.utils.SettingManager;
import com.sec.samsung.utils.Utils;

import java.util.ArrayList;

/**
 * Created by sev_user on 11/28/2016.
 */

public class ScheduleManager {
    private Context mCtx;
    private AlarmManager mAlarmMgr;
    private ScheduleDBManager dbManager;

    public ScheduleManager(Context context) {
        this.mCtx = context;
        mAlarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        dbManager = new ScheduleDBManager(context);
    }

    /*tao lich cho tat ca */
    public void scheduleAll(long serverTime, long deviceTime) {
        Debug.log("serverTime = " + Utils.long2DateTime(serverTime));

        if (dbManager != null) {
            ArrayList<ScheduleItem> list = dbManager.getAllSchedules();
            Debug.log("Total " + list.size() + " schedules in database");
            for (ScheduleItem item : list){
                cancelSchedule(item.getScheduleId(), false);//ko xoa du lieu trong database
                makeSchedule(item, serverTime, deviceTime);
            }
        }else {
            Debug.logW("dbManager = null!!");
        }
    }

    public void deleteAllCalendar(){
        Debug.log("Delete all Calendar!!!!");

        if (dbManager != null) {
            ArrayList<ScheduleItem> items = dbManager.getAllSchedules();
            for (ScheduleItem item:items) {
                cancelSchedule(item.getScheduleId(), true);
            }
        }
    }

    public void cancelSchedule(int scheduleId, boolean deleteDatabase) {
        // Debug.log("Cancel schedule scheduleId = " + scheduleId + ", deleteDatabase = " + deleteDatabase);
        if (dbManager != null) {
            ScheduleItem item = dbManager.getSchedule(scheduleId);

            if (item != null){
                String startDate = item.getStartDate();
                String endDate = item.getEndDate();
                String startTime = item.getStartTime();
                // String endTime = item.getEndTime();

                long sTime = Utils.dateToLong(Utils.combileDateAndTime(startDate, startTime));
                long eTime = Utils.dateToLong(Utils.combileDateAndTime(endDate, startTime));

                int cnt = 0;
                for (long alarmTime = sTime; alarmTime <= eTime; alarmTime += Define.ONE_DAY) {
                    cnt++;
                    Intent intent = new Intent(mCtx, SchedulerService.class);
                    PendingIntent alarmIntent = PendingIntent.getService(mCtx,
                            Utils.createRequestId(item.getScheduleId(), cnt), intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    mAlarmMgr.cancel(alarmIntent);

                    if (deleteDatabase){
                        String log = "Cancel alarm at " + Utils.longToDate(alarmTime) + ". Request code " + Utils.createRequestId(item.getScheduleId(), cnt);
                        Debug.log(log);
                    }
                }
            }

            //xoa lich khoi database
            if (deleteDatabase)
                dbManager.deleteSchedule(scheduleId);
        }
    }

    private void makeSchedule(ScheduleItem item, long serverTime, long deviceTime) {
        Debug.log("item Id = " + item.getScheduleId() + ", serverTime = " + Utils.long2DateTime(serverTime) + ", deviceTime = " + Utils.longToDate(deviceTime));

        String startDate = item.getStartDate();
        String endDate = item.getEndDate();
        String startTime = item.getStartTime();
        String endTime = item.getEndTime();

        long timeOut = Utils.dateToLong(Utils.combileDateAndTime(startDate, endTime))
                - Utils.dateToLong(Utils.combileDateAndTime(startDate, startTime));

        long sTime = Utils.dateToLong(Utils.combileDateAndTime(startDate, startTime));
        long eTime = Utils.dateToLong(Utils.combileDateAndTime(endDate, startTime));
        long timeOffset = serverTime - deviceTime;

        Utils.TIME_OFF_SET = timeOffset;

        int cnt = 0;
        for (long alarmTime = sTime; alarmTime <= eTime; alarmTime += Define.ONE_DAY) {
            if (alarmTime > serverTime) {/*ko dat lich cho thoi gian da troi qua*/
                cnt++;
                // Intent intent = new Intent(mCtx, ScheduleBroadcastReceiver.class);
                Intent intent = new Intent(mCtx, SchedulerService.class);

                intent.putExtra(Define.JSON_SCHEDULED_ID, item.getScheduleId());

                intent.putExtra(Define.JSON_SCHEDULED_FILE_EXTENSION, item.getFileExtension());
                intent.putExtra(Define.JSON_SCHEDULED_FILE_ID, item.getFileId());
                intent.putExtra(Define.JSON_SCHEDULED_FILE_NAME, item.getFileName());
                intent.putExtra(Define.JSON_SCHEDULED_FOLDER_NAME, item.getFolderName());

                alarmTime -= Define.TIME_START_BEFORE;
                intent.putExtra(Define.TIME_OUT, timeOut);
                intent.putExtra(Define.ALARM_TIME, alarmTime);
                intent.putExtra(Define.TIME_OFFSET, timeOffset);
                intent.putExtra(Define.REQUEST_CODE, Utils.createRequestId(item.getScheduleId(), cnt));
                intent.putExtra(Define.BROADCAST_TYPE, item.getPriority());
                Debug.log("server time = " + Utils.long2DateTime(serverTime) + ", device time = " + Utils.long2DateTime(deviceTime)
                 + ", server time start = " + Utils.long2DateTime(alarmTime) + ", timeOffset = " + timeOffset);

                // PendingIntent alarmIntent = PendingIntent.getBroadcast(mCtx,
                //        Utils.createRequestId(item.getScheduleId(), cnt), intent, PendingIntent.FLAG_UPDATE_CURRENT);
                PendingIntent alarmIntent = PendingIntent.getService(mCtx,
                        Utils.createRequestId(item.getScheduleId(), cnt), intent, PendingIntent.FLAG_UPDATE_CURRENT);
                long time2Schedule = alarmTime - timeOffset;
                Debug.logI("time2Schedule of device = " + Utils.long2DateTime(time2Schedule));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    mAlarmMgr.setExact(AlarmManager.RTC_WAKEUP, time2Schedule, alarmIntent);
                } else {
                    mAlarmMgr.set(AlarmManager.RTC_WAKEUP, time2Schedule, alarmIntent);
                }

                final String log = "Set alarm at " + Utils.longToDate(alarmTime + Define.TIME_START_BEFORE)
                        + ". Request code " + Utils.createRequestId(item.getScheduleId(), cnt)
                        + ". Broadcast type = " + item.getPriority()
                        + ", File name = " + item.getFileName();
                Debug.log(log);
//                if (mCtx instanceof BaseActivity){
//                    ((BaseActivity)mCtx).runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Utils.showToast(mCtx, log);
//                        }
//                    });
//                }
               // if (iShowToast != null) {
                    //iShowToast.showToast(log);
               // }
            }else if(alarmTime < serverTime && (alarmTime + timeOut) > serverTime){
                cnt++;

                Intent intent = new Intent(mCtx, SchedulerService.class);
                // Intent intent = new Intent(mCtx, MediaActivity.class);
                intent.putExtra(Define.JSON_SCHEDULED_ID, item.getScheduleId());
                intent.putExtra(Define.JSON_SCHEDULED_FILE_EXTENSION, item.getFileExtension());
                intent.putExtra(Define.JSON_SCHEDULED_FILE_ID, item.getFileId());
                intent.putExtra(Define.JSON_SCHEDULED_FILE_NAME, item.getFileName());
                intent.putExtra(Define.JSON_SCHEDULED_FOLDER_NAME, item.getFolderName());
                intent.putExtra(Define.IS_OVER_TIME, true);

                long newTimeOut = alarmTime + timeOut - serverTime;
                if (newTimeOut > Define.TIME_FOR_START){
                    newTimeOut -= Define.TIME_FOR_START;
                }else {
                    //Utils.showToast(mCtx, "Time leave is too little!!!");
                    Debug.log("Time play ended, so return");
                    return;
                }

                intent.putExtra(Define.TIME_OUT, newTimeOut);
                intent.putExtra(Define.ALARM_TIME, deviceTime);
                intent.putExtra(Define.REQUEST_CODE, Utils.createRequestId(item.getScheduleId(), cnt));
                intent.putExtra(Define.BROADCAST_TYPE, item.getPriority());

                // PendingIntent alarmIntent = PendingIntent.getBroadcast(mCtx,
                //         Utils.createRequestId(item.getScheduleId(), cnt), intent, PendingIntent.FLAG_UPDATE_CURRENT);
                PendingIntent alarmIntent = PendingIntent.getService(mCtx,
                        Utils.createRequestId(item.getScheduleId(), cnt), intent, PendingIntent.FLAG_UPDATE_CURRENT);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    mAlarmMgr.setExact(AlarmManager.RTC_WAKEUP, alarmTime - timeOffset, alarmIntent);
                } else {
                    mAlarmMgr.set(AlarmManager.RTC_WAKEUP, alarmTime - timeOffset, alarmIntent);
                }
                //mCtx.startActivity(intent);

                String log = "Time is over, Set alarm at " + Utils.longToDate(alarmTime)
                        + ". Request code " + Utils.createRequestId(item.getScheduleId(), cnt)
                        + ". Broadcast type = " + item.getPriority()
                        + ", File name = " + item.getFileName();
                Debug.log(log);
//                if (mCtx instanceof BaseActivity){
//                    ((BaseActivity)mCtx).runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Utils.showToast(mCtx, log);
//                        }
//                    });
//                }
               //if (iShowToast != null) {
                    //iShowToast.showToast(log);
               // }
            }else {
                Debug.logW("alarmTime not valid server time = " + Utils.long2DateTime(serverTime)
                        + ", end time = " + Utils.long2DateTime(eTime));
            }
        }
    }

    public ScheduleItem getScheduleItem(int sID){
        return dbManager.getSchedule(sID);
    }

    public ScheduleDBManager getDbManager(){
        return dbManager;
    }

    //private  IShowToast iShowToast;
    //public void setOnShowToastListener(IShowToast listener){
    //    iShowToast = listener;
    //}

    //public interface IShowToast{
        /**
         * show toast
         * @param s msg to show
         */
    //    void showToast(String s);
    //}
}
