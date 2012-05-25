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

package gov.wa.wsdot.android.wsdot;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.view.MenuItem;

public class TollRatesTabActivity extends SherlockFragmentActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        ActionBar.Tab sr520Tab = getSupportActionBar().newTab();
        sr520Tab.setText("SR 520");
        sr520Tab.setTabListener(new TabListener<SR520TollRatesActivity>(this, "SR520", SR520TollRatesActivity.class));
        getSupportActionBar().addTab(sr520Tab);

        ActionBar.Tab sr16Tab = getSupportActionBar().newTab();
        sr16Tab.setText("SR 16");
        sr16Tab.setTabListener(new TabListener<SR16TollRatesActivity>(this, "SR16", SR16TollRatesActivity.class));
        getSupportActionBar().addTab(sr16Tab);        

        ActionBar.Tab sr167Tab = getSupportActionBar().newTab();
        sr167Tab.setText("SR 167");
        sr167Tab.setTabListener(new TabListener<SR167TollRatesActivity>(this, "SR167", SR167TollRatesActivity.class));
        getSupportActionBar().addTab(sr167Tab);         
        
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
	
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        /*Save the selected tab in order to restore in screen rotation*/
        outState.putInt("tab", getSupportActionBar().getSelectedNavigationIndex());
    }
    public class TabListener<T extends Fragment> implements ActionBar.TabListener {
        private Fragment mFragment;
        private final SherlockFragmentActivity mActivity;
        private final String mTag;
        private final Class<T> mClass;

        /** Constructor used each time a new tab is created.
          * @param activity  The host Activity, used to instantiate the fragment
          * @param tag  The identifier tag for the fragment
          * @param clz  The fragment's Class, used to instantiate the fragment
          */
        public TabListener(SherlockFragmentActivity activity, String tag, Class<T> clz) {
            mActivity = activity;
            mTag = tag;
            mClass = clz;
            
            FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();


            // Check to see if we already have a fragment for this tab, probably
            // from a previously saved state.  If so, deactivate it, because our
            // initial state is that a tab isn't shown.
            mFragment = mActivity.getSupportFragmentManager().findFragmentByTag(mTag);
            if (mFragment != null && !mFragment.isDetached()) {
                ft.detach(mFragment);
            }
        }       

        /* The following are each of the ActionBar.TabListener callbacks */

        public void onTabSelected(Tab tab, FragmentTransaction ft) {

                ft = mActivity.getSupportFragmentManager().beginTransaction();

            if (mFragment == null) {
                mFragment = Fragment.instantiate(mActivity, mClass.getName());
                ft.add(android.R.id.content, mFragment, mTag);
                ft.commit();
            } else {
                ft.attach(mFragment);
                ft.commit();
            }
        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft) {

            ft = mActivity.getSupportFragmentManager().beginTransaction();

            if (mFragment != null) {
                ft.detach(mFragment);
                ft.commitAllowingStateLoss();
            }   
        }

        public void onTabReselected(Tab tab, FragmentTransaction ft) {
            // User selected the already selected tab. Usually do nothing.
        }
    }    
}
