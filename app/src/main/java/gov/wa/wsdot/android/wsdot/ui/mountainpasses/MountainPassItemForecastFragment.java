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

package gov.wa.wsdot.android.wsdot.ui.mountainpasses;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.shared.ForecastItem;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.util.decoration.SimpleDividerItemDecoration;

public class MountainPassItemForecastFragment extends BaseFragment {
	
    private static final String TAG = MountainPassItemForecastFragment.class.getSimpleName();
    private ArrayList<ForecastItem> forecastItems;
	private MountainPassItemForecastAdapter mAdapter;
	private static String forecastsArray;

    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		Bundle args = activity.getIntent().getExtras();
		forecastsArray = args.getString("Forecasts");
        
        JSONArray forecasts;
        ForecastItem f = null;
        forecastItems = new ArrayList<ForecastItem>();
        
        try {
			forecasts = new JSONArray(forecastsArray);
			int numForecasts = forecasts.length();
			for (int i=0; i < numForecasts; i++) {
				JSONObject forecast = forecasts.getJSONObject(i);
				f = new ForecastItem();
				f.setDay(forecast.getString("Day"));
				f.setForecastText(forecast.getString("ForecastText"));
				f.setWeatherIcon(forecast.getInt("weather_icon"));
				forecastItems.add(f);
			}
        } catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_recycler_with_spinner, null);

        mRecyclerView = (RecyclerView) root.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        this.mAdapter = new MountainPassItemForecastAdapter(getActivity(), forecastItems);

        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));

        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        
    	return root;
	}

	/**
	 * Custom adapter for items in recycler view.
	 *
	 * Extending RecyclerView adapter this adapter binds the custom ViewHolder
	 * class to it's data.
	 *
	 * @see android.support.v7.widget.RecyclerView.Adapter
	 */
	private class MountainPassItemForecastAdapter extends RecyclerView.Adapter<ForecastViewHolder> {


		private ArrayList<ForecastItem> items;

		public MountainPassItemForecastAdapter(Context context, ArrayList<ForecastItem> data) {
			this.items = data;
		}

		@Override
		public ForecastViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

			View itemView = LayoutInflater.
					from(parent.getContext()).
					inflate(R.layout.simple_list_item_with_icon, parent, false);
			return new ForecastViewHolder(itemView);

		}

		@Override
		public void onBindViewHolder(ForecastViewHolder viewholder, int position) {

            ForecastItem o = items.get(position);

            viewholder.title.setText(o.getDay());
            viewholder.text.setText(o.getForecastText());
            viewholder.icon.setImageResource(o.getWeatherIcon());
		}

		@Override
		public int getItemCount() {
			if (items != null) {
				return items.size();
			}
			return 0;
		}
		public void setData(ArrayList<ForecastItem> data) {
			this.items = data;
			notifyDataSetChanged();
		}

		public void clear() {
			if (items != null) {
				this.items.clear();
				notifyDataSetChanged();
			}
		}
	}

	public static class ForecastViewHolder extends RecyclerView.ViewHolder {
        protected TextView title;
        protected TextView text;
        protected ImageView icon;

		public ForecastViewHolder(View itemView) {
			super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            text = (TextView) itemView.findViewById(R.id.text);
			icon = (ImageView) itemView.findViewById(R.id.icon);
		}
	}
}
