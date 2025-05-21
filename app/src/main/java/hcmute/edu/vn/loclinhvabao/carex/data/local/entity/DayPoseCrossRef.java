package hcmute.edu.vn.loclinhvabao.carex.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(
    tableName = "day_pose_cross_ref",
    primaryKeys = {"dayNumber", "poseId"},
    foreignKeys = {
        @ForeignKey(
            entity = YogaDayEntity.class,
            parentColumns = "dayNumber",
            childColumns = "dayNumber",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = YogaPoseEntity.class,
            parentColumns = "id",
            childColumns = "poseId",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index(value = "dayNumber"),
        @Index(value = "poseId")
    }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DayPoseCrossRef {
    public int dayNumber;
    public int poseId;
    public int orderInDay; // Position of the pose in the sequence for that day
}
