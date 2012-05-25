/*
 * Copyright (c) 2012 Washington State Department of Transportation
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

import gov.wa.wsdot.android.wsdot.shared.FerriesRouteItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesScheduleDateItem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;

public class RouteSchedulesDays extends SherlockListActivity {

	private static final String DEBUG_TAG = "RouteSchedulesDays";
	private FerriesRouteItem routeItems;
	private ArrayList<FerriesScheduleDateItem> scheduleDateItems = null;
	private DaysOfWeekAdapter adapter;
	DateFormat dateFormat = new SimpleDateFormat("EEEE");
	DateFormat subTitleDateFormat = new SimpleDateFormat("MMMM d");
	private View mLoadingSpinner;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String description = getIntent().getStringExtra("description");
		
        getSupportActionBar().setTitle(description);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        setContentView(R.layout.fragment_list_with_spinner);
        mLoadingSpinner = findViewById(R.id.loading_spinner);
		
		routeItems = (FerriesRouteItem)getIntent().getSerializableExtra("routeItems");
		scheduleDateItems = new ArrayList<FerriesScheduleDateItem>();
        this.adapter = new DaysOfWeekAdapter(this, android.R.layout.simple_list_item_2, scheduleDateItems);
        setListAdapter(this.adapter);
        new GetDates().execute();
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
	
    private class GetDates extends AsyncTask<String, Integer, String> {

		@Override
		protected void onPreExecute() {
			mLoadingSpinner.setVisibility(View.VISIBLE);
		}

		@Override
		protected String doInBackground(String... params) {
	    	int numDates = routeItems.getFerriesScheduleDateItem().size();
	    	scheduleDateItems = new ArrayList<FerriesScheduleDateItem>();
			
	    	try {   		
				for (int i=0; i<numDates; i++) {
					FerriesScheduleDateItem scheduleDateItem = new FerriesScheduleDateItem();
					scheduleDateItem.setDate(routeItems.getFerriesScheduleDateItem().get(i).getDate());
					for (int j=0; j<routeItems.getFerriesScheduleDateItem().get(i).getFerriesTerminalItem().size(); j++) {
						scheduleDateItem.setFerriesTerminalItem(routeItems.getFerriesScheduleDateItem().get(i).getFerriesTerminalItem().get(j));
					}
					scheduleDateItems.add(scheduleDateItem);
				}
			} catch (Exception e) {
				Log.e(DEBUG_TAG, "Error adding dates", e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			mLoadingSpinner.setVisibility(View.GONE);
			
            if (scheduleDateItems != null && scheduleDateItems.size() > 0) {
                adapter.notifyDataSetChanged();
                for(int i=0;i<scheduleDateItems.size();i++)
                	adapter.add(scheduleDateItems.get(i));
            }
            adapter.notifyDataSetChanged();
		}   
    }	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		Bundle b = new Bundle();
		Intent intent = new Intent(this, RouteSchedulesDaySailings.class);
		b.putString("dayOfWeek", dateFormat.format(new Date(Long.parseLong(scheduleDateItems.get(position).getDate()))));
		b.putSerializable("scheduleDateItems", scheduleDateItems.get(position));
		intent.putExtras(b);
		startActivity(intent);		
	}

	private class DaysOfWeekAdapter extends ArrayAdapter<FerriesScheduleDateItem> {
        private ArrayList<FerriesScheduleDateItem> items;

        public DaysOfWeekAdapter(Context context, int textViewResourceId, ArrayList<FerriesScheduleDateItem> items) {
	        super(context, textViewResourceId, items);
	        this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
	        if (convertView == null) {
	            convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, null);
	        }
	        FerriesScheduleDateItem o = items.get(position);
	        if (o != null) {
	            TextView tt = (TextView) convertView.findViewById(android.R.id.text1);
	            TextView bt = (TextView) convertView.findViewById(android.R.id.text2);
	            if(tt != null) {
	            	tt.setText(dateFormat.format(new Date(Long.parseLong(o.getDate()))));
	            }
	            if (bt != null) {
	            	try {
	            		Date date = new Date(Long.parseLong(o.getDate()));
	            		bt.setText(subTitleDateFormat.format(date));
	            	} catch (Exception e) {
	            		Log.e(DEBUG_TAG, "Error parsing date", e);
	            	}
	            }
	        }
	        return convertView;
        }
	}
	
	public static class ViewHolder {
		public TextView tt;
		public TextView bt;
	}
}
