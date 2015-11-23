package hu.varghamarkpeter.wadb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 *
 * <p/>
 * @author <a href="mailto:vmp@varghamarkpeter.hu">Mark Peter Vargha</a>
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        AppPreferences appPreferences = new AppPreferences(context);
        if (appPreferences.isStartWirelessAdbOnBoot()) {
            WirelessAdb.getInstance().setPort(appPreferences.getPort());
            WirelessAdb.getInstance().turnOnWirelessAdb(null);
            Utils.notifyWidget(context);
        }
    }

}
