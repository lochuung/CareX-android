package hcmute.edu.vn.loclinhvabao.carex.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(tableName = "health_data")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthData {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String date; // Format: yyyy-MM-dd
    private int steps;
    private int calories;
    private float distance; // in kilometers

    // Workout specific fields
    private String workoutName;
    private String workoutType; // e.g., "running", "walking", "cycling"
    private int workoutDuration; // in minutes
    private String workoutTime; // Format: HH:mm
    private boolean isWorkout; // Flag to distinguish between daily stats and workout entries
    private String notes; // Optional note for the workout

    @NonNull
    @Override
    public String toString() {
        return "HealthData{" +
                "id=" + id +
                ", date='" + date + '\'' +
                ", steps=" + steps +
                ", calories=" + calories +
                ", distance=" + distance +
                ", workoutName='" + workoutName + '\'' +
                ", workoutType='" + workoutType + '\'' +
                ", workoutDuration=" + workoutDuration +
                ", workoutTime='" + workoutTime + '\'' +
                ", isWorkout=" + isWorkout +
                ", notes='" + notes + '\'' +
                '}';
    }
}