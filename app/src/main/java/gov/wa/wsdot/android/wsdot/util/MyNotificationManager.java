package gov.wa.wsdot.android.wsdot.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;

public class MyNotificationManager {

    private static String CHANNEL_ID = "MY_ROUTES_TRACKING_SERVICE";
    private static String CHANNEL_NAME = "My Routes";
    private static String CHANNEL_DESCRIPTION = "Displayed when recording a new route";

    private Context context;

    public MyNotificationManager(Context context){
        this.context = context;
    }

    public String getMainNotificationId() {
        return CHANNEL_ID;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    public void createMainNotificationChannel() {
        String id = CHANNEL_ID;
        String name = CHANNEL_NAME;
        String description = CHANNEL_DESCRIPTION;
        int importance = android.app.NotificationManager.IMPORTANCE_LOW;

        NotificationChannel mChannel = new NotificationChannel(id, name, importance);
        mChannel.setDescription(description);
        mChannel.setLightColor(Color.GREEN);
        context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

}
