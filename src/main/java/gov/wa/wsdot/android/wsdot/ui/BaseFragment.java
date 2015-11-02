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

import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import gov.wa.wsdot.android.wsdot.R;

public abstract class BaseFragment extends Fragment {
    
    private PublisherAdView mAdView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }
    
    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    /**
     * Initialize and display ads
     */
    protected void enableAds(ViewGroup root) {
        mAdView = (PublisherAdView) root.findViewById(R.id.publisherAdView);
        PublisherAdRequest adRequest = new PublisherAdRequest.Builder()
                .addTestDevice(PublisherAdRequest.DEVICE_ID_EMULATOR) // All emulators
                .addTestDevice("5E9B6B34DD2AE096509E9B879ECEE667") // My Nexus 5
                .build();
        mAdView.loadAd(adRequest);
    }
    
    /**
     * Remove the ad so it doesn't take up any space.
     */
    protected void disableAds(ViewGroup root) {
        mAdView = (PublisherAdView) root.findViewById(R.id.publisherAdView);
        mAdView.setVisibility(View.GONE);
    }
}
