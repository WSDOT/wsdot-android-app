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

package gov.wa.wsdot.android.wsdot.ui.ferries.departures;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.shared.FerriesScheduleDateItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesTerminalItem;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
import gov.wa.wsdot.android.wsdot.ui.WsdotApplication;
import gov.wa.wsdot.android.wsdot.ui.ferries.FerrySchedulesViewModel;
import gov.wa.wsdot.android.wsdot.ui.ferries.departures.vesselwatch.VesselWatchFragment;
import gov.wa.wsdot.android.wsdot.util.MyLogger;
import gov.wa.wsdot.android.wsdot.util.TabsAdapter;

public class FerriesRouteSchedulesDayDeparturesActivity extends BaseActivity
    implements AdapterView.OnItemSelectedListener {

    private final String TAG = FerriesRouteSchedulesDayDeparturesActivity.class.getSimpleName();

    private boolean initLoad = true;

    protected static final int DAY_SPINNER_ID = 0;
    protected static final int SAILING_SPINNER_ID = 1;

    static final private int MENU_ITEM_STAR = 0;
    private boolean mIsStarred = false;

    private TabLayout mTabLayout;
    private List<Class<? extends Fragment>> tabFragments = new ArrayList<>();
    private ViewPager mViewPager;
    private AppBarLayout mAppBar;
    private gov.wa.wsdot.android.wsdot.util.TabsAdapter mTabsAdapter;
    private Toolbar mToolbar;
    private Tracker mTracker;

    private AppCompatSpinner mSailingSpinner;
    private AppCompatSpinner mDaySpinner;

    private static int mScheduleId;

    private static int mTerminalIndex = 0;
    private static int mDayIndex = 0;

    private static FerriesTerminalItem mTerminalItem = new FerriesTerminalItem();

    private static ArrayList<FerriesScheduleDateItem> mScheduleDateItems;
    private static FerrySchedulesViewModel scheduleViewModel;
    private static FerryTerminalViewModel terminalViewModel;
    private static FerryTerminalCameraViewModel terminalCameraViewModel;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
		super.onCreate(savedInstanceState);

        Bundle args = getIntent().getExtras();
        String title = args.getString("title");
        mScheduleId = args.getInt("scheduleId");
        mIsStarred = args.getInt("isStarred") != 0;

        setContentView(R.layout.activity_ferry_sailings);
        mViewPager = findViewById(R.id.pager);

        mAppBar = findViewById(R.id.appbar);

        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle(title);
        setSupportActionBar(mToolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        mSailingSpinner = this.findViewById(R.id.sailing_spinner);
        mSailingSpinner.setOnItemSelectedListener(this);
        mSailingSpinner.setId(SAILING_SPINNER_ID);

        mDaySpinner = this.findViewById(R.id.day_spinner);
        mDaySpinner.setOnItemSelectedListener(this);
        mDaySpinner.setId(DAY_SPINNER_ID);

        mTabLayout = findViewById(R.id.tab_layout);
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // Add tab titles and their corresponding fragments to the fragment list.
        tabFragments.add(mTabLayout.getTabCount(), FerriesRouteSchedulesDayDeparturesFragment.class);
        mTabLayout.addTab(mTabLayout.newTab().setText("Times"));

        tabFragments.add(mTabLayout.getTabCount(), FerriesTerminalCameraFragment.class);
        mTabLayout.addTab(mTabLayout.newTab().setText("Cameras"));

        tabFragments.add(mTabLayout.getTabCount(), VesselWatchFragment.class);
        mTabLayout.addTab(mTabLayout.newTab().setText("Vessel Watch"));

        mTabsAdapter = new TabsAdapter
                (this, tabFragments, getSupportFragmentManager(), mTabLayout.getTabCount());

        mViewPager.setAdapter(mTabsAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
                if (tab.getText().equals("Cameras")) {
                    mTracker = ((WsdotApplication) getApplication()).getDefaultTracker();
                    mTracker.setScreenName("/Ferries/Schedules/Day Sailings/" + tab.getText());
                    mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                    MyLogger.crashlyticsLog("Ferries", "Tap", "FerriesRouteSchedulesDayDeparturesActivity " + tab.getText(), 1);
                }
                if (tab.getText().equals("Vessel Watch")) {
                    mTracker = ((WsdotApplication) getApplication()).getDefaultTracker();
                    mTracker.setScreenName("/Ferries/Schedules/Day Sailings/" + tab.getText());
                    mTracker.send(new HitBuilders.ScreenViewBuilder().build());
                    MyLogger.crashlyticsLog("Ferries", "Tap", "FerriesRouteSchedulesDayDeparturesActivity " + tab.getText(), 1);

                    mAppBar.setExpanded(true, true);
                    AppBarLayout.LayoutParams params =
                            (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();
                    params.setScrollFlags(0);
                } else {
                    AppBarLayout.LayoutParams params =
                            (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();
                    params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                            | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        terminalViewModel = ViewModelProviders.of(this, viewModelFactory).get(FerryTerminalViewModel.class);

        terminalCameraViewModel = ViewModelProviders.of(this, viewModelFactory).get(FerryTerminalCameraViewModel.class);

        scheduleViewModel = ViewModelProviders.of(this, viewModelFactory).get(FerrySchedulesViewModel.class);
        scheduleViewModel.init(mScheduleId);

        scheduleViewModel.getResourceStatus().observe(this, resourceStatus -> {
            if (resourceStatus != null) {
                switch (resourceStatus.status) {
                    case LOADING:
                        break;
                    case SUCCESS:
                        break;
                    case ERROR:
                       break;
                }
            }
        });

        scheduleViewModel.getDatesWithSailings().observe(this, dates -> {
            if (dates != null) {
                mScheduleDateItems = new ArrayList<>(dates);

                initRouteSpinner(mScheduleDateItems.get(0).getFerriesTerminalItem());
                initDaySpinner(mScheduleDateItems);

                // TODO: get starting terminalIndex based on users location
                mTerminalItem = mScheduleDateItems.get(mDayIndex).getFerriesTerminalItem().get(mTerminalIndex);

                if (initLoad) {
                    initLoad = false;
                    terminalViewModel.loadDepartureTimesForTerminal(mTerminalItem);
                    terminalCameraViewModel.loadTerminalCameras(mTerminalItem.getDepartingTerminalID(), "ferries");
                }
            }
        });

        MyLogger.crashlyticsLog("Ferries", "Screen View", "FerriesRouteSchedulesDayDeparturesActivity " + title, 1);
        enableAds(getString(R.string.ferries_ad_target));

        if (savedInstanceState != null) {
            TabLayout.Tab tab = mTabLayout.getTabAt(savedInstanceState.getInt("tab", 0));
            tab.select();
        }
	}

    private void initDaySpinner(ArrayList<FerriesScheduleDateItem> schedule){

        ArrayList<CharSequence> mDaysOfWeek = new ArrayList<>();

        DateFormat dateFormat = new SimpleDateFormat("EEEE");
        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));

        int numDates = schedule.size();
        for (int i = 0; i < numDates; i++) {
            if (!schedule.get(i).getFerriesTerminalItem().isEmpty()) {
                mDaysOfWeek.add(dateFormat.format(schedule.get(i).getDate()));
            }
        }

        ArrayAdapter<CharSequence> dayOfWeekArrayAdapter = new ArrayAdapter<>(
                this, R.layout.simple_spinner_dropdown_item_white, mDaysOfWeek);
        dayOfWeekArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDaySpinner.setAdapter(dayOfWeekArrayAdapter);

    }

    private void initRouteSpinner(ArrayList<FerriesTerminalItem> sailings){

        ArrayList<CharSequence> sailingsStrings = new ArrayList<>();

        int numSailings = sailings.size();
        for (int i = 0; i < numSailings; i++) {
            sailingsStrings.add(sailings.get(i).getDepartingTerminalName() + " to " + sailings.get(i).getArrivingTerminalName());
        }

        ArrayAdapter<CharSequence> sailingsArrayAdapter = new ArrayAdapter<>(
                 this, R.layout.simple_spinner_dropdown_item_white, sailingsStrings);
        sailingsArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSailingSpinner.setAdapter(sailingsArrayAdapter);

    }


    /**
     * Callback for spinners. Gets the terminal item for the selected day and
     * requests the departure times for that day from the view model.
     */
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        Boolean shouldUpdate;

        switch(parent.getId()){
            case FerriesRouteSchedulesDayDeparturesActivity.SAILING_SPINNER_ID:
                shouldUpdate = mTerminalIndex != position;
                mTerminalIndex = position;
                break;
            case FerriesRouteSchedulesDayDeparturesActivity.DAY_SPINNER_ID:
                shouldUpdate = mDayIndex != position;
                mDayIndex = position;
                break;
            default:
                shouldUpdate = false;
        }

        // Don't bother updating the terminal if we didn't select anything new
        if (shouldUpdate) {
            mTerminalItem = mScheduleDateItems.get(mDayIndex).getFerriesTerminalItem().get(mTerminalIndex);
            terminalViewModel.setScrollToCurrent(true);
            terminalViewModel.loadDepartureTimesForTerminal(mTerminalItem);

            terminalCameraViewModel.loadTerminalCameras(mTerminalItem.getDepartingTerminalID(), "ferries");
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuItem menuItem_Star = menu.add(0, MENU_ITEM_STAR, menu.size(), R.string.description_star);
        MenuItemCompat.setShowAsAction(menuItem_Star, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
        if (mIsStarred) {
            menu.getItem(MENU_ITEM_STAR).setIcon(R.drawable.ic_menu_star_on);
            menu.getItem(MENU_ITEM_STAR).setTitle("Favorite checkbox, checked");
        } else {
            menu.getItem(MENU_ITEM_STAR).setIcon(R.drawable.ic_menu_star);
            menu.getItem(MENU_ITEM_STAR).setTitle("Favorite checkbox, not checked");
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case MENU_ITEM_STAR:
                MyLogger.crashlyticsLog("Ferries", "Tap", "FerriesRouteSchedulesDayDeparturesActivity star", 1);
                toggleStar(item);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleStar(MenuItem item) {
        Snackbar added_snackbar = Snackbar
                .make(findViewById(R.id.activity_ferry_sailings), R.string.add_favorite, Snackbar.LENGTH_SHORT);

        Snackbar removed_snackbar = Snackbar
                .make(findViewById(R.id.activity_ferry_sailings), R.string.remove_favorite, Snackbar.LENGTH_SHORT);

        added_snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onShown(Snackbar snackbar) {
                super.onShown(snackbar);
                snackbar.getView().setContentDescription("added to favorites");
                snackbar.getView().sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT);
            }
        });

        removed_snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onShown(Snackbar snackbar) {
                super.onShown(snackbar);
                snackbar.getView().setContentDescription("removed from favorites");
                snackbar.getView().sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT);
            }
        });

        if (mIsStarred) {
            item.setIcon(R.drawable.ic_menu_star);
            item.setTitle("Favorite checkbox, not checked");
            scheduleViewModel.setIsStarredFor(mScheduleId, 0);
            removed_snackbar.show();
            mIsStarred = false;
        } else {
            item.setIcon(R.drawable.ic_menu_star_on);
            item.setTitle("Favorite checkbox, checked");
            scheduleViewModel.setIsStarredFor(mScheduleId, 1);
            added_snackbar.show();
            mIsStarred = true;
        }
    }

}