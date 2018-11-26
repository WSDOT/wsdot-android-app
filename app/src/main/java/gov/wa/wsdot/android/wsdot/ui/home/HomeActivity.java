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

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;

import java.util.ArrayList;
import java.util.List;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.service.EventService;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
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

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Force use of overflow menu on devices with ICS and menu button.
        UIUtils.setHasPermanentMenuKey(this, false);

        setContentView(R.layout.activity_home);
        mViewPager = findViewById(R.id.pager);

        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle("WSDOT");

        // inflate toolbar for tap view
        mToolbar.inflateMenu(R.menu.options);

        mToolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_about:
                    startActivity(new Intent(HomeActivity.this, AboutActivity.class));
                    break;
                case R.id.menu_notifications:
                    startActivity(new Intent(HomeActivity.this, NotificationsActivity.class));
                    break;
                case R.id.menu_settings:
                    startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
                    break;
            }
            return false;
        });

        //setSupportActionBar(mToolbar);

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

                setFirebaseAnalyticsScreenName(String.valueOf(tab.getText()));

                // Disable the collapsing toolbar when on the home dashboard. Enable it when on favorites.
                if (tab.getText().equals("Favorites")) {
                    params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
                            | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED);
                } else {
                    params.setScrollFlags(0);
                    mAppBar.setExpanded(true);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        startService(new Intent(this, EventService.class));

        tryDisplayNotificationTipView();


    }

    @Override
    public void onResume() {
        super.onResume();
        setFirebaseAnalyticsScreenName("Home");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mToolbar.setNavigationOnClickListener(null);
        mToolbar.setOnMenuItemClickListener(null);
    }

    /**
     * Attempts to check if there is a new version of the notification list.
     * Alerts users with a tap target view if there is.
     */
    private void tryDisplayNotificationTipView(){

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        int topicVersion = settings.getInt(getString(R.string.firebase_notification_topics_version), 0);
        int newTopicVersion = settings.getInt(getString(R.string.new_firebase_notification_topics_version), 0);

        String title = settings.getString(getString(R.string.firebase_notification_title), "New Notifications Available");
        String description = settings.getString(getString(R.string.firebase_notification_description), "");

        AccessibilityManager am = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);

        Boolean accessibilityEnabled = false;
        if (am != null){
            accessibilityEnabled = am.isEnabled();
        }

        if ((topicVersion < newTopicVersion) && !accessibilityEnabled) {

            try {
                TapTargetView.showFor(this,
                        TapTarget.forToolbarMenuItem(mToolbar, R.id.menu_notifications, title, description)
                                // All options below are optional
                                .outerCircleColor(R.color.primary_default)      // Specify a color for the outer circle
                                .titleTextSize(20)                  // Specify the size (in sp) of the title text
                                .titleTextColor(R.color.white)      // Specify the color of the title text
                                .descriptionTextSize(15)            // Specify the size (in sp) of the description text
                                .textColor(R.color.white)            // Specify a color for both the title and description text
                                .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
                                .dimColor(R.color.black)            // If set, will dim behind the view with 30% opacity of the given color
                                .drawShadow(true)                   // Whether to draw a drop shadow or not
                                .cancelable(true)                  // Whether tapping outside the outer circle dismisses the view
                                .tintTarget(true)                   // Whether to tint the target view's color
                                .transparentTarget(true)           // Specify whether the target is transparent (displays the content underneath)
                                .targetRadius(40),                  // Specify the target radius (in dp)
                        new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                            @Override
                            public void onTargetClick(TapTargetView view) {
                                super.onTargetClick(view);
                                startActivity(new Intent(HomeActivity.this, NotificationsActivity.class));
                            }
                        });
            } catch (NullPointerException | IllegalArgumentException e) {
                Log.e(TAG, "Exception while trying to show tip view");
                Log.e(TAG, e.getMessage());
            }
        }
        settings.edit().putInt(getString(R.string.firebase_notification_topics_version), newTopicVersion).apply();
    }
}