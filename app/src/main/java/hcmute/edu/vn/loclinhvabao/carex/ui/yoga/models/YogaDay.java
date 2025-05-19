package hcmute.edu.vn.loclinhvabao.carex.ui.yoga.models;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;

@Getter
public class YogaDay implements Serializable {
    private final int dayNumber;
    private final String title;
    private final String description;
    private final List<YogaPose> poses;
    
    public YogaDay(int dayNumber, String title, String description, List<YogaPose> poses) {
        this.dayNumber = dayNumber;
        this.title = title;
        this.description = description;
        this.poses = poses;
    }

    public int getTotalDuration() {
        int duration = 0;
        for (YogaPose pose : poses) {
            duration += pose.getDurationInSeconds();
        }
        return duration;
    }
    
    public int getTotalPoses() {
        return poses.size();
    }
}
