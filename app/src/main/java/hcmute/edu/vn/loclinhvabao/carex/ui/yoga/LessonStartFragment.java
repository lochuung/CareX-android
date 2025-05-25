package hcmute.edu.vn.loclinhvabao.carex.ui.yoga;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;
import hcmute.edu.vn.loclinhvabao.carex.R;
import hcmute.edu.vn.loclinhvabao.carex.ui.shared.SharedViewModel;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.models.YogaDay;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.models.YogaPose;

@AndroidEntryPoint
public class LessonStartFragment extends Fragment {    private SharedViewModel sharedViewModel;
    private TextView tvPoseName;
    private TextView tvSanskritName;
    private TextView tvCountdown;
    private TextView tvInstructions;
    private TextView tvProgressCount;
    private ProgressBar progressSession;
    private ImageView ivPose;
    private ImageView ivPlayButton;
    private YouTubePlayerView youtubePlayerView;
    private CardView cardVideo;
    private CardView cardCountdown;
    private MaterialButton btnStartPose;
    private MaterialButton btnDone;
    private MaterialButton btnPauseCountdown;    private CountDownTimer countDownTimer;
    private boolean isCountdownActive = false;
    private boolean isCountdownPaused = false;
    private long elapsedTime = 0;
    private long startTime = 0;
    private String currentVideoId;
    private int currentPoseIndex = 0;
    private List<YogaPose> completedPoses = new ArrayList<>();
    private int totalSessionTimeInSeconds = 0; // Track actual session time

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
        tvProgressCount = view.findViewById(R.id.tv_progress_count);
        progressSession = view.findViewById(R.id.progress_session);
        ivPose = view.findViewById(R.id.iv_pose);
        ivPlayButton = view.findViewById(R.id.iv_play_button);
        youtubePlayerView = view.findViewById(R.id.youtube_player_view);
        cardVideo = view.findViewById(R.id.card_video);
        cardCountdown = view.findViewById(R.id.card_countdown);
        btnStartPose = view.findViewById(R.id.btn_start_pose);
        btnDone = view.findViewById(R.id.btn_skip); // Changed from btnSkip to btnDone
        btnPauseCountdown = view.findViewById(R.id.btn_pause_countdown);
        ImageView ivBack = view.findViewById(R.id.iv_back);
        
        // Add lifecycle observer to YouTube player
        getLifecycle().addObserver(youtubePlayerView);
        
        // Set click listeners
        btnStartPose.setOnClickListener(v -> handleStartPoseClick());
        btnDone.setOnClickListener(v -> markPoseCompleteAndContinue());
        btnPauseCountdown.setOnClickListener(v -> toggleCountdownPause());
        ivBack.setOnClickListener(v -> goBack());
        ivPlayButton.setOnClickListener(v -> playYoutubeVideo());
    }    private void observeViewModel() {
        sharedViewModel.getSelectedYogaPose().observe(getViewLifecycleOwner(), this::updateUI);
        sharedViewModel.getSelectedYogaDay().observe(getViewLifecycleOwner(), this::updateSessionProgress);
    }
      private void updateUI(YogaPose pose) {
        if (pose == null) return;
        
        // Reset session timer when starting a new pose
        totalSessionTimeInSeconds = 0;
        
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
        
        // Update current pose index
        updateCurrentPoseIndex(pose);
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
    }    @SuppressLint("SetTextI18n")
    private void showInstructionsView() {
        cardVideo.setVisibility(View.VISIBLE);
        cardCountdown.setVisibility(View.GONE);
        btnStartPose.setText("Start Pose");
        btnStartPose.setEnabled(true);
        
        // Reset timer display for new pose
        tvCountdown.setText("00:00");
        
        // Reset video view state
        youtubePlayerView.setVisibility(View.GONE);
        ivPlayButton.setVisibility(View.VISIBLE);
        
        // Release any playing video
        youtubePlayerView.getYouTubePlayerWhenReady(YouTubePlayer::pause);
    }
      @SuppressLint("SetTextI18n")
    private void showCountdownView() {
        cardVideo.setVisibility(View.VISIBLE);
        cardCountdown.setVisibility(View.VISIBLE);
        btnStartPose.setText("Pause");
        btnStartPose.setEnabled(true);
    }
      private void startPoseCountdown() {
        YogaPose selectedPose = sharedViewModel.getSelectedYogaPose().getValue();
        if (selectedPose == null) return;
        
        // Reset timer for new pose
        elapsedTime = 0;
        startTime = System.currentTimeMillis();
        
        startCountUpTimer();
    }    private void markPoseCompleteAndContinue() {
        // Cancel any ongoing countdown and reset timer
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        
        // Add actual practice time to total session time
        if (isCountdownActive && elapsedTime > 0) {
            totalSessionTimeInSeconds += (int)(elapsedTime / 1000);
        }
        
        // Reset timer state
        isCountdownActive = false;
        isCountdownPaused = false;
        elapsedTime = 0;
        
        // Mark current pose as completed
        YogaPose currentPose = sharedViewModel.getSelectedYogaPose().getValue();
        if (currentPose != null && !completedPoses.contains(currentPose)) {
            completedPoses.add(currentPose);
            
            // Update session progress display after marking pose as completed
            YogaDay currentDay = sharedViewModel.getSelectedYogaDay().getValue();
            if (currentDay != null) {
                updateSessionProgress(currentDay);
            }
        }
        
        // Move to next pose or complete day
        skipToNextPose();
    }private void skipToNextPose() {
        // Cancel any ongoing countdown and reset timer
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        
        // Reset timer state for next pose
        isCountdownActive = false;
        isCountdownPaused = false;
        elapsedTime = 0;
        
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
            completeDay(currentDay);
        }
    }    private void completeDay(YogaDay day) {
        // Use actual session time instead of calculated duration from pose specifications
        int actualDuration = totalSessionTimeInSeconds;
        
        // Estimate calories based on actual practice time (3-5 calories per minute for yoga)
        int durationMinutes = actualDuration / 60;
        int calories = Math.max(durationMinutes * 4, 1); // At least 1 calorie for any session
        
        // Calculate average confidence (default to good performance)
        float averageConfidence = 0.8f;
        
        // Mark day as completed in SharedViewModel with actual duration data
        sharedViewModel.markDayComplete(
            day.getDayNumber(), 
            actualDuration, // Pass actual duration in seconds
            calories, 
            averageConfidence, 
            new ArrayList<>(completedPoses)
        );
        
        // Update overall progress
        sharedViewModel.updateYogaProgress(day.getDayNumber());
        
        // Return to Home fragment (10-day yoga session view)
        Navigation.findNavController(requireView()).popBackStack(R.id.homeFragment, false);
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
    }    private void updateCurrentPoseIndex(YogaPose currentPose) {
        YogaDay currentDay = sharedViewModel.getSelectedYogaDay().getValue();
        if (currentDay == null) return;
        
        List<YogaPose> poses = currentDay.getPoses();
        for (int i = 0; i < poses.size(); i++) {
            if (poses.get(i).getId() == currentPose.getId()) {
                currentPoseIndex = i;
                break;
            }
        }
        // Only update progress display, don't mark as completed yet
        updateSessionProgressDisplay(currentDay);
    }
    
    private void updateSessionProgress(YogaDay day) {
        // This method will be called when progress actually changes (pose completed)
        if (day == null) return;
        updateSessionProgressDisplay(day);
    }
    
    private void updateSessionProgressDisplay(YogaDay day) {
        if (day == null) return;
        
        int totalPoses = day.getTotalPoses();
        int completedPosesCount = completedPoses.size();
        int currentPoseNumber = Math.max(completedPosesCount, currentPoseIndex) + 1;
        
        // Update progress text to show completed poses
        tvProgressCount.setText(String.format("%d/%d poses", completedPosesCount, totalPoses));
        
        // Update progress bar based on completed poses
        int progress = (int) ((float) completedPosesCount / totalPoses * 100);
        progressSession.setProgress(progress);
        progressSession.setMax(100);
    }
    
    private void handleStartPoseClick() {
        if (!isCountdownActive) {
            startPoseCountdown();
        } else {
            toggleCountdownPause();
        }
    }
    
    private void toggleCountdownPause() {
        if (isCountdownPaused) {
            resumeCountdown();
        } else {
            pauseCountdown();
        }
    }
      @SuppressLint("SetTextI18n")
    private void pauseCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            isCountdownPaused = true;
            btnStartPose.setText("Resume");
            btnPauseCountdown.setText("Resume");
            // Store current elapsed time when pausing
            elapsedTime = System.currentTimeMillis() - startTime;
        }
    }
      @SuppressLint("SetTextI18n")
    private void resumeCountdown() {
        if (isCountdownActive && isCountdownPaused) {
            isCountdownPaused = false;
            btnStartPose.setText("Pause");
            btnPauseCountdown.setText("Pause");
            // Resume from where we paused
            startTime = System.currentTimeMillis() - elapsedTime;
            startCountUpTimer();
        }
    }
    
    private void startCountUpTimer() {
        showCountdownView();
        isCountdownActive = true;
        isCountdownPaused = false;
        
        // Use a long duration countdown that counts up
        countDownTimer = new CountDownTimer(Long.MAX_VALUE, 1000) {
            @SuppressLint("DefaultLocale")
            @Override
            public void onTick(long millisUntilFinished) {
                if (!isCountdownPaused) {
                    elapsedTime = System.currentTimeMillis() - startTime;
                    long seconds = elapsedTime / 1000;
                    long minutes = seconds / 60;
                    seconds = seconds % 60;
                    tvCountdown.setText(String.format("%02d:%02d", minutes, seconds));
                }
            }

            @Override
            public void onFinish() {
                // This won't be called since we use Long.MAX_VALUE
            }
        };
        
        countDownTimer.start();
    }
}
