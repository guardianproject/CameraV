package org.witness.informacam.informa.suckers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.FileObserver;
import android.util.Log;

import org.witness.informacam.informa.SensorLogger;
import org.witness.informacam.models.j3m.ILogPack;
import org.witness.informacam.utils.Constants.Suckers;
import org.witness.informacam.utils.Constants.App.Storage;
import org.witness.informacam.utils.Constants.Logger;
import org.witness.informacam.utils.Constants.Suckers.Device.Keys;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TimerTask;

public class DeviceSucker extends SensorLogger {
    private DCIMFileMonitor fileMonitor;
    private PlugReceiver plug_receiver;

    private final static String LOG = String.format("***** DeviceSucker : %s", Suckers.LOG);

    public DeviceSucker(final Context context) {
        super(context);
        setSucker(this);

        fileMonitor = new DCIMFileMonitor(getDCIMAlias());
        plug_receiver = new PlugReceiver();

        setTask(new TimerTask() {
            @Override
            public void run() {
                // Nothing to do here.

            }
        });

        mContext.registerReceiver(plug_receiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        fileMonitor.start();

        getTimer().schedule(getTask(), 0, Suckers.Device.LOG_RATE);
    }

    private String getDCIMAlias() {
        return String.format("%s/%s", System.getenv("EXTERNAL_STORAGE"),
                Storage.DCIM.substring(Storage.DCIM.indexOf("DCIM")));
    }

    private void getPlugState(Intent intent) {
        String parse = null;
        int plugged_state = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

        Logger.d(LOG, String.format("GETTING PLUG STATE: %d", plugged_state));

        switch(plugged_state) {
            case BatteryManager.BATTERY_PLUGGED_AC:
                parse = "battery plugged AC";
                break;
            case BatteryManager.BATTERY_PLUGGED_USB:
                parse = "battery plugged USB";
                break;
        }

        if(parse != null) {
            ILogPack logPack = new ILogPack();

            logPack.put(Keys.PLUG_EVENT_TYPE, parse);
            logPack.put(Keys.PLUG_EVENT_CODE, plugged_state);

            sendToBuffer(logPack);
        }
    }

    class PlugReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            DeviceSucker.this.getPlugState(intent);
        }
    }

    class DCIMFileMonitor extends FileObserver {
        private String dcim_alias;

        public DCIMFileMonitor(String dcim_alias) {
            super(dcim_alias, FileObserver.ALL_EVENTS);
            this.dcim_alias = dcim_alias;
        }

        public void start() {
            startWatching();
        }

        public void stop() {
            stopWatching();
        }

        @Override
        public void onEvent(int event, String path) {
            String parse = null;

            switch(event) {
                case FileObserver.CREATE:
                    parse = "file created";
                    break;
                case FileObserver.MODIFY:
                    parse = "file modified";
                    break;
                case FileObserver.CLOSE_WRITE:
                    parse = "file closed/writen";
                    break;
                case FileObserver.ACCESS:
                    //parse = "file accessed";
                    break;
                case FileObserver.DELETE:
                    parse = "file deleted";
                    break;
                case FileObserver.OPEN:
                    //parse = "file opened";
                    break;
                case FileObserver.CLOSE_NOWRITE:
                    //parse = "file closed/not writen";
                    break;
                case FileObserver.ATTRIB:
                    parse = "file attribs changed";
                    break;
            }

            if(parse != null) {
                Logger.d(LOG, String.format("EVENT %d ON %s: %s", event, path, parse));
                //lsof_r1(path);

                ILogPack logPack = new ILogPack();
                logPack.put(Keys.FILE_EFFECTED, path);
                logPack.put(Keys.ACCESS_TYPE, parse);
                logPack.put(Keys.ACCESS_CODE, event);

                DeviceSucker.this.sendToBuffer(logPack);
            }
        }

        private void ls(String path) {
            runCmd(String.format("ls %s", path));
        }

        private void lsof() {
            lsof(null);
        }

        private void pwd(String path) {
            runCmd(String.format("cd %s && pwd", path));
        }

        private void lsof(String path) {
            runCmd(String.format("lsof | grep %s",
                    path == null ? Storage.DCIM : String.format("%s/%s", Storage.DCIM, path)));
        }

        private void lsof_r1(String path) {
            runCmd(String.format("lsof -r1 %s/%s", dcim_alias, path));
        }

        private void runCmd(String cmd) {
            Process check;
            Logger.d(LOG, String.format("RUN CMD %s", cmd));

            String[] run_cmd = {"sh", "-c", cmd};

            try {
                check = Runtime.getRuntime().exec(run_cmd);
                BufferedReader br = new BufferedReader(new InputStreamReader(check.getInputStream()));

                String line;
                while((line = br.readLine()) != null) {
                    Log.d(LOG, line);
                }
            } catch (IOException e) {
                Logger.e(LOG, e);
            }
        }
    }

    public void stopUpdates() {
        setIsRunning(false);

        fileMonitor.stop();
        mContext.unregisterReceiver(plug_receiver);

        Logger.d(LOG, "shutting down DCIMSucker...");
    }
}
