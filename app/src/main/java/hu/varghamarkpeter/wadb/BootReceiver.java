package hu.varghamarkpeter.wadb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static hu.varghamarkpeter.wadb.Utils.logd;

/**
 *
 * <p/>
 * @author <a href="mailto:vmp@varghamarkpeter.hu">Mark Peter Vargha</a>
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        logd("On boot completed received.");
        BackgroundIntentService.performAction(context, BackgroundIntentService.ACTION_BOOT_COMPLETE, null);
    }

}
