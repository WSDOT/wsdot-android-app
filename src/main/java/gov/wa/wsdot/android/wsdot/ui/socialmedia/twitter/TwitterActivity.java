/*
 * Copyright (c) 2014 Washington State Department of Transportation
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

package gov.wa.wsdot.android.wsdot.ui.socialmedia.twitter;

import gov.wa.wsdot.android.wsdot.R;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

public class TwitterActivity extends ActionBarActivity implements
        ActionBar.OnNavigationListener {
	
    private ArrayList<String> mTwitterAccounts;
    private HashMap<String, String> mTwitterScreenNames = new HashMap<String, String>();
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_twitter);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		mTwitterAccounts = new ArrayList<String>();
		mTwitterAccounts.add("All Accounts");
		mTwitterAccounts.add("Ferries");
		mTwitterAccounts.add("Good To Go!");
		mTwitterAccounts.add("Snoqualmie Pass");
		mTwitterAccounts.add("WSDOT");
		mTwitterAccounts.add("WSDOT Southwest");
		mTwitterAccounts.add("WSDOT Tacoma");
		mTwitterAccounts.add("WSDOT Traffic");
		
		mTwitterScreenNames.put("All Accounts", "all");
		mTwitterScreenNames.put("Ferries", "wsferries");
		mTwitterScreenNames.put("Good To Go!", "GoodToGoWSDOT");
		mTwitterScreenNames.put("Snoqualmie Pass", "SnoqualmiePass");
		mTwitterScreenNames.put("WSDOT", "wsdot");
		mTwitterScreenNames.put("WSDOT Southwest", "wsdot_sw");
		mTwitterScreenNames.put("WSDOT Tacoma", "wsdot_tacoma");
		mTwitterScreenNames.put("WSDOT Traffic", "wsdot_traffic");
		
        Context context = getSupportActionBar().getThemedContext();
        ArrayAdapter<String> list = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, mTwitterAccounts);
        list.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

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
		Bundle args = new Bundle();
		args.putString("account", mTwitterScreenNames.get(mTwitterAccounts.get(itemPosition)));
		TwitterFragment details = new TwitterFragment();
		details.setArguments(args);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.fragment_twitter, details);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.commit();
		
		return true;
	}	

}
