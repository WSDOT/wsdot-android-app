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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SeattleTrafficTravelTimes extends ListActivity {
	private static final String DEBUG_TAG = "TravelTimes";
	private ArrayList<TravelTimesItem> travelTimesItems = null;
	private TravelTimesItemAdapter adapter;
	
	//private HashMap<Integer, Integer> routeImage = new HashMap<Integer, Integer>();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seattle_incidents);
        travelTimesItems = new ArrayList<TravelTimesItem>();
        this.adapter = new TravelTimesItemAdapter(this, R.layout.traveltimes_item, travelTimesItems);
        setListAdapter(this.adapter);
        
        new GetTravelTimesItems().execute();
    }
	
    private class GetTravelTimesItems extends AsyncTask<String, Integer, String> {
    	private final ProgressDialog dialog = new ProgressDialog(SeattleTrafficTravelTimes.this);

		@Override
		protected void onPreExecute() {
	        this.dialog.setMessage("Retrieving travel times ...");
	        this.dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	        this.dialog.setCancelable(true);
	        this.dialog.setMax(119);
	        this.dialog.show();
		}
    	
		@Override
		protected void onProgressUpdate(Integer... progress) {
			this.dialog.incrementProgressBy(progress[0]);
		}

		@Override
		protected String doInBackground(String... params) {
			try {
				URL url = new URL("http://data.wsdot.wa.gov/mobile/TravelTimes.js");
				URLConnection urlConn = url.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
				String jsonFile = "";
				String line;
				
				while ((line = in.readLine()) != null)
					jsonFile += line;
				in.close();
				
				JSONObject obj = new JSONObject(jsonFile);
				JSONObject result = obj.getJSONObject("traveltimes");
				JSONArray items = result.getJSONArray("items");
				travelTimesItems = new ArrayList<TravelTimesItem>();
				TravelTimesItem i = null;
							
				for (int j=0; j < items.length(); j++) {
					JSONObject item = items.getJSONObject(j);
					i = new TravelTimesItem();
					i.setTitle(item.getString("title"));
					i.setCurrentTime(Integer.toString(item.getInt("current")) + " min");
					travelTimesItems.add(i);
					publishProgress(1);
				}			

			} catch (Exception e) {
				Log.e(DEBUG_TAG, "Error in network call", e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if (this.dialog.isShowing()) {
				this.dialog.dismiss();
			}
            if (travelTimesItems != null && travelTimesItems.size() > 0) {
                adapter.notifyDataSetChanged();
                for (int i=0; i < travelTimesItems.size(); i++)
                adapter.add(travelTimesItems.get(i));
            }
            adapter.notifyDataSetChanged();
		}   
    }
    
	private class TravelTimesItemAdapter extends ArrayAdapter<TravelTimesItem> {
        private ArrayList<TravelTimesItem> items;

        public TravelTimesItemAdapter(Context context, int textViewResourceId, ArrayList<TravelTimesItem> items) {
	        super(context, textViewResourceId, items);
	        this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
	        View v = convertView;
	        if (v == null) {
	            LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            v = vi.inflate(R.layout.traveltimes_item, null);
	        }
	        TravelTimesItem o = items.get(position);
	        if (o != null) {
	            TextView tt = (TextView) v.findViewById(R.id.toptext);
	            TextView bt = (TextView) v.findViewById(R.id.bottomtext);
	            if (tt != null) {
	            	tt.setText(o.getTitle());
	            }
	            if(bt != null) {
            		bt.setText(o.getCurrentTime());
	            }
	        }
	        return v;
        }
	}
}