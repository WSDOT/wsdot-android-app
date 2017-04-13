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

package gov.wa.wsdot.android.wsdot.ui.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.accessibility.AccessibilityManager;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.List;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
import gov.wa.wsdot.android.wsdot.ui.WsdotApplication;
import gov.wa.wsdot.android.wsdot.ui.about.AboutActivity;
import gov.wa.wsdot.android.wsdot.ui.myroute.MyRouteActivity;
import gov.wa.wsdot.android.wsdot.ui.settings.SettingsActivity;
import gov.wa.wsdot.android.wsdot.ui.widget.HomePager;
import gov.wa.wsdot.android.wsdot.util.TabsAdapter;
import gov.wa.wsdot.android.wsdot.util.UIUtils;

public class HomeActivity extends BaseActivity {

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
        mViewPager = (HomePager) findViewById(R.id.pager);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mAppBar = (android.support.design.widget.AppBarLayout) findViewById(R.id.appbar);

        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
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

        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
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

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        final Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                showTapTargetHint();
            }
        };
        handler.postDelayed(r, 500);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_about:
            	startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.menu_route:
                startActivity(new Intent(this, MyRouteActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showTapTargetHint() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        boolean seenTip = settings.getBoolean("KEY_SEEN_MY_ROUTE_HOME_TIP", false);

        AccessibilityManager am = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        if (!seenTip && !am.isEnabled()) {
            try {
                TapTargetView.showFor(this,                 // `this` is an Activity
                        TapTarget.forToolbarMenuItem(mToolbar, R.id.menu_route, "My Routes", "Easily check for highway alerts affecting your commute by creating a route.")
                                // All options below are optional
                                .outerCircleColor(R.color.primary)      // Specify a color for the outer circle
                                .targetCircleColor(R.color.white)   // Specify a color for the target circle
                                .titleTextSize(20)                  // Specify the size (in sp) of the title text
                                .titleTextColor(R.color.white)      // Specify the color of the title text
                                .descriptionTextSize(15)            // Specify the size (in sp) of the description text
                                .textColor(R.color.white)            // Specify a color for both the title and description text
                                .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                                .dimColor(R.color.black)            // If set, will dim behind the view with 30% opacity of the given color
                                .drawShadow(true)                   // Whether to draw a drop shadow or not
                                .cancelable(true)                  // Whether tapping outside the outer circle dismisses the view
                                .tintTarget(true)                   // Whether to tint the target view's color
                                .transparentTarget(false)           // Specify whether the target is transparent (displays the content underneath)
                                .targetRadius(60),                  // Specify the target radius (in dp)
                        new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                            @Override
                            public void onTargetClick(TapTargetView view) {
                                super.onTargetClick(view);      // This call is optional
                                startActivity(new Intent(HomeActivity.this, MyRouteActivity.class));
                            }
                        });
            } catch (NullPointerException e){
                Log.e(TAG, "Null pointer exception while trying to show tip view");
            }
        }
        settings.edit().putBoolean("KEY_SEEN_MY_ROUTE_HOME_TIP", true).apply();
    }
}