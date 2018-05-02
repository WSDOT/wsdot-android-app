package gov.wa.wsdot.android.wsdot.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;

public class MyNotificationManager {

    public static String MY_ROUTE_CHANNEL_ID = "MY_ROUTES_TRACKING_SERVICE";
    private static String MY_ROUTE_CHANNEL_NAME = "My Routes";
    private static String MY_ROUTE_CHANNEL_DESCRIPTION = "Displayed when recording a new route";

    public static String ALERT_CHANNEL_ID = "ALERTS";
    private static String ALERT_CHANNEL_NAME = "Alerts";
    private static String ALERT_CHANNEL_DESCRIPTION = "Notifications from WSDOT";

    private Context context;

    public MyNotificationManager(Context context){
        this.context = context;
    }

    public String getMainNotificationId() {
        return MY_ROUTE_CHANNEL_ID;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    public void createMainNotificationChannels() {
        createMyRoutesChannel();
        createAlertsChannel();
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createMyRoutesChannel(){
        int importance = android.app.NotificationManager.IMPORTANCE_LOW;

        NotificationChannel mChannel = new NotificationChannel(MY_ROUTE_CHANNEL_ID, MY_ROUTE_CHANNEL_NAME, importance);
        mChannel.setDescription(MY_ROUTE_CHANNEL_DESCRIPTION);
        mChannel.setLightColor(Color.GREEN);
        context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createAlertsChannel(){
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        NotificationChannel mChannel = new NotificationChannel(ALERT_CHANNEL_ID, ALERT_CHANNEL_NAME, importance);
        mChannel.setDescription(ALERT_CHANNEL_DESCRIPTION);
        mChannel.setLightColor(Color.GREEN);
        context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

}
