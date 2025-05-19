package hcmute.edu.vn.loclinhvabao.carex.ui.yoga.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import java.util.Locale;

import hcmute.edu.vn.loclinhvabao.carex.R;
import hcmute.edu.vn.loclinhvabao.carex.ui.shared.SharedViewModel;
import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.models.YogaPose;

/**
 * Handles dialog creation and management for yoga pose instructions.
 * Follows the Single Responsibility Principle by encapsulating dialog management.
 */
public class YogaPoseDialogHelper {
    private final Context context;
    
    public YogaPoseDialogHelper(Context context) {
        this.context = context;
    }
    
    /**
     * Show dialog with instructions for current pose
     */
    public void showInstructionsDialog(String poseName, float confidence) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_pose_instructions, null);
        
        TextView tvPoseName = dialogView.findViewById(R.id.tv_pose_name);
        TextView tvInstructions = dialogView.findViewById(R.id.tv_instructions);
        TextView tvConfidence = dialogView.findViewById(R.id.tv_confidence);
        Button btnClose = dialogView.findViewById(R.id.btn_close);
        Button btnLearnMore = dialogView.findViewById(R.id.btn_learn_more);
        
        if (poseName == null || poseName.isEmpty()) {
            tvPoseName.setText("No Pose Detected");
            tvInstructions.setText("Please position yourself in the camera view.");
            tvConfidence.setVisibility(View.GONE);
        } else {
            tvPoseName.setText(poseName);
            // Here we could fetch detailed instructions for specific poses from a repository
            // For now using a generic message
            tvInstructions.setText("1. Ensure your full body is visible in the frame\n" +
                    "2. Hold the pose steady\n" +
                    "3. Breathe deeply and maintain correct alignment\n" +
                    "4. Follow the on-screen overlay for proper positioning");
            tvConfidence.setText(String.format(Locale.getDefault(), "Confidence: %.1f%%", confidence * 100));
            tvConfidence.setVisibility(View.VISIBLE);
        }
        
        // Create dialog without buttons in the builder
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        
        // Set custom button click listeners
        btnClose.setOnClickListener(v -> dialog.dismiss());
        btnLearnMore.setOnClickListener(v -> {
            if (poseName != null && !poseName.isEmpty()) {
                dialog.dismiss();
                showDetailedInstructionsDialog(poseName);
            }
        });
        
        // Apply animations when showing the dialog
        dialog.getWindow().getAttributes().windowAnimations = android.R.style.Animation_Dialog;
        dialog.show();
    }
    
    /**
     * Show detailed instructions dialog for a specific pose
     */
    public void showDetailedInstructionsDialog(String poseName) {
        // This would navigate to a detailed instructions page
        // Could be implemented to launch LessonStartFragment for specific pose
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_pose_instructions, null);
        
        TextView tvPoseTitle = dialogView.findViewById(R.id.title);
        TextView tvPoseName = dialogView.findViewById(R.id.tv_pose_name);
        TextView tvInstructions = dialogView.findViewById(R.id.tv_instructions);
        TextView tvConfidence = dialogView.findViewById(R.id.tv_confidence);
        Button btnClose = dialogView.findViewById(R.id.btn_close);
        Button btnLearnMore = dialogView.findViewById(R.id.btn_learn_more);
        
        // Setup with more detailed information
        tvPoseTitle.setText("DETAILED INSTRUCTIONS");
        tvPoseName.setText(poseName);
        tvConfidence.setVisibility(View.GONE);
        
        // Provide more detailed instructions for the specific pose
        // In a real app, this would be fetched from a database or API
        String detailedInstructions = getDetailedInstructions(poseName);
        tvInstructions.setText(detailedInstructions);
        
        // Create and show dialog
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        
        // Set custom button click listeners
        btnClose.setOnClickListener(v -> dialog.dismiss());
        btnLearnMore.setText("TRY THIS POSE");
        btnLearnMore.setOnClickListener(v -> {
            dialog.dismiss();
            // This would integrate with the LessonStartFragment for this specific pose
            // For now, just showing a toast message
            Toast.makeText(context, "This would launch LessonStartFragment for " + poseName, 
                    Toast.LENGTH_SHORT).show();
            
            // In a real implementation, we would create a YogaPose object and set it in SharedViewModel,
            // then navigate to LessonStartFragment. Example code (commented out):
            /*
            YogaPose pose = new YogaPose();
            pose.setEnglishName(poseName);
            // Set other properties...
            
            SharedViewModel viewModel = new ViewModelProvider(this).get(SharedViewModel.class);
            viewModel.selectYogaPose(pose);
            
            // Start the activity containing the LessonStartFragment
            Intent intent = new Intent(this, YogaSessionActivity.class);
            startActivity(intent);
            */
        });
        
        dialog.getWindow().getAttributes().windowAnimations = android.R.style.Animation_Dialog;
        dialog.show();
    }
    
    /**
     * Get detailed instructions for a specific pose
     */
    private String getDetailedInstructions(String poseName) {
        // In a real app, this would be fetched from a database or API
        // For now, just return a generic detailed instruction
        return "1. Begin in a standing position with feet hip-width apart and arms at your sides.\n\n" +
               "2. Distribute your weight evenly across both feet, grounding down through the four corners of each foot.\n\n" +
               "3. Engage your thigh muscles slightly, lifting your kneecaps without locking your knees.\n\n" +
               "4. Lengthen your tailbone toward the floor while drawing your abdomen in gently.\n\n" +
               "5. Roll your shoulders back and down, away from your ears.\n\n" +
               "6. Keep your head centered, with your chin parallel to the floor.\n\n" +
               "7. Breathe deeply, focusing on your posture and alignment.\n\n" +
               "8. Hold the position for 30-60 seconds, maintaining awareness of your entire body.";
    }
}
