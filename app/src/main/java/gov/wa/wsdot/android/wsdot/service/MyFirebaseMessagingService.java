package gov.wa.wsdot.android.wsdot.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.res.ResourcesCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.ui.alert.PushNotificationAlertActivity;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //sendNotification(remoteMessage.getNotification().getBody(), remoteMessage.getData());

        Intent intent = new Intent(this, PushNotificationAlertActivity.class);
        intent.putExtra("message", remoteMessage.getNotification().getBody());
        for (Map.Entry<String, String> entry : remoteMessage.getData().entrySet()) {
            intent.putExtra(entry.getKey(), entry.getValue());
        }
        startActivity(intent);


    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *  @param messageBody FCM message body received.
     *  @param data
     */
    private void sendNotification(String messageBody, Map<String, String> data) {

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_status_bar)
                .setColor(ResourcesCompat.getColor(getResources(), R.color.primary, null))
                .setContentTitle("WSDOT")
                .setContentText(messageBody)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(messageBody))
                .setAutoCancel(true)
                .setSound(defaultSoundUri);

        Intent intent = new Intent(this, PushNotificationAlertActivity.class);
        for (Map.Entry<String, String> entry : data.entrySet()) {
            intent.putExtra(entry.getKey(), entry.getValue());
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        notificationBuilder.setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify((int)(System.currentTimeMillis()/1000), notificationBuilder.build());
    }

}