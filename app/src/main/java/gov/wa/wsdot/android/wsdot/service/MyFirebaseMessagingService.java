package gov.wa.wsdot.android.wsdot.service;
/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Map;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.ui.home.HomeActivity;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.TrafficMapActivity;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String from = remoteMessage.getFrom();
        Map data = remoteMessage.getData();

        String title = remoteMessage.getNotification().getTitle();
        String message = remoteMessage.getNotification().getBody();

        String latitude = (String) data.get("latitude");
        String longitude = (String) data.get("longitude");
        int id = Integer.valueOf((String)data.get("id"));

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // retrieve the ids of last messages received (Stores up to the number of topics)
        JSONArray default_ids = new JSONArray();
        String ids_str = preferences.getString("fcm_ids", default_ids.toString());

        JSONArray ids = null;
        try {
            ids = new JSONArray(ids_str);

            if (!ids.toString().contains("\"" + String.valueOf(id) + "\"") ) {

                // message received from some topic.
                if (from.startsWith("/topics/")) {

                    // set up intent
                    Intent notificationIntent = new Intent(this, TrafficMapActivity.class);
                    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                    notificationIntent.putExtra("lat", Float.valueOf(latitude));
                    notificationIntent.putExtra("long", Float.valueOf(longitude));
                    notificationIntent.putExtra("zoom", 13);
                    notificationIntent.putExtra("forceUpdate", true);
                    notificationIntent.setAction("dummy_action" + id);

                    Intent upIntent = new Intent(this, HomeActivity.class);

                    // Use TaskStackBuilder to build the back stack and get the PendingIntent
                    PendingIntent pIntent =
                            TaskStackBuilder.create(this)
                                    // add all of DetailsActivity's parents to the stack,
                                    // followed by DetailsActivity itself
                                    .addNextIntentWithParentStack(upIntent)
                                    .addNextIntent(notificationIntent)
                                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                    createNotification(title, message, pIntent, id);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // [END receive_message]

    // Creates notification
    private void createNotification(String title, String body, PendingIntent intent, int id) {
        Context context = getApplicationContext();

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_list_wsdot).setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(body))
                .setContentIntent(intent)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS);

        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(id, mBuilder.build());
    }
}