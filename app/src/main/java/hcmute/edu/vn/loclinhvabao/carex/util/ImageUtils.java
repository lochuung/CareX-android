package hcmute.edu.vn.loclinhvabao.carex.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;

import java.nio.ByteBuffer;

public class ImageUtils {
    public static ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        TensorImage tensorImage = new TensorImage(DataType.UINT8);
        tensorImage.load(bitmap);
        return tensorImage.getBuffer();
    }

    /**
     * Flips a bitmap horizontally.
     */
    public static Bitmap flipHorizontally(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.preScale(-1.0f, 1.0f);
        return Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * Rotates a bitmap by the given degrees.
     */
    public static Bitmap rotate(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * Resize bitmap to the given size.
     */
    public static Bitmap resize(Bitmap bitmap, int width, int height) {
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }
}
