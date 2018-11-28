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

import android.Manifest;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;

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
import gov.wa.wsdot.android.wsdot.ui.ferries.departures.cameras.FerriesTerminalCameraFragment;
import gov.wa.wsdot.android.wsdot.ui.ferries.departures.cameras.FerryTerminalCameraViewModel;
import gov.wa.wsdot.android.wsdot.ui.ferries.departures.vesselwatch.VesselWatchFragment;
import gov.wa.wsdot.android.wsdot.ui.ferries.helpers.FerryHelper;
import gov.wa.wsdot.android.wsdot.util.MyLogger;
import gov.wa.wsdot.android.wsdot.util.TabsAdapter;

public class FerriesRouteSchedulesDayDeparturesActivity extends BaseActivity
    implements AdapterView.OnItemSelectedListener, LocationListener {

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

    private AppCompatSpinner mSailingSpinner;
    private AppCompatSpinner mDaySpinner;

    private int mScheduleId;

    private int mTerminalIndex = 0;
    private int mDayIndex = 0;

    private static FerriesTerminalItem mTerminalItem = new FerriesTerminalItem();

    private static ArrayList<FerriesScheduleDateItem> mScheduleDateItems;
    private static FerryScheduleViewModel scheduleViewModel;
    private static FerryTerminalViewModel terminalViewModel;
    private static FerryTerminalCameraViewModel terminalCameraViewModel;

    private ArrayAdapter<CharSequence> mDayOfWeekArrayAdapter;
    private ArrayAdapter<CharSequence> mSailingsArrayAdapter;

    boolean isAccessibilityEnabled = false;
    boolean isExploreByTouchEnabled = false;

    protected Location mLastLocation;

    private final int REQUEST_ACCESS_FINE_LOCATION = 100;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
		super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            initLoad = savedInstanceState.getBoolean("initLoad", true);
            mDayIndex = savedInstanceState.getInt("dayIndex", 0);
            mTerminalIndex = savedInstanceState.getInt("terminalIndex", 0);
        }

        AccessibilityManager am = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        if (am != null) {
            isAccessibilityEnabled = am.isEnabled();
            isExploreByTouchEnabled = am.isTouchExplorationEnabled();
        }

        Bundle args = getIntent().getExtras();

        String title = "Schedule Unavailable";
        if (args != null) {
            title = args.getString("title");
            mScheduleId = args.getInt("scheduleId");
            mIsStarred = args.getInt("isStarred") != 0;
        }

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


        // set up the sailings spinner
        mSailingSpinner = this.findViewById(R.id.sailing_spinner);
        mSailingSpinner.setOnItemSelectedListener(this);
        mSailingSpinner.setId(SAILING_SPINNER_ID);
        mSailingsArrayAdapter = new ArrayAdapter<>(
                this, R.layout.simple_spinner_dropdown_item_white);
        mSailingsArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSailingSpinner.setAdapter(mSailingsArrayAdapter);

        // set up the day spinner
        mDaySpinner = this.findViewById(R.id.day_spinner);
        mDaySpinner.setOnItemSelectedListener(this);
        mDaySpinner.setId(DAY_SPINNER_ID);
        mDayOfWeekArrayAdapter = new ArrayAdapter<>(
                this, R.layout.simple_spinner_dropdown_item_white);
        mDayOfWeekArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDaySpinner.setAdapter(mDayOfWeekArrayAdapter);

        // set up tab layout

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

                FerriesRouteSchedulesDayDeparturesActivity.this.setFirebaseAnalyticsScreenName(
                        String.format("%s%s", "Ferry", tab.getText()).replaceAll("\\W", ""));

                MyLogger.crashlyticsLog("Ferries", "Tap", "FerriesRouteSchedulesDayDeparturesActivity " + tab.getText(), 1);

                if (tab.getText().equals("Vessel Watch")) {

                    mAppBar.setExpanded(true, true);

                    AppBarLayout.LayoutParams params =
                            (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();
                    params.setScrollFlags(0);
                } else {

                    if (!isAccessibilityEnabled && !isExploreByTouchEnabled) {

                        mAppBar.setExpanded(true); // set expanded true so scroll flags take effect. Not sure why this works.
                        AppBarLayout.LayoutParams params =
                                (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();
                        params.setScrollFlags(
                                AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                                        | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
                                        | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED);
                    }

                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });


        terminalViewModel = ViewModelProviders.of(this, viewModelFactory).get(FerryTerminalViewModel.class);

        terminalCameraViewModel = ViewModelProviders.of(this, viewModelFactory).get(FerryTerminalCameraViewModel.class);

        scheduleViewModel = ViewModelProviders.of(this, viewModelFactory).get(FerryScheduleViewModel.class);
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

                // only request location on init load
                if (initLoad) {
                    requestLocation();
                } else {
                    setViewContent();
                }
            }
        });

        MyLogger.crashlyticsLog("Ferries", "Screen View", "FerriesRouteSchedulesDayDeparturesActivity " + title, 1);
        enableAds(getString(R.string.ferries_ad_target));

        // Accessibility
        if (isAccessibilityEnabled || isExploreByTouchEnabled){
            mAppBar.setExpanded(true, true);
            AppBarLayout.LayoutParams params =
                    (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();
            params.setScrollFlags(0);
        }

	}

	@Override
    public void onResume() {
        super.onResume();
        setFirebaseAnalyticsScreenName("FerryTimes");
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("dayIndex", mDayIndex);
        savedInstanceState.putInt("terminalIndex", mTerminalIndex);
    }

    /**
     * Updates global array adapter with new day strings from schedule
     * @param schedule ferry schedule data
     */
    private void setDaySpinner(ArrayList<FerriesScheduleDateItem> schedule){

	    mDayOfWeekArrayAdapter.clear();

        DateFormat dateFormat = new SimpleDateFormat("EEEE");
        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));

        int numDates = schedule.size();
        for (int i = 0; i < numDates; i++) {
            if (!schedule.get(i).getFerriesTerminalItem().isEmpty()) {
                mDayOfWeekArrayAdapter.add(dateFormat.format(schedule.get(i).getDate()));
            }
        }

        mDaySpinner.setSelection(mDayIndex);

    }

    /**
     * Updates global array adapter with new values from sailings
     * @param sailings A to B terminal sailings
     */
    private void setRouteSpinner(ArrayList<FerriesTerminalItem> sailings){

	    mSailingsArrayAdapter.clear();

        int numSailings = sailings.size();
        for (int i = 0; i < numSailings; i++) {
            mSailingsArrayAdapter.add(sailings.get(i).getDepartingTerminalName() + " to " + sailings.get(i).getArrivingTerminalName());
        }

        mSailingSpinner.setSelection(mTerminalIndex);
    }

    private void setViewContent() {

        mTerminalItem = mScheduleDateItems.get(mDayIndex).getFerriesTerminalItem().get(mTerminalIndex);

        setRouteSpinner(mScheduleDateItems.get(mDayIndex).getFerriesTerminalItem());
        setDaySpinner(mScheduleDateItems);

        if (initLoad) {
            initLoad = false;
            terminalViewModel.loadDepartureTimesForTerminal(mTerminalItem);
            terminalCameraViewModel.loadTerminalCameras(mTerminalItem.getDepartingTerminalID(), "ferries");
        }
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

    private int getTerminalIndexForTerminalClosestTo(double latitude, double longitude) {

        int closestTerminalIndex = 0;
        int minDistance = Integer.MAX_VALUE;

        int index = 0;

        for (FerriesTerminalItem terminalItem: mScheduleDateItems.get(mDayIndex).getFerriesTerminalItem()) {

            int distance = FerryHelper.getDistanceFromTerminal(terminalItem.getDepartingTerminalID(), latitude, longitude);
            if (distance < minDistance){
                minDistance = distance;
                closestTerminalIndex = index;
            }

            index++;
        }

        return closestTerminalIndex;
    }

    // Location logic
    /**
     * Request location updates after checking permissions first.
     */
    private void requestLocation() {
        if (ContextCompat.checkSelfPermission(FerriesRouteSchedulesDayDeparturesActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    FerriesRouteSchedulesDayDeparturesActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
                boolean hasSeenRationale = settings.getBoolean("KEY_SEEN_FERRY_LOCATION_RATIONALE", false);

                if (!hasSeenRationale) {
                    // Show explanation to user explaining why we need the permission
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Allow location services to let WSDOT select the nearest terminal.");
                    builder.setTitle("Find Nearest Departure Terminal?");
                    builder.setPositiveButton("OK", (dialog, which) -> ActivityCompat.requestPermissions(
                            FerriesRouteSchedulesDayDeparturesActivity.this,
                            new String[]{
                                    Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_ACCESS_FINE_LOCATION));
                    builder.setNegativeButton("no thanks", ((dialog, which) -> setViewContent()));
                    AlertDialog dialog = builder.show();
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.primary_default));
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.primary_default));
                } else {
                    setViewContent();
                }

                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("KEY_SEEN_FERRY_LOCATION_RATIONALE", true);
                editor.apply();

            } else {
                // No explanation needed, we can request the permission
                ActivityCompat.requestPermissions(FerriesRouteSchedulesDayDeparturesActivity.this,
                        new String[] {
                                Manifest.permission.ACCESS_FINE_LOCATION },
                        REQUEST_ACCESS_FINE_LOCATION);
            }
        } else { // We have permission!
            LocationServices.getFusedLocationProviderClient(this).getLastLocation().addOnSuccessListener(location -> {
                mLastLocation = location;
                if (mLastLocation != null) {
                    mTerminalIndex = getTerminalIndexForTerminalClosestTo(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                }
                setViewContent();
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 || permissions.length > 0) { // Check if request was canceled.
            switch (requestCode) {
                case REQUEST_ACCESS_FINE_LOCATION:
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // Permission granted, try and get location again!
                        requestLocation();
                    } else {
                        // Permission was denied or request was cancelled
                        // Just show the first terminal in the list
                        setViewContent();
                    }
                    break;
                default:
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    public void onLocationChanged(Location arg0) {
        // TODO Auto-generated method stub
    }

}