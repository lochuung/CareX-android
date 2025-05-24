package hcmute.edu.vn.loclinhvabao.carex.ui.discover;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.loclinhvabao.carex.R;

public class DietRecommendationFragment extends Fragment {

    private TextView tvPersonalizedTitle;
    private RecyclerView rvRecommendations;
    private DietRecommendationAdapter adapter;
    private List<DietRecommendation> recommendations;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_diet_recommendation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        loadRecommendations();
        setupClickListeners(view);
    }

    private void initViews(View view) {
        tvPersonalizedTitle = view.findViewById(R.id.tv_personalized_title);
        rvRecommendations = view.findViewById(R.id.rv_recommendations);
    }

    private void setupClickListeners(View view) {
        ImageView btnBack = view.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void setupRecyclerView() {
        recommendations = new ArrayList<>();
        adapter = new DietRecommendationAdapter(recommendations);
        rvRecommendations.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRecommendations.setAdapter(adapter);
    }

    private void loadRecommendations() {
        // Demo data - in a real app, this would come from an API or database
        recommendations.clear();

        recommendations.add(new DietRecommendation(
                "Mediterranean Diet",
                "Rich in healthy fats, vegetables, and lean proteins",
                "🥗",
                "Heart Health",
                4.8f,
                "This diet emphasizes olive oil, fish, fruits, and vegetables. Perfect for cardiovascular health and weight management."
        ));

        recommendations.add(new DietRecommendation(
                "DASH Diet",
                "Dietary Approach to Stop Hypertension",
                "🍎",
                "Blood Pressure",
                4.7f,
                "Focuses on reducing sodium intake while increasing fruits, vegetables, and whole grains. Ideal for managing blood pressure."
        ));

        recommendations.add(new DietRecommendation(
                "Plant-Based Diet",
                "Emphasizes whole plant foods",
                "🌱",
                "Overall Health",
                4.6f,
                "Rich in fiber, vitamins, and minerals while being naturally low in calories. Great for weight loss and disease prevention."
        ));

        recommendations.add(new DietRecommendation(
                "Intermittent Fasting",
                "Time-restricted eating pattern",
                "⏰",
                "Weight Management",
                4.5f,
                "Involves cycling between periods of eating and fasting. Can help with weight loss and metabolic health."
        ));

        recommendations.add(new DietRecommendation(
                "Low-Carb Diet",
                "Reduced carbohydrate intake",
                "🥩",
                "Weight Loss",
                4.4f,
                "Focuses on proteins and healthy fats while limiting carbohydrates. Effective for rapid weight loss and blood sugar control."
        ));

        adapter.notifyDataSetChanged();
        updatePersonalizedTitle();
    }

    private void updatePersonalizedTitle() {
        // In a real app, this would be personalized based on user data
        tvPersonalizedTitle.setText("Personalized for You");
    }

    // Inner class for diet recommendation data
    public static class DietRecommendation {
        private String name;
        private String description;
        private String emoji;
        private String category;
        private float rating;
        private String details;

        public DietRecommendation(String name, String description, String emoji, 
                                String category, float rating, String details) {
            this.name = name;
            this.description = description;
            this.emoji = emoji;
            this.category = category;
            this.rating = rating;
            this.details = details;
        }

        // Getters
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getEmoji() { return emoji; }
        public String getCategory() { return category; }
        public float getRating() { return rating; }
        public String getDetails() { return details; }
    }

    // RecyclerView Adapter
    public static class DietRecommendationAdapter extends RecyclerView.Adapter<DietRecommendationAdapter.ViewHolder> {
        private List<DietRecommendation> recommendations;

        public DietRecommendationAdapter(List<DietRecommendation> recommendations) {
            this.recommendations = recommendations;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_diet_recommendation, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DietRecommendation recommendation = recommendations.get(position);
            
            holder.tvEmoji.setText(recommendation.getEmoji());
            holder.tvName.setText(recommendation.getName());
            holder.tvDescription.setText(recommendation.getDescription());
            holder.tvCategory.setText(recommendation.getCategory());
            holder.tvRating.setText(String.format("%.1f", recommendation.getRating()));
            holder.tvDetails.setText(recommendation.getDetails());

            // Set rating color based on value
            if (recommendation.getRating() >= 4.5f) {
                holder.tvRating.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_green_dark));
            } else if (recommendation.getRating() >= 4.0f) {
                holder.tvRating.setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_orange_dark));
            } else {
                holder.tvRating.setTextColor(holder.itemView.getContext().getColor(R.color.text_secondary));
            }
        }

        @Override
        public int getItemCount() {
            return recommendations.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvEmoji, tvName, tvDescription, tvCategory, tvRating, tvDetails;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvEmoji = itemView.findViewById(R.id.tv_emoji);
                tvName = itemView.findViewById(R.id.tv_name);
                tvDescription = itemView.findViewById(R.id.tv_description);
                tvCategory = itemView.findViewById(R.id.tv_category);
                tvRating = itemView.findViewById(R.id.tv_rating);
                tvDetails = itemView.findViewById(R.id.tv_details);
            }
        }
    }
}
