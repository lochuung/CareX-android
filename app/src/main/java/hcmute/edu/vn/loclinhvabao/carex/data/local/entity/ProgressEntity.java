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
    tableName = "progress",
    foreignKeys = {
        @ForeignKey(
            entity = YogaDayEntity.class,
            parentColumns = "dayNumber",
            childColumns = "dayNumber",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = UserProfileEntity.class,
            parentColumns = "id",
            childColumns = "userId",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index("dayNumber"),
        @Index("userId")
    }
)
@Data
@Builder
@NoArgsConstructor
public class ProgressEntity {
    @PrimaryKey
    @NonNull
    private String id;
    private String userId;
    private int dayNumber;
    private boolean isCompleted;
    private long completionTimestamp;
    private int duration; // Actual duration in seconds
    private int calories;
    private float averageConfidence;
    private String completedPosesJson; // IDs of poses that were completed

    @Ignore
    public ProgressEntity(@NonNull String id, String userId, int dayNumber, boolean isCompleted,
                         long completionTimestamp, int duration, int calories, 
                         float averageConfidence, String completedPosesJson) {
        this.id = id;
        this.userId = userId;
        this.dayNumber = dayNumber;
        this.isCompleted = isCompleted;
        this.completionTimestamp = completionTimestamp;
        this.duration = duration;
        this.calories = calories;
        this.averageConfidence = averageConfidence;
        this.completedPosesJson = completedPosesJson;
    }
}
