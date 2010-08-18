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

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class RouteSchedulesDaySailings extends ListActivity {
	
	private static final String DEBUG_TAG = "RouteSchedulesDaySailings";
	private FerriesScheduleDateItem scheduleDateItems;
	private ArrayList<FerriesTerminalItem> terminalItems = null;
	private SailingsAdapter adapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		scheduleDateItems = (FerriesScheduleDateItem)getIntent().getSerializableExtra("scheduleDateItems");
		setContentView(R.layout.main);
		((TextView)findViewById(R.id.sub_section)).setText("Ferries Route Schedules");
		terminalItems = new ArrayList<FerriesTerminalItem>();
        this.adapter = new SailingsAdapter(this, android.R.layout.simple_list_item_1, terminalItems);
        setListAdapter(this.adapter);
        new GetTerminals().execute();
	}
	
    private class GetTerminals extends AsyncTask<String, Integer, String> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
		}
    	
		@Override
		protected void onProgressUpdate(Integer... progress) {
			// TODO Auto-generated method stub
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
		Bundle b = new Bundle();
		Intent intent = new Intent(this, RouteScheduleDayDepartures.class);
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
	        View v = convertView;
	        
	        if (v == null) {
	            LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            v = vi.inflate(android.R.layout.simple_list_item_1, null);
	        }
	        FerriesTerminalItem o = items.get(position);
	        if (o != null) {
	            TextView tt = (TextView) v.findViewById(android.R.id.text1);
	            if(tt != null) {
	            	tt.setText(o.getDepartingTerminalName() + " to " + o.getArrivingTerminalName());
	            }
	        }
	        return v;
        }
	}
    

}
