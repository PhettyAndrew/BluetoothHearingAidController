package com.example.hearingaidcontroller;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<DeviceSettings> activeDeviceSettings = new MutableLiveData<>();
    private final MutableLiveData<BluetoothDeviceInfo> connectedDevice = new MutableLiveData<>();

    public void setActiveDeviceSettings(DeviceSettings settings) {
        activeDeviceSettings.setValue(settings);
    }

    public LiveData<DeviceSettings> getActiveDeviceSettings() {
        return activeDeviceSettings;
    }

    public void setConnectedDevice(BluetoothDeviceInfo deviceInfo) {
        connectedDevice.setValue(deviceInfo);
    }

    public LiveData<BluetoothDeviceInfo> getConnectedDevice() {
        return connectedDevice;
    }

    public static class BluetoothDeviceInfo {
        public String address;
        public String name;
        public boolean isConnected;

        public BluetoothDeviceInfo(String address, String name, boolean isConnected) {
            this.address = address;
            this.name = name;
            this.isConnected = isConnected;
        }
    }
}
