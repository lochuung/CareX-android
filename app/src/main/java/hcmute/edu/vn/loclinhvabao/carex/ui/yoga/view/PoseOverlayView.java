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

package hcmute.edu.vn.loclinhvabao.carex.ui.yoga.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hcmute.edu.vn.loclinhvabao.carex.data.local.model.pose_detect.BodyPart;
import hcmute.edu.vn.loclinhvabao.carex.data.local.model.pose_detect.KeyPoint;

/**
 * A view that draws the detected pose on top of the camera feed.
 */
public class PoseOverlayView extends View {

    private static final int LINE_WIDTH = 8; // Width of the lines connecting keypoints
    private static final int POINT_SIZE = 10; // Size of the keypoints

    // Define connections between keypoints for drawing the skeleton
    private static final Map<Pair<BodyPart, BodyPart>, Integer> KEYPOINT_CONNECTIONS = new HashMap<>();

    static {
        // Define connections based on KEYPOINT_EDGE_INDS_TO_COLOR
        // 👤 Face connections
        KEYPOINT_CONNECTIONS.put(new Pair<>(BodyPart.NOSE, BodyPart.LEFT_EYE), Color.RED);
        KEYPOINT_CONNECTIONS.put(new Pair<>(BodyPart.NOSE, BodyPart.RIGHT_EYE), Color.RED);
        KEYPOINT_CONNECTIONS.put(new Pair<>(BodyPart.LEFT_EYE, BodyPart.LEFT_EAR), Color.RED);
        KEYPOINT_CONNECTIONS.put(new Pair<>(BodyPart.RIGHT_EYE, BodyPart.RIGHT_EAR), Color.RED);

        // 🫁 Shoulders & Torso
        KEYPOINT_CONNECTIONS.put(new Pair<>(BodyPart.NOSE, BodyPart.LEFT_SHOULDER), Color.RED);
        KEYPOINT_CONNECTIONS.put(new Pair<>(BodyPart.NOSE, BodyPart.RIGHT_SHOULDER), Color.RED);
        KEYPOINT_CONNECTIONS.put(new Pair<>(BodyPart.LEFT_SHOULDER, BodyPart.RIGHT_SHOULDER), Color.RED);
        KEYPOINT_CONNECTIONS.put(new Pair<>(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_HIP), Color.RED);
        KEYPOINT_CONNECTIONS.put(new Pair<>(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_HIP), Color.RED);
        KEYPOINT_CONNECTIONS.put(new Pair<>(BodyPart.LEFT_HIP, BodyPart.RIGHT_HIP), Color.RED);

        // 💪 Arms
        KEYPOINT_CONNECTIONS.put(new Pair<>(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_ELBOW), Color.RED);
        KEYPOINT_CONNECTIONS.put(new Pair<>(BodyPart.LEFT_ELBOW, BodyPart.LEFT_WRIST), Color.RED);
        KEYPOINT_CONNECTIONS.put(new Pair<>(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_ELBOW), Color.RED);
        KEYPOINT_CONNECTIONS.put(new Pair<>(BodyPart.RIGHT_ELBOW, BodyPart.RIGHT_WRIST), Color.RED);

        // 🦵 Legs
        KEYPOINT_CONNECTIONS.put(new Pair<>(BodyPart.LEFT_HIP, BodyPart.LEFT_KNEE), Color.RED);
        KEYPOINT_CONNECTIONS.put(new Pair<>(BodyPart.LEFT_KNEE, BodyPart.LEFT_ANKLE), Color.RED);
        KEYPOINT_CONNECTIONS.put(new Pair<>(BodyPart.RIGHT_HIP, BodyPart.RIGHT_KNEE), Color.RED);
        KEYPOINT_CONNECTIONS.put(new Pair<>(BodyPart.RIGHT_KNEE, BodyPart.RIGHT_ANKLE), Color.RED);

    }

    private final Paint linePaint = new Paint();
    private final Paint pointPaint = new Paint();
    private final Paint textPaint = new Paint();
    private final List<KeyPoint> keyPoints = new ArrayList<>();
    private final float MIN_CONFIDENCE = 0.25f;

    public PoseOverlayView(Context context) {
        super(context);
        init();
    }

    public PoseOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Set up line paint
        linePaint.setStrokeWidth(LINE_WIDTH);
        linePaint.setStyle(Paint.Style.STROKE);

        // Set up point paint
        pointPaint.setColor(Color.YELLOW);
        pointPaint.setStyle(Paint.Style.FILL);

        // Set up text paint for debugging
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(30);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
    }

    public void setKeyPoints(List<KeyPoint> keyPoints) {
        this.keyPoints.clear();
        this.keyPoints.addAll(keyPoints);
        invalidate(); // Request redraw
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        if (keyPoints.isEmpty()) {
            return;
        }

        // Calculate scale factors to map keypoints to view dimensions
        float scaleX = getWidth();
        float scaleY = getHeight();

        // Draw connections between keypoints
        for (Map.Entry<Pair<BodyPart, BodyPart>, Integer> connection : KEYPOINT_CONNECTIONS.entrySet()) {
            BodyPart from = connection.getKey().first;
            BodyPart to = connection.getKey().second;
            int color = connection.getValue();

            KeyPoint fromKeyPoint = getKeyPointByBodyPart(from);
            KeyPoint toKeyPoint = getKeyPointByBodyPart(to);

            if (fromKeyPoint != null && toKeyPoint != null &&
                    fromKeyPoint.getScore() > MIN_CONFIDENCE &&
                    toKeyPoint.getScore() > MIN_CONFIDENCE) {

                PointF fromPoint = fromKeyPoint.getCoordinate();
                PointF toPoint = toKeyPoint.getCoordinate();

                // Mirror x-coordinates if using front camera
                float fromX = fromPoint.x * scaleX;
                float toX = toPoint.x * scaleX;

                linePaint.setColor(color);
                canvas.drawLine(
                        fromX,
                        fromPoint.y * scaleY,
                        toX,
                        toPoint.y * scaleY,
                        linePaint);
            }
        }

        // Draw keypoints
        for (KeyPoint keyPoint : keyPoints) {
            if (keyPoint.getScore() > MIN_CONFIDENCE) {
                PointF point = keyPoint.getCoordinate();

                float x = point.x * scaleX;

                // Use different colors for different keypoint categories
                int pointColor = Color.YELLOW;
                BodyPart bodyPart = keyPoint.getBodyPart();

                // Choose color based on body part
                if (bodyPart == BodyPart.LEFT_WRIST || bodyPart == BodyPart.LEFT_ELBOW ||
                        bodyPart == BodyPart.LEFT_SHOULDER || bodyPart == BodyPart.LEFT_HIP ||
                        bodyPart == BodyPart.LEFT_KNEE || bodyPart == BodyPart.LEFT_ANKLE) {
                    pointColor = Color.RED;
                } else if (bodyPart == BodyPart.RIGHT_WRIST || bodyPart == BodyPart.RIGHT_ELBOW ||
                        bodyPart == BodyPart.RIGHT_SHOULDER || bodyPart == BodyPart.RIGHT_HIP ||
                        bodyPart == BodyPart.RIGHT_KNEE || bodyPart == BodyPart.RIGHT_ANKLE) {
                    pointColor = Color.BLUE;
                }

                // Set paint color for this point
                pointPaint.setColor(pointColor);

                // Draw the keypoint
                canvas.drawCircle(
                        x,
                        point.y * scaleY,
                        POINT_SIZE,
                        pointPaint);

                // Optionally draw keypoint index for debugging
                // canvas.drawText(String.valueOf(bodyPart.ordinal()), x + POINT_SIZE, point.y * scaleY, textPaint);
            }
        }
    }

    private KeyPoint getKeyPointByBodyPart(BodyPart bodyPart) {
        for (KeyPoint keyPoint : keyPoints) {
            if (keyPoint.getBodyPart() == bodyPart) {
                return keyPoint;
            }
        }
        return null;
    }

    /**
     * Simple pair class for body part connections
     */
    private static class Pair<F, S> {
        public final F first;
        public final S second;

        public Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }
    }
}
