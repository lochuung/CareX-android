package hcmute.edu.vn.loclinhvabao.carex.ui.yoga.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import hcmute.edu.vn.loclinhvabao.carex.R;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.models.YogaDay;

public class YogaDayAdapter extends ListAdapter<YogaDay, YogaDayAdapter.YogaDayViewHolder> {

    private final OnDayClickListener listener;

    public interface OnDayClickListener {
        void onDayClick(YogaDay day);
    }

    public YogaDayAdapter(OnDayClickListener listener) {
        super(new YogaDayDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public YogaDayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_yoga_day, parent, false);
        return new YogaDayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull YogaDayViewHolder holder, int position) {
        YogaDay day = getItem(position);
        holder.bind(day, listener);
    }

    static class YogaDayViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardYogaDay;
        private final TextView tvDayNumber;
        private final TextView tvDayTitle;
        private final TextView tvDayDescription;
        private final TextView tvPoseCount;
        private final TextView tvDuration;

        public YogaDayViewHolder(@NonNull View itemView) {
            super(itemView);
            cardYogaDay = itemView.findViewById(R.id.card_yoga_day);
            tvDayNumber = itemView.findViewById(R.id.tv_day_number);
            tvDayTitle = itemView.findViewById(R.id.tv_day_title);
            tvDayDescription = itemView.findViewById(R.id.tv_day_description);
            tvPoseCount = itemView.findViewById(R.id.tv_pose_count);
            tvDuration = itemView.findViewById(R.id.tv_duration);
        }

        @SuppressLint({"SetTextI18n", "DefaultLocale"})
        public void bind(YogaDay day, OnDayClickListener listener) {
            tvDayNumber.setText("Day " + day.getDayNumber());
            tvDayTitle.setText(day.getTitle());
            tvDayDescription.setText(day.getDescription());
            tvPoseCount.setText(day.getTotalPoses() + " poses");
            
            // Format duration
            int totalSeconds = day.getTotalDuration();
            int minutes = totalSeconds / 60;
            int seconds = totalSeconds % 60;
            tvDuration.setText(String.format("%d min %02d sec", minutes, seconds));
            
            // Set click listener
            cardYogaDay.setOnClickListener(v -> listener.onDayClick(day));
        }
    }

    static class YogaDayDiffCallback extends DiffUtil.ItemCallback<YogaDay> {
        @Override
        public boolean areItemsTheSame(@NonNull YogaDay oldItem, @NonNull YogaDay newItem) {
            return oldItem.getDayNumber() == newItem.getDayNumber();
        }

        @Override
        public boolean areContentsTheSame(@NonNull YogaDay oldItem, @NonNull YogaDay newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) 
                    && oldItem.getDescription().equals(newItem.getDescription());
        }
    }
}
