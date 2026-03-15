package com.example.hearingaidcontroller;

public class DeviceSettings {
    private int id;
    private int userId;
    private String deviceAddress;
    private String deviceName;
    private int volumeLevel; // 0-100
    private int bassLevel;   // 0-100
    private int trebleLevel; // 0-100

    public DeviceSettings() {}

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getDeviceAddress() { return deviceAddress; }
    public void setDeviceAddress(String deviceAddress) { this.deviceAddress = deviceAddress; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    public int getVolumeLevel() { return volumeLevel; }
    public void setVolumeLevel(int volumeLevel) { this.volumeLevel = volumeLevel; }
    public int getBassLevel() { return bassLevel; }
    public void setBassLevel(int bassLevel) { this.bassLevel = bassLevel; }
    public int getTrebleLevel() { return trebleLevel; }
    public void setTrebleLevel(int trebleLevel) { this.trebleLevel = trebleLevel; }
}
