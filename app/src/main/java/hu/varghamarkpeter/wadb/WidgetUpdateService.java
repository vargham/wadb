package hu.varghamarkpeter.wadb;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 *
 * <p/>
 * NowTechnologies Zrt. 2013-2015
 * @author <a href="mailto:vmp@varghamarkpeter.hu">Mark Peter Vargha</a>
 */
public class WidgetUpdateService extends Service implements WirelessAdb.WorkFinishedListener {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        WirelessAdb.getInstance().checkWirelessAdbPort(this);
        return Service.START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onWorkFinished(WirelessAdb wirelessAdb) {
        WadbWidgetProvider.displayState(this, getActualStateMessage(wirelessAdb));
    }

    private String getActualStateMessage(WirelessAdb wirelessAdb) {
        String statusMessage = "";
        if (wirelessAdb.isRootAvailable()) {
            if (wirelessAdb.isWirelessAdbEnabled()) {
                statusMessage = getString(R.string.widget_adb_enabled) + "\n" + Utils.getWifiIp(this) + ":" + wirelessAdb.getPort();
            }
            else {
                statusMessage = getString(R.string.widget_adb_disabled) + "\n" + getString(R.string.widget_tap_here);
            }
        }
        else {
            statusMessage = getString(R.string.widget_adb_disabled) + "\n" + getString(R.string.no_root);
        }
        return statusMessage;
    }

}