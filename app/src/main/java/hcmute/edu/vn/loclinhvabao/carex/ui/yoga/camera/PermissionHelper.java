package hcmute.edu.vn.loclinhvabao.carex.ui.yoga.camera;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

/**
 * Helper class for handling permission-related operations.
 */
public class PermissionHelper {
    
    /**
     * Checks if the specified permission is granted.
     *
     * @param context Application context
     * @param permission The permission to check
     * @return True if the permission is granted, false otherwise
     */
    public static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED;
    }
}
