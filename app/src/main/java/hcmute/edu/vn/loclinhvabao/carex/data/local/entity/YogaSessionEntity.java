package hcmute.edu.vn.loclinhvabao.carex.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(tableName = "yoga_sessions")
@Data
@Builder
@NoArgsConstructor
public class YogaSessionEntity {
    @PrimaryKey
    @NonNull
    private String id;
    private long dateTimestamp; // Store date as timestamp
    private String title;
    private String type;
    private int duration;
    private int calories;
    private String posesJson; // Store poses as JSON string
    private float completionRate;
    private float overallScore;
    private String notes;

    @Ignore
    public YogaSessionEntity(@NonNull String id, long dateTimestamp, String title, String type,
                             int duration, int calories, String posesJson, float completionRate,
                             float overallScore, String notes) {
        this.id = id;
        this.dateTimestamp = dateTimestamp;
        this.title = title;
        this.type = type;
        this.duration = duration;
        this.calories = calories;
        this.posesJson = posesJson;
        this.completionRate = completionRate;
        this.overallScore = overallScore;
        this.notes = notes;
    }
}