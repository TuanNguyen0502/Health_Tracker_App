package hcmute.edu.vn.healthtrackerapp.fragment;

import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.healthtrackerapp.R;
import hcmute.edu.vn.healthtrackerapp.adapter.SessionAdapter;
import hcmute.edu.vn.healthtrackerapp.db.StorageHelper;
import hcmute.edu.vn.healthtrackerapp.model.Session;
import hcmute.edu.vn.healthtrackerapp.service.StepCounterService;

public class HealthFragment extends Fragment {

    private static final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1001;
    private FitnessOptions fitnessOptions;

    private TextView tvSteps, tvDistance, tvCalories, tvTime;
    private MaterialButton btnStart, btnStop, btnPickDate;
    private TextView tvSelectedDate, tvTotalSteps, tvTotalDistance, tvTotalCalories;
    private RecyclerView rvSessions;

    private SessionAdapter sessionAdapter;
    private List<Session> sessionList = new ArrayList<>();
    private StorageHelper storageHelper;
    private String currentDate;

    private BroadcastReceiver stepReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (StepCounterService.ACTION_UPDATE.equals(intent.getAction())) {
                int steps = intent.getIntExtra("steps", 0);
                float distance = intent.getFloatExtra("distance", 0f);
                float calories = intent.getFloatExtra("calories", 0f);
                long elapsed = intent.getLongExtra("timeElapsed", 0L);
                boolean sessionCompleted = intent.getBooleanExtra("session_completed", false);
                
                tvSteps.setText("Steps: " + steps);
                tvDistance.setText(String.format(Locale.getDefault(), "Distance: %.2f km", distance));
                tvCalories.setText(String.format(Locale.getDefault(), "Calories: %.2f kcal", calories));
                tvTime.setText(formatTime(elapsed));
                
                // If session is completed, update the list
                if (sessionCompleted) {
                    loadSummary(currentDate);
                }
            }
        }
    };

    private BroadcastReceiver serviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (StepCounterService.ACTION_UPDATE.equals(intent.getAction())) {
                loadSummary(currentDate);
            }
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_health, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        storageHelper = new StorageHelper(requireContext());

        // Initialize Google Fit options
        fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
                .build();

        tvSteps = view.findViewById(R.id.tvSteps);
        tvDistance = view.findViewById(R.id.tvDistance);
        tvCalories = view.findViewById(R.id.tvCalories);
        tvTime = view.findViewById(R.id.tvTime);
        btnStart = view.findViewById(R.id.btnStart);
        btnStop = view.findViewById(R.id.btnStop);
        btnPickDate = view.findViewById(R.id.btnPickDate);
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        tvTotalSteps = view.findViewById(R.id.tvTotalSteps);
        tvTotalDistance = view.findViewById(R.id.tvTotalDistance);
        tvTotalCalories = view.findViewById(R.id.tvTotalCalories);
        rvSessions = view.findViewById(R.id.rvSessions);

        initRecyclerView();
        initCurrentDate();
        loadSummary(currentDate);

        btnStart.setOnClickListener(v -> {
            if (checkGoogleFitPermission()) {
                requireContext().startService(
                    new Intent(requireContext(), StepCounterService.class)
                );
            }
        });

        btnStop.setOnClickListener(v -> {
            requireContext().stopService(
                    new Intent(requireContext(), StepCounterService.class)
            );
            loadSummary(currentDate);
        });

        btnPickDate.setOnClickListener(v -> showDatePicker());
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(stepReceiver,
                        new IntentFilter(StepCounterService.ACTION_UPDATE));
        LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(serviceReceiver,
                        new IntentFilter(StepCounterService.ACTION_UPDATE));
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(requireContext())
                .unregisterReceiver(stepReceiver);
        LocalBroadcastManager.getInstance(requireContext())
                .unregisterReceiver(serviceReceiver);
    }

    private void initRecyclerView() {
        rvSessions.setLayoutManager(new LinearLayoutManager(requireContext()));
        sessionAdapter = new SessionAdapter(sessionList);
        rvSessions.setAdapter(sessionAdapter);
    }

    private void initCurrentDate() {
        currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        tvSelectedDate.setText(currentDate);
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (DatePicker view, int year, int month, int dayOfMonth) -> {
            currentDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            tvSelectedDate.setText(currentDate);
            loadSummary(currentDate);
        },
                Integer.parseInt(currentDate.substring(0,4)),
                Integer.parseInt(currentDate.substring(5,7)) - 1,
                Integer.parseInt(currentDate.substring(8,10))).show();
    }

    private void loadSummary(String date) {
        List<Session> sessions = storageHelper.getSessionsByDate(date);
        sessionList.clear();
        sessionList.addAll(sessions);
        sessionAdapter.updateData(sessions);

        int totalSteps = 0;
        float totalDistance = 0f, totalCalories = 0f;
        for (Session s : sessions) {
            totalSteps += s.getSteps();
            totalDistance += s.getDistance();
            totalCalories += s.getCalories();
        }
        tvTotalSteps.setText("Total Steps: " + totalSteps);
        tvTotalDistance.setText(String.format(Locale.getDefault(), "Total Distance: %.2f km", totalDistance));
        tvTotalCalories.setText(String.format(Locale.getDefault(), "Total Calories: %.2f kcal", totalCalories));
    }

    private String formatTime(long millis) {
        int seconds = (int) (millis / 1000) % 60;
        int minutes = (int) ((millis / (1000 * 60)) % 60);
        int hours = (int) (millis / (1000 * 60 * 60));
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }

    private boolean checkGoogleFitPermission() {
        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(requireContext(), fitnessOptions);
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                this,
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                account,
                fitnessOptions);
            return false;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
            if (resultCode == android.app.Activity.RESULT_OK) {
                Toast.makeText(requireContext(), "Google Fit permission granted", Toast.LENGTH_SHORT).show();
                requireContext().startService(
                    new Intent(requireContext(), StepCounterService.class)
                );
            } else {
                Toast.makeText(requireContext(), "Google Fit permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
