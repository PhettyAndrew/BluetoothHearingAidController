package com.example.hearingaidcontroller;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;


public class SavedSettingsFragment extends Fragment {

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private DatabaseHelper dbHelper;
    private int userId;
    private List<DeviceSettings> settingsList;
    private SharedViewModel sharedViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved_settings, container, false);

        listView = view.findViewById(R.id.listViewSavedSettings);

        dbHelper = new DatabaseHelper(getActivity());
        SharedPreferences prefs = getActivity().getSharedPreferences("app", getActivity().MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        loadSavedSettings();

        listView.setOnItemClickListener((parent, view1, position, id) -> {
            DeviceSettings selected = settingsList.get(position);
            // Send to ViewModel so ControlsFragment can load it
            sharedViewModel.setActiveDeviceSettings(selected);
            Toast.makeText(getActivity(), "Loaded settings for " + selected.getDeviceName(), Toast.LENGTH_SHORT).show();
            // Optionally navigate to ControlsFragment
            Navigation.findNavController(view).navigate(R.id.controlsFragment);
        });

        return view;
    }

    private void loadSavedSettings() {
        settingsList = dbHelper.getAllDeviceSettings(userId);
        String[] items = new String[settingsList.size()];
        for (int i = 0; i < settingsList.size(); i++) {
            DeviceSettings s = settingsList.get(i);
            items[i] = s.getDeviceName() + "\n" +
                    "Volume: " + s.getVolumeLevel() + "  Bass: " + s.getBassLevel() + "  Treble: " + s.getTrebleLevel();
        }
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);
    }
}