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

package gov.wa.wsdot.android.wsdot.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.ads.mediation.admob.AdMobExtras;

import gov.wa.wsdot.android.wsdot.R;

public abstract class BaseActivity extends AppCompatActivity {

    private PublisherAdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    /**
     * Initialize and display ads.
     */
    protected void enableAds(String target) {
        mAdView = (PublisherAdView) findViewById(R.id.publisherAdView);

        PublisherAdRequest adRequest = new PublisherAdRequest.Builder()
                //.addTestDevice(PublisherAdRequest.DEVICE_ID_EMULATOR) // All emulators
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
