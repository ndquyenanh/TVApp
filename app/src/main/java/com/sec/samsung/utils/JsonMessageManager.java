package com.sec.samsung.utils;

import android.content.Context;

import com.sec.samsung.device.DeviceInfoDefine;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonMessageManager {

    public static JSONObject createRequestMessage(Context context){
        JSONObject json = new JSONObject();
        try {
            json.put(Define.JSON_TYPE, Define.JSON_CONNECT);
            json.put(Define.JSON_NAME, Utils.getDeviceName(context));
            json.put(Define.JSON_SERIAL_NUMBER, Utils.getSerialNumber());
            json.put(Define.JSON_MAC_ADDRESS, DeviceInfoDefine.getMacAddr(context));
            json.put(Define.JSON_DEVICE_TYPE, "android");
        } catch (JSONException e) {
            Debug.logW(e.getLocalizedMessage());
        }

        return json;
    }

    public static JSONObject createScheduledResponse(Context c, int file_id, int schedule_id, boolean success){
        JSONObject object = new JSONObject();

        try {
            object.put(Define.JSON_TYPE, Define.JSON_SCHEDULED);
            object.put(Define.JSON_SCHEDULED_FILE_ID, file_id);
            object.put(Define.JSON_SCHEDULED_ID, schedule_id);
            object.put(Define.JSON_SERIAL_NUMBER, DeviceInfoDefine.getSerialNumber());
            object.put(Define.JSON_MAC_ADDRESS, DeviceInfoDefine.getMacAddr(c));
            object.put(Define.JSON_SCHEDULED_SUCCESS, success);
        } catch (JSONException e) {
            Debug.logW(e.getLocalizedMessage());
        }

        return object;
    }

    public static JSONObject createDownloadResponse(Context c, int file_id, boolean success, String folderName) {
        JSONObject object = new JSONObject();
        try {
            object.put(Define.JSON_TYPE, Define.JSON_DOWNLOAD);
            object.put(Define.JSON_DOWNLOAD_FILEID, file_id);
            object.put(Define.JSON_SERIAL_NUMBER, DeviceInfoDefine.getSerialNumber());
            object.put(Define.JSON_MAC_ADDRESS, DeviceInfoDefine.getMacAddr(c));
            object.put(Define.JSON_DOWNLOAD_SUCCESS, success);
            object.put(Define.JSON_DOWNLOAD_FOLDERNAME, folderName);
        } catch (JSONException e) {
            Debug.logW(e.getLocalizedMessage());
        }

        return object;
    }

    public static JSONObject createCancelDownloadResponse(Context c, int file_id) {
        JSONObject object = new JSONObject();
        try {
            object.put(Define.JSON_TYPE, Define.JSON_DOWNLOAD_CANCEL);
            object.put(Define.JSON_DOWNLOAD_FILEID, file_id);
            object.put(Define.JSON_SERIAL_NUMBER, DeviceInfoDefine.getSerialNumber());
            object.put(Define.JSON_MAC_ADDRESS, DeviceInfoDefine.getMacAddr(c));
        } catch (JSONException e) {
            Debug.logW(e.getLocalizedMessage());
        }

        return object;
    }

    public static JSONObject createReportDownloadResponse(Context c, int file_id, long size_downloaded) {
        JSONObject object = new JSONObject();
        try {
            object.put(Define.JSON_TYPE, Define.JSON_DOWNLOAD_REPORT);
            object.put(Define.JSON_DOWNLOAD_FILEID, file_id);
            object.put(Define.JSON_DOWNLOAD_FILESIZE_DOWNLOADED, size_downloaded);
            object.put(Define.JSON_SERIAL_NUMBER, DeviceInfoDefine.getSerialNumber());
            object.put(Define.JSON_MAC_ADDRESS, DeviceInfoDefine.getMacAddr(c));
        } catch (JSONException e) {
            Debug.logW(e.getLocalizedMessage());
        }

        return object;
    }

    public static JSONObject createDeletedFileResponse(Context c, int file_id) {
        JSONObject object = new JSONObject();
        try {
            object.put(Define.JSON_TYPE, Define.JSON_DELETED_FOLDER);
            object.put(Define.JSON_DOWNLOAD_FILEID, file_id);
            object.put(Define.JSON_SERIAL_NUMBER, DeviceInfoDefine.getSerialNumber());
            object.put(Define.JSON_MAC_ADDRESS, DeviceInfoDefine.getMacAddr(c));
        } catch (JSONException e) {
            Debug.logW(e.getLocalizedMessage());
        }

        return object;
    }

    public static JSONObject createUpdateResponse(Context c, int file_id, boolean success) {
        JSONObject object = new JSONObject();
        try {
            object.put(Define.JSON_TYPE, Define.JSON_UPDATE);
            object.put(Define.JSON_UPDATE_SOFTWARE_ID, file_id);
            object.put(Define.JSON_SERIAL_NUMBER, DeviceInfoDefine.getSerialNumber());
            object.put(Define.JSON_MAC_ADDRESS, DeviceInfoDefine.getMacAddr(c));
            object.put(Define.JSON_UPDATE_SUCCESS, success);
        } catch (JSONException e) {
            Debug.logW(e.getLocalizedMessage());
        }

        return object;
    }

    public static JSONObject createReportUpdateResponse(Context c, int file_id, long size_downloaded) {
        JSONObject object = new JSONObject();
        try {
            object.put(Define.JSON_TYPE, Define.JSON_UPDATE_REPORT);
            object.put(Define.JSON_UPDATE_SOFTWARE_ID, file_id);
            object.put(Define.JSON_UPDATE_FILESIZE_DOWNLOADED, size_downloaded);
            object.put(Define.JSON_SERIAL_NUMBER, DeviceInfoDefine.getSerialNumber());
            object.put(Define.JSON_MAC_ADDRESS, DeviceInfoDefine.getMacAddr(c));
        } catch (JSONException e) {
            Debug.logW(e.getLocalizedMessage());
        }

        return object;
    }

    public static JSONObject createOutOfSpace(Context c) {
        JSONObject object = new JSONObject();
        try {
            object.put(Define.JSON_TYPE, Define.JSON_OUT_OF_SPACE);
            object.put(Define.JSON_SERIAL_NUMBER, DeviceInfoDefine.getSerialNumber());
            object.put(Define.JSON_MAC_ADDRESS, DeviceInfoDefine.getMacAddr(c));
        } catch (JSONException e) {
            Debug.logW(e.getLocalizedMessage());
        }

        return object;
    }

    public static JSONObject createCheckConnect(Context c) {
        JSONObject object = new JSONObject();
        try {
            object.put(Define.JSON_TYPE, Define.JSON_CHECK_CONNECT);
            object.put(Define.JSON_VALUE, "yes");
            object.put(Define.JSON_SERIAL_NUMBER, DeviceInfoDefine.getSerialNumber());
            object.put(Define.JSON_MAC_ADDRESS, DeviceInfoDefine.getMacAddr(c));
        } catch (JSONException e) {
            Debug.logW(e.getLocalizedMessage());
        }

        return object;
    }

    public static JSONObject createJsonIncluded(Context c, String data){
        JSONObject object = new JSONObject();
        try {
            object.put(Define.JSON_TYPE, "lenh_gop");
            object.put(Define.JSON_VALUE, data);
            object.put(Define.JSON_SERIAL_NUMBER, DeviceInfoDefine.getSerialNumber());
            object.put(Define.JSON_MAC_ADDRESS, DeviceInfoDefine.getMacAddr(c));
        } catch (JSONException e) {
            Debug.logW(e.getLocalizedMessage());
        }

        return object;
    }
}
