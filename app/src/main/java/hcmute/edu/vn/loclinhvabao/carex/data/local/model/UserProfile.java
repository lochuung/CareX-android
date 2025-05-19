package hcmute.edu.vn.loclinhvabao.carex.data.local.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    private String id;
    private String name;
    private float height; // in cm
    private float weight; // in kg
    private int age;
    private YogaGoal goal;
    private boolean notificationsEnabled;
    private String reminderTime; // Format: "HH:mm"
    private List<String> reminderDays; // "MONDAY", "WEDNESDAY", etc.
    private int currentStreak; // days
}