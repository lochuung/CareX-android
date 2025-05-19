package hcmute.edu.vn.loclinhvabao.carex.ui.yoga.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

/**
 * Handles permission requests for the application.
 * Follows the Single Responsibility Principle by encapsulating permission management.
 */
public class PermissionHandler {
    private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;
    
    private final AppCompatActivity activity;
    private final PermissionCallback permissionCallback;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    
    public interface PermissionCallback {
        void onPermissionGranted();
        void onPermissionDenied();
    }
    
    public PermissionHandler(AppCompatActivity activity, PermissionCallback permissionCallback) {
        this.activity = activity;
        this.permissionCallback = permissionCallback;
        this.requestPermissionLauncher = registerPermissionLauncher();
    }
    
    /**
     * Registers the permission request callback.
     */
    private ActivityResultLauncher<String> registerPermissionLauncher() {
        return activity.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        permissionCallback.onPermissionGranted();
                    } else {
                        permissionCallback.onPermissionDenied();
                    }
                });
    }
    
    /**
     * Checks if the camera permission is granted.
     */
    public boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(activity, CAMERA_PERMISSION) 
                == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Requests the camera permission if not already granted.
     * @return true if permission is already granted, false if a request was made
     */
    public boolean requestCameraPermissionIfNeeded() {
        if (hasCameraPermission()) {
            return true;
        } else {
            requestPermissionLauncher.launch(CAMERA_PERMISSION);
            return false;
        }
    }
}
