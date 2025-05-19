/*
 * Copyright 2022 The TensorFlow Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hcmute.edu.vn.loclinhvabao.carex.data.local.model.pose_detect;

import lombok.Getter;

/**
 * Enum representing human body keypoints detected by pose estimation models.
 */
@Getter
public enum BodyPart {
    NOSE(0),
    LEFT_EYE(1),
    RIGHT_EYE(2),
    LEFT_EAR(3),
    RIGHT_EAR(4),
    LEFT_SHOULDER(5),
    RIGHT_SHOULDER(6),
    LEFT_ELBOW(7),
    RIGHT_ELBOW(8),
    LEFT_WRIST(9),
    RIGHT_WRIST(10),
    LEFT_HIP(11),
    RIGHT_HIP(12),
    LEFT_KNEE(13),
    RIGHT_KNEE(14),
    LEFT_ANKLE(15),
    RIGHT_ANKLE(16);

    private final int value;

    BodyPart(int value) {
        this.value = value;
    }

    public static BodyPart fromValue(int value) {
        for (BodyPart bodyPart : BodyPart.values()) {
            if (bodyPart.value == value) {
                return bodyPart;
            }
        }
        return null;
    }
}
