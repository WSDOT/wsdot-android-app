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

package gov.wa.wsdot.android.wsdot.ui.home;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.List;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.service.EventService;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
import gov.wa.wsdot.android.wsdot.ui.WsdotApplication;
import gov.wa.wsdot.android.wsdot.ui.about.AboutActivity;
import gov.wa.wsdot.android.wsdot.ui.notifications.NotificationsActivity;
import gov.wa.wsdot.android.wsdot.ui.settings.SettingsActivity;
import gov.wa.wsdot.android.wsdot.ui.widget.HomePager;
import gov.wa.wsdot.android.wsdot.util.TabsAdapter;
import gov.wa.wsdot.android.wsdot.util.UIUtils;

public class HomeActivity extends BaseActivity implements Injectable {

    private static final String TAG = HomeActivity.class.getSimpleName();
    private TabLayout mTabLayout;
    private List<Class<? extends Fragment>> tabFragments = new ArrayList<>();
    private HomePager mViewPager;
    private TabsAdapter mtabsAdapter;
    private Toolbar mToolbar;
    private android.support.design.widget.AppBarLayout mAppBar;
    private Tracker mTracker;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Force use of overflow menu on devices with ICS and menu button.
        UIUtils.setHasPermanentMenuKey(this, false);

        mTracker = ((WsdotApplication) getApplication()).getDefaultTracker();

        setContentView(R.layout.activity_home);
        mViewPager = findViewById(R.id.pager);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mAppBar = findViewById(R.id.appbar);

        mTabLayout = findViewById(R.id.tab_layout);
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // Add tab titles and their corresponding fragments to the fragment list.
        tabFragments.add(mTabLayout.getTabCount(), DashboardFragment.class);
        mTabLayout.addTab(mTabLayout.newTab().setText("Home"));
        tabFragments.add(mTabLayout.getTabCount(), FavoritesFragment.class);
        mTabLayout.addTab(mTabLayout.newTab().setText("Favorites"));

        mtabsAdapter = new TabsAdapter
                (this, tabFragments, getSupportFragmentManager(), mTabLayout.getTabCount());

        mViewPager.setAdapter(mtabsAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));

        mViewPager.setOffscreenPageLimit(0);

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                mViewPager.setCurrentItem(tab.getPosition());
                AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();

                // Disable the collapsing toolbar when on the home dashboard. Enable it when on favorites.
                if (tab.getText().equals("Favorites")) {
                    // GA tracker. Only track Favorites Tab b/c home activity is auto tracked.
                    mTracker.setScreenName("/" + tab.getText());
                    mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                    params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
                            | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED);
                } else {
                    params.setScrollFlags(0);
                    mAppBar.setExpanded(true);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        startService(new Intent(this, EventService.class));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_about:
            	startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.menu_notifications:
                startActivity(new Intent(this, NotificationsActivity.class));
                break;
            case R.id.menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}