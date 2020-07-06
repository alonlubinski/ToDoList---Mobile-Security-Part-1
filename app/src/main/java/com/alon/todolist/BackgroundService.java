package com.alon.todolist;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.alon.todolist.BuildConfig;
import com.alon.todolist.MainActivity;
import com.alon.todolist.R;
import com.alon.todolist.utils.models.Activity;
import com.alon.todolist.utils.models.Location;
import com.alon.todolist.utils.MySP;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BackgroundService extends Service {

    public static final String START_FOREGROUND_SERVICE = "START_FOREGROUND_SERVICE";
    public static final String PAUSE_FOREGROUND_SERVICE = "PAUSE_FOREGROUND_SERVICE";
    public static final String STOP_FOREGROUND_SERVICE = "STOP_FOREGROUND_SERVICE";
    private boolean isServiceRunningRightNow = false;
    private String activity = "", randomId, curTime;
    public static int NOTIFICATION_ID = 153;
    private int lastShownNotificationId = -1;
    public static String CHANNEL_ID = "com.alon.todolist.CHANNEL_ID_FOREGROUND";
    public static String MAIN_ACTION = "com.alon.todolist.backgroundservice.action.main";
    private NotificationCompat.Builder notificationBuilder;
    private FirebaseFirestore db;
    private Activity newActivity;
    private Location curLocation;
    private ActivityRecognitionClient activityRecognitionClient;
    private TransitionReceiver transitionReceiver;
    private PendingIntent pendingIntent;
    private final String TRANSITION_ACTION_RECEIVER =
            BuildConfig.APPLICATION_ID + ".TRANSITION_ACTION_RECEIVER";

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private final LocationCallback locationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            if (locationResult.getLastLocation() != null) {
                updateLiveLocationDB(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude(), activity);
                curLocation.setLat(locationResult.getLastLocation().getLatitude());
                curLocation.setLng(locationResult.getLastLocation().getLongitude());
            } else {
                Log.d("pttt", "Location information isn't available.");
            }
        }

        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);
            locationAvailability.isLocationAvailable();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        db = FirebaseFirestore.getInstance();
        newActivity = new Activity();
        curLocation = new Location();
        Intent intent = new Intent(TRANSITION_ACTION_RECEIVER);
        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        transitionReceiver = new TransitionReceiver();
        registerReceiver(transitionReceiver, new IntentFilter(TRANSITION_ACTION_RECEIVER));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getAction().equals(START_FOREGROUND_SERVICE)){
            if(isServiceRunningRightNow){
                return START_STICKY;
            }
            isServiceRunningRightNow = true;
            notifyToUserForForegroundService();
            startTracking();
            requestActivityDetection();

        } else if(intent.getAction().equals(PAUSE_FOREGROUND_SERVICE)){

        } else if(intent.getAction().equals(STOP_FOREGROUND_SERVICE)){
            stopForeground(true);
            stopSelf();
            isServiceRunningRightNow = false;
            return START_NOT_STICKY;
        }
        return START_STICKY;
    }

    // Method that starts tracking the user's location.
    private void startTracking() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            locationRequest = new LocationRequest();
            locationRequest.setSmallestDisplacement(1.0f);
            locationRequest.setInterval(3000);
            locationRequest.setFastestInterval(3000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void notifyToUserForForegroundService() {
        // On notification click
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder = getNotificationBuilder(this,
                CHANNEL_ID,
                NotificationManagerCompat.IMPORTANCE_LOW); //Low importance prevent visual appearance for this notification channel on top

        notificationBuilder.setContentIntent(pendingIntent) // Open activity
                .setOngoing(true)
//                .setSmallIcon(R.drawable.ic_sattelite)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                .setContentTitle("App in progress")
                .setContentText("Content");

        Notification notification = notificationBuilder.build();

        startForeground(NOTIFICATION_ID, notification);

        if (NOTIFICATION_ID != lastShownNotificationId) {
            // Cancel previous notification
            final NotificationManager notificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
            notificationManager.cancel(lastShownNotificationId);
        }
        lastShownNotificationId = NOTIFICATION_ID;
    }

    public static NotificationCompat.Builder getNotificationBuilder(Context context, String channelId, int importance) {
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            prepareChannel(context, channelId, importance);
            builder = new NotificationCompat.Builder(context, channelId);
        } else {
            builder = new NotificationCompat.Builder(context);
        }
        return builder;
    }

    @TargetApi(26)
    private static void prepareChannel(Context context, String id, int importance) {
        final String appName = context.getString(R.string.app_name);
        String notifications_channel_description = "ToDoList channel";
        String description = notifications_channel_description;
        final NotificationManager nm = (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);

        if(nm != null) {
            NotificationChannel nChannel = nm.getNotificationChannel(id);

            if (nChannel == null) {
                nChannel = new NotificationChannel(id, appName, importance);
                nChannel.setDescription(description);

                // from another answer
                nChannel.enableLights(true);
                nChannel.setLightColor(Color.BLUE);

                nm.createNotificationChannel(nChannel);
            }
        }
    }

    // Method that updates the live doc in db.
    private void updateLiveLocationDB(Double lat, Double lng, String activity){
        String phone = MySP.getInstance().getString("Phone", "");
        Map<String, Object> docData = new HashMap<>();
        docData.put("lat", lat);
        docData.put("lng", lng);
        docData.put("activity", activity);
        db.collection("locations").
                document(phone).
                collection("data").
                document("live").
                set(docData).
                addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    // Method that updates history doc in db.
    private void updateHistoryDB(){
        String phone = MySP.getInstance().getString("Phone", "");
        String curDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        Map<String, Object> docData = new HashMap<>();
        docData.put("type", newActivity.getType());
        docData.put("timestampStart", newActivity.getTimestampStart());
        if(newActivity.getTimestampEnd() != null){
            docData.put("timestampEnd", newActivity.getTimestampEnd());
        } else {
            docData.put("timestampEnd", null);
        }
        if(newActivity.getType().equals("STILL")){
            docData.put("route", curLocation);
        }
        db.collection("locations")
                .document(phone)
                .collection("history")
                .document(curDate)
                .collection("activities")
                .document(randomId)
                .set(docData, SetOptions.merge());
    }

    // Method that updates route in history in db.
    private void updateLocationArrayHistoryDB(){
        String phone = MySP.getInstance().getString("Phone", "");
        String curDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        DocumentReference documentReference = db.collection("locations")
                .document(phone)
                .collection("history")
                .document(curDate)
                .collection("activities")
                .document(randomId);
        documentReference.update("route", FieldValue.arrayUnion(curLocation));
    }

    // Method that registers activity detection listener.
    private void requestActivityDetection(){
        activityRecognitionClient = new ActivityRecognitionClient(this);
        // Register for Transitions Updates.
        Task<Void> task =
                activityRecognitionClient
                        .requestActivityUpdates(5000, pendingIntent);


        task.addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Log.d("pttt", "Transitions Api was successfully registered.");

                    }
                });
        task.addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("pttt", "Transitions Api was not successfully registered.");

                    }
                });
    }


    public class TransitionReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(ActivityRecognitionResult.hasResult(intent)){
                ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
                for(int i = 0; i < result.getProbableActivities().size(); i++){
                    if(result.getProbableActivities().get(i).getConfidence() > 80 && !toActivityString(result.getProbableActivities().get(i).getType()).equals("UNKNOWN")) {
                        // If the activity have confidence higher then 80 (confidence is between 0-100) and not unknown activity, then:
                        if (!activity.equals(toActivityString(result.getProbableActivities().get(i).getType())) && !activity.equals("")) {
                            // If activity change detected and it's not the first one.
                            curTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                            newActivity.setTimestampEnd(curTime);
                            updateHistoryDB();
                            if(!newActivity.getType().equals("STILL")){
                                updateActivityDB(newActivity, randomId);
                            }
                            activity = toActivityString(result.getProbableActivities().get(i).getType());
                            newActivity = new Activity();
                            curTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                            newActivity.setTimestampStart(curTime);
                            newActivity.setType(activity);
                            generateRandomId();
                            newActivity.setId(randomId);
                            newActivity.addLocationToArrayList(curLocation);
                            updateHistoryDB();
                        } else if(!activity.equals(toActivityString(result.getProbableActivities().get(i).getType()))){
                            // If activity change detected and it's the first one.
                            activity = toActivityString(result.getProbableActivities().get(i).getType());
                            curTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                            newActivity.setTimestampStart(curTime);
                            newActivity.setType(activity);
                            generateRandomId();
                            newActivity.setId(randomId);
                            newActivity.addLocationToArrayList(curLocation);
                            updateHistoryDB();
                        } else if(!toActivityString(result.getProbableActivities().get(i).getType()).equals("STILL")){
                            // If no activity change detected and it's not "still" activity.
                            newActivity.addLocationToArrayList(curLocation);
                            updateLocationArrayHistoryDB();
                        }
                    }
                }
            }
        }
    }

    // Method that updates activity collection by type.
    private void updateActivityDB(final Activity activity, String historyId) {
        String phone = MySP.getInstance().getString("Phone", "");
        String curDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        db.collection("locations")
                .document(phone)
                .collection("history")
                .document(curDate)
                .collection("activities")
                .document(historyId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        CollectionReference collectionReference = db.collection(activity.getType());
                        String id = collectionReference.document().getId();
                        collectionReference.document(id).set(task.getResult().getData());
                    }
                });


    }

    // Method that coverts activity to string.
    private static String toActivityString(int activity) {
        switch (activity) {
            case DetectedActivity.STILL:
                return "STILL";
            case DetectedActivity.WALKING:
                return "WALKING";
            case DetectedActivity.RUNNING:
                return "RUNNING";
            case DetectedActivity.IN_VEHICLE:
                return "IN_VEHICLE";
            default:
                return "UNKNOWN";
        }
    }


    // Method that generates random firebase doc id.
    private void generateRandomId(){
        String phone = MySP.getInstance().getString("Phone", "");
        CollectionReference collectionReference = db.collection("locations").document(phone).collection("history");
        randomId = collectionReference.document().getId();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }



}
