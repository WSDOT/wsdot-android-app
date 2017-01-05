package gov.wa.wsdot.android.wsdot.ui.alert;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
import gov.wa.wsdot.android.wsdot.ui.home.HomeActivity;
import gov.wa.wsdot.android.wsdot.ui.socialmedia.blogger.BlogActivity;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.TrafficMapActivity;

public class PushNotificationAlertActivity extends BaseActivity {

    private static final String TAG = PushNotificationAlertActivity.class.getSimpleName();
    private TextView textview;
    private Button details_button;
    private Button dismiss_button;
    private String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.push_notification_alert);

        message = "";

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            message = extras.getString("message");
        } else {
            message = "Error loading message content.";
        }

        textview = (TextView)findViewById(R.id.textview);
        textview.setText(message);

        dismiss_button = (Button)findViewById(R.id.dismiss_button);
        dismiss_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                finish();
            }
        });

        details_button = (Button)findViewById(R.id.details_button);

        checkForCloudMessageCommand(getIntent().getExtras(), details_button);


    }

    /**
     * Checks FCM payload for a command to open an activity. Will add a "View Details" button
     * to the alert if it finds one.
     *
     * Adds an onClick listener to the view details button.
     *
     * @param data
     * @param details_button
     */
    private void checkForCloudMessageCommand(Bundle data, Button details_button){

        details_button.setVisibility(View.GONE);

        if (data != null) {

            String command = data.getString("open");

            // FCM included extra content
            if (command != null) {

                details_button.setVisibility(View.VISIBLE);

                switch (command) {

                    case "blog":
                        details_button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(view.getContext(), BlogActivity.class);
                                startActivity(intent);
                            }
                        });
                        break;
                    case "map":

                        // Grab lat & long for TrafficMapActivity
                        if (data.get("latitude") != null && data.get("longitude") != null) {

                            String latitude = data.getString("latitude");
                            String longitude = data.getString("longitude");
                            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
                            SharedPreferences.Editor editor = settings.edit();

                            try {
                                editor.putString("KEY_TRAFFICMAP_LAT", String.valueOf(latitude));
                                editor.putString("KEY_TRAFFICMAP_LON", String.valueOf(longitude));
                                editor.putInt("KEY_TRAFFICMAP_ZOOM", 13);
                            } catch (NullPointerException e) {
                                editor.putString("KEY_TRAFFICMAP_LAT", "47.5990");
                                editor.putString("KEY_TRAFFICMAP_LON", "-122.3350");
                                editor.putInt("KEY_TRAFFICMAP_ZOOM", 12);
                            }

                            editor.apply();
                        }
                        details_button.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view){
                                Intent intent = new Intent(view.getContext(), TrafficMapActivity.class);
                                startActivity(intent);
                            }

                        });
                        break;
                    default:
                        details_button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view){
                                Intent intent = new Intent(view.getContext(), HomeActivity.class);
                                startActivity(intent);
                            }
                        });
                        break;
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
