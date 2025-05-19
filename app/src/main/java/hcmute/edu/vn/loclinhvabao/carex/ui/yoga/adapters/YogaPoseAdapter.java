package hcmute.edu.vn.loclinhvabao.carex.ui.yoga.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import hcmute.edu.vn.loclinhvabao.carex.R;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.models.YogaPose;

public class YogaPoseAdapter extends ListAdapter<YogaPose, YogaPoseAdapter.YogaPoseViewHolder> {

    private final OnPoseClickListener listener;

    public interface OnPoseClickListener {
        void onPoseClick(YogaPose pose);
    }

    public YogaPoseAdapter(OnPoseClickListener listener) {
        super(new YogaPoseDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public YogaPoseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_yoga_pose, parent, false);
        return new YogaPoseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull YogaPoseViewHolder holder, int position) {
        YogaPose pose = getItem(position);
        holder.bind(pose, listener);
    }

    static class YogaPoseViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardPose;
        private final ImageView ivPose;
        private final TextView tvPoseName;
        private final TextView tvPoseSanskritName;
        private final TextView tvDuration;

        public YogaPoseViewHolder(@NonNull View itemView) {
            super(itemView);
            cardPose = itemView.findViewById(R.id.card_pose);
            ivPose = itemView.findViewById(R.id.iv_pose);
            tvPoseName = itemView.findViewById(R.id.tv_pose_name);
            tvPoseSanskritName = itemView.findViewById(R.id.tv_pose_sanskrit_name);
            tvDuration = itemView.findViewById(R.id.tv_duration);
        }

        public void bind(YogaPose pose, OnPoseClickListener listener) {
            tvPoseName.setText(pose.getEnglishName());
            tvPoseSanskritName.setText(pose.getSanskritName());
            tvDuration.setText(pose.getFormattedDuration());
            
            // Load image with Glide
            Glide.with(itemView.getContext())
                    .load(pose.getImageUrl())
                    .placeholder(R.drawable.yoga_session_background)
                    .into(ivPose);
            
            // Set click listener
            cardPose.setOnClickListener(v -> listener.onPoseClick(pose));
        }
    }

    static class YogaPoseDiffCallback extends DiffUtil.ItemCallback<YogaPose> {
        @Override
        public boolean areItemsTheSame(@NonNull YogaPose oldItem, @NonNull YogaPose newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull YogaPose oldItem, @NonNull YogaPose newItem) {
            return oldItem.getEnglishName().equals(newItem.getEnglishName()) 
                    && oldItem.getSanskritName().equals(newItem.getSanskritName());
        }
    }
}
