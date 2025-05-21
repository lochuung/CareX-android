package hcmute.edu.vn.loclinhvabao.carex.ui.yoga.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.fragment.app.Fragment;

public class PermissionHandler {
    private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;

    private final Context context;
    private final LifecycleOwner lifecycleOwner;
    private final PermissionCallback permissionCallback;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    public interface PermissionCallback {
        void onPermissionGranted();

        void onPermissionDenied();
    }

    public PermissionHandler(@NonNull Context context,
                             @NonNull LifecycleOwner lifecycleOwner,
                             @NonNull PermissionCallback callback) {
        this.context = context;
        this.lifecycleOwner = lifecycleOwner;
        this.permissionCallback = callback;
    }

    public void initialize(@NonNull Fragment fragment) {
        requestPermissionLauncher = fragment.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        permissionCallback.onPermissionGranted();
                    } else {
                        permissionCallback.onPermissionDenied();
                    }
                });
    }

    public boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(context, CAMERA_PERMISSION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public boolean requestCameraPermissionIfNeeded() {
        if (hasCameraPermission()) {
            return true;
        } else {
            requestPermissionLauncher.launch(CAMERA_PERMISSION);
            return false;
        }
    }
}
