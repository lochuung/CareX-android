package hcmute.edu.vn.loclinhvabao.carex.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(tableName = "user_profiles")
@Data
@Builder
@NoArgsConstructor
public class UserProfileEntity {
    @PrimaryKey
    @NonNull
    private String id;
    private String name;
    private float height;
    private float weight;
    private int age;
    private String goal; // Store enum as string
    private boolean notificationsEnabled;
    private String reminderTime;
    private String reminderDaysJson; // Store list as JSON
    private int currentStreak;

    @Ignore
    public UserProfileEntity(@NonNull String id, String name, float height, float weight,
                             int age, String goal, boolean notificationsEnabled,
                             String reminderTime, String reminderDaysJson, int currentStreak) {
        this.id = id;
        this.name = name;
        this.height = height;
        this.weight = weight;
        this.age = age;
        this.goal = goal;
        this.notificationsEnabled = notificationsEnabled;
        this.reminderTime = reminderTime;
        this.reminderDaysJson = reminderDaysJson;
        this.currentStreak = currentStreak;
    }
}