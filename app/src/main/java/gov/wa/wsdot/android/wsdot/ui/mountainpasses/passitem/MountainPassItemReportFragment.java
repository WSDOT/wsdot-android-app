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

package gov.wa.wsdot.android.wsdot.ui.mountainpasses.passitem;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import javax.inject.Inject;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.ui.mountainpasses.MountainPassViewModel;

public class MountainPassItemReportFragment extends BaseFragment implements Injectable {

    private static final String TAG = MountainPassItemReportFragment.class.getSimpleName();	
	private ViewGroup mRootView;
	private String mWeatherCondition;
	private String mTemperatureInFahrenheit;
	private String mElevationInFeet;
	private String mRoadCondition;
	private String mRestrictionOneTravelDirection;
	private String mRestrictionOneText;
	private String mRestrictionTwoTravelDirection;
	private String mRestrictionTwoText;
	private String mDateUpdated;
    private int mPassId;

    private static MountainPassViewModel viewModel;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getActivity().getIntent().getExtras();
        mPassId = args.getInt("id");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = (ViewGroup) inflater.inflate(R.layout.mountainpass_item_details, null);

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MountainPassViewModel.class);

        viewModel.getPassFor(mPassId).observe(getViewLifecycleOwner(), pass -> {
            if (pass != null){

                mWeatherCondition = pass.getWeatherCondition();
                mTemperatureInFahrenheit = pass.getTemperature();

                if (mWeatherCondition.equals("")) mWeatherCondition = "Not available";
                if (mTemperatureInFahrenheit.equals("null")) {
                    mTemperatureInFahrenheit = "Not available";
                } else {
                    mTemperatureInFahrenheit = mTemperatureInFahrenheit + "\u00b0F";
                }

                mDateUpdated = pass.getDateUpdated();
                mElevationInFeet = pass.getElevation();

                mRoadCondition = pass.getRoadCondition();
                mRestrictionOneTravelDirection = pass.getRestrictionOneDirection();
                mRestrictionOneText = pass.getRestrictionOne();
                mRestrictionTwoTravelDirection = pass.getRestrictionTwoDirection();
                mRestrictionTwoText = pass.getRestrictionTwo();

                setUpView();
            }
        });

        return mRootView;		
	}

	private void setUpView() {
		Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
		Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");

		TextView date_updated = (TextView)mRootView.findViewById(R.id.date_updated);
		date_updated.setTypeface(tf);
		date_updated.setText(mDateUpdated);

		TextView weather_condition_header = (TextView)mRootView.findViewById(R.id.weather_condition_title);
		weather_condition_header.setTypeface(tfb);
		weather_condition_header.setText("Weather:");

		TextView weather_condition_text = (TextView)mRootView.findViewById(R.id.weather_condition_text);
		weather_condition_text.setTypeface(tf);
		weather_condition_text.setText(mWeatherCondition);

		TextView temperature_header = (TextView)mRootView.findViewById(R.id.temperature_title);
		temperature_header.setTypeface(tfb);
		temperature_header.setText("Temperature:");

		TextView temperature_text = (TextView)mRootView.findViewById(R.id.temperature_text);
		temperature_text.setTypeface(tf);
		temperature_text.setText(mTemperatureInFahrenheit);

		TextView elevation_header = (TextView)mRootView.findViewById(R.id.elevation_title);
		elevation_header.setTypeface(tfb);
		elevation_header.setText("Elevation:");

		TextView elevation_text = (TextView)mRootView.findViewById(R.id.elevation_text);
		elevation_text.setTypeface(tf);
		elevation_text.setText(mElevationInFeet + " ft");

		TextView road_condition_header = (TextView)mRootView.findViewById(R.id.road_condition_title);
		road_condition_header.setTypeface(tfb);
		road_condition_header.setText("Conditions:");

		TextView road_condition_text = (TextView)mRootView.findViewById(R.id.road_condition_text);
		road_condition_text.setTypeface(tf);
		road_condition_text.setText(mRoadCondition);

		TextView restriction_one_header = (TextView)mRootView.findViewById(R.id.restriction_one_title);
		restriction_one_header.setTypeface(tfb);
		restriction_one_header.setText("Restrictions " + mRestrictionOneTravelDirection + ":");

		TextView restriction_one_text = (TextView)mRootView.findViewById(R.id.restriction_one_text);
		restriction_one_text.setTypeface(tf);
		restriction_one_text.setText(mRestrictionOneText);

		TextView restriction_two_header = (TextView)mRootView.findViewById(R.id.restriction_two_title);
		restriction_two_header.setTypeface(tfb);
		restriction_two_header.setText("Restrictions " + mRestrictionTwoTravelDirection + ":");

		TextView restriction_two_text = (TextView)mRootView.findViewById(R.id.restriction_two_text);
		restriction_two_text.setTypeface(tf);
		restriction_two_text.setText(mRestrictionTwoText);

		disableAds(mRootView);

	}
}
