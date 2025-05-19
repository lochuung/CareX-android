package hcmute.edu.vn.loclinhvabao.carex.ui.report.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.loclinhvabao.carex.R;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.YogaPose;

public class PoseAdapter extends RecyclerView.Adapter<PoseAdapter.PoseViewHolder> {

    private List<YogaPose> poses = new ArrayList<>();

    public void setPoses(List<YogaPose> poses) {
        this.poses = poses != null ? poses : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PoseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pose, parent, false);
        return new PoseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PoseViewHolder holder, int position) {
        holder.bind(poses.get(position));
    }

    @Override
    public int getItemCount() {
        return poses.size();
    }

    static class PoseViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivPoseIcon;
        private final TextView tvPoseName, tvPoseDuration, tvPoseScore, tvScoreText;

        public PoseViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoseIcon = itemView.findViewById(R.id.iv_pose_icon);
            tvPoseName = itemView.findViewById(R.id.tv_pose_name);
            tvPoseDuration = itemView.findViewById(R.id.tv_pose_duration);
            tvPoseScore = itemView.findViewById(R.id.tv_pose_score);
            tvScoreText = itemView.findViewById(R.id.tv_score_text);
        }

        public void bind(YogaPose pose) {
            tvPoseName.setText(pose.getName());
            tvPoseDuration.setText(String.format("%d sec", pose.getDuration()));

            float score = pose.getScore();
            tvPoseScore.setText(String.format("%.1f", score));

            // Set score text based on score value
            String scoreText;
            if (score >= 8.5f) {
                scoreText = itemView.getContext().getString(R.string.excellent);
            } else if (score >= 7.0f) {
                scoreText = itemView.getContext().getString(R.string.good);
            } else if (score >= 5.0f) {
                scoreText = itemView.getContext().getString(R.string.average);
            } else {
                scoreText = itemView.getContext().getString(R.string.need_improvement);
            }
            tvScoreText.setText(scoreText);
        }
    }
}