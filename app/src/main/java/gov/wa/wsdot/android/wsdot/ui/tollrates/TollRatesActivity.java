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

package gov.wa.wsdot.android.wsdot.ui.tollrates;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
import gov.wa.wsdot.android.wsdot.util.MyLogger;
import gov.wa.wsdot.android.wsdot.util.TabsAdapter;

public class TollRatesActivity extends BaseActivity {

    private TabLayout mTabLayout;
    private List<Class<? extends Fragment>> tabFragments = new ArrayList<>();
    private ViewPager mViewPager;
    private TabsAdapter mTabsAdapter;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_with_tabs);
        mViewPager = findViewById(R.id.pager);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        AppBarLayout.LayoutParams params =
                (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();
        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);

        Spinner mSpinner = findViewById(R.id.spinner);
        mSpinner.setVisibility(View.GONE);

        mTabLayout = findViewById(R.id.tab_layout);
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // Add tab titles and their corresponding fragments to the fragment list.
        tabFragments.add(mTabLayout.getTabCount(), SR520TollRatesFragment.class);
        mTabLayout.addTab(mTabLayout.newTab().setText("SR 520"));
        tabFragments.add(mTabLayout.getTabCount(), SR16TollRatesFragment.class);
        mTabLayout.addTab(mTabLayout.newTab().setText("SR 16"));
        tabFragments.add(mTabLayout.getTabCount(), SR99TollRatesFragment.class);
        mTabLayout.addTab(mTabLayout.newTab().setText("SR 99"));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            tabFragments.add(mTabLayout.getTabCount(), SR167TollRatesFragment.class);
            mTabLayout.addTab(mTabLayout.newTab().setText("SR 167"));
            tabFragments.add(mTabLayout.getTabCount(), I405TollRatesFragment.class);
            mTabLayout.addTab(mTabLayout.newTab().setText("I-405"));
        }


        mTabsAdapter = new TabsAdapter(this, tabFragments, getSupportFragmentManager(), mTabLayout.getTabCount());

        mViewPager.setAdapter(mTabsAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
                TollRatesActivity.this.setFirebaseAnalyticsScreenName(
                        String.format("%s%s", "TollRates", tab.getText()).replaceAll("\\W", ""));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });


        if (savedInstanceState != null) {
            TabLayout.Tab tab = mTabLayout.getTabAt(savedInstanceState.getInt("tab", 0));
            tab.select();
        }

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        boolean seenTip = settings.getBoolean("KEY_SEEN_TOLL_WARNING", false);

        if (!seenTip) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.WSDOT_popup);
            builder.setMessage("Remember to never use the WSDOT app while driving.");
            builder.setPositiveButton("Ok", (dialog, whichButton) -> dialog.dismiss());
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

        settings.edit().putBoolean("KEY_SEEN_TOLL_WARNING", true).apply();

        disableAds();
        MyLogger.crashlyticsLog("Toll Rates", "Screen View", "TollRatesActivity", 1);
    }

    @Override
    public void onResume() {
        super.onResume();
        setFirebaseAnalyticsScreenName("TollRatesSR520");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tollrates, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_mygoodtogo_link:
                Intent intent = new Intent();

                setFirebaseAnalyticsEvent("open_link", "type", "tolling_good_to_go");
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://mygoodtogo.com"));

                startActivity(intent);
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
