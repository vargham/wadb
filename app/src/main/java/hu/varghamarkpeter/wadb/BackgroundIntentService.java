package hu.varghamarkpeter.wadb;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import static hu.varghamarkpeter.wadb.Utils.*;

/**
 * <p/>
 * NowTechnologies Zrt. 2013-2015
 *
 * @author <a href="mailto:vmp@nowtech.hu">Mark Peter Vargha</a>
 */
public class BackgroundIntentService extends IntentService implements WirelessAdb.WorkFinishedListener {

    public static final String ACTION_BOOT_COMPLETE = "hu.varghamarkpeter.wadb.ACTION_BOOT_COMPLETE";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public BackgroundIntentService() {
        super("BackgroundIntentService");
    }

    /**
     *
     * @param context
     * @param action
     * @param extras
     */
    public static void performAction(Context context, String action, Bundle extras) {
        if ((context == null) || (action == null) || action.equals("")) return;
        Intent intent = new Intent(context, BackgroundIntentService.class);
        intent.setAction(action);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (extras != null) intent.putExtras(extras);
        context.startService(intent);
    }

    /**
     *
     * @param intent
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        if ((action == null) || (action.equals(""))) return;
        switch (action) {
            case ACTION_BOOT_COMPLETE:
                AppPreferences appPreferences = new AppPreferences(this);
                if (appPreferences.isStartWirelessAdbOnBoot()) {
                    WirelessAdb.getInstance().setPort(appPreferences.getPort());
                    WirelessAdb.getInstance().turnOnWirelessAdb(this);
                    Toast.makeText(this, "ADB over WiFi auto start.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * Notifies widget 3 seconds later.
     * @param wirelessAdb
     */
    @Override
    public void onWorkFinished(WirelessAdb wirelessAdb) {
        final Context context = this;
        new Handler(context.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Utils.notifyWidget(context);
            }
        }, 3000);
    }
}
