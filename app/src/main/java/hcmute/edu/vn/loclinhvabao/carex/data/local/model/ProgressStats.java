package hcmute.edu.vn.loclinhvabao.carex.data.local.model;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressStats {
    private int totalSessions;
    private int totalMinutes;
    private int totalCalories;
    private float avgSessionsPerWeek;
    private float avgDurationPerSession;
    private float avgCaloriesPerSession;
    private String favoriteYogaStyle;
    private String mostConsistentDay;
    private List<DailyProgress> weeklyProgress;
    private List<DailyProgress> monthlyProgress;
    private Map<String, Integer> yogaStyleDistribution; // style -> minutes
}