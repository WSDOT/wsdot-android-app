/*
 * Copyright (c) 2012 Washington State Department of Transportation
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
import gov.wa.wsdot.android.wsdot.shared.FerriesScheduleDateItem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

public class FerriesRouteSchedulesDayDeparturesActivity extends SherlockFragmentActivity
	implements ActionBar.OnNavigationListener {

	private static ArrayList<FerriesScheduleDateItem> mScheduleDateItems;
	private static ArrayList<String> mDaysOfWeek;
	private static int mPosition;
	
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        setContentView(R.layout.activity_ferries_route_schedules_day_departures);
		
		DateFormat dateFormat = new SimpleDateFormat("EEEE");
		Bundle args = getIntent().getExtras();
		String title = args.getString("terminalNames");
		mPosition = args.getInt("position");
		mScheduleDateItems = (ArrayList<FerriesScheduleDateItem>) getIntent().getSerializableExtra("scheduleDateItems");
		
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDaysOfWeek = new ArrayList<String>();
        int numDates = mScheduleDateItems.size();
        for (int i=0; i < numDates; i++) {
        	mDaysOfWeek.add(dateFormat.format(new Date(Long.parseLong(mScheduleDateItems.get(i).getDate()))));
        }
        
        Context context = getSupportActionBar().getThemedContext();
        ArrayAdapter<String> list = new ArrayAdapter<String>(context, R.layout.sherlock_spinner_item, mDaysOfWeek);
        list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);

        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getSupportActionBar().setListNavigationCallbacks(list, this);
        
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

	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		Log.d("DAY OF WEEK", Integer.toString(itemPosition));
		Bundle args = new Bundle();
		args.putSerializable("terminalItems", mScheduleDateItems.get(itemPosition).getFerriesTerminalItem().get(mPosition));
		FerriesRouteSchedulesDayDeparturesFragment departures = new FerriesRouteSchedulesDayDeparturesFragment();
		departures.setArguments(args);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.fragment_departures, departures);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.commit();
		
		return true;
	}

}
