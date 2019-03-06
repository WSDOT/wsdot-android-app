package gov.wa.wsdot.android.wsdot.ui.home;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;

public class EventActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        String title = sharedPref.getString(getString(R.string.event_title_key), "Event");

        Toolbar mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle(title);
        setSupportActionBar(mToolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        String details = sharedPref.getString(getString(R.string.event_details_key), "Error loading details");
        TextView detailsTextView = findViewById(R.id.event_details);
        detailsTextView.setText(details);

    }

    @Override
    public void onResume() {
        super.onResume();
        setFirebaseAnalyticsScreenName("Event");
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
