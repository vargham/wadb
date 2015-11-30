package hu.varghamarkpeter.wadb;

import android.provider.Settings;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import eu.chainfire.libsuperuser.Shell;

/**
 * TODO Check USB ADB and turn it on.
 * <p/>
 * @author <a href="mailto:vmp@varghamarkpeter.hu">Mark Peter Vargha</a>
 */
public final class WirelessAdb {

    private final static int PORT_DEFAULT = 5555;
    private final static int PORT_WADB_OFF = -1;
    private final static String CMD_SET_ADB_PORT = "setprop service.adb.tcp.port";
    private final static String CMD_GET_ADB_PORT = "getprop service.adb.tcp.port";
    private final static String CMD_SET_USB_DEBUG_ENABLED = "setprop persist.sys.usb.config adb";
    private final static String CMD_STOP_ADBD = "stop adbd";
    private final static String CMD_START_ADBD = "start adbd";

    private boolean isRootChecked = false;
    private boolean isRootAvailable = false;
    private boolean wirelessAdbChecked = false;
    private boolean wirelessAdbEnabled = false;
    private int portCurrent = PORT_WADB_OFF;
    private boolean isPortChecked = false;
    private int portToSet = PORT_DEFAULT;

    private final WorkerThread workerThread;
    private final ConcurrentHashMap<WorkFinishedListener, Object> workFinishedListeners = new ConcurrentHashMap<>();

    private final static WirelessAdb INSTANCE = new WirelessAdb();

    public interface WorkFinishedListener {
        void onWorkFinished(WirelessAdb wirelessAdb);
    }

    private WirelessAdb() {
        this.workerThread = new WorkerThread();
        this.workerThread.start();
        this.workerThread.addRunnable(new Runnable() {
            @Override
            public void run() {
                doCheckRootAvailable();
            }
        });
        checkWirelessAdbPort(null);
    }

    public synchronized static WirelessAdb getInstance() {
        return INSTANCE;
    }

    private void addWorkFinishedListener(WorkFinishedListener listener) {
        if (listener != null) this.workFinishedListeners.put(listener, new Object());
    }

    private void notifyFinishListeners() {
        for (Map.Entry entry : this.workFinishedListeners.entrySet()) {
            ((WorkFinishedListener) entry.getKey()).onWorkFinished(INSTANCE);
        }
        this.workFinishedListeners.clear();
    }

    private void doSetPortTo(final int port, WorkFinishedListener listener) {
        addWorkFinishedListener(listener);
        this.workerThread.addRunnable(new Runnable() {
            @Override
            public void run() {
                writePort(port);
            }
        });
    }

    private void doCheckRootAvailable() {
        this.isRootChecked = true;
        this.isRootAvailable = Shell.SU.available();
    }

    /* PUBLIC */

    public boolean isRootAvailable() {
        return this.isRootAvailable;
    }

    public void checkWirelessAdbPort(WorkFinishedListener listener) {
        addWorkFinishedListener(listener);
        this.workerThread.addRunnable(new Runnable() {
            @Override
            public void run() {
                readPort();
            }
        });
    }

    public boolean isWirelessAdbEnabled() {
        return this.wirelessAdbEnabled;
    }

    public void setPort(int port) {
        this.portToSet = port;
    }

    public int getPort() {
        return this.portToSet;
    }

    public void turnOnWirelessAdb(WorkFinishedListener listener) {
        doSetPortTo(this.portToSet, listener);
    }

    public void flipWirelessAdbState(WorkFinishedListener listener) {
        doSetPortTo(this.wirelessAdbEnabled ? PORT_WADB_OFF : this.portToSet, listener);
    }

    public void turnOffWirelessAdb(WorkFinishedListener listener) {
        doSetPortTo(PORT_WADB_OFF, listener);
    }

    public void checkRootAvailable(WorkFinishedListener listener) {
        if (this.isRootChecked) {
            listener.onWorkFinished(INSTANCE);
            return;
        }
        this.addWorkFinishedListener(listener);
        this.workerThread.addRunnable(new Runnable() {
            @Override
            public void run() {
                doCheckRootAvailable();
            }
        });
    }

    /* WORKER */

    private void writePort(int port) {
        //int adb = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.ADB_ENABLED, 0);
        this.portCurrent = runCommandsForIntResult(CMD_SET_ADB_PORT + " " + port, CMD_STOP_ADBD, CMD_START_ADBD, CMD_GET_ADB_PORT);
        checkState();
    }

    private void readPort() {
        this.portCurrent = runCommandsForIntResult(CMD_GET_ADB_PORT);
        checkState();
    }

    private void checkState() {
        this.wirelessAdbChecked = true;
        this.wirelessAdbEnabled = this.portCurrent > PORT_WADB_OFF;
    }

    private int runCommandsForIntResult(String... commands) {
        int resultCode = PORT_WADB_OFF;
        if (isRootAvailable()) {
            List<String> result = Shell.SU.run(commands);
            if (result != null && !result.isEmpty()) {
                try {
                    resultCode = Integer.parseInt(result.get(0));
                } catch (NumberFormatException e) {
                    resultCode = PORT_WADB_OFF;
                }
            }
        }
        return resultCode;
    }

    private class WorkerThread extends Thread {

        private final ConcurrentLinkedQueue<Runnable> runQueue = new ConcurrentLinkedQueue<>();

        public WorkerThread() {
            setName("WiFi ADB worker");
        }

        public void addRunnable(Runnable... runnables) {
            if (runnables.length == 0) return;
            this.runQueue.addAll(Arrays.asList(runnables));
            synchronized (WirelessAdb.this) {
                WirelessAdb.this.notifyAll();
            }
        }

        @Override
        public void run() {
            while(true) {
                try {
                    synchronized (WirelessAdb.this) {
                        WirelessAdb.this.wait();
                    }
                } catch (InterruptedException e) {
                    //Continue running.
                }
                while (!this.runQueue.isEmpty()) {
                    this.runQueue.poll().run();
                }
                notifyFinishListeners();
            }
        }

    }

}
