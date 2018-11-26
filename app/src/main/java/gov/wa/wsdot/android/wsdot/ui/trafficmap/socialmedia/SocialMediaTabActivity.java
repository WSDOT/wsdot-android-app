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

package gov.wa.wsdot.android.wsdot.ui.trafficmap.socialmedia;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.socialmedia.blog.BlogFragment;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.socialmedia.facebook.FacebookFragment;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.socialmedia.twitter.TwitterFragment;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.socialmedia.youtube.YouTubeFragment;
import gov.wa.wsdot.android.wsdot.util.MyLogger;

public class SocialMediaTabActivity extends BaseActivity {

    private final String TAG = SocialMediaTabActivity.class.getSimpleName();

    private List<Class<? extends Fragment>> tabFragments = new ArrayList<>();
    private ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_with_tabs);
        mViewPager = (ViewPager) findViewById(R.id.pager);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        Spinner mSpinner = (Spinner) findViewById(R.id.spinner);
        mSpinner.setVisibility(View.GONE);

        TabLayout mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // Add tab titles and their corresponding fragments to the fragment list.
        tabFragments.add(mTabLayout.getTabCount(), TwitterFragment.class);
        mTabLayout.addTab(mTabLayout.newTab().setText("Twitter"));

        tabFragments.add(mTabLayout.getTabCount(), FacebookFragment.class);
        mTabLayout.addTab(mTabLayout.newTab().setText("Facebook"));

        tabFragments.add(mTabLayout.getTabCount(), BlogFragment.class);
        mTabLayout.addTab(mTabLayout.newTab().setText("Blog"));

        tabFragments.add(mTabLayout.getTabCount(), YouTubeFragment.class);
        mTabLayout.addTab(mTabLayout.newTab().setText("YouTube"));

        gov.wa.wsdot.android.wsdot.util.TabsAdapter mTabsAdapter = new gov.wa.wsdot.android.wsdot.util.TabsAdapter
                (this, tabFragments, getSupportFragmentManager(), mTabLayout.getTabCount());

        mViewPager.setAdapter(mTabsAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));


        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}

        });

        MyLogger.crashlyticsLog("Traffic", "Screen View", "SocialMediaTabActivity", 1);

        enableAds(getString(R.string.traffic_ad_target));
    }

    @Override
    public void onResume() {
        super.onResume();
        setFirebaseAnalyticsScreenName("socialMedia");
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
}
