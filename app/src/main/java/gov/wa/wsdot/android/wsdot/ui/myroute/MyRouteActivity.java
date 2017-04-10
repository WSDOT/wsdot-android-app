package gov.wa.wsdot.android.wsdot.ui.myroute;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.accessibility.AccessibilityManager;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.ui.WsdotApplication;
import gov.wa.wsdot.android.wsdot.ui.myroute.newroute.NewRouteActivity;

public class MyRouteActivity extends FindFavoritesOnRouteActivity
        implements RouteOptionsDialogFragment.Listener {

    private MountainPassesSyncReceiver mMountainPassesSyncReceiver;
    private FerriesSchedulesSyncReceiver mFerriesSchedulesSyncReceiver;
    private TravelTimesSyncReceiver mTravelTimesSyncReceiver;
    private CamerasSyncReceiver mCamerasSyncReceiver;
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_route);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // GA tracker
                mTracker = ((WsdotApplication) getApplication()).getDefaultTracker();
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Button Tap")
                        .setAction("Create New Route")
                        .setLabel("My Routes")
                        .build());
                startActivity(new Intent(MyRouteActivity.this, NewRouteActivity.class));
            }
        });

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        boolean seenTip = settings.getBoolean("KEY_SEEN_NEW_MY_ROUTE_TIP", false);

        AccessibilityManager am = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        if (!seenTip && !am.isEnabled()) {
            TapTargetView.showFor(this, // `this` is an Activity
                    TapTarget.forView(fab, "Create your first route", "Track your commute using your phone's GPS to get a personal list of highway alerts on your route.")
                            // All options below are optional
                            .outerCircleColor(R.color.primary)      // Specify a color for the outer circle
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
                            super.onTargetClick(view);      // This call is optional
                            startActivity(new Intent(MyRouteActivity.this, NewRouteActivity.class));
                        }
                    });
        }

        settings.edit().putBoolean("KEY_SEEN_NEW_MY_ROUTE_TIP", true).apply();
    }

    @Override
    public void onResume(){
        super.onResume();
        // Ferries Route Schedules
        IntentFilter ferriesSchedulesFilter = new IntentFilter(
                "gov.wa.wsdot.android.wsdot.intent.action.FERRIES_SCHEDULES_RESPONSE");
        ferriesSchedulesFilter.addCategory(Intent.CATEGORY_DEFAULT);
        mFerriesSchedulesSyncReceiver = new FerriesSchedulesSyncReceiver();
        registerReceiver(mFerriesSchedulesSyncReceiver, ferriesSchedulesFilter);

        // Mountain Passes
        IntentFilter mountainPassesFilter = new IntentFilter(
                "gov.wa.wsdot.android.wsdot.intent.action.MOUNTAIN_PASSES_RESPONSE");
        mountainPassesFilter.addCategory(Intent.CATEGORY_DEFAULT);
        mMountainPassesSyncReceiver = new MountainPassesSyncReceiver();
        registerReceiver(mMountainPassesSyncReceiver, mountainPassesFilter);

        // Travel Times
        IntentFilter travelTimesFilter = new IntentFilter(
                "gov.wa.wsdot.android.wsdot.intent.action.TRAVEL_TIMES_RESPONSE");
        travelTimesFilter.addCategory(Intent.CATEGORY_DEFAULT);
        mTravelTimesSyncReceiver = new TravelTimesSyncReceiver();
        registerReceiver(mTravelTimesSyncReceiver, travelTimesFilter);

        // Cameras
        IntentFilter camerasFilter = new IntentFilter(
                "gov.wa.wsdot.android.wsdot.intent.action.CAMERAS_RESPONSE");
        camerasFilter.addCategory(Intent.CATEGORY_DEFAULT);
        mCamerasSyncReceiver = new CamerasSyncReceiver();
        registerReceiver(mCamerasSyncReceiver, camerasFilter);
    }

    @Override
    public void onPause(){
        super.onPause();
        unregisterReceiver(mFerriesSchedulesSyncReceiver);
        unregisterReceiver(mMountainPassesSyncReceiver);
        unregisterReceiver(mTravelTimesSyncReceiver);
        unregisterReceiver(mCamerasSyncReceiver);
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

    public void onOptionClicked(long routeID, int position) {
        MyRouteFragment fragment = (MyRouteFragment) getSupportFragmentManager().findFragmentById(R.id.my_route_fragment);
        fragment.onOptionSelected(routeID, position);
    }

    @Override
    protected void taskComplete() {
        MyRouteFragment fragment = (MyRouteFragment) getSupportFragmentManager().findFragmentById(R.id.my_route_fragment);
        fragment.myTaskComplete();
    }

    @Override
    protected List<LatLng> getRoute() {
        MyRouteFragment fragment = (MyRouteFragment) getSupportFragmentManager().findFragmentById(R.id.my_route_fragment);
        return fragment.myGetRoute();
    }
}