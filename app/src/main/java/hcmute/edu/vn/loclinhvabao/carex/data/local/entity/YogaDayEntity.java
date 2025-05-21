package hcmute.edu.vn.loclinhvabao.carex.data.local.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(tableName = "yoga_days")
@Data
@Builder
@NoArgsConstructor
public class YogaDayEntity {
    @PrimaryKey
    private int dayNumber;
    private String title;
    private String description;
    private String posesJson; // Store list of pose IDs as JSON array
    private int totalDuration; // Pre-calculated total duration in seconds
    private String category; // e.g., "Foundation", "Balance", "Core Strength", etc.
    private String difficulty; // e.g., "Beginner", "Intermediate", "Advanced"
    private String focusArea; // e.g., "Full Body", "Lower Body", "Upper Body", "Core", etc.
    
    @Ignore
    public YogaDayEntity(int dayNumber, String title, String description, String posesJson,
                        int totalDuration, String category, String difficulty, String focusArea) {
        this.dayNumber = dayNumber;
        this.title = title;
        this.description = description;
        this.posesJson = posesJson;
        this.totalDuration = totalDuration;
        this.category = category;
        this.difficulty = difficulty;
        this.focusArea = focusArea;
    }
}
