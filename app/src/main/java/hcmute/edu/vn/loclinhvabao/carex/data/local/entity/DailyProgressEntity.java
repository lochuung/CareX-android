package hcmute.edu.vn.loclinhvabao.carex.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(
    tableName = "daily_progress",
    foreignKeys = @ForeignKey(
        entity = UserProfileEntity.class,
        parentColumns = "id",
        childColumns = "userId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {
        @Index("userId"),
        @Index("dateTimestamp")
    }
)
@Data
@Builder
@NoArgsConstructor
public class DailyProgressEntity {
    @PrimaryKey
    @NonNull
    private String id;
    private String userId;
    private long dateTimestamp;
    private int totalDuration; // Total duration of all sessions in a day (in seconds)
    private int totalCalories;
    private int sessionsCount;
    private float averageScore;
    private String yogaStylesJson; // JSON array of yoga styles practiced that day

    @Ignore
    public DailyProgressEntity(@NonNull String id, String userId, long dateTimestamp,
                              int totalDuration, int totalCalories, int sessionsCount,
                              float averageScore, String yogaStylesJson) {
        this.id = id;
        this.userId = userId;
        this.dateTimestamp = dateTimestamp;
        this.totalDuration = totalDuration;
        this.totalCalories = totalCalories;
        this.sessionsCount = sessionsCount;
        this.averageScore = averageScore;
        this.yogaStylesJson = yogaStylesJson;
    }
}
