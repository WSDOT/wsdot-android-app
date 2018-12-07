package gov.wa.wsdot.android.wsdot.ui.notifications;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
import gov.wa.wsdot.android.wsdot.util.MyLogger;


public class NotificationsActivity extends BaseActivity {

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_notification);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        MyLogger.crashlyticsLog("Notifications", "Screen View", "NotificationsActivity", 1);

    }

    @Override
    public void onResume() {
        super.onResume();
        setFirebaseAnalyticsScreenName("NotificationSettings");
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
