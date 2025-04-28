package hcmute.edu.vn.healthtrackerapp.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.SensorsClient;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;

import java.util.concurrent.TimeUnit;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import hcmute.edu.vn.healthtrackerapp.R;
import hcmute.edu.vn.healthtrackerapp.model.Session;
import hcmute.edu.vn.healthtrackerapp.db.StorageHelper;

public class StepCounterService extends Service {
    public static final String ACTION_UPDATE = "com.example.ACTION_UPDATE";
    private static final String CHANNEL_ID = "step_counter_channel";

    private SensorsClient sensorsClient;
    private OnDataPointListener listener;
    private StorageHelper storageHelper;
    private int steps;
    private float distance;
    private float calories;
    private long startTime;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> timeUpdateFuture;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        android.util.Log.d("StepCounterService", "Service onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        android.util.Log.d("StepCounterService", "Service onCreate");
        storageHelper = new StorageHelper(this);
        steps = 0; distance = 0f; calories = 0f;
        startTime = System.currentTimeMillis();

        createNotificationChannel();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Activity Tracking")
                .setSmallIcon(R.drawable.ic_walk)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        updateNotification();
        startForeground(1, notificationBuilder.build());

        // Start time update scheduler
        scheduler = Executors.newSingleThreadScheduledExecutor();
        timeUpdateFuture = scheduler.scheduleWithFixedDelay(() -> {
            try {
                Intent intent = new Intent(ACTION_UPDATE);
                intent.putExtra("steps", steps);
                intent.putExtra("distance", distance);
                intent.putExtra("calories", calories);
                intent.putExtra("timeElapsed", System.currentTimeMillis() - startTime);
                LocalBroadcastManager.getInstance(StepCounterService.this).sendBroadcast(intent);
                updateNotification();
            } catch (Exception e) {
                android.util.Log.e("StepCounterService", "Error in time update: " + e.getMessage());
            }
        }, 0, 1, TimeUnit.SECONDS);

        try {
            FitnessOptions fitnessOptions = FitnessOptions.builder()
                    .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                    .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
                    .build();

            GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(this, fitnessOptions);
            android.util.Log.d("StepCounterService", "Google account: " + (account != null ? account.getEmail() : "null"));

            sensorsClient = Fitness.getSensorsClient(this, account);
            android.util.Log.d("StepCounterService", "Fitness client initialized");

            listener = new OnDataPointListener() {
                @Override
                public void onDataPoint(DataPoint dataPoint) {
                    try {
                        for (Field field : dataPoint.getDataType().getFields()) {
                            if (field.equals(Field.FIELD_STEPS)) {
                                int newSteps = dataPoint.getValue(field).asInt();
                                steps += newSteps;
                                android.util.Log.d("StepCounterService", "New steps: " + newSteps + ", Total: " + steps);
                            } else if (field.equals(Field.FIELD_DISTANCE)) {
                                float newDistance = dataPoint.getValue(field).asFloat();
                                distance += newDistance;
                                android.util.Log.d("StepCounterService", "New distance: " + newDistance + ", Total: " + distance);
                            } else if (field.equals(Field.FIELD_CALORIES)) {
                                float newCalories = dataPoint.getValue(field).asFloat();
                                calories += newCalories;
                                android.util.Log.d("StepCounterService", "New calories: " + newCalories + ", Total: " + calories);
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("StepCounterService", "Error processing data point: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            };

            // Register a single listener for all data types
            sensorsClient.add(new SensorRequest.Builder()
                    .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                    .setSamplingRate(1, TimeUnit.SECONDS)
                    .build(), listener);

            sensorsClient.add(new SensorRequest.Builder()
                    .setDataType(DataType.TYPE_DISTANCE_DELTA)
                    .setSamplingRate(1, TimeUnit.SECONDS)
                    .build(), listener);

            sensorsClient.add(new SensorRequest.Builder()
                    .setDataType(DataType.TYPE_CALORIES_EXPENDED)
                    .setSamplingRate(1, TimeUnit.SECONDS)
                    .build(), listener);
            android.util.Log.d("StepCounterService", "Sensor listener registered");
        } catch (Exception e) {
            android.util.Log.e("StepCounterService", "Error initializing service: " + e.getMessage());
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sensorsClient != null && listener != null) {
            sensorsClient.remove(listener);
        }
        if (timeUpdateFuture != null) {
            timeUpdateFuture.cancel(false);
        }
        if (scheduler != null) {
            scheduler.shutdown();
        }
        long endTime = System.currentTimeMillis();
        String date = new java.text.SimpleDateFormat("yyyy-MM-dd",
                java.util.Locale.getDefault()).format(new java.util.Date(startTime));
        Session session = new Session(startTime, endTime, steps, distance, calories);
        storageHelper.addSession(date, session);
        
        // Send final broadcast to update UI
        Intent intent = new Intent(ACTION_UPDATE);
        intent.putExtra("steps", steps);
        intent.putExtra("distance", distance);
        intent.putExtra("calories", calories);
        intent.putExtra("timeElapsed", endTime - startTime);
        intent.putExtra("session_completed", true); // Add flag to indicate session completion
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        
        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Activity Tracking", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Tracks activity in background");
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                    .createNotificationChannel(channel);
        }
    }

    private void updateNotification() {
        long elapsedTime = System.currentTimeMillis() - startTime;
        String timeText = formatTime(elapsedTime);
        
        notificationBuilder.setContentText(String.format(Locale.getDefault(),
            "Steps: %d | Time: %s", steps, timeText));
        
        notificationManager.notify(1, notificationBuilder.build());
    }

    private String formatTime(long millis) {
        int seconds = (int) (millis / 1000) % 60;
        int minutes = (int) ((millis / (1000 * 60)) % 60);
        int hours = (int) (millis / (1000 * 60 * 60));
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }
}