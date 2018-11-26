package gov.wa.wsdot.android.wsdot.ui.myroute.myroutealerts;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;

/**
 * Displays a list of alerts on the users selected custom route
 */

public class MyRouteAlertsListActivity extends BaseActivity {

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_route_alerts);

        mToolbar = findViewById(R.id.toolbar);

        Bundle args = getIntent().getExtras();
        String title = args.getString("title");
        mToolbar.setTitle(title);

        setSupportActionBar(mToolbar);

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        enableAds(getString(R.string.default_ad_target));
    }

    @Override
    public void onResume() {
        super.onResume();
        setFirebaseAnalyticsScreenName("MyRouteAlerts");
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