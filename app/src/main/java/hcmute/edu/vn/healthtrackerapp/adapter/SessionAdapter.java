package hcmute.edu.vn.healthtrackerapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.healthtrackerapp.R;
import hcmute.edu.vn.healthtrackerapp.model.Session;

public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.ViewHolder> {
    private List<Session> sessions;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public SessionAdapter(List<Session> sessions) {
        this.sessions = sessions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_session, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Session session = sessions.get(position);
        Date startDate = new Date(session.getStartTime());
        Date endDate = new Date(session.getEndTime());
        
        String timeText = String.format(Locale.getDefault(), "%s - %s",
                timeFormat.format(startDate),
                timeFormat.format(endDate));
        holder.tvTime.setText(timeText);
        
        holder.tvSteps.setText(String.format(Locale.getDefault(), "👣 %d steps", session.getSteps()));
        holder.tvDistance.setText(String.format(Locale.getDefault(), "🚶 %.2f km", session.getDistance()));
        holder.tvCalories.setText(String.format(Locale.getDefault(), "🔥 %.1f kcal", session.getCalories()));
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    public void updateData(List<Session> newSessions) {
        this.sessions = newSessions;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvSteps, tvDistance, tvCalories;

        ViewHolder(View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvSteps = itemView.findViewById(R.id.tvSteps);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvCalories = itemView.findViewById(R.id.tvCalories);
        }
    }
}