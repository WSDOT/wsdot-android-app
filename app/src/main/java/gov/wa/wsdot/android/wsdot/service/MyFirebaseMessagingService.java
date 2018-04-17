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
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.Map;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.ui.alert.detail.HighwayAlertDetailsActivity;
import gov.wa.wsdot.android.wsdot.ui.ferries.schedules.bulletins.FerriesRouteAlertsBulletinDetailsActivity;
import gov.wa.wsdot.android.wsdot.util.Utils;

import static gov.wa.wsdot.android.wsdot.util.MyNotificationManager.ALERT_CHANNEL_ID;

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

        Log.e(TAG, "onMessageReceived");

        Map data = remoteMessage.getData();

        String title = "no title";
        String message = "no text";

        if (data.get("title") != null) {
            title = data.get("title").toString();
        }

        if (data.get("message") != null) {
            message = data.get("message").toString();
        }

        if (data.get("push_alert_id") != null) {

            int id = Integer.valueOf(data.get("push_alert_id").toString());

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

            ArrayList<Integer> receivedAlerts = Utils.loadOrderedIntList("KEY_RECEIVED_ALERTS", settings);

            if (receivedAlerts != null) {
                if (!receivedAlerts.contains(id)) {
                    createAlert(id, title, message, data);
                }  // if we've already seen this alert, do nothing
            } else {
                createAlert(id, title, message, data);
                receivedAlerts = new ArrayList<>();
            }

            if (!receivedAlerts.contains(id)) {
                receivedAlerts.add(id);
            }

            // only save the ID's of the last 20 notifications
            if (receivedAlerts.size() > 20) {
                receivedAlerts.remove(0);
            }

            Utils.saveOrderedList(receivedAlerts, "KEY_RECEIVED_ALERTS", settings);

        }
    }

    // Calls the appropriate alert constructor function based on the alert type.
    private void createAlert(int id, String title, String message, Map data){

        String alertType = data.get("type").toString();

        if (alertType.equals("ferry_alert")) {
            startFerriesBulletinActivity(id, title, message, data);
        } else if (alertType.equals("highway_alert")) {
            startHighwayAlertDetailsActivity(id, title, message, data);
        }
    }

    private void startHighwayAlertDetailsActivity(int id, String title, String message, Map data) {

        // Create an Intent for the activity you want to start
        Intent resultIntent = new Intent(this, HighwayAlertDetailsActivity.class);
        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        Bundle b1 = new Bundle();
        b1.putInt("id", Integer.valueOf(data.get("alert_id").toString()));
        b1.putBoolean("refresh", true);
        resultIntent.putExtras(b1);
        resultIntent.setAction("actionstring" + System.currentTimeMillis());
        stackBuilder.addNextIntentWithParentStack(resultIntent);

        Bundle b2 = new Bundle();

        if ((data.get("lat") != null && data.get("long") != null)) {

            b2.putDouble("lat", Double.valueOf(data.get("lat").toString()));
            b2.putDouble("long", Double.valueOf(data.get("long").toString()));
            b2.putInt("zoom", 15);

            Log.e(TAG, data.get("lat").toString());
            Log.e(TAG, data.get("long").toString());

        } else {
            Log.e(TAG, "no data for bundle");
        }

        // set extras for the FerriesRouteAlertsBulletinsFragment
        stackBuilder.editIntentAt(stackBuilder.getIntentCount() - 2).putExtras(b2);

        // Get the PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_list_wsdot).setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(id, builder.build());
    }

    /*
      Sets up a deep link to the Ferry bulletin details fragment with
      a back stack set in the Manifest file.
     */
    private void startFerriesBulletinActivity(int id, String title, String message, Map data) {

        // Create an Intent for the activity you want to start
        Intent resultIntent = new Intent(this, FerriesRouteAlertsBulletinDetailsActivity.class);
        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        Bundle b1 = new Bundle();
        b1.putInt("routeId", Integer.valueOf(data.get("route_id").toString()));
        b1.putInt("alertId", Integer.valueOf(data.get("alert_id").toString()));
        b1.putString("AlertFullTitle", title);

        resultIntent.putExtras(b1);
        resultIntent.setAction("actionstring" + System.currentTimeMillis());
        stackBuilder.addNextIntentWithParentStack(resultIntent);

        Bundle b2 = new Bundle();
        b2.putString("title", data.get("route_title").toString());
        b2.putInt("routeId", Integer.valueOf(data.get("route_id").toString()));

        // set extras for the FerriesRouteAlertsBulletinsFragment
        stackBuilder.editIntentAt(stackBuilder.getIntentCount() - 2).putExtras(b2);

        // Get the PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_list_wsdot).setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(id, builder.build());

    }


}