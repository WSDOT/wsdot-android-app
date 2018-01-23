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

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.viewpagerindicator.LinePageIndicator;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.database.highwayalerts.HighwayAlertEntity;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.ui.alert.detail.HighwayAlertDetailsActivity;
import gov.wa.wsdot.android.wsdot.ui.trafficmap.alertsinarea.HighwayAlertViewModel;
import gov.wa.wsdot.android.wsdot.util.MyLogger;

public class HighImpactAlertsFragment extends BaseFragment implements
        Injectable {

    static final String TAG = HighImpactAlertsFragment.class.getSimpleName();

	private ViewGroup mRootView;
    private ViewPagerAdapter mAdapter;
    private ViewPager mPager;
    private LinePageIndicator mIndicator;
	private View mLoadingSpinner;
	private List<HighwayAlertEntity> alertItems = new ArrayList<>();
	private Handler mHandler = new Handler();

	private static HighwayAlertViewModel viewModel;

	@Inject
	ViewModelProvider.Factory viewModelFactory;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(HighwayAlertViewModel.class);

        viewModel.init(HighwayAlertViewModel.AlertPriority.HIGHEST);

        viewModel.getResourceStatus().observe(this, resourceStatus -> {
            if (resourceStatus != null) {
                switch (resourceStatus.status) {
                    case LOADING:
                        mLoadingSpinner.setVisibility(View.VISIBLE);
                        break;
                    case SUCCESS:
                        mLoadingSpinner.setVisibility(View.GONE);
                        break;
                    case ERROR:
                        alertItems.clear();
                        mLoadingSpinner.setVisibility(View.GONE);
                        HighwayAlertEntity item = new HighwayAlertEntity();
                        item.setCategory("error");
                        item.setHeadline(resourceStatus.message);
                        alertItems.add(item);

                        mAdapter = new ViewPagerAdapter(alertItems);
                        mPager.setAdapter(mAdapter);
                        mIndicator.setViewPager(mPager);

                        mPager.setVisibility(View.VISIBLE);
                        mIndicator.setVisibility(View.VISIBLE);
                }
            }
        });

        viewModel.getHighwayAlerts().observe(this, highwayAlerts -> {

            if (highwayAlerts != null) {

                alertItems.clear();
                alertItems = highwayAlerts;

                if (alertItems.size() == 0) {
                    HighwayAlertEntity item = new HighwayAlertEntity();
                    item.setCategory("empty");
                    alertItems.add(item);
                }

                mAdapter = new ViewPagerAdapter(alertItems);
                mPager.setAdapter(mAdapter);
                mIndicator.setViewPager(mPager);
                mIndicator.setCurrentItem(0);

                mPager.setVisibility(View.VISIBLE);
                mIndicator.setVisibility(View.VISIBLE);

            }
        });
    }

	@Override
	public void onResume() {
		super.onResume();
        mHandler.postDelayed(runnable, (DateUtils.MINUTE_IN_MILLIS)); // Check every minute.
	}

	@Override
	public void onPause() {
		super.onPause();
		mHandler.removeCallbacks(runnable);
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_high_impact_alerts, container);
        mLoadingSpinner = mRootView.findViewById(R.id.loading_spinner);
        mPager = mRootView.findViewById(R.id.pager);
        mIndicator = mRootView.findViewById(R.id.indicator);

        mLoadingSpinner.setVisibility(View.VISIBLE);

        return mRootView;
    }
	
    private Runnable runnable = new Runnable() {
        public void run() {
        	viewModel.forceRefreshHighwayAlerts();
			mHandler.postDelayed(runnable, (DateUtils.MINUTE_IN_MILLIS)); // Check every minute.
        }
    };

	public class ViewPagerAdapter extends PagerAdapter {
		private List<HighwayAlertEntity> items;
        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
	 
	    public ViewPagerAdapter(List<HighwayAlertEntity> items) {
	        this.items = items;
	    }
	  
	    @Override
	    public int getCount() {
	        return items.size();
	    }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view;

            String category = items.get(position).getCategory();

            if (category.equalsIgnoreCase("empty")) {
                view = getActivity().getLayoutInflater().inflate(R.layout.high_impact_alerts_inactive, null);
            } else if (category.equalsIgnoreCase("error")) {
                view = getActivity().getLayoutInflater().inflate(R.layout.high_impact_alerts_inactive, null);
                TextView title = view.findViewById(R.id.title_alert);
                title.setTypeface(tf);
                title.setText(items.get(position).getHeadline());
            } else {
                view = getActivity().getLayoutInflater().inflate(R.layout.high_impact_alerts_active, null);
                view.setOnClickListener(v -> {
                    MyLogger.crashlyticsLog("Home", "Tap", "Highway Alert Details", 1);
                    Bundle b = new Bundle();
                    Intent intent = new Intent(getActivity(), HighwayAlertDetailsActivity.class);
                    b.putInt("id", items.get(position).getAlertId());
                    intent.putExtras(b);
                    startActivity(intent);
                });
                TextView title = view.findViewById(R.id.title_alert);
                title.setTypeface(tf);
                title.setText(items.get(position).getHeadline());
            }

            if (getCount() < 2) mIndicator.setVisibility(View.GONE);

            container.addView(view, 0);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View)object);
        }
	 
	    @Override
	    public boolean isViewFromObject(View view, Object object) {
	        return view.equals(object);
	    }
	}
}
