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

import gov.wa.wsdot.android.wsdot.shared.FerriesScheduleDateItem;
import gov.wa.wsdot.android.wsdot.shared.FerriesTerminalItem;

import java.util.ArrayList;

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

public class RouteSchedulesDaySailings extends SherlockListActivity {
	
	private static final String DEBUG_TAG = "RouteSchedulesDaySailings";
	private FerriesScheduleDateItem scheduleDateItems;
	private ArrayList<FerriesTerminalItem> terminalItems = null;
	private SailingsAdapter adapter;
	private View mLoadingSpinner;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String dayOfWeek = getIntent().getStringExtra("dayOfWeek");
		
        getSupportActionBar().setTitle(dayOfWeek);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        setContentView(R.layout.fragment_list_with_spinner);
        mLoadingSpinner = findViewById(R.id.loading_spinner);		
		
		scheduleDateItems = (FerriesScheduleDateItem)getIntent().getSerializableExtra("scheduleDateItems");
		terminalItems = new ArrayList<FerriesTerminalItem>();
        this.adapter = new SailingsAdapter(this, android.R.layout.simple_list_item_1, terminalItems);
        setListAdapter(this.adapter);
        new GetTerminals().execute();
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
	
    private class GetTerminals extends AsyncTask<String, Integer, String> {

		@Override
		protected void onPreExecute() {
			mLoadingSpinner.setVisibility(View.VISIBLE);
		}

		@Override
		protected String doInBackground(String... params) {
	    	int numTerminals = scheduleDateItems.getFerriesTerminalItem().size();
	    	terminalItems = new ArrayList<FerriesTerminalItem>();
			
	    	try {   		
				for (int i=0; i<numTerminals; i++) {
					FerriesTerminalItem terminalItem = new FerriesTerminalItem();
					terminalItem.setArrivingTerminalID(scheduleDateItems.getFerriesTerminalItem().get(i).getArrivingTerminalID());
					terminalItem.setArrivingTerminalName(scheduleDateItems.getFerriesTerminalItem().get(i).getArrivingTerminalName());
					terminalItem.setDepartingTerminalID(scheduleDateItems.getFerriesTerminalItem().get(i).getDepartingTerminalID());
					terminalItem.setDepartingTerminalName(scheduleDateItems.getFerriesTerminalItem().get(i).getDepartingTerminalName());

					for (int j=0; j<scheduleDateItems.getFerriesTerminalItem().get(i).getAnnotations().size(); j++) {
						terminalItem.setAnnotations(scheduleDateItems.getFerriesTerminalItem().get(i).getAnnotations().get(j));
					}					
					
					for (int k=0; k<scheduleDateItems.getFerriesTerminalItem().get(i).getScheduleTimes().size(); k++) {
						terminalItem.setScheduleTimes(scheduleDateItems.getFerriesTerminalItem().get(i).getScheduleTimes().get(k));
					}				
					
					terminalItems.add(terminalItem);
				}
			} catch (Exception e) {
				Log.e(DEBUG_TAG, "Error adding terminal info", e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			mLoadingSpinner.setVisibility(View.GONE);
			
            if (terminalItems != null && terminalItems.size() > 0) {
                adapter.notifyDataSetChanged();
                for(int i=0;i<terminalItems.size();i++)
                	adapter.add(terminalItems.get(i));
            }
            adapter.notifyDataSetChanged();
		}   
    }

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		String terminalNames = terminalItems.get(position).getDepartingTerminalName() + " to " +
				terminalItems.get(position).getArrivingTerminalName();
		Bundle b = new Bundle();
		Intent intent = new Intent(this, RouteScheduleDayDepartures.class);
		b.putString("terminalNames", terminalNames);
		b.putSerializable("terminalItems", terminalItems.get(position));
		intent.putExtras(b);
		startActivity(intent);		
	}
    
	private class SailingsAdapter extends ArrayAdapter<FerriesTerminalItem> {
        private ArrayList<FerriesTerminalItem> items;

        public SailingsAdapter(Context context, int textViewResourceId, ArrayList<FerriesTerminalItem> items) {
	        super(context, textViewResourceId, items);
	        this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
	        if (convertView == null) {
	            convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
	        }
	        FerriesTerminalItem o = items.get(position);
	        if (o != null) {
	            TextView tt = (TextView) convertView.findViewById(android.R.id.text1);
	            if(tt != null) {
	            	tt.setText(o.getDepartingTerminalName() + " to " + o.getArrivingTerminalName());
	            }
	        }
	        return convertView;
        }
	}
	
	public static class ViewHolder {
		public TextView tt;
	}
}
