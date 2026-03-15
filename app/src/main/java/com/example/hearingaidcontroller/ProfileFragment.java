package com.example.hearingaidcontroller;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class ProfileFragment extends Fragment {
    private EditText etFullName, etEmail, etHearingLoss;
    private Button btnUpdate, btnLogout;
    private DatabaseHelper dbHelper;
    private int userId;
    private User currentUser;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        dbHelper = new DatabaseHelper(getActivity());
        SharedPreferences prefs = getActivity().getSharedPreferences("app", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        etFullName = view.findViewById(R.id.editFullName);
        etEmail = view.findViewById(R.id.editEmail);
        etHearingLoss = view.findViewById(R.id.editHearingLoss);
        btnUpdate = view.findViewById(R.id.btnUpdate);
        btnLogout = view.findViewById(R.id.btnLogout);

        loadUserData();

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullName = etFullName.getText().toString().trim();
                String hearingLoss = etHearingLoss.getText().toString().trim();
                if (fullName.isEmpty() || hearingLoss.isEmpty()) {
                    Toast.makeText(getActivity(), "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                currentUser.setFullName(fullName);
                currentUser.setHearingLossType(hearingLoss);
                boolean updated = dbHelper.updateUser(currentUser);
                if (updated) {
                    Toast.makeText(getActivity(), "Profile updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Update failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = getActivity().getSharedPreferences("app", MODE_PRIVATE);
                prefs.edit().clear().apply();
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
            }
        });

        return view;
    }

    private void loadUserData() {
        currentUser = dbHelper.getUserById(userId);
        if (currentUser != null) {
            etFullName.setText(currentUser.getFullName());
            etEmail.setText(currentUser.getEmail());
            etHearingLoss.setText(currentUser.getHearingLossType());
        }
    }
}