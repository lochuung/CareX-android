package hcmute.edu.vn.loclinhvabao.carex.ui.report.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.loclinhvabao.carex.R;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.YogaSession;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {

    private List<YogaSession> sessions = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    private OnSessionClickListener listener;

    public ActivityAdapter() {
    }

    public void setSessions(List<YogaSession> sessions) {
        this.sessions = sessions;
        notifyDataSetChanged();
    }

    public void setOnSessionClickListener(OnSessionClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        YogaSession session = sessions.get(position);
        holder.bind(session);
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    public class ActivityViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivActivityIcon;
        private final TextView tvActivityName;
        private final TextView tvActivityDate;
        private final TextView tvActivityDuration;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            ivActivityIcon = itemView.findViewById(R.id.iv_activity_icon);
            tvActivityName = itemView.findViewById(R.id.tv_activity_name);
            tvActivityDate = itemView.findViewById(R.id.tv_activity_date);
            tvActivityDuration = itemView.findViewById(R.id.tv_activity_duration);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onSessionClick(sessions.get(position));
                }
            });
        }

        public void bind(YogaSession session) {
            tvActivityName.setText(session.getTitle());

            // Format date and time
            String dateStr = dateFormat.format(session.getDate());
            String timeStr = timeFormat.format(session.getDate());
            tvActivityDate.setText(String.format("%s, %s", dateStr, timeStr));

            // Format duration and calories
            tvActivityDuration.setText(String.format("%d min • %d cal",
                    session.getDuration(), session.getCalories()));

            // Set icon based on session type
            if ("meditation".equalsIgnoreCase(session.getType())) {
                ivActivityIcon.setImageResource(R.drawable.ic_meditation);
            } else {
                ivActivityIcon.setImageResource(R.drawable.ic_yoga);
            }
        }
    }

    public interface OnSessionClickListener {
        void onSessionClick(YogaSession session);
    }
}