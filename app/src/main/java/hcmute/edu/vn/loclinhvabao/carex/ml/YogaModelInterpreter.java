package hcmute.edu.vn.loclinhvabao.carex.ml;

import android.content.Context;
import android.util.Log;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.InterpreterApi;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class responsible for running yoga pose classification model.
 */
public class YogaModelInterpreter implements Closeable {
    private static final String TAG = "YogaModelInterpreter";
    
    // Yoga classification model
    private static final String YOGA_MODEL_PATH = "yoga_model.tflite";
    private static final String[] YOGA_CLASS_NAMES = {
            "chair", "cobra", "dog", "no_pose", "shoudler_stand", "traingle", "tree", "warrior"
    };
      private final InterpreterApi yogaInterpreter;
    private final List<String> yogaPoseClasses;
    private final TensorBuffer yogaOutputBuffer;
    private final int maxResults;
    
    // Using Recognition record from YogaPoseClassifier
    
    /**
     * Creates a YogaModelInterpreter instance.
     *
     * @param context The application context
     * @param maxResults The maximum number of results to return
     * @param options The interpreter options to use
     * @return A new YogaModelInterpreter instance
     * @throws IOException If the model cannot be loaded
     */
    public static YogaModelInterpreter create(Context context, int maxResults, InterpreterApi.Options options) throws IOException {
        // Load Yoga classification model
        InterpreterApi yogaInterpreter =
                InterpreterApi.create(FileUtil.loadMappedFile(context, YOGA_MODEL_PATH), options);
        
        // Get output tensor info
        int[] outputShape = yogaInterpreter.getOutputTensor(0).shape();
        DataType outputDataType = yogaInterpreter.getOutputTensor(0).dataType();
        TensorBuffer outputBuffer = TensorBuffer.createFixedSize(outputShape, outputDataType);
        
        // Load class names
        List<String> classNames = new ArrayList<>(Arrays.asList(YOGA_CLASS_NAMES));
        
        return new YogaModelInterpreter(yogaInterpreter, classNames, outputBuffer, maxResults);
    }
    
    private YogaModelInterpreter(InterpreterApi yogaInterpreter, List<String> yogaPoseClasses, 
                               TensorBuffer yogaOutputBuffer, int maxResults) {
        this.yogaInterpreter = yogaInterpreter;
        this.yogaPoseClasses = yogaPoseClasses;
        this.yogaOutputBuffer = yogaOutputBuffer;
        this.maxResults = maxResults;
    }
    
    /**
     * Classify yoga pose based on normalized landmarks.
     *     * @param normalizedLandmarks The normalized landmarks to classify
     * @return A list of recognition results
     */
    public List<YogaPoseClassifier.Recognition> classifyYogaPose(float[] normalizedLandmarks) {
        // Validate input
        if (normalizedLandmarks == null) {
            Log.e(TAG, "Cannot classify pose: normalized landmarks are null");
            return new ArrayList<>();
        }

        // Prepare input tensor
        int[] inputShape = yogaInterpreter.getInputTensor(0).shape();
        DataType inputType = yogaInterpreter.getInputTensor(0).dataType();

        // Log input shape for debugging
        Log.d(TAG, "Yoga model input shape: " + Arrays.toString(inputShape));
        Log.d(TAG, "Normalized landmarks length: " + normalizedLandmarks.length);

        // Create a 1-dimensional tensor for the model input (34 floats)
        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(normalizedLandmarks.length * 4); 
        inputBuffer.order(ByteOrder.nativeOrder());

        // Fill the input buffer with all values
        for (float value : normalizedLandmarks) {
            inputBuffer.putFloat(value);
        }

        inputBuffer.rewind();

        try {
            // Run inference with the input
            yogaInterpreter.run(inputBuffer, yogaOutputBuffer.getBuffer());

            // Get results
            float[] outputs = yogaOutputBuffer.getFloatArray();

            // Validate outputs for debugging
            boolean hasValidOutput = false;
            for (float output : outputs) {
                if (output > 0) {
                    hasValidOutput = true;
                    break;
                }
            }

            if (!hasValidOutput) {
                Log.w(TAG, "All yoga model outputs are zero or negative");
            }

            Log.d(TAG, "Yoga model outputs: " + Arrays.toString(outputs));

            // Create recognition list
            Map<String, Float> resultMap = new HashMap<>();
            for (int i = 0; i < yogaPoseClasses.size() && i < outputs.length; i++) {
                resultMap.put(yogaPoseClasses.get(i), outputs[i]);
            }

            // Sort and get top results
            return getTopKProbability(resultMap, maxResults);
        } catch (Exception e) {
            Log.e(TAG, "Error running yoga classification", e);
            return new ArrayList<>();
        }
    }
      /**
     * Gets the top {@code maxResults} results.
     */
    private static List<YogaPoseClassifier.Recognition> getTopKProbability(
            Map<String, Float> labelProb, int maxResults) {
        // Create a list of recognitions
        List<YogaPoseClassifier.Recognition> recognitions = new ArrayList<>();

        // Convert to list and sort by probability (high to low)
        List<Map.Entry<String, Float>> entries = new ArrayList<>(labelProb.entrySet());
        entries.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        // Take top results
        int resultSize = Math.min(entries.size(), maxResults);
        for (int i = 0; i < resultSize; i++) {
            Map.Entry<String, Float> entry = entries.get(i);
            recognitions.add(new YogaPoseClassifier.Recognition(entry.getKey(), entry.getValue()));
        }

        return recognitions;
    }
    
    @Override
    public void close() {
        if (yogaInterpreter != null) {
            yogaInterpreter.close();
        }
    }
}
