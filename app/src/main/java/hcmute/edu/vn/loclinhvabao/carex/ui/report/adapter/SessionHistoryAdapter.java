package hcmute.edu.vn.loclinhvabao.carex.ui.report.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.loclinhvabao.carex.R;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.YogaSession;
import hcmute.edu.vn.loclinhvabao.carex.util.DateUtils;

public class SessionHistoryAdapter extends RecyclerView.Adapter<SessionHistoryAdapter.SessionViewHolder> {

    private List<YogaSession> sessions = new ArrayList<>();
    private final SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.getDefault());
    private final SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());
    private final SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());

    private OnSessionClickListener listener;

    public interface OnSessionClickListener {
        void onSessionClick(YogaSession session);
    }

    public SessionHistoryAdapter(OnSessionClickListener listener) {
        this.listener = listener;
    }

    public void setSessions(List<YogaSession> sessions) {
        this.sessions = sessions != null ? sessions : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_session_history, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        holder.bind(sessions.get(position));
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    class SessionViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDay, tvMonth, tvYear, tvSessionName, tvDuration, tvCalories;

        public SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tv_day);
            tvMonth = itemView.findViewById(R.id.tv_month);
            tvYear = itemView.findViewById(R.id.tv_year);
            tvSessionName = itemView.findViewById(R.id.tv_session_name);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            tvCalories = itemView.findViewById(R.id.tv_calories);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onSessionClick(sessions.get(position));
                }
            });
        }

        public void bind(YogaSession session) {
            if (session == null || session.getDate() == null) return;

            // Format date
            tvDay.setText(dayFormat.format(session.getDate()));
            tvMonth.setText(monthFormat.format(session.getDate()));
            tvYear.setText(yearFormat.format(session.getDate()));

            // Set session details
            tvSessionName.setText(session.getTitle());
            tvDuration.setText(String.format("%d min", session.getDuration()));
            tvCalories.setText(String.format("%d cal", session.getCalories()));
        }
    }
}