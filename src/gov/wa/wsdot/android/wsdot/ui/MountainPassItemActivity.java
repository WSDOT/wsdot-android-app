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
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.MountainPasses;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class MountainPassItemActivity extends SherlockFragmentActivity {
	
	DateFormat parseDateFormat = new SimpleDateFormat("yyyy,M,d,H,m"); //e.g. [2010, 11, 2, 8, 22, 32, 883, 0, 0]
	DateFormat displayDateFormat = new SimpleDateFormat("MMMM d, yyyy h:mm a");
	
    private ViewPager mViewPager;
	private TabsAdapter mTabsAdapter;
	private boolean mIsStarred = false;
	private ContentResolver resolver;
	private int mId;
	
	static final private int MENU_ITEM_STAR = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

		mViewPager = new ViewPager(this);
		mViewPager.setId(R.id.pager);
		
		setContentView(mViewPager);
	    
	    Bundle b = getIntent().getExtras();
	    mId = b.getInt("id");
	    String mountainPassName = b.getString("MountainPassName");
	    String cameras = b.getString("Cameras");
	    String forecast = b.getString("Forecasts");
	    mIsStarred = b.getInt("isStarred") != 0;
	    
        getSupportActionBar().setTitle(mountainPassName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
	    mTabsAdapter = new TabsAdapter(this, mViewPager);
	    mTabsAdapter.addTab(getSupportActionBar().newTab().setText("Report"),
	    		MountainPassItemReportFragment.class, b);
	    
	    if (!cameras.equals("[]")) {
		    mTabsAdapter.addTab(getSupportActionBar().newTab().setText("Cameras"),
		    		MountainPassItemCameraFragment.class, b);
	    }
        
	    if (!forecast.equals("[]")) {
		    mTabsAdapter.addTab(getSupportActionBar().newTab().setText("Forecast"),
		    		MountainPassItemForecastFragment.class, b);
	    }
        
        if (savedInstanceState != null) {
            getSupportActionBar().setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
        }        
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.add(0, MENU_ITEM_STAR, menu.size(), R.string.description_star)
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		
		if (mIsStarred) {
			menu.getItem(MENU_ITEM_STAR).setIcon(R.drawable.ic_menu_star_on);
		} else {
			menu.getItem(MENU_ITEM_STAR).setIcon(R.drawable.ic_menu_star);
		}
		
		return super.onPrepareOptionsMenu(menu);
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
				
				Toast.makeText(this, R.string.remove_favorite, Toast.LENGTH_SHORT).show();			
				mIsStarred = false;
	    	} catch (Exception e) {
	    		Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
	    		Log.e("MountainPassItemActivity", "Error: " + e.getMessage());
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
				
				Toast.makeText(this, R.string.add_favorite, Toast.LENGTH_SHORT).show();
				mIsStarred = true;
			} catch (Exception e) {
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
	    		Log.e("MountainPassItemActivity", "Error: " + e.getMessage());
	    	}
		}		
	}
	
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        /*Save the selected tab in order to restore in screen rotation*/
        outState.putInt("tab", getSupportActionBar().getSelectedNavigationIndex());
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

		public TabsAdapter(SherlockFragmentActivity activity, ViewPager pager) {
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

		public void onPageScrollStateChanged(int state)	{
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
