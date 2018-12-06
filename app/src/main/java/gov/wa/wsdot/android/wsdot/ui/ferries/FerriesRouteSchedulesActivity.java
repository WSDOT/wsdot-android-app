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

package gov.wa.wsdot.android.wsdot.ui.ferries;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;

public class FerriesRouteSchedulesActivity extends BaseActivity {

	private Toolbar mToolbar;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_ferries_route_schedules);

		mToolbar = findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
		if(getSupportActionBar() != null){
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setDisplayShowHomeEnabled(true);
		}

        enableAds(getString(R.string.ferries_ad_target));

	}

	@Override
	public void onResume() {
		super.onResume();
		setFirebaseAnalyticsScreenName("FerrySchedules");
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_ferries, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.menu_reservations_link:
				Intent intent = new Intent();
				setFirebaseAnalyticsEvent("open_link", "type", "ferry_reservation");

				intent.setAction(Intent.ACTION_VIEW);
				intent.setData(Uri.parse("https://secureapps.wsdot.wa.gov/Ferries/Reservations/Vehicle/Mobile/MobileDefault.aspx"));

				startActivity(intent);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}


}


