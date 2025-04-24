package hcmute.edu.vn.healthtrackerapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.healthtrackerapp.adapter.MedicalRecordAdapter;
import hcmute.edu.vn.healthtrackerapp.model.MedicalRecord;

public class MedicalRecordsActivity extends AppCompatActivity {
    RecyclerView recyclerMedicalRecords;
    MedicalRecordAdapter adapter;
    List<MedicalRecord> recordList = new ArrayList<>();

    DatabaseReference medicalRecordsRef;
    String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // ensure user logged in

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medical_records);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerMedicalRecords = findViewById(R.id.recyclerMedicalRecords);
        recyclerMedicalRecords.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MedicalRecordAdapter(recordList, this);
        recyclerMedicalRecords.setAdapter(adapter);

        medicalRecordsRef = FirebaseDatabase.getInstance()
                .getReference("users").child(currentUserId).child("medicalRecords");

        loadMedicalRecords();
    }

    private void loadMedicalRecords() {
        medicalRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recordList.clear();
                for (DataSnapshot recordSnap : snapshot.getChildren()) {
                    MedicalRecord record = recordSnap.getValue(MedicalRecord.class);
                    recordList.add(record);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MedicalRecordsActivity.this, "Failed to load records", Toast.LENGTH_SHORT).show();
            }
        });
    }
}