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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

public class CameraActivity extends SherlockFragmentActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    boolean hasVideo = false;
	    
	    Bundle b = getIntent().getExtras();
	    String title = b.getString("title");
	    String url = b.getString("url");
	    
		String[] param = url.split(",");
		hasVideo = Integer.parseInt(param[1]) != 0;
	    
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        ActionBar.Tab reportTab = getSupportActionBar().newTab();
        reportTab.setText("Camera");
        reportTab.setTabListener(new TabListener<CameraImageFragment>(this, "Camera", CameraImageFragment.class, b));
        getSupportActionBar().addTab(reportTab);	    
	    
	    if (hasVideo) {
	        ActionBar.Tab camerasTab = getSupportActionBar().newTab();
	        camerasTab.setText("Video");
	        camerasTab.setTabListener(new TabListener<CameraVideoFragment>(this, "Video", CameraVideoFragment.class, b));
	        getSupportActionBar().addTab(camerasTab); 
	    }
        
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
        private final Bundle mArgs;

        /** Constructor used each time a new tab is created.
          * @param activity  The host Activity, used to instantiate the fragment
          * @param tag  The identifier tag for the fragment
          * @param clz  The fragment's Class, used to instantiate the fragment
          * @param args The fragment's passed arguments
          */
        public TabListener(SherlockFragmentActivity activity, String tag, Class<T> clz, Bundle args) {
            mActivity = activity;
            mTag = tag;
            mClass = clz;
            mArgs = args;

            // Check to see if we already have a fragment for this tab, probably
            // from a previously saved state.  If so, deactivate it, because our
            // initial state is that a tab isn't shown.
            mFragment = mActivity.getSupportFragmentManager().findFragmentByTag(mTag);
            if (mFragment != null && !mFragment.isDetached()) {
            	FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
                ft.detach(mFragment);
                ft.commit();
            }
        }       

        /* The following are each of the ActionBar.TabListener callbacks */

        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            if (mFragment == null) {
                mFragment = Fragment.instantiate(mActivity, mClass.getName(), mArgs);
                ft.add(android.R.id.content, mFragment, mTag);
            } else {
                ft.attach(mFragment);
            }
        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            if (mFragment != null) {
                ft.detach(mFragment);
            }   
        }

        public void onTabReselected(Tab tab, FragmentTransaction ft) {
        }
    }	
}
