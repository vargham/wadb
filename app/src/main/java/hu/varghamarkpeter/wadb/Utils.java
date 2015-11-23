package hu.varghamarkpeter.wadb;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

/**
 *
 * <p/>
 * @author <a href="mailto:vmp@varghamarkpeter.hu">Mark Peter Vargha</a>
 */
public final class Utils {

    private Utils() {}

    public final static String LOG_TAG = "WADB";

    public static void logd(String message) {
        Log.d(LOG_TAG, message);
    }

    public static void logd(Object object, String message) {
        logd(object == null ? "" : object.getClass().getSimpleName() + ": " + message);
    }

    public static String intToIP(int i) {
        return (( i & 0xFF) + "." + ((i >> 8 ) & 0xFF) + "." + ((i >> 16 ) & 0xFF) + "." + ((i >> 24 ) & 0xFF));
    }

    public static String getWifiIp(Context context) {
        WifiInfo wifiInfo = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo();
        return intToIP(wifiInfo.getIpAddress());
    }

    public static void notifyWidget(final Context context) {
        final Intent intent = new Intent(context, WadbWidgetProvider.class);
        intent.setAction(WadbWidgetProvider.WIDGET_UPDATE_ALL);
        context.sendBroadcast(intent);
    }

}
