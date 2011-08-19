/*
 * Copyright (c) 2011 Washington State Department of Transportation
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

import gov.wa.wsdot.android.wsdot.shared.TravelTimesItem;
import gov.wa.wsdot.android.wsdot.util.AnalyticsUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SeattleTrafficTravelTimes extends ListActivity {
	private static final String DEBUG_TAG = "TravelTimes";
	private ArrayList<TravelTimesItem> travelTimesItems = null;
	private TravelTimesItemAdapter adapter;
	
	//private HashMap<Integer, Integer> routeImage = new HashMap<Integer, Integer>();
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        AnalyticsUtils.getInstance(this).trackPageView("/Traffic Map/Seattle/Travel Times");
        
        setContentView(R.layout.main);
        ((TextView)findViewById(R.id.sub_section)).setText("Seattle Area Travel Times");
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
	        this.dialog.setMax(119);
			this.dialog.setOnCancelListener(new OnCancelListener() {
	            public void onCancel(DialogInterface dialog) {
	                cancel(true);
	            }
			});
	        this.dialog.show();
		}
    	
	    protected void onCancelled() {
	        Toast.makeText(SeattleTrafficTravelTimes.this, "Cancelled", Toast.LENGTH_SHORT).show();
	    }
		
		@Override
		protected void onProgressUpdate(Integer... progress) {
			this.dialog.incrementProgressBy(progress[0]);
		}

		@Override
		protected String doInBackground(String... params) {
			try {
				URL url = new URL("http://data.wsdot.wa.gov/mobile/TravelTimes.js.gz");
				URLConnection urlConn = url.openConnection();
				
				BufferedInputStream bis = new BufferedInputStream(urlConn.getInputStream());
                GZIPInputStream gzin = new GZIPInputStream(bis);
                InputStreamReader is = new InputStreamReader(gzin);
                BufferedReader in = new BufferedReader(is);
				
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
					if (!this.isCancelled()) {
						JSONObject item = items.getJSONObject(j);
						i = new TravelTimesItem();
						i.setTitle(item.getString("title"));
						i.setCurrentTime(Integer.toString(item.getInt("current")));
						i.setAverageTime(Integer.toString(item.getInt("average")));
						i.setDistance(item.getString("distance") + " miles");
						i.setRouteID(item.getString("routeid"));
						travelTimesItems.add(i);
						publishProgress(1);
					} else {
						break;
					}
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

	@Override
	protected void onListItemClick(ListView l, View v, final int position, long id) {
		super.onListItemClick(l, v, position, id);
		/*
		Bundle b = new Bundle();
		Intent intent = new Intent(this, RouteAlertsItems.class);
		b.putSerializable("routeItems", routeItems.get(position));
		intent.putExtras(b);
		startActivity(intent);
		*/
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
	            TextView description = (TextView) v.findViewById(R.id.route_description);
	            TextView distance = (TextView) v.findViewById(R.id.distance);
	            TextView average = (TextView) v.findViewById(R.id.average);
	            TextView current = (TextView) v.findViewById(R.id.current);
	            
	            if (description != null) {
	            	description.setText(o.getTitle());
	            }
	            if (distance != null) {
            		distance.setText(o.getDistance());
	            }
	            if (average != null) {
	            	if (Integer.parseInt(o.getAverageTime()) == 0) {
	            		average.setText("Not Available");
	            	} else {
	            		average.setText(o.getAverageTime() + " min");
	            	}
	            }
	            if (current != null) {
	            	if (Integer.parseInt(o.getCurrentTime()) < Integer.parseInt(o.getAverageTime())) {
	            		current.setTextColor(0xFF017359);
	            	} else if (Integer.parseInt(o.getCurrentTime()) > Integer.parseInt(o.getAverageTime()) && (Integer.parseInt(o.getAverageTime()) != 0)) {
	            		current.setTextColor(Color.RED);
	            	} else {
	            		current.setTextColor(Color.BLUE);
	            	}
	            	current.setText(o.getCurrentTime() + " min");
	            }
	        }
	        return v;
        }
	}
}