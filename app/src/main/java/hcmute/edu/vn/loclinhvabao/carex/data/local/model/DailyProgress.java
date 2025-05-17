package hcmute.edu.vn.loclinhvabao.carex.data.local.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyProgress {
    private Date date;
    private int duration;
    private int calories;
    private float score;
}