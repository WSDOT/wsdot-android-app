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

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
import gov.wa.wsdot.android.wsdot.util.TabsAdapter;

public class CameraActivity extends BaseActivity {

    private TabLayout mTabLayout;
    private List<Class<? extends Fragment>> tabFragments = new ArrayList<>();
    private ViewPager mViewPager;
    private TabsAdapter mTabsAdapter;
    private Toolbar mToolbar;

	CameraViewModel viewModel;

	@Inject
	ViewModelProvider.Factory viewModelFactory;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
	    super.onCreate(savedInstanceState);

	    Bundle b = getIntent().getExtras();
	    int id = b.getInt("id");

	    viewModel = ViewModelProviders.of(this, viewModelFactory).get(CameraViewModel.class);

	    viewModel.getCamera(id).observe(this, camera -> {
	        if (camera != null) {
                mToolbar.setTitle(camera.getTitle());
                if (camera.getHasVideo() == 1) {
                    tabFragments.add(mTabLayout.getTabCount(), CameraVideoFragment.class);
                    mTabLayout.addTab(mTabLayout.newTab().setText("Video"));
                }
            }
        });

		Bundle args = new Bundle();
		args.putInt("id", id);

        setContentView(R.layout.activity_with_tabs);
        mViewPager = findViewById(R.id.pager);

        mToolbar = findViewById(R.id.toolbar);

		setSupportActionBar(mToolbar);
		if(getSupportActionBar() != null){
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setDisplayShowHomeEnabled(true);
		}
		Spinner mSpinner = findViewById(R.id.spinner);
		mSpinner.setVisibility(View.GONE);

        mTabLayout = findViewById(R.id.tab_layout);
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // Add tab titles and their corresponding fragments to the fragment list.
        tabFragments.add(mTabLayout.getTabCount(), CameraImageFragment.class);


        mTabLayout.addTab(mTabLayout.newTab().setText("Camera"));


        mTabsAdapter = new TabsAdapter
                (this, tabFragments, getSupportFragmentManager(), mTabLayout.getTabCount(), args);

        mViewPager.setAdapter(mTabsAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
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
