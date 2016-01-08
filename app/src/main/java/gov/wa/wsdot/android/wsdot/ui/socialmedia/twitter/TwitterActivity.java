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

package gov.wa.wsdot.android.wsdot.ui.socialmedia.twitter;

import java.util.HashMap;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;

public class TwitterActivity extends BaseActivity implements
        AdapterView.OnItemSelectedListener {

    private HashMap<String, String> mTwitterScreenNames = new HashMap<String, String>();
    private Toolbar mToolbar;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Mapping of account names to wsdot url call for tweets.
        mTwitterScreenNames.put("All Accounts", "all");
		mTwitterScreenNames.put("Ferries", "wsferries");
		mTwitterScreenNames.put("Good To Go!", "GoodToGoWSDOT");
		mTwitterScreenNames.put("Snoqualmie Pass", "SnoqualmiePass");
		mTwitterScreenNames.put("WSDOT", "wsdot");
		mTwitterScreenNames.put("WSDOT Southwest", "wsdot_sw");
		mTwitterScreenNames.put("WSDOT Tacoma", "wsdot_tacoma");
		mTwitterScreenNames.put("WSDOT Traffic", "wsdot_traffic");

        // Set up custom spinner
        Spinner spinner = (Spinner) findViewById(R.id.spinner_nav);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.twitter_accounts, R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(this);

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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Bundle args = new Bundle();
        args.putString("account", mTwitterScreenNames.get(getResources().getStringArray(R.array.twitter_accounts)[position]));
        TwitterFragment details = new TwitterFragment();
        details.setArguments(args);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_twitter, details);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}