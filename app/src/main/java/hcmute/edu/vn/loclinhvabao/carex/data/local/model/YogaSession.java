package hcmute.edu.vn.loclinhvabao.carex.data.local.model;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YogaSession {
    private String id;
    private Date date;
    private String title;
    private String type; // yoga, meditation, etc.
    private int duration; // in minutes
    private int calories;
    private List<YogaPose> poses;
    private float completionRate; // 0 to 1
    private float overallScore; // 0 to 10
    private String notes;
}