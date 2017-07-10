package com.sec.samsung.schedule;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sec.samsung.utils.Debug;

import java.util.ArrayList;


/**
 * Created by sev_user on 11/28/2016.
 */

public class ScheduleDBManager {
    public static final String DATABASE_NAME = "schedule_database";
    public static final String TABLE_NAME = "schedule_table";
    public static final int DATABASE_VERSION = 1;

    public static final String ID = "_id";

    public static final String SCHEDULE_ID = "schedule_id";

    public static final String FILE_EXTENSION = "file_extension";
    public static final String FILE_ID = "file_id";
    public static final String FILE_NAME = "file_name";
    public static final String FOLDER_NAME = "folder_name";

    public static final String START_DATE = "start_date";
    public static final String END_DATE = "end_date";
    public static final String START_TIME = "start_time";
    public static final String END_TIME = "end_time";

    public static final String PRIORITY = "priority";

    public class ScheduleSQLiteOpenHelper extends SQLiteOpenHelper {

        public ScheduleSQLiteOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String createTableCmd = "create table " + TABLE_NAME + "("
                    + ID + " integer primary key autoincrement, "
                    + SCHEDULE_ID + " integer not null, "
                    + FILE_EXTENSION + " varchar, "
                    + FILE_ID + " integer, "
                    + FILE_NAME + " varchar, "
                    + FOLDER_NAME + " varchar, "
                    + START_DATE + " varchar not null, "
                    + END_DATE + " varchar not null, "
                    + START_TIME + " varchar not null, "
                    + END_TIME + " varchar not null, "
                    + PRIORITY + " integer)";

            db.execSQL(createTableCmd);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("drop table if exists " + TABLE_NAME);
            onCreate(db);
        }
    }

    private ScheduleSQLiteOpenHelper mHelper;

    public ScheduleDBManager(Context context) {
        mHelper = new ScheduleSQLiteOpenHelper(context);
    }

    public void addSchedule(ScheduleItem item) {
        Debug.log("Add item");

        if (mHelper != null) {
            SQLiteDatabase db = mHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(SCHEDULE_ID, item.getScheduleId());

            cv.put(FILE_EXTENSION, item.getFileExtension());
            cv.put(FILE_ID, item.getFileId());
            cv.put(FILE_NAME, item.getFileName());
            cv.put(FOLDER_NAME, item.getFolderName());

            cv.put(START_DATE, item.getStartDate());
            cv.put(END_DATE, item.getEndDate());
            cv.put(START_TIME, item.getStartTime());
            cv.put(END_TIME, item.getEndTime());

            cv.put(PRIORITY, item.getPriority());

            db.insert(TABLE_NAME, null, cv);
            db.close();

            String log = "Add new alarm to database: "
                    + "schedule id = " + item.getScheduleId() + ", "
                    + "file name = " + item.getFileName() + ", "
                    + "start time = " + item.getStartDate() + " " + item.getStartTime();
            Debug.log(log);
        }
    }

    public void updateSchedule(ScheduleItem item){
        if (mHelper != null){
            SQLiteDatabase db = mHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(SCHEDULE_ID, item.getScheduleId());

            cv.put(FILE_EXTENSION, item.getFileExtension());
            cv.put(FILE_ID, item.getFileId());
            cv.put(FILE_NAME, item.getFileName());
            cv.put(FOLDER_NAME, item.getFolderName());

            cv.put(START_DATE, item.getStartDate());
            cv.put(END_DATE, item.getEndDate());
            cv.put(START_TIME, item.getStartTime());
            cv.put(END_TIME, item.getEndTime());

            cv.put(PRIORITY, item.getPriority());

            String sql = SCHEDULE_ID + "=" + item.getScheduleId();
            db.update(TABLE_NAME, cv, sql, null);
            db.close();

            String log = "Update alarm to database: "
                    + "schedule id = " + item.getScheduleId() + ", "
                    + "file name = " + item.getFileName() + ", "
                    + "start time = " + item.getStartDate() + " " + item.getStartTime();
            Debug.log(log);
        }
    }

    public void deleteSchedule(int sId){
        if (mHelper != null){
            String sql = "delete from " + TABLE_NAME
                    + " where " + SCHEDULE_ID + "=" + sId + ";";
            SQLiteDatabase db = mHelper.getWritableDatabase();
            db.execSQL(sql);
            db.close();

            String log = "Delete alarm from database: "
                    + "schedule id = " + sId;
            Debug.log(log);
        }
    }

//    public String getFolderName(int s_id){
//        if (mHelper != null) {
//            String sql = "select " + FOLDER_NAME + " from " + TABLE_NAME
//                    + " where " + SCHEDULE_ID + "=" + s_id + ";";
//            SQLiteDatabase db = mHelper.getWritableDatabase();
//            Cursor cursor = db.rawQuery(sql, null);
//
//            String log = "Delete alarm from database: "
//                    + "schedule id = " + sId;
//            Debug.log(log);
//        }
//    }

    public ScheduleItem getSchedule(int sId){
        Debug.log("sId = " + sId);

        ScheduleItem item = null;
        if (mHelper != null){
            SQLiteDatabase db = mHelper.getWritableDatabase();
            String query = "select * from " + TABLE_NAME
                    + " where " + SCHEDULE_ID + "=" + sId + ";";
            Cursor cursor = db.rawQuery(query, null);

            int scheduleIdIdx = cursor.getColumnIndex(SCHEDULE_ID);

            int fileExtensionIdx = cursor.getColumnIndex(FILE_EXTENSION);
            int fileIdIdx = cursor.getColumnIndex(FILE_ID);
            int fileNameIdx = cursor.getColumnIndex(FILE_NAME);
            int folderNameIdx = cursor.getColumnIndex(FOLDER_NAME);

            int startDateIdx = cursor.getColumnIndex(START_DATE);
            int endDateIdx = cursor.getColumnIndex(END_DATE);
            int startTimeIdx = cursor.getColumnIndex(START_TIME);
            int endTimeIdx = cursor.getColumnIndex(END_TIME);

            int priorityIdx = cursor.getColumnIndex(PRIORITY);

            if (cursor.moveToNext()) {
                item = new ScheduleItem(cursor.getInt(scheduleIdIdx),
                        cursor.getString(fileExtensionIdx), cursor.getInt(fileIdIdx),
                        cursor.getString(fileNameIdx), cursor.getString(folderNameIdx),
                        cursor.getString(startDateIdx), cursor.getString(endDateIdx),
                        cursor.getString(startTimeIdx), cursor.getString(endTimeIdx),
                        cursor.getInt(priorityIdx));
            }

            cursor.close();
            db.close();

            String log = "Get alarm from database: schedule id = " + sId;
            Debug.log(log);
        }

        return item;
    }

    public ArrayList<ScheduleItem> getAllSchedules() {
        Debug.log("getAllSchedules");
        ArrayList<ScheduleItem> list = new ArrayList<>();

        if (mHelper != null) {
            SQLiteDatabase db = mHelper.getWritableDatabase();
            String query = "select * from " + TABLE_NAME + ";";
            Cursor cursor = db.rawQuery(query, null);

            int scheduleIdIdx = cursor.getColumnIndex(SCHEDULE_ID);

            int fileExtensionIdx = cursor.getColumnIndex(FILE_EXTENSION);
            int fileIdIdx = cursor.getColumnIndex(FILE_ID);
            int fileNameIdx = cursor.getColumnIndex(FILE_NAME);
            int folderNameIdx = cursor.getColumnIndex(FOLDER_NAME);

            int startDateIdx = cursor.getColumnIndex(START_DATE);
            int endDateIdx = cursor.getColumnIndex(END_DATE);
            int startTimeIdx = cursor.getColumnIndex(START_TIME);
            int endTimeIdx = cursor.getColumnIndex(END_TIME);

            int priorityIdx = cursor.getColumnIndex(PRIORITY);

            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    ScheduleItem item = new ScheduleItem(cursor.getInt(scheduleIdIdx),
                            cursor.getString(fileExtensionIdx), cursor.getInt(fileIdIdx),
                            cursor.getString(fileNameIdx), cursor.getString(folderNameIdx),
                            cursor.getString(startDateIdx), cursor.getString(endDateIdx),
                            cursor.getString(startTimeIdx), cursor.getString(endTimeIdx),
                            cursor.getInt(priorityIdx));
                    list.add(item);
                }
            }
            cursor.close();
            db.close();

            String log = "|-|-|-|-|-|-|- Get all alarm from database -|-|-|-|-|-|-|-|-|";
            Debug.log(log);
        }
        return list;
    }

    public void deleteAll() {
        if (mHelper != null) {
            String sql = "delete from " + TABLE_NAME;
            SQLiteDatabase db = mHelper.getWritableDatabase();
            db.execSQL(sql);
            db.close();
        }
    }


}
