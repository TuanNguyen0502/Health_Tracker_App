package hcmute.edu.vn.healthtrackerapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

import hcmute.edu.vn.healthtrackerapp.model.Appointment;

public class MakeAppointmentActivity extends AppCompatActivity {
    private EditText editTextDoctor;
    private TextView textViewDate, textViewTime;
    private Button buttonPickDate, buttonPickTime, buttonBook;
    private Calendar calendar;
    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_make_appointment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        initialize();

        calendar = Calendar.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("appointments");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        buttonPickDate.setOnClickListener(v -> showDatePicker());
        buttonPickTime.setOnClickListener(v -> showTimePicker());
        buttonBook.setOnClickListener(v -> bookAppointment());
    }

    private void initialize() {
        editTextDoctor = findViewById(R.id.editTextDoctor);
        textViewDate = findViewById(R.id.textViewDate);
        textViewTime = findViewById(R.id.textViewTime);
        buttonPickDate = findViewById(R.id.buttonPickDate);
        buttonPickTime = findViewById(R.id.buttonPickTime);
        buttonBook = findViewById(R.id.buttonBook);
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            textViewDate.setText(dayOfMonth + "/" + (month+1) + "/" + year);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            textViewTime.setText(hourOfDay + ":" + String.format("%02d", minute));
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        timePickerDialog.show();
    }

    private void bookAppointment() {
        String doctorEmail = editTextDoctor.getText().toString().trim();
        if (doctorEmail.isEmpty()) {
            Toast.makeText(this, "Enter Doctor Email", Toast.LENGTH_SHORT).show();
            return;
        }

        String appointmentId = databaseReference.push().getKey();
        Appointment appointment = new Appointment(
                appointmentId,
                currentUser.getEmail(),
                doctorEmail,
                textViewDate.getText().toString(),
                textViewTime.getText().toString()
        );

        databaseReference.child(appointmentId).setValue(appointment)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Appointment booked!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}