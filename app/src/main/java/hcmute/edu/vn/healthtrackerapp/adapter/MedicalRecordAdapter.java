package hcmute.edu.vn.healthtrackerapp.adapter;

import static java.security.AccessController.getContext;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudinary.Cloudinary;
import com.cloudinary.android.MediaManager;
import com.cloudinary.utils.ObjectUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import hcmute.edu.vn.healthtrackerapp.FullImageActivity;
import hcmute.edu.vn.healthtrackerapp.R;
import hcmute.edu.vn.healthtrackerapp.model.MedicalRecord;

public class MedicalRecordAdapter extends RecyclerView.Adapter<MedicalRecordAdapter.RecordViewHolder> {
    private List<MedicalRecord> recordList;
    private Context context;

    public MedicalRecordAdapter(List<MedicalRecord> recordList, Context context) {
        this.recordList = recordList;
        this.context = context;
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_medical_record, parent, false);
        return new RecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        MedicalRecord record = recordList.get(position);
        holder.textTitle.setText(record.getTitle());
        Picasso.get()
                .load(record.getImageUrl())
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_health)
                .into(holder.imageMedical);
        holder.imageMedical.setOnClickListener(v -> {
            Intent intent = new Intent(context, FullImageActivity.class);
            intent.putExtra("imageUrl", record.getImageUrl());
            context.startActivity(intent);
        });
        holder.buttonDeleteImage.setOnClickListener(v -> {
            // Delete image from cloudinary
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    Map result = MediaManager.get().getCloudinary().uploader().destroy(record.getPublic_id(), ObjectUtils.emptyMap());
                    Log.d("Delete", "Result = " + result.toString());
                } catch (Exception e) {
                    Log.e("Delete", "Failed to delete image from Cloudinary: " + e.getMessage(), e);
                }
            });

            // Delete image from firebase
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            String userId = currentUser.getUid();
            FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId)
                    .child("medicalRecords")
                    .child(record.getId()) // recordId lấy từ đối tượng MedicalRecord
                    .removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
                        // Cập nhật RecyclerView sau khi xóa
                        recordList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, recordList.size());
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }

    public static class RecordViewHolder extends RecyclerView.ViewHolder {
        ImageView imageMedical;
        TextView textTitle;
        Button buttonDeleteImage;

        public RecordViewHolder(@NonNull View itemView) {
            super(itemView);
            imageMedical = itemView.findViewById(R.id.imageMedical);
            textTitle = itemView.findViewById(R.id.textTitle);
            buttonDeleteImage = itemView.findViewById(R.id.buttonDeleteImage);
        }
    }
}