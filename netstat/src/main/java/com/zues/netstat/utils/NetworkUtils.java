package com.zues.netstat.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Pair;

import androidx.annotation.RequiresApi;

import com.zues.netstat.sm.NetworkStrengthManager;
import com.zues.netstat.dm.SignalStats;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class NetworkUtils {

    public static final String TAG = NetworkStrengthManager.class.getName();

    public static final String TXT_TYPE_WIFI = "W";
    public static final String TXT_TYPE_MOBILE = "M";
    public static final String TXT_TYPE_NONE = "X";
    public static final String TXT_TYPE_UNKNOWN = "U";

    public static final String TXT_CONNECTION_NO_CONNECTION = "NO CONNECTION";
    public static final String TXT_CONNECTION_POOR = "POOR";
    public static final String TXT_CONNECTION_MODERATE = "MODERATE";
    public static final String TXT_CONNECTION_GOOD = "GOOD";
    public static final String TXT_CONNECTION_EXCELLENT = "EXCELLENT";
    public static final String TXT_CONNECTION_UNKNOWN = "UNKNOWN";

    public static final int TYPE_NONE = -1;
    public static final int CONNECTION_NO_CONNECTION = 1;
    public static final int CONNECTION_POOR = 2;
    public static final int CONNECTION_MODERATE = 3;
    public static final int CONNECTION_GOOD = 4;
    public static final int CONNECTION_EXCELLENT = 5;
    public static final int CONNECTION_UNKNOWN = 6;

    public static NetworkInfo getNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    public static boolean isConnected(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        return (info != null && info.isConnected());
    }

    public static boolean isConnectedWifi(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
    }

    public static boolean isConnectedMobile(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }

    public static boolean isConnectedFast(Context context, String s) {
        NetworkInfo info = getNetworkInfo(context);
        return (info != null && info.isConnected() && isConnectionFast(info.getType(), info.getSubtype()));
    }

    private static boolean isConnectionFast(int type, int subType) {
        if (type == ConnectivityManager.TYPE_WIFI) {
            return true;
        } else if (type == ConnectivityManager.TYPE_MOBILE) {
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_1xRTT: // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA: // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE: // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS: // ~ 100 kbps
                    return false;

                case TelephonyManager.NETWORK_TYPE_EVDO_0: // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A: // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA: // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA: // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA: // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS: // ~ 400-7000 kbps
                    return true;
                /*
                 * Above API level 7, make sure to set android:targetSdkVersion
                 * to appropriate level to use these
                 */
                case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11  // ~ 1-2 Mbps
                case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9 // ~ 5 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13 // ~ 10-20 Mbps
                case TelephonyManager.NETWORK_TYPE_LTE: // API level 11 // ~ 10+ Mbps
                    return true;
                case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8 // ~25 kbps
                    return false;
                // Unknown
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    // region ----------------------------------- New way-------------------------

    public static SignalStats getConnectionSignalStats(Context context) {

        Pair<Integer, Integer> cd = getConnectionDetails(context);
        Pair<String, String> icd = getIdentifiersOfConnectionDetails(cd);

        return new SignalStats(cd.first, cd.second, icd.first, icd.second);
    }

    public static Pair<String, String> getFormattedConnectionDetails(Context context) {
        Pair<Integer, Integer> connectionDetails = getConnectionDetails(context);
        return getIdentifiersOfConnectionDetails(connectionDetails);
    }

    public static Pair<String, String> getIdentifiersOfConnectionDetails(Pair<Integer, Integer> connectionDetails) {

        if (connectionDetails.first == TYPE_NONE && connectionDetails.second == CONNECTION_NO_CONNECTION) {
            return Pair.create(TXT_TYPE_NONE, TXT_CONNECTION_NO_CONNECTION);
        }
        String firstValue = "";
        String secondValue = "";

        if (connectionDetails.first == ConnectivityManager.TYPE_WIFI) {
            firstValue = TXT_TYPE_WIFI;
        } else if (connectionDetails.first == ConnectivityManager.TYPE_MOBILE) {
            firstValue = TXT_TYPE_MOBILE;
        } else {
            firstValue = TXT_TYPE_UNKNOWN;
        }

        if (connectionDetails.second == CONNECTION_NO_CONNECTION) {
            secondValue = TXT_CONNECTION_NO_CONNECTION;
        } else if (connectionDetails.second == CONNECTION_POOR) {
            secondValue = TXT_CONNECTION_POOR;
        } else if (connectionDetails.second == CONNECTION_MODERATE) {
            secondValue = TXT_CONNECTION_MODERATE;
        } else if (connectionDetails.second == CONNECTION_GOOD) {
            secondValue = TXT_CONNECTION_GOOD;
        } else if (connectionDetails.second == CONNECTION_EXCELLENT) {
            secondValue = TXT_CONNECTION_EXCELLENT;
        } else {
            secondValue = TXT_CONNECTION_UNKNOWN;
        }
        return Pair.create(firstValue, secondValue);
    }


    public static Pair<Integer, Integer> getConnectionDetails(Context context) {

        if (!isConnected(context)) {
            return Pair.create(TYPE_NONE, CONNECTION_NO_CONNECTION);
        } else {
            NetworkInfo info = getNetworkInfo(context);
            if (isConnectedWifi(context)) {
                return getWifiConnectionSignalStrength(context);
            } else if (isConnectedMobile(context)) {
                return getMobileConnectionSignalStrength(info);
            } else {
                return Pair.create(CONNECTION_UNKNOWN, CONNECTION_UNKNOWN);
            }
        }
    }

    private static Pair<Integer, Integer> getMobileConnectionSignalStrength(NetworkInfo networkInfo) {
        switch (networkInfo.getSubtype()) {
            case TelephonyManager.NETWORK_TYPE_1xRTT: // ~ 50-100 kbps
            case TelephonyManager.NETWORK_TYPE_CDMA: // ~ 14-64 kbps
            case TelephonyManager.NETWORK_TYPE_EDGE: // ~ 50-100 kbps
            case TelephonyManager.NETWORK_TYPE_GPRS: // ~ 100 kbps
            case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8 // ~25 kbps
                return Pair.create(ConnectivityManager.TYPE_MOBILE, CONNECTION_POOR);

            case TelephonyManager.NETWORK_TYPE_EVDO_0: // ~ 400-1000 kbps
            case TelephonyManager.NETWORK_TYPE_EVDO_A: // ~ 600-1400 kbps
            case TelephonyManager.NETWORK_TYPE_HSDPA: // ~ 2-14 Mbps
            case TelephonyManager.NETWORK_TYPE_HSPA: // ~ 700-1700 kbps
            case TelephonyManager.NETWORK_TYPE_HSUPA: // ~ 1-23 Mbps
                return Pair.create(ConnectivityManager.TYPE_MOBILE, CONNECTION_MODERATE);

            case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11  // ~ 1-2 Mbps
            case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9 // ~ 5 Mbps
            case TelephonyManager.NETWORK_TYPE_UMTS: // ~ 400-7000 kbps
                return Pair.create(ConnectivityManager.TYPE_MOBILE, CONNECTION_GOOD);
            case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13 // ~ 10-20 Mbps
            case TelephonyManager.NETWORK_TYPE_LTE: // API level 11 // ~ 10+ Mbps
                return Pair.create(ConnectivityManager.TYPE_MOBILE, CONNECTION_EXCELLENT);

            // Unknown
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                return Pair.create(ConnectivityManager.TYPE_MOBILE, CONNECTION_UNKNOWN);
            default:
                return Pair.create(ConnectivityManager.TYPE_MOBILE, CONNECTION_POOR);
        }
    }

    private static Pair<Integer, Integer> getWifiConnectionSignalStrength(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int calculatedSignalLevel = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 5);
        return Pair.create(ConnectivityManager.TYPE_WIFI, calculatedSignalLevel);
    }


    // endregion ----------------------------------- New way----------------------


    public static String getWifiLevel(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int linkSpeed = wifiManager.getConnectionInfo().getRssi();
        int level = WifiManager.calculateSignalLevel(linkSpeed, 5);
        return " > level  is : " + level;
    }

    @RequiresApi(api = Build.VERSION_CODES.M) // level - 23
    public static String getMobileNetworkSpeed(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        NetworkCapabilities nc = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        int downSpeed = nc.getLinkDownstreamBandwidthKbps();
        int upSpeed = nc.getLinkUpstreamBandwidthKbps();
        return " > downSpeed : " + downSpeed + " || upSpeed : " + upSpeed;
    }

    public static void isServerReachable(){
        try {
            InetAddress.getByName("google.com").isReachable(5000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void pingByHttpURLConnection(URL url){
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(5000);
            connection.setConnectTimeout(5000);
            connection.setRequestMethod("GET");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
