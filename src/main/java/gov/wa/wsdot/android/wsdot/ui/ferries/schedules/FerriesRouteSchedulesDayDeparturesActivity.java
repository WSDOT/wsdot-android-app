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

package gov.wa.wsdot.android.wsdot.ui.ferries.schedules;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.view.MenuItem;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;

public class FerriesRouteSchedulesDayDeparturesActivity extends BaseActivity {

    private ViewPager mViewPager;
    private TabsAdapter mTabsAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        Bundle args = getIntent().getExtras();
        String title = args.getString("terminalNames");

        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
		mViewPager = new ViewPager(this);
		mViewPager.setId(R.id.pager);

		setContentView(mViewPager);
        
        mTabsAdapter = new TabsAdapter(this, mViewPager);
        mTabsAdapter.addTab(getSupportActionBar().newTab().setText("Times"),
                FerriesRouteSchedulesDayDeparturesFragment.class, args);
        mTabsAdapter.addTab(getSupportActionBar().newTab().setText("Cameras"),
                FerriesTerminalCameraFragment.class, args);        
        
        if (savedInstanceState != null) {
            getSupportActionBar().setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
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

    public static class TabsAdapter extends FragmentPagerAdapter implements
        ActionBar.TabListener, ViewPager.OnPageChangeListener {

        private final Context mContext;
        private final ActionBar mActionBar;
        private final ViewPager mViewPager;
        private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
    
        static final class TabInfo {
            private final Class<?> clss;
            private final Bundle args;
    
            TabInfo(Class<?> _class, Bundle _args) {
                clss = _class;
                args = _args;
            }
        }
    
        public TabsAdapter(BaseActivity activity, ViewPager pager) {
            super(activity.getSupportFragmentManager());
            mContext = activity;
            mActionBar = activity.getSupportActionBar();
            mViewPager = pager;
            mViewPager.setAdapter(this);
            mViewPager.setOnPageChangeListener(this);
        }
    
        public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args) {
            TabInfo info = new TabInfo(clss, args);
            tab.setTag(info);
            tab.setTabListener(this);
            mTabs.add(info);
            mActionBar.addTab(tab);
            notifyDataSetChanged();
        }
    
        @Override
        public int getCount() {
            return mTabs.size();
        }
    
        @Override
        public Fragment getItem(int position) {
            TabInfo info = mTabs.get(position);
            return Fragment.instantiate(mContext, info.clss.getName(),
                    info.args);
        }
    
        public void onPageScrolled(int position, float positionOffset,
                int positionOffsetPixels) {
        }
    
        public void onPageSelected(int position) {
            mActionBar.setSelectedNavigationItem(position);
        }
    
        public void onPageScrollStateChanged(int state) {
        }
    
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            Object tag = tab.getTag();
            for (int i = 0; i < mTabs.size(); i++) {
                if (mTabs.get(i) == tag) {
                    mViewPager.setCurrentItem(i);
                }
            }
        }
    
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        }
    
        public void onTabReselected(Tab tab, FragmentTransaction ft) {
        }
    }

}
