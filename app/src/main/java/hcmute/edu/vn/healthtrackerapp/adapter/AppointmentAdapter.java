package hcmute.edu.vn.healthtrackerapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import hcmute.edu.vn.healthtrackerapp.R;
import hcmute.edu.vn.healthtrackerapp.fragment.AppointmentUpdateListener;
import hcmute.edu.vn.healthtrackerapp.model.Appointment;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {
    private List<Appointment> appointmentList;
    private Context context;

    public AppointmentAdapter(List<Appointment> appointmentList, Context context) {
        this.appointmentList = appointmentList;
        this.context = context;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointmentList.get(position);

        holder.textViewDate.setText("Date: " + appointment.date);
        holder.textViewTime.setText("Time: " + appointment.time);
        holder.textViewUser.setText("User: " + appointment.userEmail);

        // Nếu userEmail khác rỗng → đã đặt → đổi màu item thành xám
        if (appointment.userEmail != null && !appointment.userEmail.isEmpty()) {
            holder.itemView.setBackgroundColor(Color.parseColor("#EEEEEE")); // Xám nhạt
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }

        holder.itemView.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Xác nhận")
                    .setMessage("Bạn có muốn đặt lịch hẹn này không?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                        DatabaseReference appointmentRef = FirebaseDatabase.getInstance()
                                .getReference("Appointments").child(appointment.id);

                        appointmentRef.child("userEmail").setValue(currentUserEmail)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Đã đặt lịch hẹn thành công", Toast.LENGTH_SHORT).show();

                                    // Gọi lại loadAppointments thông qua một callback
                                    if (context instanceof AppointmentUpdateListener) {
                                        ((AppointmentUpdateListener) context).onAppointmentUpdated();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Lỗi khi đặt lịch: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("Không", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return appointmentList.size();
    }

    public static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDate;
        TextView textViewTime;
        TextView textViewUser;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            textViewUser = itemView.findViewById(R.id.textViewUser);
        }
    }
}

