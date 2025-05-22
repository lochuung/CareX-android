package hcmute.edu.vn.loclinhvabao.carex.data.local.model;

import java.util.Date;
import java.util.List;

import hcmute.edu.vn.loclinhvabao.carex.ui.yoga.models.YogaPose;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model class representing a user's progress for a specific yoga day
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Progress {
    private String id;
    private String userId;
    private int dayNumber;
    private boolean isCompleted;
    private Date completionDate;
    private int duration;  // in seconds
    private int calories;
    private float averageConfidence;
    private List<YogaPose> completedPoses;
    
    /**
     * Get the completion date formatted as a string
     */
    /**
     * Get the completion date formatted as a string
     * 
     * @param context The context to use for date formatting
     * @return A formatted date string or "Not completed" if the date is null
     */
    public String getFormattedCompletionDate(android.content.Context context) {
        if (completionDate == null) return "Not completed";
        return android.text.format.DateFormat.getDateFormat(context).format(completionDate);
    }
    
    /**
     * Get the duration formatted as a string (e.g., "30 min 15 sec")
     */
    public String getFormattedDuration() {
        int minutes = duration / 60;
        int seconds = duration % 60;
        
        if (minutes > 0) {
            return String.format("%d min %02d sec", minutes, seconds);
        } else {
            return String.format("%d seconds", seconds);
        }
    }
}
