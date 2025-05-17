package hcmute.edu.vn.loclinhvabao.carex.data.local.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YogaPose {
    private String id;
    private String name;
    private float score; // 0 to 10
    private int duration; // in seconds
    private String imageUrl;
}