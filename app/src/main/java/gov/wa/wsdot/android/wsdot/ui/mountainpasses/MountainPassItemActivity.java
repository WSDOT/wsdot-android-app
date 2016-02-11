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

package gov.wa.wsdot.android.wsdot.ui.mountainpasses;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.MountainPasses;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
import gov.wa.wsdot.android.wsdot.ui.WsdotApplication;

public class MountainPassItemActivity extends BaseActivity {
	
	private DateFormat parseDateFormat = new SimpleDateFormat("yyyy,M,d,H,m", Locale.US); //e.g. [2010, 11, 2, 8, 22, 32, 883, 0, 0]
	private DateFormat displayDateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a", Locale.US);
	private boolean mIsStarred = false;
	private ContentResolver resolver;
	private int mId;
	private Tracker mTracker;

    private TabLayout mTabLayout;
    private List<Class<? extends Fragment>> tabFragments = new ArrayList<>();
    private ViewPager mViewPager;
    private gov.wa.wsdot.android.wsdot.util.TabsAdapter mTabsAdapter;
    private Toolbar mToolbar;
	
	static final private int MENU_ITEM_STAR = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    Bundle b = getIntent().getExtras();
	    mId = b.getInt("id");
	    String mountainPassName = b.getString("MountainPassName");
	    String cameras = b.getString("Cameras");
	    String forecast = b.getString("Forecasts");
	    mIsStarred = b.getInt("isStarred") != 0;
	    
	    // GA tracker
    	mTracker = ((WsdotApplication) getApplication()).getDefaultTracker();
    	mTracker.setScreenName("/Mountain Passes/Details/" + mountainPassName);
    	mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        setContentView(R.layout.activity_with_tabs);
        mViewPager = (ViewPager) findViewById(R.id.pager);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		Spinner mSpinner = (Spinner) findViewById(R.id.spinner);
		mSpinner.setVisibility(View.GONE);

        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // Add tab titles and their corresponding fragments to the fragment list.
        tabFragments.add(mTabLayout.getTabCount(), MountainPassItemReportFragment.class);
        mTabLayout.addTab(mTabLayout.newTab().setText("Report"));
        if (!cameras.equals("[]")) {
            tabFragments.add(mTabLayout.getTabCount(), MountainPassItemCameraFragment.class);
            mTabLayout.addTab(mTabLayout.newTab().setText("Cameras"));
        }
        if (!forecast.equals("[]")) {
            tabFragments.add(mTabLayout.getTabCount(), MountainPassItemForecastFragment.class);
            mTabLayout.addTab(mTabLayout.newTab().setText("Forecast"));
        }

        mTabsAdapter = new gov.wa.wsdot.android.wsdot.util.TabsAdapter
                (this, tabFragments, getSupportFragmentManager(), mTabLayout.getTabCount());

        mViewPager.setAdapter(mTabsAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));

        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				mViewPager.setCurrentItem(tab.getPosition());
				// GA tracker
				mTracker.setScreenName("/Mountain Passes/Details/" + tab.getText());
				mTracker.send(new HitBuilders.ScreenViewBuilder().build());
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab) {

			}

			@Override
			public void onTabReselected(TabLayout.Tab tab) {

			}
		});

        enableAds();

        if (savedInstanceState != null) {
            TabLayout.Tab tab = mTabLayout.getTabAt(savedInstanceState.getInt("tab", 0));
            tab.select();
        }        
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem menuItem_Star = menu.add(0, MENU_ITEM_STAR, menu.size(), R.string.description_star);
		MenuItemCompat.setShowAsAction(menuItem_Star, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
	
		if (mIsStarred) {
			menu.getItem(MENU_ITEM_STAR).setIcon(R.drawable.ic_menu_star_on);
		} else {
			menu.getItem(MENU_ITEM_STAR).setIcon(R.drawable.ic_menu_star);
		}
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
	    case android.R.id.home:
	    	finish();
	    	return true;
		case MENU_ITEM_STAR:
			toggleStar(item);
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	private void toggleStar(MenuItem item) {
		resolver = getContentResolver();
		Snackbar added_snackbar = Snackbar
				.make(findViewById(R.id.activity_with_tabs), R.string.add_favorite, Snackbar.LENGTH_SHORT);

		Snackbar removed_snackbar = Snackbar
				.make(findViewById(R.id.activity_with_tabs), R.string.remove_favorite, Snackbar.LENGTH_SHORT);

		if (mIsStarred) {
			item.setIcon(R.drawable.ic_menu_star);
			try {
				ContentValues values = new ContentValues();
				values.put(MountainPasses.MOUNTAIN_PASS_IS_STARRED, 0);
				resolver.update(
						MountainPasses.CONTENT_URI,
						values,
						MountainPasses.MOUNTAIN_PASS_ID + "=?",
						new String[] {Integer.toString(mId)}
						);
				
				removed_snackbar.show();
				mIsStarred = false;
	    	} catch (Exception e) {
	    		Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
	    		Log.e("MountainPasItemActivity", "Error: " + e.getMessage());
	    	}
		} else {
			item.setIcon(R.drawable.ic_menu_star_on);
			try {
				ContentValues values = new ContentValues();
				values.put(MountainPasses.MOUNTAIN_PASS_IS_STARRED, 1);
				resolver.update(
						MountainPasses.CONTENT_URI,
						values,
						MountainPasses.MOUNTAIN_PASS_ID + "=?",
						new String[] {Integer.toString(mId)}
						);			
				
				added_snackbar.show();
				mIsStarred = true;
			} catch (Exception e) {
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
	    		Log.e("MountainPasItemActivity", "Error: " + e.getMessage());
	    	}
		}		
	}
	
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save the selected tab in order to restore in screen rotation
        outState.putInt("tab", mTabLayout.getSelectedTabPosition());
    }
}
