/*
 * Copyright (c) 2015 Washington State Department of Transportation
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

package gov.wa.wsdot.android.wsdot.ui;

import gov.wa.wsdot.android.wsdot.R;
import aje.android.sdk.AdError;
import aje.android.sdk.AdJugglerAdView;
import aje.android.sdk.AdListener;
import aje.android.sdk.AdRequest;
import aje.android.sdk.IncorrectAdRequestException;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;

public class FerriesRouteAlertsBulletinsActivity extends ActionBarActivity {
	
    private static final String TAG = FerriesRouteAlertsBulletinsActivity.class.getSimpleName();
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ferries_route_alerts_bulletins);
		
		Bundle args = getIntent().getExtras();
        String title = getString(R.string.title_ferriesroutealerts) + " - " + args.getString("title");

        getSupportActionBar().setTitle(title);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
        final AdJugglerAdView mAdJugglerAdView = (AdJugglerAdView) findViewById(R.id.ajAdView);
        mAdJugglerAdView.setListener(new AdListener() {

            public boolean onClickAd(String arg0) {
                return false;
            }

            public void onExpand() {
            }

            public void onExpandClose() {
            }

            public void onFailedToClickAd(String arg0, String arg1) {
            }

            public void onFailedToFetchAd(AdError arg0, String arg1) {
            }

            public void onFetchAdFinished() {
            }

            public void onFetchAdStarted() {
            }

            public void onResize() {
            }

            public void onResizeClose() {
            }
        });
        
        try {
            AdRequest adRequest = new AdRequest();
            adRequest.setServer(getString(R.string.adRequest_server));
            adRequest.setZone(getString(R.string.adRequest_zone));
            adRequest.setAdSpot(getString(R.string.adRequest_adspot));
            mAdJugglerAdView.showAd(adRequest);
        } catch (IncorrectAdRequestException e) {
            Log.e(TAG, "Error showing banner ad", e);
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
