package com.example.hearingaidcontroller;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DevicesFragment extends Fragment {
    private static final String TAG = "DevicesFragment";
    private static final int REQUEST_BLUETOOTH_SCAN = 1001;
    private static final int REQUEST_LOCATION = 1002;

    private ListView listView;
    private Button btnScan;
    private TextView txtConnected;
    private ArrayAdapter<String> adapter;
    private List<String> deviceNames = new ArrayList<>();
    private List<BluetoothDevice> deviceList = new ArrayList<>();
    private BluetoothHelper bluetoothHelper;
    private Set<String> connectedDevices = new HashSet<>();
    private DatabaseHelper dbHelper;
    private int userId;
    private SharedViewModel sharedViewModel;
    private LinearLayout scanProgressLayout;
    private ProgressBar progressBar;
    private TextView txtScanning;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && !deviceList.contains(device)) {
                    deviceList.add(device);
                    String name = device.getName();
                    if (name == null) name = "Unknown";
                    deviceNames.add(name + "\n" + device.getAddress());
                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "Found device: " + name + " " + device.getAddress());
                }
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    connectedDevices.add(device.getAddress());
                    String name = device.getName();
                    if (name == null) name = "Unknown";
                    txtConnected.setText("Connected: " + name);
                    SharedViewModel.BluetoothDeviceInfo info =
                            new SharedViewModel.BluetoothDeviceInfo(device.getAddress(), name, true);
                    sharedViewModel.setConnectedDevice(info);
                    applyStoredSettings(device);
                }
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    connectedDevices.remove(device.getAddress());
                    txtConnected.setText("No device connected");
                    SharedViewModel.BluetoothDeviceInfo info =
                            new SharedViewModel.BluetoothDeviceInfo(device.getAddress(),
                                    device.getName() != null ? device.getName() : "Unknown", false);
                    sharedViewModel.setConnectedDevice(info);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                // Discovery finished, hide progress
                hideScanProgress();
                if (deviceList.isEmpty()) {
                    Toast.makeText(getActivity(), "No devices found", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_devices, container, false);

        dbHelper = new DatabaseHelper(getActivity());
        SharedPreferences prefs = getActivity().getSharedPreferences("app", getActivity().MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        listView = view.findViewById(R.id.listViewDevices);
        btnScan = view.findViewById(R.id.btnScan);
        txtConnected = view.findViewById(R.id.txtConnected);
        scanProgressLayout = view.findViewById(R.id.scanProgressLayout);
        progressBar = view.findViewById(R.id.progressBar);
        txtScanning = view.findViewById(R.id.txtScanning);

        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, deviceNames);
        listView.setAdapter(adapter);

        bluetoothHelper = new BluetoothHelper(getActivity());

        if (!bluetoothHelper.isBluetoothEnabled()) {
            bluetoothHelper.enableBluetooth();
        }

        loadPairedDevices();

        // Check for already connected devices (at startup)
        bluetoothHelper.getConnectedA2dpDevices(new BluetoothHelper.ConnectedDevicesCallback() {
            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            @Override
            public void onConnectedDevices(List<BluetoothDevice> devices) {
                requireActivity().runOnUiThread(() -> {
                    if (!devices.isEmpty()) {
                        BluetoothDevice device = devices.get(0); // Assume first is the active one
                        String name = device.getName();
                        if (name == null) name = "Unknown";
                        txtConnected.setText("Connected: " + name);
                        connectedDevices.add(device.getAddress());
                        SharedViewModel.BluetoothDeviceInfo info =
                                new SharedViewModel.BluetoothDeviceInfo(device.getAddress(), name, true);
                        sharedViewModel.setConnectedDevice(info);
                        applyStoredSettings(device);
                    } else {
                        txtConnected.setText("No device connected");
                    }
                });
            }
        });

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check Bluetooth scan permission
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_SCAN)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN}, REQUEST_BLUETOOTH_SCAN);
                    return;
                }
                // Check location permission (required for scan results on Android 10+)
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
                    return;
                }
                // Ensure Bluetooth is on
                if (!bluetoothHelper.isBluetoothEnabled()) {
                    bluetoothHelper.enableBluetooth();
                    Toast.makeText(getActivity(), "Turning Bluetooth on...", Toast.LENGTH_SHORT).show();
                }
                startDiscovery();
            }
        });

        listView.setOnItemClickListener((parent, view1, position, id) -> {
            BluetoothDevice selected = deviceList.get(position);

            // Save selected device as active
            getActivity().getSharedPreferences("app", getActivity().MODE_PRIVATE).edit()
                    .putString("active_device_address", selected.getAddress())
                    .putString("active_device_name", selected.getName())
                    .apply();

            if (connectedDevices.contains(selected.getAddress())) {
                applyStoredSettings(selected);
            } else {
                // Not connected – show dialog
                new AlertDialog.Builder(getActivity())
                        .setTitle("Device not connected")
                        .setMessage("Please connect to " + selected.getName() + " via Bluetooth settings.")
                        .setPositiveButton("Open Settings", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();

                // Send placeholder settings so ControlsFragment can show the device name
                DeviceSettings placeholder = new DeviceSettings();
                placeholder.setUserId(userId);
                placeholder.setDeviceAddress(selected.getAddress());
                placeholder.setDeviceName(selected.getName());
                placeholder.setVolumeLevel(50);
                placeholder.setBassLevel(50);
                placeholder.setTrebleLevel(50);
                sharedViewModel.setActiveDeviceSettings(placeholder);
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        bluetoothHelper.registerReceiver(receiver, filter);

        return view;
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private void startDiscovery() {
        deviceList.clear();
        deviceNames.clear();
        adapter.notifyDataSetChanged();
        bluetoothHelper.startDiscovery();
        // Show progress indicator
        scanProgressLayout.setVisibility(View.VISIBLE);
        Toast.makeText(getActivity(), "Scanning for devices...", Toast.LENGTH_SHORT).show();
    }

    private void hideScanProgress() {
        scanProgressLayout.setVisibility(View.GONE);
    }

    private void loadPairedDevices() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        List<BluetoothDevice> paired = bluetoothHelper.getPairedDevices();
        deviceList.addAll(paired);
        for (BluetoothDevice d : paired) {
            String name = d.getName();
            if (name == null) name = "Unknown";
            deviceNames.add(name + "\n" + d.getAddress());
        }
        adapter.notifyDataSetChanged();
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private void applyStoredSettings(BluetoothDevice device) {
        DeviceSettings settings = dbHelper.getDeviceSettings(userId, device.getAddress());
        if (settings == null) {
            settings = new DeviceSettings();
            settings.setUserId(userId);
            settings.setDeviceAddress(device.getAddress());
            settings.setDeviceName(device.getName());
            settings.setVolumeLevel(50);
            settings.setBassLevel(50);
            settings.setTrebleLevel(50);
        }
        sharedViewModel.setActiveDeviceSettings(settings);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_SCAN || requestCode == REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // If both permissions are granted (we may need to check both), start scan
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_SCAN)
                        == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED) {
                    startDiscovery();
                } else {
                    Toast.makeText(getActivity(), "All permissions are required for scanning", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "Permission denied. Cannot scan for devices.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bluetoothHelper.unregisterReceiver(receiver);
        bluetoothHelper.cancelDiscovery();
        hideScanProgress();
    }
}