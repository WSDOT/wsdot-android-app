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

package gov.wa.wsdot.android.wsdot.ui.camera;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.Cameras;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
import gov.wa.wsdot.android.wsdot.util.TabsAdapter;

public class CameraActivity extends BaseActivity {
	
    private ContentResolver resolver;
    private TabLayout mTabLayout;
    private List<Class<? extends Fragment>> tabFragments = new ArrayList<>();
    private ViewPager mViewPager;
    private TabsAdapter mTabsAdapter;
    private Toolbar mToolbar;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    resolver = getContentResolver();
	    Cursor cursor = null;
	    
	    int cameraId = 0;
	    String title = "";
	    String url = "";
	    boolean hasVideo = false;
	    int isStarred = 0;

	    String[] projection = {
	    		Cameras.CAMERA_ID,
	    		Cameras.CAMERA_TITLE,
	    		Cameras.CAMERA_URL,
	    		Cameras.CAMERA_HAS_VIDEO,
	    		Cameras.CAMERA_IS_STARRED
	    		};	    
	    
	    Bundle b = getIntent().getExtras();
	    int id = b.getInt("id");


		try {
			cursor = resolver.query(
					Cameras.CONTENT_URI,
					projection,
					Cameras.CAMERA_ID + "=?",
					new String[] {Integer.toString(id)},
					null
					);
			
			if (cursor != null && cursor.moveToFirst()) {
				cameraId = cursor.getInt(0);
				title = cursor.getString(1);
				url = cursor.getString(2);
				hasVideo = cursor.getInt(3) != 0;
				isStarred = cursor.getInt(4);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}	    
	    
		Bundle args = new Bundle();
		args.putInt("id", cameraId);
		args.putString("title", title);
		args.putString("url", url);
		args.putInt("isStarred", isStarred);

        setContentView(R.layout.activity_with_tabs);
        mViewPager = (ViewPager) findViewById(R.id.pager);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(title);
		setSupportActionBar(mToolbar);
		if(getSupportActionBar() != null){
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setDisplayShowHomeEnabled(true);
		}
		Spinner mSpinner = (Spinner) findViewById(R.id.spinner);
		mSpinner.setVisibility(View.GONE);

        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // Add tab titles and their corresponding fragments to the fragment list.
        tabFragments.add(mTabLayout.getTabCount(), CameraImageFragment.class);


        mTabLayout.addTab(mTabLayout.newTab().setText("Camera"));
        if (hasVideo) {
            tabFragments.add(mTabLayout.getTabCount(), CameraVideoFragment.class);
            mTabLayout.addTab(mTabLayout.newTab().setText("Video"));
        }

        mTabsAdapter = new TabsAdapter
                (this, tabFragments, getSupportFragmentManager(), mTabLayout.getTabCount(), args);

        mViewPager.setAdapter(mTabsAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));

        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        //Disable scrolling toolbar for this activity.
        AppBarLayout.LayoutParams params =
                (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();
        params.setScrollFlags(0);

        if (savedInstanceState != null) {
            TabLayout.Tab tab = mTabLayout.getTabAt(savedInstanceState.getInt("tab", 0));
            tab.select();
        }

        String adTarget = b.getString("advertisingTarget");
        enableAds(adTarget);
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
        //Save the selected tab in order to restore in screen rotation
        outState.putInt("tab", mTabLayout.getSelectedTabPosition());
    }
}
