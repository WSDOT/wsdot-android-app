/*
 * Copyright (c) 2017 Washington State Department of Transportation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package gov.wa.wsdot.android.wsdot.ui.ferries.bulletins;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
import gov.wa.wsdot.android.wsdot.util.MyLogger;

public class FerriesRouteAlertsBulletinDetailsActivity extends BaseActivity {

    private static final String TAG = FerriesRouteAlertsBulletinDetailsActivity.class.getSimpleName();
    private Toolbar mToolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_ferries_route_alerts_bulletin_details);

		Bundle b = getIntent().getExtras();

		String mAlertFullTitle = "Unavailable";

		if (b != null){
			if (b.getString("AlertFullTitle") != null) {
				mAlertFullTitle = b.getString("AlertFullTitle");
			}

            if ( b.getBoolean("from_notification")){
				setFirebaseAnalyticsEvent("notification_received","type", "ferry_bulletin");
            }

        }

		mToolbar = findViewById(R.id.toolbar);
		mToolbar.setTitle(mAlertFullTitle);
		setSupportActionBar(mToolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

		MyLogger.crashlyticsLog("Ferries", "Tap", "FerriesRouteAlertsBulletinDetailsActivity " + mAlertFullTitle, 1);


	}

	@Override
	public void onResume() {
		super.onResume();
		setFirebaseAnalyticsScreenName("FerryBulletin");
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
