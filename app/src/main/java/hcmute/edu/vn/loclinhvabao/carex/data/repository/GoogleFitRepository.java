package hcmute.edu.vn.loclinhvabao.carex.data.repository;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

import hcmute.edu.vn.loclinhvabao.carex.data.local.entity.HealthData;
import lombok.Getter;

@Singleton
public class GoogleFitRepository {
    private static final String TAG = "GoogleFitRepository";

    private final Context appContext;
    @Getter
    private final FitnessOptions fitnessOptions;
    private final MutableLiveData<Boolean> isConnected = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private GoogleSignInAccount googleSignInAccountCache = null;
    private OnDataPointListener stepListener;

    @Inject
    public GoogleFitRepository(Context appContext) {
        this.appContext = appContext;

        fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                // Adding activity recognition
                .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ)
                // Adding workout sessions
                .addDataType(DataType.TYPE_WORKOUT_EXERCISE, FitnessOptions.ACCESS_READ)
                .build();
    }

    public boolean hasOAuthPermission() {
        return GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(appContext),
                fitnessOptions);
    }

    public GoogleSignInAccount getGoogleAccount() {
        if (googleSignInAccountCache == null) {
            googleSignInAccountCache = GoogleSignIn.getAccountForExtension(appContext, fitnessOptions);
        }
        return googleSignInAccountCache;
    }

    public LiveData<Boolean> isConnected() {
        return isConnected;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /*
     * Reads today's step count
     */
    public LiveData<HealthData> readTodayData() {
        // build the request
        MutableLiveData<HealthData> result = new MutableLiveData<>();

        // Kiểm tra quyền
        if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            errorMessage.setValue("Activity recognition permission not granted");
            HealthData emptyData = createEmptyHealthData();
            result.setValue(emptyData);
            return result;
        }

        Calendar calendar = Calendar.getInstance();
        Date now = new Date();
        calendar.setTime(now);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long startTime = calendar.getTimeInMillis();

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
                .setTimeRange(startTime, now.getTime(), TimeUnit.MILLISECONDS)
                .bucketByTime(1, TimeUnit.DAYS)
                .build();

        // google account
        GoogleSignInAccount account = getGoogleAccount();
        if (account == null) {
            errorMessage.setValue("Google account not found");
            return result;
        }

        // send request
        Fitness.getHistoryClient(appContext, account)
                .readData(readRequest)
                .addOnSuccessListener(response -> {
                    HealthData healthData = new HealthData();

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    healthData.setDate(dateFormat.format(now));

                    healthData.setWorkout(false);
                    result.setValue(healthData);
                    if (response.getBuckets().isEmpty()) {
                        Log.i(TAG, "No data found");
                        return;
                    }

                    for (Bucket bucket : response.getBuckets()) {
                        for (DataSet dataSet : bucket.getDataSets()) {
                            dumpData(dataSet, healthData);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to read today's data", e);
                    errorMessage.setValue("Failed to read fitness data: " + e.getMessage());
                });

        // xử lý timeout
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            // Nếu result vẫn chưa được cập nhật sau 10 giây, trả về lỗi timeout
            if (result.getValue() == null) {
                errorMessage.setValue("Request timed out. Please check your connection.");
                // Tạo dữ liệu trống để ứng dụng không bị treo
                HealthData emptyData = createEmptyHealthData();
                result.setValue(emptyData);
            }
        }, 10000); // 10 giây timeout


        return result;
    }

    private HealthData createEmptyHealthData() {
        HealthData emptyData = new HealthData();
        emptyData.setDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        emptyData.setWorkout(false);
        emptyData.setSteps(0);
        emptyData.setCalories(0);
        emptyData.setDistance(0);
        return emptyData;
    }

    /*
     * Read workout sessions from Google Fit
     */
    public LiveData<List<HealthData>> readTodayWorkouts() {
        MutableLiveData<List<HealthData>> result = new MutableLiveData<>();
        List<HealthData> workouts = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        Date now = new Date();
        calendar.setTime(now);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long startTime = calendar.getTimeInMillis();

        GoogleSignInAccount account = getGoogleAccount();
        if (account == null) {
            errorMessage.setValue("Google account not found");
            result.setValue(workouts);
            return result;
        }

        // Create a session read request
        SessionReadRequest request = new SessionReadRequest.Builder()
                .setTimeInterval(startTime, now.getTime(), TimeUnit.MILLISECONDS)
                .read(DataType.TYPE_ACTIVITY_SEGMENT)
                .read(DataType.TYPE_CALORIES_EXPENDED)
                .read(DataType.TYPE_DISTANCE_DELTA)
                .read(DataType.TYPE_STEP_COUNT_DELTA)
                .readSessionsFromAllApps()
                .build();

        // Invoke the Sessions API to fetch workout sessions
        Fitness.getSessionsClient(appContext, account)
                .readSession(request)
                .addOnSuccessListener(response -> {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    String today = dateFormat.format(now);

                    for (Session session : response.getSessions()) {
                        // Create a new workout entry for each session
                        HealthData workout = new HealthData();
                        workout.setDate(today);
                        workout.setWorkout(true);
                        workout.setWorkoutName(session.getName());

                        // Determine workout type from activity type
                        String activityType = getActivityTypeFromSession(session);
                        workout.setWorkoutType(activityType);

                        // Set workout time from session start time
                        Date sessionStart = new Date(session.getStartTime(TimeUnit.MILLISECONDS));
                        workout.setWorkoutTime(timeFormat.format(sessionStart));

                        // Calculate duration in minutes
                        long durationMillis = session.getEndTime(TimeUnit.MILLISECONDS) -
                                session.getStartTime(TimeUnit.MILLISECONDS);
                        workout.setWorkoutDuration((int) (durationMillis / 60000)); // Convert to minutes

                        // Process the data sets for this session
                        for (DataSet dataSet : response.getDataSet(session)) {
                            processWorkoutDataSet(dataSet, workout);
                        }

                        workouts.add(workout);
                    }

                    result.setValue(workouts);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to read workout sessions", e);
                    errorMessage.setValue("Failed to read workout sessions: " + e.getMessage());
                    result.setValue(workouts); // Return empty list on error
                });

        return result;
    }

    public void subscribeToRealtimeUpdates(Consumer<HealthData> healthDataConsumer) {
        if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            errorMessage.setValue("Activity recognition permission not granted");
            return;
        }

        if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            errorMessage.setValue("Fine location permission not granted");
            return;
        }

        GoogleSignInAccount account = getGoogleAccount();
        if (account == null) {
            errorMessage.setValue("Google account not found");
            return;
        }

        Fitness.getRecordingClient(appContext, account)
                .subscribe(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(aVoid -> Log.i(TAG, "Subscribed to steps"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to subscribe to steps", e));

        Fitness.getRecordingClient(appContext, account)
                .subscribe(DataType.TYPE_CALORIES_EXPENDED)
                .addOnSuccessListener(aVoid -> Log.i(TAG, "Subscribed to calories"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to subscribe to calories", e));

        Fitness.getRecordingClient(appContext, account)
                .subscribe(DataType.TYPE_DISTANCE_DELTA)
                .addOnSuccessListener(aVoid -> Log.i(TAG, "Subscribed to distance"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to subscribe to distance", e));

        SensorRequest stepRequest = new SensorRequest.Builder()
                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .setSamplingRate(1, TimeUnit.SECONDS)
                .build();

        SensorRequest calorieRequest = new SensorRequest.Builder()
                .setDataType(DataType.TYPE_CALORIES_EXPENDED)
                .setSamplingRate(1, TimeUnit.SECONDS)
                .build();

        SensorRequest distanceRequest = new SensorRequest.Builder()
                .setDataType(DataType.TYPE_DISTANCE_DELTA)
                .setSamplingRate(1, TimeUnit.SECONDS)
                .build();

        stepListener = dataPoint -> {
            HealthData healthData = new HealthData();
            for (Field field : dataPoint.getDataType().getFields()) {
                if (field.equals(Field.FIELD_STEPS)) {
                    healthData.setSteps(dataPoint.getValue(field).asInt());
                } else if (field.equals(Field.FIELD_CALORIES)) {
                    healthData.setCalories((int) dataPoint.getValue(field).asFloat());
                } else if (field.equals(Field.FIELD_DISTANCE)) {
                    healthData.setDistance(dataPoint.getValue(field).asFloat() / 1000); // meters to km
                }
            }
            healthDataConsumer.accept(healthData);
        };

        Fitness.getSensorsClient(appContext, account)
                .add(stepRequest, stepListener)
                .addOnFailureListener(e -> Log.e(TAG, "Failed to register step sensor", e));

        Fitness.getSensorsClient(appContext, account)
                .add(calorieRequest, stepListener)
                .addOnFailureListener(e -> Log.e(TAG, "Failed to register calorie sensor", e));

        Fitness.getSensorsClient(appContext, account)
                .add(distanceRequest, stepListener)
                .addOnFailureListener(e -> Log.e(TAG, "Failed to register distance sensor", e));
    }


    public void unsubscribeFromRealtimeUpdates() {
        GoogleSignInAccount account = getGoogleAccount();
        if (account == null) {
            return;
        }

        if (stepListener != null) {
            Fitness.getSensorsClient(appContext, account).remove(stepListener)
                    .addOnSuccessListener(unused -> Log.i("StepRepo", "Sensor removed"))
                    .addOnFailureListener(e -> Log.e("StepRepo", "Remove failed", e));
        }

        // Hủy đăng ký từng loại dữ liệu
        Fitness.getRecordingClient(appContext, account)
                .unsubscribe(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(unused ->
                        Log.i(TAG, "Successfully unsubscribed from step count updates"))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to unsubscribe from step count", e));

        Fitness.getRecordingClient(appContext, account)
                .unsubscribe(DataType.TYPE_CALORIES_EXPENDED)
                .addOnSuccessListener(unused ->
                        Log.i(TAG, "Successfully unsubscribed from calories updates"))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to unsubscribe from calories", e));

        Fitness.getRecordingClient(appContext, account)
                .unsubscribe(DataType.TYPE_DISTANCE_DELTA)
                .addOnSuccessListener(unused ->
                        Log.i(TAG, "Successfully unsubscribed from distance updates"))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to unsubscribe from distance", e));
    }

    /**
     * Sign out from Google Fit
     */
    public void signOut() {
        // Clear the cached account
        googleSignInAccountCache = null;

        // Sign out from Google
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        GoogleSignIn.getClient(appContext, options).signOut()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Successfully signed out from Google Fit");
                    isConnected.postValue(false);
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to sign out from Google Fit", e));
    }

    private String getActivityTypeFromSession(Session session) {
        // Map Google Fit activity type to our app's workout types
        switch (session.getActivity()) {
            case FitnessActivities.RUNNING:
                return "running";
            case FitnessActivities.WALKING:
                return "walking";
            case FitnessActivities.BIKING:
                return "cycling";
            case FitnessActivities.HIGH_INTENSITY_INTERVAL_TRAINING:
                return "hiit";
            case FitnessActivities.GYMNASTICS:
                return "gym";
            default:
                return "other";
        }
    }

    private void processWorkoutDataSet(DataSet dataSet, HealthData workout) {
        for (DataPoint dp : dataSet.getDataPoints()) {
            for (Field field : dp.getDataType().getFields()) {
                if (dataSet.getDataType().equals(DataType.TYPE_STEP_COUNT_DELTA) &&
                        field.equals(Field.FIELD_STEPS)) {
                    workout.setSteps(dp.getValue(field).asInt());
                } else if (dataSet.getDataType().equals(DataType.TYPE_CALORIES_EXPENDED) &&
                        field.equals(Field.FIELD_CALORIES)) {
                    workout.setCalories((int) dp.getValue(field).asFloat());
                } else if (dataSet.getDataType().equals(DataType.TYPE_DISTANCE_DELTA) &&
                        field.equals(Field.FIELD_DISTANCE)) {
                    // Convert from meters to kilometers
                    workout.setDistance(dp.getValue(field).asFloat() / 1000);
                }
            }
        }
    }

    private void dumpData(DataSet dataSet, HealthData healthData) {
        for (DataPoint dp : dataSet.getDataPoints()) {
            for (Field field : dp.getDataType().getFields()) {
                if (dp.getDataType().equals(DataType.AGGREGATE_STEP_COUNT_DELTA) &&
                        field.equals(Field.FIELD_STEPS)) {
                    healthData.setSteps(dp.getValue(field).asInt());
                } else if (dp.getDataType().equals(DataType.AGGREGATE_CALORIES_EXPENDED) &&
                        field.equals(Field.FIELD_CALORIES)) {
                    healthData.setCalories((int) dp.getValue(field).asFloat());
                } else if (dp.getDataType().equals(DataType.AGGREGATE_DISTANCE_DELTA) &&
                        field.equals(Field.FIELD_DISTANCE)) {
                    // Convert from meters to kilometers
                    healthData.setDistance(dp.getValue(field).asFloat() / 1000);
                }
            }
        }
    }

}