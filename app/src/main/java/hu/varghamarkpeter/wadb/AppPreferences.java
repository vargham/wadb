package hu.varghamarkpeter.wadb;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 *
 * <p/>
 * @author <a href="mailto:vmp@varghamarkpeter.hu">Mark Peter Vargha</a>
 */
public class AppPreferences {

    private final SharedPreferences sharedPreferences;
    private boolean startWirelessAdbOnBoot = false;
    private int port = 5555;

    public AppPreferences(Context context) {
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        load();
    }

    public boolean isStartWirelessAdbOnBoot() {
        load();
        return startWirelessAdbOnBoot;
    }

    public void setStartWirelessAdbOnBoot(boolean startWirelessAdbOnBoot) {
        this.startWirelessAdbOnBoot = startWirelessAdbOnBoot;
        save();
    }

    public int getPort() {
        load();
        return port;
    }

    public void setPort(int port) {
        this.port = port;
        save();
    }

    private void load() {
        this.startWirelessAdbOnBoot = this.sharedPreferences.getBoolean("startWirelessAdbOnBoot", this.startWirelessAdbOnBoot);
        this.port = this.sharedPreferences.getInt("port", this.port);
    }

    private void save() {
        SharedPreferences.Editor editor = this.sharedPreferences.edit();
        editor.putBoolean("startWirelessAdbOnBoot", this.startWirelessAdbOnBoot);
        editor.putInt("port", this.port);
        editor.apply();
    }

}
