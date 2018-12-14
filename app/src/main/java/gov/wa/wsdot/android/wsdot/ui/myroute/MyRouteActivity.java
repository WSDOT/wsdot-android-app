package gov.wa.wsdot.android.wsdot.ui.myroute;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.accessibility.AccessibilityManager;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.ui.BaseActivity;
import gov.wa.wsdot.android.wsdot.ui.myroute.newroute.NewRouteActivity;

public class MyRouteActivity extends BaseActivity implements RouteOptionsDialogFragment.Listener {

    final String TAG = MyRouteActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_route);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            startActivity(new Intent(MyRouteActivity.this, NewRouteActivity.class));
        });

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        boolean seenTip = settings.getBoolean("KEY_SEEN_NEW_MY_ROUTE_TIP", false);

        AccessibilityManager am = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);

        if (!seenTip && !am.isEnabled()) {
            try {
            TapTargetView.showFor(this, // `this` is an Activity
                    TapTarget.forView(fab, "Create your first route", "Track your commute using your phone's GPS to get a personal list of highway alerts on your route.")
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
                            super.onTargetClick(view);      // This call is optional
                            startActivity(new Intent(MyRouteActivity.this, NewRouteActivity.class));
                        }
                    });
            } catch (NullPointerException e){
                Log.e(TAG, "Null pointer exception while trying to show tip view");
            }
        }

        settings.edit().putBoolean("KEY_SEEN_NEW_MY_ROUTE_TIP", true).apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        setFirebaseAnalyticsScreenName("MyRoutes");
    }

    public void onOptionClicked(long routeID, String routeName, int position) {
        MyRouteFragment fragment = (MyRouteFragment) getSupportFragmentManager().findFragmentById(R.id.my_route_fragment);
        fragment.onOptionSelected(routeID, routeName, position);
    }
}