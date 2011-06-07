/*
 * Copyright (c) 2010 Washington State Department of Transportation
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

package gov.wa.wsdot.android.wsdot;

import gov.wa.wsdot.android.wsdot.shared.ForecastItem;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MountainPassItemForecast extends ListActivity {
	private ArrayList<ForecastItem> forecastItems;
	private MountainPassItemForecastAdapter adapter;
		
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        forecastItems = (ArrayList<ForecastItem>)getIntent().getSerializableExtra("Forecasts");
        this.adapter = new MountainPassItemForecastAdapter(this, R.layout.simple_list_item, forecastItems);
        setListAdapter(this.adapter);        
	}
		
	private class MountainPassItemForecastAdapter extends ArrayAdapter<ForecastItem> {
        private ArrayList<ForecastItem> items;

        public MountainPassItemForecastAdapter(Context context, int textViewResourceId, ArrayList<ForecastItem> items) {
                super(context, textViewResourceId, items);
                this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
	        View v = convertView;
	        if (v == null) {
	            LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            v = vi.inflate(R.layout.simple_list_item, null);
	        }
	        ForecastItem o = items.get(position);
	        if (o != null) {
	            TextView tt = (TextView) v.findViewById(R.id.title);
	            TextView bt = (TextView) v.findViewById(R.id.description);
	            if (tt != null) {
	            	tt.setText(o.getDay());
	            }
	            if(bt != null) {
            		bt.setText(o.getForecastText());
	            }
	        }
	        return v;
        }
	}
}
