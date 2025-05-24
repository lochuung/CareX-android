package hcmute.edu.vn.loclinhvabao.carex.util;

/**
 * Application-wide constants
 */
public class Constants {
    // Default user ID for local profile
    public static final String CURRENT_USER_ID = "local_user";
    public static final String DEFAULT_USER_ID = "default_user";
    public static final String DEFAULT_USER_NAME = "CareX User";
    
    // SharedPreferences keys
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_USER_NAME = "user_name";
    public static final String PREF_USER_EMAIL = "user_email";
    
    // Navigation
    public static final String KEY_SESSION_ID = "sessionId";
    
    // Default values
    public static final float DEFAULT_HEIGHT = 175.0f;
    public static final float DEFAULT_WEIGHT = 70.0f;
    public static final int DEFAULT_AGE = 30;
    
    private Constants() {
        // Private constructor to prevent instantiation
    }
}
