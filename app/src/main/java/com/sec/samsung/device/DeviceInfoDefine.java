package com.sec.samsung.device;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.sec.samsung.utils.Debug;
import com.sec.samsung.utils.Utils;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;


public class DeviceInfoDefine {

    private final static String         PROP_KIES_SOM_MODE         = "dev.kies.sommode";
    private final static String         PROP_PRODUCT_MODEL         = "ro.product.model";
    private final static String         PROP_BUILD_PDA_VERSION     = "ro.build.PDA";
    private final static String         PROP_BUILD_PHONE_VERSION   = "ril.sw_ver";
    private final static String         PROP_BUILD_CSC_VERSION     = "ril.official_cscver";

    private final static String         PROP_RIL_PRODUCT_CODE      = "ril.product_code";
    private final static String         PROP_CSC_SALES_CODE        = "ro.csc.sales_code";
    private final static String         PROP_RIL_SALES_CODE        = "ril.sales_code";

    private final static String         PROP_SERAIL_NUMBER         = "ril.serialnumber";
    private final static String         PROP_SERAIL_NUMBER1        = "ro.serialno";

    private final static String         SETTING_SYSTEM_DEVICE_NAME = "device_name";

    private final static String         PRODUCTCODE_RSP            = "XXXXXXXXXXXXXX";
    private final static String         PRODUCTCODE_CTC_RSP        = "XXXXXXXXXXXCTC";
    private final static String         PRODUCTCODE_DCM_RSP        = "XXXXXXXXXXXDCM";
    private final static String         PRODUCTCODE_KOR_RSP        = "XXX";


    public static String getModelName()
    {
        String modelName = "";
        modelName = Utils.getSystemProperty(PROP_PRODUCT_MODEL);
        if (TextUtils.isEmpty(modelName))
            modelName = "";

        if (modelName.contains("SAMSUNG-"))
            modelName = modelName.replace("SAMSUNG-", "");

        return modelName;
    }

    public static String getUserFriendlyDisplayName(Context context)
    {
        String userFriendlyDisplayName = "";
        userFriendlyDisplayName = Settings.System.getString(context.getContentResolver(), SETTING_SYSTEM_DEVICE_NAME);
        if (TextUtils.isEmpty(userFriendlyDisplayName)){
            //Debug.log("userFriendlyDisplayName isEmpty");
            userFriendlyDisplayName = Settings.Global.getString(context.getContentResolver(), SETTING_SYSTEM_DEVICE_NAME);
            if(userFriendlyDisplayName == null) {
                Debug.log("userFriendlyDisplayName isEmpty again!!");
                userFriendlyDisplayName = "";
            }
        }
        Debug.log("userFriendlyDisplayName " + userFriendlyDisplayName);
        return userFriendlyDisplayName;
    }

    public static String getSerialNumber()
    {
        String serialNumber = "";
        serialNumber = Utils.getSystemProperty(PROP_SERAIL_NUMBER);
        if (TextUtils.isEmpty(serialNumber) || serialNumber.equals("00000000000"))
            serialNumber = Utils.getSystemProperty(PROP_SERAIL_NUMBER1);
        return serialNumber;
    }

    @Nullable
    public static String getMacAddr(Context c) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            try {
                List<NetworkInterface> nws = Collections.list(NetworkInterface.getNetworkInterfaces());
                for (NetworkInterface networkInterface : nws) {
                    if (!networkInterface.getName().equalsIgnoreCase("wlan0")) {
                        continue;
                    }

                    byte[] macBytes = networkInterface.getHardwareAddress();
                    if (macBytes == null) {
                        return null;
                    }

                    StringBuffer sb = new StringBuffer();
                    for (byte b : macBytes) {
                        sb.append(Integer.toHexString(b & 0xFF) + ":");
                    }

                    if (sb.length() > 0) {
                        sb.deleteCharAt(sb.length() - 1);
                    }

                    return sb.toString();
                }
            } catch (SocketException e) {
                Debug.logW(e.getLocalizedMessage());
                return null;
            }
        }

        WifiManager wm = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);

        WifiInfo wi = wm.getConnectionInfo();
        String s = wi.getMacAddress();
        return s;
    }
}
