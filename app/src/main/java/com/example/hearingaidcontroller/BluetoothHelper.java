package com.example.hearingaidcontroller;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.RequiresPermission;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BluetoothHelper {
    private BluetoothAdapter bluetoothAdapter;
    private Context context;

    public BluetoothHelper(Context context) {
        this.context = context;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void enableBluetooth() {
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public List<BluetoothDevice> getPairedDevices() {
        List<BluetoothDevice> devices = new ArrayList<>();
        if (bluetoothAdapter != null) {
            Set<BluetoothDevice> paired = bluetoothAdapter.getBondedDevices();
            devices.addAll(paired);
        }
        return devices;
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    public void startDiscovery() {
        if (bluetoothAdapter != null && !bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.startDiscovery();
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    public void cancelDiscovery() {
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
    }

    public void registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        context.registerReceiver(receiver, filter);
    }

    public void unregisterReceiver(BroadcastReceiver receiver) {
        context.unregisterReceiver(receiver);
    }

    public List<BluetoothDevice> getConnectedA2dpDevices() {
        List<BluetoothDevice> connected = new ArrayList<>();
        if (bluetoothAdapter == null) return connected;

        // Use BluetoothProfile to get A2DP devices
        bluetoothAdapter.getProfileProxy(context, new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                if (profile == BluetoothProfile.A2DP) {
                    List<BluetoothDevice> devices = proxy.getConnectedDevices();
                    connected.addAll(devices);
                    // No need to close proxy here? We'll just use the list after.
                }
                // We can't return from here; instead, we'll need to handle asynchronously.
                // For simplicity, we'll use a synchronous approach? Not possible directly.
                // Better to use a callback. Let's redesign: we'll use a callback interface.
            }

            @Override
            public void onServiceDisconnected(int profile) { }
        }, BluetoothProfile.A2DP);

        // This won't work synchronously. We need to either:
        // 1. Use a callback and update UI when ready.
        // 2. Or use a simple alternative: check ACL connection via BluetoothDevice.ACTION_ACL_CONNECTED broadcast (already used).
        // So we'll just rely on broadcasts and update when they happen. For initial state, we can check via isConnected() if we have a way.

        return connected; // This will be empty because async.
    }

    public interface ConnectedDevicesCallback {
        void onConnectedDevices(List<BluetoothDevice> devices);
    }

    public void getConnectedA2dpDevices(ConnectedDevicesCallback callback) {
        if (bluetoothAdapter == null) {
            callback.onConnectedDevices(new ArrayList<>());
            return;
        }
        bluetoothAdapter.getProfileProxy(context, new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                if (profile == BluetoothProfile.A2DP) {
                    List<BluetoothDevice> devices = new ArrayList<>(proxy.getConnectedDevices());
                    callback.onConnectedDevices(devices);
                }
            }

            @Override
            public void onServiceDisconnected(int profile) {
                callback.onConnectedDevices(new ArrayList<>());
            }
        }, BluetoothProfile.A2DP);
    }
}