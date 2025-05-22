package hcmute.edu.vn.loclinhvabao.carex.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(tableName = "yoga_poses")
@Data
@Builder
@NoArgsConstructor
public class YogaPoseEntity {
    @PrimaryKey
    private int id;
    private String englishName;
    private String sanskritName;
    private String instructions;
    private int durationInSeconds;
    private String videoUrl;
    private String imageUrl;
    private String category; // e.g., "Standing", "Seated", "Balance", etc.
    private String difficulty; // e.g., "Beginner", "Intermediate", "Advanced"
    private String benefitsJson; // Store list of benefits as JSON string

    @Ignore
    public YogaPoseEntity(int id, String englishName, String sanskritName, String instructions,
                         int durationInSeconds, String videoUrl, String imageUrl, 
                         String category, String difficulty, String benefitsJson) {
        this.id = id;
        this.englishName = englishName;
        this.sanskritName = sanskritName;
        this.instructions = instructions;
        this.durationInSeconds = durationInSeconds;
        this.videoUrl = videoUrl;
        this.imageUrl = imageUrl;
        this.category = category;
        this.difficulty = difficulty;
        this.benefitsJson = benefitsJson;
    }
}
