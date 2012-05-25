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

import gov.wa.wsdot.android.wsdot.shared.FerriesAnnotationIndexesItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesAnnotationsItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesScheduleTimesItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesTerminalItem;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;

public class RouteScheduleDayDepartures extends SherlockListActivity {
	
	private static final String DEBUG_TAG = "RouteScheduleDayDepartures";
	private FerriesTerminalItem terminalItem;
	private ArrayList<FerriesAnnotationsItem> annotations = null;
	private ArrayList<FerriesScheduleTimesItem> times = null;
	private DepartureTimesAdapter adapter;
	private View mLoadingSpinner;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String terminalNames = getIntent().getStringExtra("terminalNames");

        getSupportActionBar().setTitle(terminalNames);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        setContentView(R.layout.fragment_list_with_spinner);
        mLoadingSpinner = findViewById(R.id.loading_spinner);
		
		terminalItem = (FerriesTerminalItem)getIntent().getSerializableExtra("terminalItems");
		times = new ArrayList<FerriesScheduleTimesItem>();
        this.adapter = new DepartureTimesAdapter(this, android.R.layout.simple_list_item_2, times);
        setListAdapter(this.adapter);
        new GetDepartureTimes().execute();
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
	
	private class GetDepartureTimes extends AsyncTask<String, Integer, String> {

		@Override
		protected void onPreExecute() {
			mLoadingSpinner.setVisibility(View.VISIBLE);
		}

		@Override
		protected String doInBackground(String... params) {
			int numAnnotations = terminalItem.getAnnotations().size();
	    	int numTimes = terminalItem.getScheduleTimes().size();
	    	annotations = new ArrayList<FerriesAnnotationsItem>();
	    	times = new ArrayList<FerriesScheduleTimesItem>();
			
	    	try {
	    		for (int i=0; i<numAnnotations; i++) {
	    			FerriesAnnotationsItem annotationItem = new FerriesAnnotationsItem();
	    			annotationItem.setAnnotation(terminalItem.getAnnotations().get(i).getAnnotation());
	    			annotations.add(annotationItem);
	    		}
	    		
				for (int i=0; i<numTimes; i++) {
					FerriesScheduleTimesItem timesItem = new FerriesScheduleTimesItem();
					timesItem.setDepartingTime(terminalItem.getScheduleTimes().get(i).getDepartingTime());
					
					for (int j=0; j<terminalItem.getScheduleTimes().get(i).getAnnotationIndexes().size(); j++) {
						FerriesAnnotationIndexesItem index = new FerriesAnnotationIndexesItem();
						index.setIndex(terminalItem.getScheduleTimes().get(i).getAnnotationIndexes().get(j).getIndex());
						timesItem.setAnnotationIndexes(index);
					}
					
					times.add(timesItem);
				}
			} catch (Exception e) {
				Log.e(DEBUG_TAG, "Error adding terminal departure times", e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			mLoadingSpinner.setVisibility(View.GONE);
			
            if (times != null && times.size() > 0) {
                adapter.notifyDataSetChanged();
                for(int i=0;i<times.size();i++)
                	adapter.add(times.get(i));
            }
            adapter.notifyDataSetChanged();
		}   
    }

	private class DepartureTimesAdapter extends ArrayAdapter<FerriesScheduleTimesItem> {
        private ArrayList<FerriesScheduleTimesItem> items;

        public DepartureTimesAdapter(Context context, int textViewResourceId, ArrayList<FerriesScheduleTimesItem> items) {
	        super(context, textViewResourceId, items);
	        this.items = items;
        }

        @SuppressWarnings("unused")
		public boolean areAllItemsSelectable() {
        	return false;
        }
        
        public boolean isEnabled(int position) {  
        	return false;  
        }        
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
	        DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
	        
	        if (convertView == null) {
	            convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, null);
	        }
	        
	        FerriesScheduleTimesItem o = items.get(position);
	        String annotation = "";

	        for (int i=0; i < items.get(position).getAnnotationIndexes().size(); i++) {
	        	FerriesAnnotationsItem p = annotations.get(items.get(position).getAnnotationIndexes().get(i).getIndex());
	        	annotation += p.getAnnotation();
	        }
	        
	        if (o != null) {
	            TextView tt = (TextView) convertView.findViewById(android.R.id.text1);
	            if(tt != null) {
	            	tt.setText(dateFormat.format(new Date(Long.parseLong(o.getDepartingTime()))));
	            }
	            TextView bt = (TextView) convertView.findViewById(android.R.id.text2);
	            if(bt != null) {
            		bt.setText(annotation);
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
