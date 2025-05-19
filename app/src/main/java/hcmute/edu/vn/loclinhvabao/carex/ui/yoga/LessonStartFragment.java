package hcmute.edu.vn.loclinhvabao.carex.ui.yoga;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.google.android.material.button.MaterialButton;

import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;
import hcmute.edu.vn.loclinhvabao.carex.R;
import hcmute.edu.vn.loclinhvabao.carex.ui.shared.SharedViewModel;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.models.YogaDay;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.models.YogaPose;

@AndroidEntryPoint
public class LessonStartFragment extends Fragment {

    private SharedViewModel sharedViewModel;    private TextView tvPoseName;
    private TextView tvSanskritName;
    private TextView tvCountdown;
    private TextView tvInstructions;
    private ImageView ivPose;
    private ImageView ivPlayButton;
    private YouTubePlayerView youtubePlayerView;
    private CardView cardVideo;
    private CardView cardCountdown;
    private MaterialButton btnStartPose;
    private CountDownTimer countDownTimer;
    private boolean isCountdownActive = false;
    private String currentVideoId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lesson_start, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize ViewModel
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        
        // Initialize UI
        initUI(view);
        observeViewModel();
    }    private void initUI(View view) {
        tvPoseName = view.findViewById(R.id.tv_pose_name);
        tvSanskritName = view.findViewById(R.id.tv_sanskrit_name);
        tvCountdown = view.findViewById(R.id.tv_countdown);
        tvInstructions = view.findViewById(R.id.tv_instructions);
        ivPose = view.findViewById(R.id.iv_pose);
        ivPlayButton = view.findViewById(R.id.iv_play_button);
        youtubePlayerView = view.findViewById(R.id.youtube_player_view);
        cardVideo = view.findViewById(R.id.card_video);
        cardCountdown = view.findViewById(R.id.card_countdown);
        btnStartPose = view.findViewById(R.id.btn_start_pose);
        MaterialButton btnSkip = view.findViewById(R.id.btn_skip);
        ImageView ivBack = view.findViewById(R.id.iv_back);
        
        // Add lifecycle observer to YouTube player
        getLifecycle().addObserver(youtubePlayerView);
        
        // Set click listeners
        btnStartPose.setOnClickListener(v -> startPoseCountdown());
        btnSkip.setOnClickListener(v -> skipToNextPose());
        ivBack.setOnClickListener(v -> goBack());
        ivPlayButton.setOnClickListener(v -> playYoutubeVideo());
    }

    private void observeViewModel() {
        sharedViewModel.getSelectedYogaPose().observe(getViewLifecycleOwner(), this::updateUI);
    }
    
    private void updateUI(YogaPose pose) {
        if (pose == null) return;
        
        tvPoseName.setText(pose.getEnglishName());
        tvSanskritName.setText(pose.getSanskritName());
        tvInstructions.setText(pose.getInstructions());
        
        // Load pose image
        Glide.with(requireContext())
                .load(pose.getImageUrl())
                .placeholder(R.drawable.yoga_session_background)
                .into(ivPose);
        
        // Setup YouTube player
        String videoId = extractYoutubeVideoId(pose.getVideoUrl());
        setupYoutubePlayer(videoId);
        
        // Show instructions view first
        showInstructionsView();
    }
    
    private String extractYoutubeVideoId(String videoUrl) {
        String videoId = "";
        if (videoUrl != null && videoUrl.contains("youtube.com") || Objects.requireNonNull(videoUrl).contains("youtu.be")) {
            int lastSlashIndex = videoUrl.lastIndexOf('/');
            if (lastSlashIndex != -1) {
                videoId = videoUrl.substring(lastSlashIndex + 1);
            }
            
            // Handle watch?v= format
            if (videoId.contains("watch?v=")) {
                int equalIndex = videoId.indexOf('=');
                if (equalIndex != -1) {
                    videoId = videoId.substring(equalIndex + 1);
                }
            }
            
            // Handle additional parameters after video ID
            if (videoId.contains("&")) {
                int ampIndex = videoId.indexOf('&');
                if (ampIndex != -1) {
                    videoId = videoId.substring(0, ampIndex);
                }
            }
        }
        return videoId;
    }
      private void setupYoutubePlayer(String videoId) {
        // Store the current video ID
        this.currentVideoId = videoId;
        
        // We'll load the video when the play button is clicked
        youtubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                // Video will be loaded when play button is clicked
            }
        });
    }
    
    private void playYoutubeVideo() {
        if (currentVideoId != null && !currentVideoId.isEmpty()) {
            // Show YouTube player and hide image/play button
            youtubePlayerView.setVisibility(View.VISIBLE);
            ivPlayButton.setVisibility(View.GONE);
            
            // Play the video
            youtubePlayerView.getYouTubePlayerWhenReady(youTubePlayer -> youTubePlayer.loadVideo(currentVideoId, 0));
        }
    }
      @SuppressLint("SetTextI18n")
      private void showInstructionsView() {
        cardVideo.setVisibility(View.VISIBLE);
        cardCountdown.setVisibility(View.GONE);
        btnStartPose.setText("Start Pose");
        btnStartPose.setEnabled(true);
        
        // Reset video view state
        youtubePlayerView.setVisibility(View.GONE);
        ivPlayButton.setVisibility(View.VISIBLE);
        
        // Release any playing video
        youtubePlayerView.getYouTubePlayerWhenReady(YouTubePlayer::pause);
    }
    
    @SuppressLint("SetTextI18n")
    private void showCountdownView() {
        cardVideo.setVisibility(View.GONE);
        cardCountdown.setVisibility(View.VISIBLE);
        btnStartPose.setText("In Progress...");
        btnStartPose.setEnabled(false);
    }
    
    private void startPoseCountdown() {
        YogaPose selectedPose = sharedViewModel.getSelectedYogaPose().getValue();
        if (selectedPose == null) return;
        
        int duration = selectedPose.getDurationInSeconds();
        
        showCountdownView();
        isCountdownActive = true;
        
        countDownTimer = new CountDownTimer(duration * 1000L, 1000) {
            @SuppressLint("DefaultLocale")
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                long minutes = seconds / 60;
                seconds = seconds % 60;
                tvCountdown.setText(String.format("%02d:%02d", minutes, seconds));
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFinish() {
                isCountdownActive = false;
                btnStartPose.setEnabled(true);
                btnStartPose.setText("Done");
                tvCountdown.setText("00:00");
                
                // Auto-proceed to next pose after a short delay
                new CountDownTimer(2000, 2000) {
                    @Override
                    public void onTick(long millisUntilFinished) {}

                    @Override
                    public void onFinish() {
                        skipToNextPose();
                    }
                }.start();
            }
        };
        
        countDownTimer.start();
    }
    
    private void skipToNextPose() {
        // Cancel any ongoing countdown
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        
        // Get current day
        YogaPose currentPose = sharedViewModel.getSelectedYogaPose().getValue();
        if (currentPose == null) return;
        
        // Get all poses from the current day
        YogaDay currentDay = sharedViewModel.getSelectedYogaDay().getValue();
        if (currentDay == null) return;
        
        // Find the next pose
        YogaPose nextPose = null;
        boolean foundCurrent = false;
        
        for (YogaPose pose : currentDay.getPoses()) {
            if (foundCurrent) {
                nextPose = pose;
                break;
            }
            
            if (pose.getId() == currentPose.getId()) {
                foundCurrent = true;
            }
        }
        
        if (nextPose != null) {
            // Move to next pose
            sharedViewModel.selectYogaPose(nextPose);
        } else {
            // Completed all poses for this day
            sharedViewModel.markDayComplete(currentDay.getDayNumber());
            
            // Return to Home fragment
            Navigation.findNavController(requireView()).popBackStack(R.id.homeFragment, false);
        }
    }
    
    private void goBack() {
        // Cancel any ongoing countdown
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        
        // Navigate back
        Navigation.findNavController(requireView()).popBackStack();
    }
    
    @Override
    public void onDestroy() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        super.onDestroy();
    }
}
