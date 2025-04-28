package hcmute.edu.vn.healthtrackerapp;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import hcmute.edu.vn.healthtrackerapp.fragment.AppointmentsFragment;
import hcmute.edu.vn.healthtrackerapp.fragment.HealthFragment;
import hcmute.edu.vn.healthtrackerapp.fragment.MedicalRecordsFragment;
import hcmute.edu.vn.healthtrackerapp.fragment.ProfileFragment;

public class UserMainActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();

        requestPermission();
    }

    private void initViews() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        loadFragment(new HealthFragment());

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            if (item.getItemId() == R.id.nav_health) {
                selectedFragment = new HealthFragment();
            } else if (item.getItemId() == R.id.nav_appointments) {
                selectedFragment = new AppointmentsFragment();
            } else if (item.getItemId() == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            } else if (item.getItemId() == R.id.nav_medical_records) {
                selectedFragment = new MedicalRecordsFragment();
            } else {
                selectedFragment = new HealthFragment();
            }
            loadFragment(selectedFragment);
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .commit();
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(UserMainActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(UserMainActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_IMAGE_REQUEST);
        }

        if (ContextCompat.checkSelfPermission(UserMainActivity.this, android.Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(UserMainActivity.this,
                new String[]{android.Manifest.permission.ACTIVITY_RECOGNITION}, 100);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(UserMainActivity.this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(UserMainActivity.this,
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }
}