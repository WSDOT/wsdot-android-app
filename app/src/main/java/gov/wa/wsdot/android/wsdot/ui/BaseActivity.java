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
package gov.wa.wsdot.android.wsdot.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.firebase.analytics.FirebaseAnalytics;

import gov.wa.wsdot.android.wsdot.R;

public abstract class BaseActivity extends AppCompatActivity {

    private PublisherAdView mAdView;

    private FirebaseAnalytics mFirebaseAnalytics;

    /**
     * Check shardPref for an active event that might change the theme
     */
    @Override
    public Resources.Theme getTheme() {
        Resources.Theme theme = super.getTheme();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        int themeNumber = prefs.getInt(getString(R.string.set_theme_key), 0);

        // Check if a current event is changing the theme
        Boolean eventIsActive = prefs.getBoolean(getString(R.string.event_is_active), false);
        if (eventIsActive) {
            themeNumber = prefs.getInt(getString(R.string.event_theme_key), 0);
        }

        switch (themeNumber) {
            case 1: // worker safety
                theme.applyStyle(R.style.WSDOTWorkerOrange, true);
                break;
            default:
                theme.applyStyle(R.style.WSDOT, true);
        }

        return theme;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }
    
    @Override
    protected void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    protected void setFirebaseAnalyticsScreenName(String name) {
        if (mFirebaseAnalytics == null){
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
            mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);

        }

        // Bundle params = new Bundle();
        // params.putString("screen_name", name);
        // mFirebaseAnalytics.logEvent("wsdot_screen_view", params);

        mFirebaseAnalytics.setCurrentScreen(this, name, null);

    }

    /**
     * Initialize and display ads.
     */
    protected void enableAds(String target) {
        mAdView = findViewById(R.id.publisherAdView);

        PublisherAdRequest adRequest = new PublisherAdRequest.Builder()
                .addTestDevice(PublisherAdRequest.DEVICE_ID_EMULATOR) // All emulators
                .addCustomTargeting("wsdotapp", target)
                .build();

        mAdView.setVisibility(View.GONE);

        mAdView.setAdListener(new AdListener() {
            @Override public void onAdLoaded() {
                super.onAdLoaded();
                mAdView.setVisibility(View.VISIBLE);
            }
        });

        mAdView.loadAd(adRequest);
    }
    
    /**
     * Remove the ad so it doesn't take up any space.
     */
    protected void disableAds() {
        mAdView = (PublisherAdView) findViewById(R.id.publisherAdView);
        mAdView.setVisibility(View.GONE);
    }

}
