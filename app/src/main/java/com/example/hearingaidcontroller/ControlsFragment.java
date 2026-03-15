package com.example.hearingaidcontroller;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


public class ControlsFragment extends Fragment {
    private TextView txtDeviceName, txtVolume, txtBass, txtTreble;
    private SeekBar seekVolume, seekBass, seekTreble;
    private Button btnSave;
    private AudioController audioController;
    private DatabaseHelper dbHelper;
    private int userId;
    private DeviceSettings currentSettings;
    private SharedViewModel sharedViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_controls, container, false);

        txtDeviceName = view.findViewById(R.id.txtDeviceName);
        txtVolume = view.findViewById(R.id.txtVolume);
        txtBass = view.findViewById(R.id.txtBass);
        txtTreble = view.findViewById(R.id.txtTreble);
        seekVolume = view.findViewById(R.id.seekVolume);
        seekBass = view.findViewById(R.id.seekBass);
        seekTreble = view.findViewById(R.id.seekTreble);
        btnSave = view.findViewById(R.id.btnSave);

        dbHelper = new DatabaseHelper(getActivity());
        SharedPreferences prefs = getActivity().getSharedPreferences("app", getActivity().MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        // Initialize audio controller
        audioController = new AudioController(getActivity());
        audioController.startAudio();

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Observe active device settings
        sharedViewModel.getActiveDeviceSettings().observe(getViewLifecycleOwner(), new Observer<DeviceSettings>() {
            @Override
            public void onChanged(DeviceSettings settings) {
                if (settings != null) {
                    currentSettings = settings;
                    updateUI(settings);
                }
            }
        });

        // Observe connected device info
        sharedViewModel.getConnectedDevice().observe(getViewLifecycleOwner(), new Observer<SharedViewModel.BluetoothDeviceInfo>() {
            @Override
            public void onChanged(SharedViewModel.BluetoothDeviceInfo deviceInfo) {
                if (currentSettings != null) {
                    updateConnectionStatus(deviceInfo);
                }
            }
        });

        // SeekBar listeners
        seekVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtVolume.setText("Volume: " + progress);
                audioController.setVolume(progress);
                if (currentSettings != null) currentSettings.setVolumeLevel(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekBass.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtBass.setText("Bass: " + progress);
                audioController.setBass(progress);
                if (currentSettings != null) currentSettings.setBassLevel(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekTreble.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtTreble.setText("Treble: " + progress);
                audioController.setTreble(progress);
                if (currentSettings != null) currentSettings.setTrebleLevel(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentSettings == null || currentSettings.getDeviceAddress() == null) {
                    Toast.makeText(getActivity(), "No device selected", Toast.LENGTH_SHORT).show();
                    return;
                }
                dbHelper.addOrUpdateDeviceSettings(currentSettings);
                Toast.makeText(getActivity(), "Settings saved for " + currentSettings.getDeviceName(), Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void updateUI(DeviceSettings settings) {
        // Update connection status using latest connected device info
        SharedViewModel.BluetoothDeviceInfo connInfo = sharedViewModel.getConnectedDevice().getValue();
        updateConnectionStatus(connInfo);

        seekVolume.setProgress(settings.getVolumeLevel());
        seekBass.setProgress(settings.getBassLevel());
        seekTreble.setProgress(settings.getTrebleLevel());
        txtVolume.setText("Volume: " + settings.getVolumeLevel());
        txtBass.setText("Bass: " + settings.getBassLevel());
        txtTreble.setText("Treble: " + settings.getTrebleLevel());

        // Apply to hardware
        audioController.setVolume(settings.getVolumeLevel());
        audioController.setBass(settings.getBassLevel());
        audioController.setTreble(settings.getTrebleLevel());
    }

    private void updateConnectionStatus(SharedViewModel.BluetoothDeviceInfo connInfo) {
        if (currentSettings == null) return;
        boolean isConnected = (connInfo != null && connInfo.isConnected &&
                connInfo.address.equals(currentSettings.getDeviceAddress()));
        txtDeviceName.setText("Device: " + currentSettings.getDeviceName() +
                (isConnected ? " (Connected)" : " (Not Connected)"));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        audioController.stopAudio();
    }
}