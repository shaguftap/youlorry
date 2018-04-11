package com.youlorryintracity.NotificationClasses;

import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.youlorryintracity.ExtraaClasses.Global;
import com.youlorryintracity.Home;


import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by hp on 1/9/2018.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    String NewMessage="",imageUrl;

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    private NotificationUtils notificationUtils;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage == null)
            return;

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
                    NewMessage=remoteMessage.getNotification().getBody();
                    Global.TRIP_Status=NewMessage;
                    handleNotification(remoteMessage.getNotification().getBody());
        }

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Data Payload: " + remoteMessage.getData().toString());

            try {
                JSONObject json = new JSONObject(remoteMessage.getData());
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                // Vibrate for 500 milliseconds
                v.vibrate(500);
                handleDataMessage(json);
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }
    }

    private void handleNotification(String message) {
        if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
            // app is in foreground, broadcast the push message
           /* Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
            pushNotification.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
*/ // play notification sound
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 500 milliseconds
            v.vibrate(500);
            NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
            notificationUtils.playNotificationSound();
        }else{

            // If the app is in background, firebase itself handles the notification
        }
    }

    private void handleDataMessage(JSONObject json) {
        Log.e(TAG, "push json: " + json.toString());

        try {
            JSONObject data = new JSONObject(String.valueOf(json));
            if (NewMessage.equals("Truck Reached Destination")){

                String TotalFare = data.getString("total_fare");
                String distance_fare = data.getString("base_fare");
                String Distance = data.getString("distance");
                Global.RideFare=TotalFare;
                Global.Distance=Distance;
                Global.Base_Fare=distance_fare;
                Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                pushNotification.putExtra("message", NewMessage);
                LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

            }else if (NewMessage.equals("Driver is On the way!")){
                Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                pushNotification.putExtra("message", NewMessage);
                LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
                imageUrl = data.getString("image");
                String latitude = data.getString("latitude_longitude");
                String DriverName = data.getString("name");
                String DriverNumber = data.getString("contact");
             //   String longitude = data.getString("longitude");
                String quotation_id = data.getString("quote_ID");
                String[] s = latitude.split(",");
               Double lat = Double.valueOf(s[0]);
               Double  lang = Double.valueOf(s[1]);
                Global.DriverName=DriverName;
                Global.Driver_Contact=DriverNumber;
                Global.Quotation_ID=quotation_id;
                Global.Driver_Lat= lat;
                Global.Driver_Lang= lang;

            }


            if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
                // app is in foreground, broadcast the push message
                Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                pushNotification.putExtra("message", NewMessage);
                LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

                // play notification sound
                NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
                notificationUtils.playNotificationSound();
            } else {
                // app is in background, show the notification in notification tray
                Intent resultIntent = new Intent(getApplicationContext(), Home.class);
                resultIntent.putExtra("message", NewMessage);

                // check for image attachment
                if (TextUtils.isEmpty(imageUrl)) {
                    showNotificationMessage(getApplicationContext(), "INTRA CITY", NewMessage, "3000", resultIntent);
                } else {
                    // image is present, show notification with image
                    showNotificationMessageWithBigImage(getApplicationContext(), "INTRA CITY", NewMessage, "3000", resultIntent, imageUrl);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Json Exception: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    /**
     * Showing notification with text only
     */
    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent);
    }

    /**
     * Showing notification with text and image
     */
    private void showNotificationMessageWithBigImage(Context context, String title, String message, String timeStamp, Intent intent, String imageUrl) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent, imageUrl);
    }
}