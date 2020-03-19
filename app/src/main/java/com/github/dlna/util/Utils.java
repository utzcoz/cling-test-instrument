package com.github.dlna.util;

import android.os.Build;
import android.util.Log;

import com.github.dlna.Settings;

import org.fourthline.cling.model.types.UDN;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.UUID;

public class Utils {
    private static final String TAG = "UpnpUtil";
    public static final String MANUFACTURER = android.os.Build.MANUFACTURER;
    public static final String DMR_NAME = "MSI MediaRenderer";

    public static final String DMR_DESC = "MSI MediaRenderer";
    public static final String DMR_MODEL_URL = "http://4thline.org/projects/cling/mediarenderer/";

    public static int getRealTime(String paramString) {
        int i = paramString.indexOf(":");
        int j = 0;
        if (i > 0) {
            String[] arrayOfString = paramString.split(":");
            j = Integer.parseInt(arrayOfString[2]) + 60
                    * Integer.parseInt(arrayOfString[1]) + 3600
                    * Integer.parseInt(arrayOfString[0]);
        }
        return j;
    }

    public static UDN uniqueSystemIdentifier(String salt) {
        StringBuilder systemSalt = new StringBuilder();
        systemSalt.append(Settings.getUUID());
        systemSalt.append(Build.MODEL);
        systemSalt.append(Build.MANUFACTURER);
        Log.i(TAG, "uniqueSystemIdentifier " + systemSalt.toString());

        try {
            byte[] hash = MessageDigest.getInstance("MD5").digest(systemSalt.toString().getBytes());
            return new UDN(new UUID(new BigInteger(-1, hash).longValue(), salt.hashCode()));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
