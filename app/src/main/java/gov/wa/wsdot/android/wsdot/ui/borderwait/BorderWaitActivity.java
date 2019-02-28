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

package gov.wa.wsdot.android.wsdot.ui.borderwait;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
import gov.wa.wsdot.android.wsdot.util.MyLogger;
import gov.wa.wsdot.android.wsdot.util.TabsAdapter;

public class BorderWaitActivity extends BaseActivity {

	private TabLayout mTabLayout;
	private List<Class<? extends Fragment>> tabFragments = new ArrayList<>();
	private ViewPager mViewPager;
	private TabsAdapter mTabsAdapter;
	private Toolbar mToolbar;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_with_tabs);
        mViewPager = (ViewPager) findViewById(R.id.pager);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        Spinner mSpinner = (Spinner) findViewById(R.id.spinner);
        mSpinner.setVisibility(View.GONE);

        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

		tabFragments.add(mTabLayout.getTabCount(), BorderWaitNorthboundFragment.class);
		mTabLayout.addTab(mTabLayout.newTab().setText("Northbound"));
		tabFragments.add(mTabLayout.getTabCount(), BorderWaitSouthboundFragment.class);
		mTabLayout.addTab(mTabLayout.newTab().setText("Southbound"));

        mTabsAdapter = new TabsAdapter
                (this, tabFragments, getSupportFragmentManager(), mTabLayout.getTabCount());

        mViewPager.setAdapter(mTabsAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
                BorderWaitActivity.this.setFirebaseAnalyticsScreenName(
                        String.format("%s%s", "BorderWaits", tab.getText()).replaceAll("\\W", ""));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        enableAds(getString(R.string.border_ad_target));

        MyLogger.crashlyticsLog("Border Waits", "Screen View", "BorderWaitActivity", 1);

        if (savedInstanceState != null) {
            TabLayout.Tab tab = mTabLayout.getTabAt(savedInstanceState.getInt("tab", 0));
            tab.select();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        setFirebaseAnalyticsScreenName("BorderWaitsNorthbound");
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        /*Save the selected tab in order to restore in screen rotation*/
        outState.putInt("tab", mTabLayout.getSelectedTabPosition());
    }
}