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

import android.graphics.PointF;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * A class to hold information about a detected person and their keypoints.
 */
@Getter
public class Person {
    private final List<KeyPoint> keyPoints;
    private final RectF boundingBox;
    private final float score;
    @Setter
    private Integer id;

    public Person(List<KeyPoint> keyPoints, RectF boundingBox, float score) {
        this.keyPoints = keyPoints;
        this.boundingBox = boundingBox;
        this.score = score;
    }

    public Person(List<KeyPoint> keyPoints, RectF boundingBox, float score, Integer id) {
        this.keyPoints = keyPoints;
        this.boundingBox = boundingBox;
        this.score = score;
        this.id = id;
    }

    public KeyPoint getKeyPoint(BodyPart bodyPart) {
        for (KeyPoint keyPoint : keyPoints) {
            if (keyPoint.getBodyPart() == bodyPart) {
                return keyPoint;
            }
        }
        return null;
    }

    /**
     * Create a Person object from the MoveNet model output
     */
    public static Person fromKeyPointsWithScores(
            float[][][] keypointsWithScores,
            int imageHeight,
            int imageWidth,
            float keypointScoreThreshold) {

        // Single pose detection - only using the first person detected
        float[][] scores = keypointsWithScores[0];

        List<KeyPoint> keyPoints = new ArrayList<>();
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        float maxY = Float.MIN_VALUE;

        for (int i = 0; i < scores.length; i++) {
            // MoveNet model returns [y, x, confidence] for each keypoint
            // Note: The order is IMPORTANT - y is first, then x, then confidence
            float y = scores[i][0]; // First value is y
            float x = scores[i][1]; // Second value is x
            float score = scores[i][2]; // Third value is confidence

            // Check for invalid values
            if (Float.isNaN(x) || Float.isNaN(y) || Float.isNaN(score) ||
                    Float.isInfinite(x) || Float.isInfinite(y) || Float.isInfinite(score)) {
                // Use default values for invalid keypoints
                x = 0.5f;
                y = 0.5f;
                score = 0.0f;
            }

            // For visualization in the PoseOverlayView, we need normalized coordinates (0-1)
            // The model already returns normalized coordinates, so we don't need to convert
            keyPoints.add(new KeyPoint(
                    BodyPart.fromValue(i),
                    new PointF(x, y), // Keep coordinates as normalized (0-1)
                    score));

            // Update bounding box using normalized coordinates (0-1)
            if (score >= keypointScoreThreshold) {
                if (x < minX) minX = x;
                if (y < minY) minY = y;
                if (x > maxX) maxX = x;
                if (y > maxY) maxY = y;
            }
        }

        // Calculate bounding box in pixels
        RectF boundingBox = new RectF(
                minX * imageWidth,
                minY * imageHeight,
                maxX * imageWidth,
                maxY * imageHeight);

        // Calculate person score by averaging keypoint scores
        float personScore = 0;
        int countAboveThreshold = 0;
        for (float[] floats : scores) {
            if (floats[2] > keypointScoreThreshold) {
                personScore += floats[2];
                countAboveThreshold++;
            }
        }
        personScore = countAboveThreshold > 0 ? personScore / countAboveThreshold : 0;

        return new Person(keyPoints, boundingBox, personScore);
    }
}
