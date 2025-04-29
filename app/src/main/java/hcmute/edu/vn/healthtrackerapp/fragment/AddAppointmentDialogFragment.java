package hcmute.edu.vn.healthtrackerapp.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Locale;

import hcmute.edu.vn.healthtrackerapp.R;
import hcmute.edu.vn.healthtrackerapp.model.Appointment;

public class AddAppointmentDialogFragment extends DialogFragment {

    private TextView tvSelectedDate;
    private TextView tvSelectedTime;
    private Button btnSave;
    private String doctorEmail;
    private String selectedDate, selectedTime;

    public interface OnAppointmentAddedListener {
        void onAppointmentAdded();
    }

    public static AddAppointmentDialogFragment newInstance(String doctorEmail) {
        AddAppointmentDialogFragment fragment = new AddAppointmentDialogFragment();
        Bundle args = new Bundle();
        args.putString("doctorEmail", doctorEmail);
        fragment.setArguments(args);
        return fragment;
    }

    private OnAppointmentAddedListener listener;

    public void setOnAppointmentAddedListener(OnAppointmentAddedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            doctorEmail = getArguments().getString("doctorEmail");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_appointment, null);

        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        tvSelectedTime = view.findViewById(R.id.tvSelectedTime);
        btnSave = view.findViewById(R.id.btnSaveAppointment);

        // Date picker dialog
        tvSelectedDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view1, year1, month1, dayOfMonth) -> {
                        selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month1 + 1, year1);
                        tvSelectedDate.setText(selectedDate);
                    }, year, month, day);
            datePickerDialog.show();
        });

        // Time picker dialog
        tvSelectedTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
                    (view12, hourOfDay, minute1) -> {
                        selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);
                        tvSelectedTime.setText(selectedTime);
                    }, hour, minute, true);
            timePickerDialog.show();
        });

        btnSave.setOnClickListener(v -> saveAppointment());

        builder.setView(view);
        return builder.create();
    }

    private void saveAppointment() {
        if (TextUtils.isEmpty(selectedDate) || TextUtils.isEmpty(selectedTime)) {
            Toast.makeText(getContext(), "Vui lòng chọn ngày và giờ", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Appointments");
        String id = ref.push().getKey();

        Appointment appointment = new Appointment(id, null, doctorEmail, selectedDate, selectedTime); // userEmail = null

        ref.child(id).setValue(appointment).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Đã thêm lịch hẹn", Toast.LENGTH_SHORT).show();
                if (listener != null) listener.onAppointmentAdded();
                dismiss();
            } else {
                Toast.makeText(getContext(), "Thêm thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
