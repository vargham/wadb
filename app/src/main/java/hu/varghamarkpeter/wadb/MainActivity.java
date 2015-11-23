package hu.varghamarkpeter.wadb;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 *
 * @author <a href="mailto:vmp@varghamarkpeter.hu">Mark Peter Vargha</a>
 */
public class MainActivity extends Activity {

    private TextView textViewData;
    private TextView textViewIp;
    private EditText editTextPort;
    private ToggleButton toggleButtonStartStop;
    private ToggleButton toggleButtonStartStopOnBoot;
    private WirelessAdb wirelessAdb = WirelessAdb.getInstance();
    private AppPreferences appPreferences;
    private WirelessAdb.WorkFinishedListener checkRootFinishListener = new WirelessAdb.WorkFinishedListener() {
        @Override
        public void onWorkFinished(WirelessAdb wirelessAdb) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    checkRootAccess();
                }
            });
        }
    };
    private WirelessAdb.WorkFinishedListener changeFinishListener = new WirelessAdb.WorkFinishedListener() {
        @Override
        public void onWorkFinished(WirelessAdb wirelessAdb) {
            Utils.notifyWidget(MainActivity.this);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateUi();
                }
            });
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.appPreferences = new AppPreferences(this);
        this.textViewData = (TextView) findViewById(R.id.main_textViewData);
        this.textViewIp = (TextView) findViewById(R.id.main_textViewIp);
        this.editTextPort = (EditText) findViewById(R.id.main_editTextPort);
        this.toggleButtonStartStop = (ToggleButton) findViewById(R.id.main_toggleButtonStartStop);
        this.toggleButtonStartStopOnBoot = (ToggleButton) findViewById(R.id.main_toggleButtonStartStopOnBoot);
        updateUi();
        putVersionInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.wirelessAdb = WirelessAdb.getInstance();
        this.wirelessAdb.checkRootAvailable(this.checkRootFinishListener);
    }

    private void putVersionInfo() {
        String version;
        try {
            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName + (BuildConfig.DEBUG ? "D" : "R");
        } catch (PackageManager.NameNotFoundException e) {
            version = "0.0.0";
        }
        String title = getString(R.string.app_name) + " " + version;
        ((TextView) findViewById(R.id.main_textViewTitle)).setText(title);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.main_toggleButtonStartStop:
                int port = Integer.parseInt(this.editTextPort.getText().toString());
                if (port < 0 || port > 65535) port = 5555;
                this.wirelessAdb.setPort(port);
                this.appPreferences.setPort(port);
                if (this.toggleButtonStartStop.isChecked()) this.wirelessAdb.turnOnWirelessAdb(this.changeFinishListener);
                else this.wirelessAdb.turnOffWirelessAdb(this.changeFinishListener);
                break;
            case R.id.main_toggleButtonStartStopOnBoot:
                this.appPreferences.setStartWirelessAdbOnBoot(toggleButtonStartStopOnBoot.isChecked());
                break;
        }
    }

    private void checkRootAccess() {
        if (this.wirelessAdb.isRootAvailable()) {
            ((TextView) findViewById(R.id.main_textViewRoot)).setText(R.string.root);
            findViewById(R.id.groupNoRoot).setVisibility(View.GONE);
            findViewById(R.id.groupRoot).setVisibility(View.VISIBLE);
            this.wirelessAdb.checkWirelessAdbPort(this.changeFinishListener);
        }
        else {
            ((TextView) findViewById(R.id.main_textViewRoot)).setText(R.string.no_root);
        }
        updateUi();
    }

    private void updateUi() {
        this.toggleButtonStartStop.setChecked(this.wirelessAdb.isWirelessAdbEnabled());
        this.toggleButtonStartStopOnBoot.setChecked(this.appPreferences.isStartWirelessAdbOnBoot());
        this.editTextPort.setEnabled(!this.wirelessAdb.isWirelessAdbEnabled());
        this.editTextPort.setText("" + this.wirelessAdb.getPort());
        this.textViewIp.setText(Utils.getWifiIp(this) + ":");
    }

}