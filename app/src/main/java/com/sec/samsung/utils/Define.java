package com.sec.samsung.utils;

import android.os.Environment;

import java.io.File;

/**
 * Created by Qnv96 on 17-Nov-16.
 */

public class Define {
    //Save file on: /storage/extSdCard/TvContentSync
    public static final String DOWNLOAD_PATH                    = "/TvContentSync";

    // FOLDER APPLICATION
    public static final String APP_PATH                         = Environment.getExternalStorageDirectory() + File.separator + "TvContentSync/";
    public static final String APP_PATH_LOGS                    = APP_PATH + "LOGS/";

    public static final String JSON_TYPE                        = "type";
    public static final String JSON_CONNECT                     = "connect";
    public static final String JSON_NAME                        = "name";
    public static final String JSON_SERIAL_NUMBER               = "serial_number";
    public static final String JSON_MAC_ADDRESS                 = "mac_address";
    public static final String JSON_DEVICE_TYPE                 = "device_type";
    public static final String JSON_CONNECTED                   = "connected";
    public static final String JSON_DELETED_FOLDER              = "delete_folder";
    public static final String JSON_REMOVE_GROUP                = "delete_all_schedule";
    public static final String JSON_OUT_OF_SPACE                = "out_of_space";
    public static final String JSON_CHECK_CONNECT               = "ping";
    public static final String JSON_VALUE                       = "value";

    // JSON FOR DOWNLOAD
    public static final String JSON_DOWNLOAD                    = "download";
    public static final String JSON_DOWNLOAD_CANCEL             = "cancel_download";
    public static final String JSON_DOWNLOAD_REPORT             = "report_download";

    public static final String JSON_DOWNLOAD_URL                = "url";
    public static final String JSON_DOWNLOAD_FILEID             = "file_id";
    public static final String JSON_DOWNLOAD_FILESIZE           = "file_size";
    public static final String JSON_DOWNLOAD_FILESIZE_DOWNLOADED = "file_size_downloaded";
    public static final String JSON_DOWNLOAD_FILENAME           = "file_name";
    public static final String JSON_DOWNLOAD_FILEEXTENSION      = "file_extension";
    public static final String JSON_DOWNLOAD_FOLDERNAME         = "folder_name";
    public static final String JSON_DOWNLOAD_SUCCESS            = "success";

    // JSON FOR UPDATE APP
    public static final String JSON_UPDATE                      = "update";
    public static final String JSON_UPDATE_REPORT               = "report_update";

    public static final String JSON_UPDATE_URL                  = "url";
    public static final String JSON_UPDATE_FILESIZE             = "file_size";
    public static final String JSON_UPDATE_FILESIZE_DOWNLOADED  = "file_size_downloaded";
    public static final String JSON_UPDATE_FILEEXTENSION        = "file_extension";
    public static final String JSON_UPDATE_SOFTWARE_ID          = "software_id";
    public static final String JSON_UPDATE_FILENAME             = "file_name";
    public static final String JSON_UPDATE_SUCCESS              = "success";

    // JSON FOR SCHEDULED
    public static final String JSON_SCHEDULED                   = "schedule";
    public static final String JSON_SCHEDULED_ACTION            = "action";

    public static final String JSON_SCHEDULED_ID                = "schedule_id";
    public static final String JSON_SCHEDULED_FILE_ID           = "file_id";
    public static final String JSON_SCHEDULED_FILE_EXTENSION    = "file_extension";
    public static final String JSON_SCHEDULED_FILE_NAME         = "file_name";
    public static final String JSON_SCHEDULED_FOLDER_NAME       = "folder_name";
    public static final String JSON_SCHEDULED_START_DATE        = "start_date";
    public static final String JSON_SCHEDULED_END_DATE          = "end_date";
    public static final String JSON_SCHEDULED_START_TIME        = "start_time";
    public static final String JSON_SCHEDULED_END_TIME          = "end_time";
    public static final String JSON_SCHEDULED_BROADCAST         = "broadcast";
    public static final String JSON_SCHEDULED_DATE_TIME_NOW     = "datetime_now";


    // UPDATE APPLICATION VIA SERVICE
    public static final String SERVICE_INSTALLER_NAME           = "com.qnv96.apkinstaller.ServiceInstaller";
    public static final String SERVICE_INSTALLER_PKG            = "com.qnv96.apkinstaller";
    public static final String SERVICE_INSTALLER_APK            = "ApkInstaller.apk";
    public static final String APK_UPDATE_PATH                  = "apk_path";
    public static final String APK_UPDATE_PKG                   = "apk_pkg";
    public static final String APK_PREV_VER                     = "apk_prev_ver";
    public static final String APK_UPDATE_SWID                  = "software_id";

    //action
    public static final String ACTION_START_CONNECT_THREAD      = "com.sec.samsung.connect.START_THREAD";
    public static final String ACTION_STOP_CONNECT_THREAD       = "com.sec.samsung.connect.STOP_THREAD";
    public static final String ACTION_REFRESH_CONNECT_TIME      = "com.sec.samsung.connect.REFRESH_CONNECT_TIME";


    // share preference
    public static final String TV_PREFERENCE                    = "TV_Preference";
    public static final String IP_ADDRESS                       = "ip_address";
    // public static final String IP_ADDRESS                       = "ip_address";

    // setting
    public static final String USER_MODE                        = "user_mode";


    // JSON FOR SCHEDULED PLAYER RESULT
    public static final String JSON_SCHEDULED_SUCCESS           = "success";

    // PLAY MEDIA / PHOTO FILES
    public static final String PLAY_FILE_NAME                   = "play_file_name";
    public static final String PLAY_TIME                        = "play_time";
    public static final int PLAY_SLIDE_TIME_INTERVAL            = 8 * 1000;

    // play media
    public static final int TIME_GIAM_DI                        = 1500;
    public static final int TIME_START_BEFORE                   = 5 * 1000;
    public static final int TIME_START_PLAY_BEFORE              = 850;

    public static final String ALARM_TIME                       = "alarm_time";
    public static final String TIME_OFFSET                      = "time_offset";
    public static final String TIME_OUT                         = "time_out";
    public static final String REQUEST_CODE                     = "request_code";
    public static final String BROADCAST_TYPE                   = "broadcast_type";
    public static final long ONE_DAY                            = 24 * 60 *60 * 1000;

    public static final long CHECK_CONNECT_TIMER                = 1000 * 60 * 30;
    public static final long TIME_GTC_OFFSET                    = 7 * 3600 * 1000;

    public static final String TIME_OF_CLIENT_NOW               = "cur_cl_time";
    public static final String TIME_OF_SERVER_NOW               = "cur_sv_time";
    public static final String JSON_SERVER_DATETIME_NOW         = "datetimesv";

    public static final String CHECK_NETWORK_CONN               = "com.tv.sync.nwconn";
    public static final String CHECK_SERVER_CONN                = "com.action.tvsync.connectreceiver";
    public static final String SERVER_ISCONNECTED               = "isconnected";
    public static final String DATA_CONN                        = "data_conn";

    public static final String[] AUDIO_FILE_EX                  = {".mp3", ".m4a", ".3gp"};
    public static final int TIME_FOR_START                      = 4 * 1000;

    // Connection status
    public static final int CONNECTED_STATUS                    = 1;
    public static final int CONNECTING_STATUS                   = 2;
    public static final int DISCONNECTED_STATUS                 = 3;
    public static final int NETWORK_ERROR_STATUS                = 4;

    // Json setting
    public static final String JSON_SETTING_SOUND               = "enable_sound";
    public static final String JSON_SETTING_LOG                 = "get_log";
    public static final String JSON_SETTING_BG                  = "back_ground";
    public static final String JSON_RESTART_APP                 = "restart";
    public static final String BOOT_COMPLETE                    = "start_boot";
    public static final String BOOT_OK                          = "OK";

    // delete scheduler and folder
    public static final String JSON_DELETE_SCHEDULER            = "delete_schedule";
    public static final String JSON_IS_DELETE_FOLDER            = "is_delete_folder";

    // app status
    public static final int PLAYING_STATUS                      = 5;
    public static final int IDLE_STATUS                         = 7;

    // app service status
    public static final int DOWNLOADING_STATUS                  = 6;
    public static final int REQUESTING_CONNECT_STATUS           = 8;
    public static final int IDLE_SERVICE_STATUS                 = 9;

    public static final String SHOW_TOAST                       = "com.action.show.toast";
    public static final String ID_SHOW_TOAST                    = "data_id";
    public static final String MSG_SHOW_TOAST                   = "data_msg";
    public static final String IS_OVER_TIME                     = "over_time";

    //
    public static final String FTP_NAME                         = "ftp_name";
    public static final String FTP_PASSWORD                     = "ftp_password";
}
