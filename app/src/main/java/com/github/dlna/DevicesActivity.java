package com.github.dlna;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.dlna.dmr.MediaRenderer;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;

public class DevicesActivity extends AppCompatActivity {
    private final static String TAG = "DevicesActivity";

    private static MediaListener mediaListener;

    private long exitTime = 0;

    private AndroidUpnpService upnpService;

    private DeviceListRegistryListener deviceListRegistryListener;

    private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            upnpService = (AndroidUpnpService) service;
            BaseApplication.upnpService = upnpService;

            Log.v(TAG, "Connected to UPnP Service");

            MediaRenderer mediaRenderer =
                    new MediaRenderer(DevicesActivity.this);
            upnpService.getRegistry().addDevice(mediaRenderer.getDevice());

            // Getting ready for future device advertisements
            upnpService.getRegistry().addListener(deviceListRegistryListener);
            // Refresh device list
            upnpService.getControlPoint().search();
        }

        public void onServiceDisconnected(ComponentName className) {
            upnpService = null;
        }
    };

    public static void setMediaListener(MediaListener mediaListener) {
        DevicesActivity.mediaListener = mediaListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.devices);

        deviceListRegistryListener = new DeviceListRegistryListener();

        getApplicationContext()
                .bindService(
                        new Intent(this, AndroidUpnpServiceImpl.class),
                        serviceConnection,
                        Context.BIND_AUTO_CREATE
                );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (upnpService != null) {
            upnpService.getRegistry().removeListener(deviceListRegistryListener);
        }
        getApplicationContext().unbindService(serviceConnection);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), R.string.exit,
                        Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public static class DeviceListRegistryListener extends DefaultRegistryListener {

        @Override
        public void remoteDeviceDiscoveryStarted(Registry registry,
                                                 RemoteDevice device) {
        }

        @Override
        public void remoteDeviceDiscoveryFailed(Registry registry,
                                                final RemoteDevice device,
                                                final Exception ex) {
        }

        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
            Log.e(TAG, "remoteDeviceAdded:" + device.toString() + device.getType().getType());
        }

        @Override
        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
            Log.e(TAG, "remoteDeviceRemoved:" + device.toString() + device.getType().getType());
        }

        @Override
        public void localDeviceAdded(Registry registry, LocalDevice device) {
            Log.e(TAG, "localDeviceAdded:" + device.toString() + device.getType().getType());
        }

        @Override
        public void localDeviceRemoved(Registry registry, LocalDevice device) {
            Log.e(TAG, "localDeviceRemoved:" + device.toString() + device.getType().getType());
        }
    }
}
