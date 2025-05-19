package hcmute.edu.vn.loclinhvabao.carex.ui.yoga.models;

import android.annotation.SuppressLint;

import java.io.Serializable;

import lombok.Getter;

@Getter
public class YogaPose implements Serializable {
    private final int id;
    private final String englishName;
    private final String sanskritName;
    private final String instructions;
    private final int durationInSeconds;
    private final String videoUrl;
    private final String imageUrl;
    
    public YogaPose(int id, String englishName, String sanskritName, String instructions, 
                   int durationInSeconds, String videoUrl, String imageUrl) {
        this.id = id;
        this.englishName = englishName;
        this.sanskritName = sanskritName;
        this.instructions = instructions;
        this.durationInSeconds = durationInSeconds;
        this.videoUrl = videoUrl;
        this.imageUrl = imageUrl;
    }

    @SuppressLint("DefaultLocale")
    public String getFormattedDuration() {
        int minutes = durationInSeconds / 60;
        int seconds = durationInSeconds % 60;
        
        if (minutes > 0) {
            return String.format("%d min %02d sec", minutes, seconds);
        } else {
            return String.format("%d seconds", seconds);
        }
    }
}
