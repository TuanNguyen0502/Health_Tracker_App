package hcmute.edu.vn.healthtrackerapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

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
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }

    public static class RecordViewHolder extends RecyclerView.ViewHolder {
        ImageView imageMedical;
        TextView textTitle;

        public RecordViewHolder(@NonNull View itemView) {
            super(itemView);
            imageMedical = itemView.findViewById(R.id.imageMedical);
            textTitle = itemView.findViewById(R.id.textTitle);
        }
    }
}